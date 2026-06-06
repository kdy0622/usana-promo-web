package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PromotionDao {
    @Transaction
    @Query("SELECT * FROM promotions ORDER BY endDate ASC")
    fun getPromotionsWithCheckpoints(): Flow<List<PromotionWithCheckpoints>>

    @Transaction
    @Query("SELECT * FROM promotions WHERE id = :id")
    fun getPromotionWithCheckpointsById(id: Long): Flow<PromotionWithCheckpoints?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promotion: PromotionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckpoints(checkpoints: List<CheckpointEntity>)

    @Query("UPDATE checkpoints SET currentValue = :value WHERE id = :id")
    suspend fun updateCheckpointValue(id: Long, value: Double)

    @Delete
    suspend fun deletePromotion(promotion: PromotionEntity)

    @Query("DELETE FROM checkpoints WHERE promotionId = :promotionId")
    suspend fun deleteCheckpointsForPromotion(promotionId: Long)

    @Query("SELECT COUNT(*) FROM promotions")
    suspend fun getPromotionCount(): Int

    @Query("SELECT * FROM partners WHERE promotionId = :promotionId ORDER BY joinDate DESC")
    fun getPartnersByPromotionId(promotionId: Long): Flow<List<PartnerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: PartnerEntity): Long

    @Update
    suspend fun updatePartner(partner: PartnerEntity)

    @Delete
    suspend fun deletePartner(partner: PartnerEntity)

    @Query("UPDATE checkpoints SET currentValue = 0.0 WHERE promotionId = :promotionId")
    suspend fun resetCheckpointsForPromotion(promotionId: Long)

    @Query("DELETE FROM partners WHERE promotionId = :promotionId")
    suspend fun deletePartnersForPromotion(promotionId: Long)
}
