package utils.geometryUtils.geospark

import com.vividsolutions.jts.geom.{Coordinate, Envelope, GeometryFactory, Point}
import model.TileGranularities
import utils.configuration.Constants.Relation
import utils.configuration.Constants.Relation.Relation

import scala.math.{max, min}


object EnvelopeOp {

    val epsilon: Double = 1e-8
    val geometryFactory = new GeometryFactory()
    val SPLIT_LOG_BASE: Int = 50

    def checkIntersection(env1: Envelope, env2: Envelope, relation: Relation): Boolean = {
        relation match {
            case Relation.CONTAINS | Relation.COVERS => env1.contains(env2)
            case Relation.WITHIN | Relation.COVEREDBY => env2.contains(env1)
            case Relation.INTERSECTS | Relation.CROSSES | Relation.OVERLAPS | Relation.DE9IM=> env1.intersects(env2)
            case Relation.TOUCHES => env1.getMaxX == env2.getMaxX || env1.getMinX == env2.getMinX || env1.getMaxY == env2.getMaxY || env1.getMinY == env2.getMinY
            case Relation.DISJOINT => !env1.intersects(env2)
            case Relation.EQUALS => env1.equals(env2)
            case _ => false
        }
    }

    /**
     * check if the envelopes satisfy the input relations
     *
     * @param env1 envelope
     * @param env2 envelope
     * @param relations a sequence of relations
     * @return true if the envelope satisfy all relations
     */
    def intersectingMBR(env1: Envelope, env2: Envelope, relations: Seq[Relation]): Boolean = relations.exists { r => checkIntersection(env1, env2, r) }

    def getArea(env: Envelope): Double = env.getArea

    def getIntersectingInterior(env1: Envelope, env2: Envelope): Envelope = env1.intersection(env2)

    def getCentroid(env: Envelope): Point = {
        val x = (env.getMaxX + env.getMinX)/2
        val y = (env.getMaxY + env.getMinY)/2
        geometryFactory.createPoint(new Coordinate(x, y))
    }

    def adjust(env: Envelope, tileGranularities: TileGranularities): Envelope ={
        val maxX = env.getMaxX / tileGranularities.x
        val minX = env.getMinX / tileGranularities.x
        val maxY = env.getMaxY / tileGranularities.y
        val minY = env.getMinY / tileGranularities.y

        new Envelope(minX, maxX, minY, maxY)
    }

    def getReferencePoint(env1: Envelope, env2: Envelope, theta: TileGranularities): (Double, Double) ={

        val minX1 = env1.getMinX /theta.x
        val minX2 = env2.getMinX /theta.x
        val maxY1 = env1.getMaxY /theta.y
        val maxY2 = env2.getMaxY /theta.y

        val rfX: Double = max(minX1, minX2)+epsilon
        val rfY: Double = min(maxY1, maxY2)+epsilon
        (rfX, rfY)
    }


    def getReferencePoint(env1: Envelope, env2: Envelope): (Double, Double) = {
        val rfX: Double = max(env1.getMinX, env2.getMinX) + epsilon
        val rfY: Double = min(env1.getMaxY, env2.getMaxY) + epsilon
        (rfX, rfY)
    }


    }
