package model.entities

import model.TileGranularities
import org.locationtech.jts.geom.{Envelope, Geometry}
import utils.configuration.Constants.Relation
import utils.configuration.Constants.Relation.Relation
import utils.geometryUtils.EnvelopeOp

case class FineGrainedEntity(originalID: String, geometry: Geometry, fineGrainedEnvelopes: Seq[Envelope]) extends Entity{

    def envelopeIntersection(envelopes: Seq[Envelope]): Boolean ={
        for (e1 <- fineGrainedEnvelopes; e2 <- envelopes; if EnvelopeOp.checkIntersection(e1, e2, Relation.INTERSECTS))
              return true
        false
    }

    override def intersectingMBR(se: Entity, relation: Relation): Boolean =
        se match {
            case fge: FineGrainedEntity =>
                EnvelopeOp.checkIntersection(env, fge.env, Relation.INTERSECTS) &&
                        envelopeIntersection(fge.fineGrainedEnvelopes)

            case e: Entity =>
                EnvelopeOp.checkIntersection(env, e.env, Relation.INTERSECTS) &&
                        envelopeIntersection(Seq(e.env))
        }
}

object FineGrainedEntity{
    def apply(originalID: String, geom: Geometry, theta: TileGranularities, envelopeRefiner: Geometry => Seq[Envelope]): FineGrainedEntity ={
        FineGrainedEntity(originalID, geom, envelopeRefiner(geom))
    }
}
