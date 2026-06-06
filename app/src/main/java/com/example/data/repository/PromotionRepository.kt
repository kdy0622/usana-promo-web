package com.example.data.repository

import com.example.data.database.CheckpointEntity
import com.example.data.database.PartnerEntity
import com.example.data.database.PromotionDao
import com.example.data.database.PromotionEntity
import com.example.data.database.PromotionWithCheckpoints
import kotlinx.coroutines.flow.Flow

class PromotionRepository(private val dao: PromotionDao) {
    val allPromotions: Flow<List<PromotionWithCheckpoints>> = dao.getPromotionsWithCheckpoints()

    fun getPromotionById(id: Long): Flow<PromotionWithCheckpoints?> = dao.getPromotionWithCheckpointsById(id)

    suspend fun updateCheckpointValue(id: Long, currentValue: Double) {
        dao.updateCheckpointValue(id, currentValue)
    }

    suspend fun insertPromotion(promotion: PromotionEntity, checkpoints: List<CheckpointEntity>): Long {
        val promoId = dao.insertPromotion(promotion)
        val updatedCheckpoints = checkpoints.map { it.copy(promotionId = promoId) }
        dao.insertCheckpoints(updatedCheckpoints)
        return promoId
    }

    suspend fun deletePromotion(promotionWithCheckpoints: PromotionWithCheckpoints) {
        dao.deleteCheckpointsForPromotion(promotionWithCheckpoints.promotion.id)
        dao.deletePromotion(promotionWithCheckpoints.promotion)
    }

    suspend fun updatePromotion(promotion: PromotionEntity, checkpoints: List<CheckpointEntity>) {
        dao.insertPromotion(promotion)
        dao.deleteCheckpointsForPromotion(promotion.id)
        val updatedCheckpoints = checkpoints.map { it.copy(promotionId = promotion.id) }
        dao.insertCheckpoints(updatedCheckpoints)
    }

    fun getPartnersByPromotionId(promotionId: Long): Flow<List<PartnerEntity>> = dao.getPartnersByPromotionId(promotionId)

    suspend fun insertPartner(partner: PartnerEntity): Long = dao.insertPartner(partner)

    suspend fun updatePartner(partner: PartnerEntity) = dao.updatePartner(partner)

    suspend fun deletePartner(partner: PartnerEntity) = dao.deletePartner(partner)

    suspend fun resetPromotionData(promotionId: Long) {
        dao.resetCheckpointsForPromotion(promotionId)
        dao.deletePartnersForPromotion(promotionId)
    }
}
