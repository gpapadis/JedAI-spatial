package interlinkers.progressive

import model.TileGranularities
import model.entities.Entity
import org.apache.spark.Partitioner
import org.apache.spark.rdd.RDD
import org.locationtech.jts.geom.Envelope
import utils.configuration.Constants
import utils.configuration.Constants.ProgressiveAlgorithm
import utils.configuration.Constants.ProgressiveAlgorithm.ProgressiveAlgorithm
import utils.configuration.Constants.WeightingFunction.WeightingFunction

object ProgressiveAlgorithmsFactory {


    def get(matchingAlgorithm: ProgressiveAlgorithm, source: RDD[(Int, Entity)], target: RDD[(Int, Entity)],
            tileGranularities: TileGranularities, partitionBorders: Array[Envelope], partitioner: Partitioner, sourceCount: Long,
            budget: Int = 0, mainWF: WeightingFunction, secondaryWF: Option[WeightingFunction],
            ws: Constants.WeightingScheme ):
    ProgressiveInterlinkerT ={

        matchingAlgorithm match {
            case ProgressiveAlgorithm.RANDOM =>
                RandomScheduling(source, target, tileGranularities, partitionBorders, sourceCount, mainWF, secondaryWF, budget, partitioner, ws)
            case ProgressiveAlgorithm.TOPK =>
                TopKPairs(source, target, tileGranularities, partitionBorders, sourceCount, mainWF, secondaryWF, budget, partitioner, ws)
            case ProgressiveAlgorithm.RECIPROCAL_TOPK =>
                ReciprocalTopK(source, target, tileGranularities, partitionBorders, sourceCount, mainWF, secondaryWF, budget, partitioner, ws)
            case ProgressiveAlgorithm.DYNAMIC_PROGRESSIVE_GIANT =>
                DynamicProgressiveGIAnt(source, target, tileGranularities, partitionBorders, sourceCount, mainWF, secondaryWF, budget, partitioner, ws)
            case ProgressiveAlgorithm.PROGRESSIVE_GIANT | _ =>
                ProgressiveGIAnt(source, target, tileGranularities, partitionBorders, sourceCount, mainWF, secondaryWF, budget, partitioner, ws)
        }
    }
}
