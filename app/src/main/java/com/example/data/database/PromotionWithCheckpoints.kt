package com.example.data.database

import androidx.room.Embedded
import androidx.room.Relation

data class PromotionWithCheckpoints(
    @Embedded val promotion: PromotionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "promotionId"
    )
    val checkpoints: List<CheckpointEntity>
) {
    /**
     * Checks if the promotion's period has ended based on current current timestamp.
     */
    fun isExpired(currentTimeMillis: Long): Boolean {
        return currentTimeMillis > promotion.endDate
    }

    /**
     * Calculates the achievement progress percentage (0.0 to 100.0) based on all checkpoints.
     */
    val progressPercent: Double
        get() {
            if (checkpoints.isEmpty()) return 0.0
            val totalProgress = checkpoints.sumOf { checkpoint ->
                val progress = if (checkpoint.targetValue <= 0.0) 0.0
                else (checkpoint.currentValue / checkpoint.targetValue).coerceIn(0.0, 1.0)
                progress
            }
            return (totalProgress / checkpoints.size) * 100.0
        }

    /**
     * Checks if all checkpoints have achieved their targeted values.
     */
    val isAchieved: Boolean
        get() {
            if (checkpoints.isEmpty()) return false
            return checkpoints.all { it.currentValue >= it.targetValue }
        }
}
