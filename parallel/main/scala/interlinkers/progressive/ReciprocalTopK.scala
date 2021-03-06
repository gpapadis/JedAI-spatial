package interlinkers.progressive

import model.entities.Entity
import model.{SpatialIndex, StaticComparisonPQ, TileGranularities}
import org.apache.spark.Partitioner
import org.apache.spark.rdd.RDD
import org.locationtech.jts.geom.Envelope
import utils.configuration.Constants
import utils.configuration.Constants.Relation.Relation
import utils.configuration.Constants.WeightingFunction.WeightingFunction



case class ReciprocalTopK(joinedRDD: RDD[(Int, (Iterable[Entity], Iterable[Entity]))],
                          tileGranularities: TileGranularities, partitionBorders: Array[Envelope],
                          mainWF: WeightingFunction, secondaryWF: Option[WeightingFunction], budget: Int,
                          totalSourceEntities: Long, ws: Constants.WeightingScheme)
    extends ProgressiveInterlinkerT {

    /**
     * Find the top-K comparisons of target and source and keep only the comparison (i, j) that belongs to both
     * top-K comparisons of i and j.
     *
     * @param source source dataset
     * @param target target dataset
     * @param partition current partition
     * @param relation examining relation
     * @return prioritized comparisons as a PQ
     */
    def prioritize(source: Array[Entity], target: Array[Entity], partition: Envelope, relation: Relation):  StaticComparisonPQ = {
        val localBudget = math.ceil(budget*source.length.toDouble/totalSourceEntities.toDouble).toLong
        val sourceIndex = SpatialIndex(source, tileGranularities)

        val sourceK = (math.ceil(localBudget / source.length).toInt + 1) * 2 // +1 to avoid k=0
        val targetK = (math.ceil(localBudget / target.length).toInt + 1) * 2 // +1 to avoid k=0

        val sourcePQ: Array[StaticComparisonPQ] = new Array(source.length)
        val targetPQ: StaticComparisonPQ = StaticComparisonPQ(targetK)
        val partitionPQ: StaticComparisonPQ = StaticComparisonPQ(localBudget)
        var counter = 0

        val targetSet: Array[Set[Int]] = new Array(target.length)
        target
            .indices
            .foreach {j =>
                val t = target(j)
                val candidates = getAllCandidatesWithIndex(t, sourceIndex, partition, relation)
                candidates.foreach { case (i, s) =>
                    val wp = weightedPairFactory.createWeightedPair(counter, s, i, t, j)
                    counter += 1

                    // set top-K PQ for the examining target entity
                    targetPQ.enqueue(wp)

                    // update source entities' top-K
                    if (sourcePQ(i) == null)
                        sourcePQ(i) = StaticComparisonPQ(sourceK)
                    sourcePQ(i).enqueue(wp)
                }
                // add comparisons into corresponding HashSet
                targetSet(j) = targetPQ.iterator().map(_.entityId1).toSet
                targetPQ.clear()
            }

        // add comparison into PQ only if is contained by both top-K PQs
        sourcePQ
            .filter(_ != null)
            .foreach { pq =>
                pq.iterator()
                    .filter(wp => targetSet(wp.entityId2).contains(wp.entityId1))
                    .foreach(wp => partitionPQ.enqueue(wp))
            }
        partitionPQ
    }
}

object ReciprocalTopK{

    def apply(source:RDD[(Int, Entity)], target:RDD[(Int, Entity)],
              tileGranularities: TileGranularities, partitionBorders: Array[Envelope], sourceCount: Long, wf: WeightingFunction,
              swf: Option[WeightingFunction] = None, budget: Int, partitioner: Partitioner,
              ws: Constants.WeightingScheme): ReciprocalTopK ={

        val joinedRDD = source.cogroup(target, partitioner)
        ReciprocalTopK(joinedRDD, tileGranularities, partitionBorders,  wf, swf, budget, sourceCount, ws)
    }
}
