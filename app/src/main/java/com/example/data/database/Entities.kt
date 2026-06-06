package com.example.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "UFI", "PHU_QUOC", "CZECH", "SANOVIV", "CUSTOM"
    val description: String,
    val qualifications: String, // String with multiple points, separated by newlines
    val startDate: Long,
    val endDate: Long,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "checkpoints",
    foreignKeys = [
        ForeignKey(
            entity = PromotionEntity::class,
            parentColumns = ["id"],
            childColumns = ["promotionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CheckpointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val promotionId: Long,
    val title: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String
)

@Entity(
    tableName = "partners",
    foreignKeys = [
        ForeignKey(
            entity = PromotionEntity::class,
            parentColumns = ["id"],
            childColumns = ["promotionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PartnerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val promotionId: Long,
    val name: String,
    val joinDate: String, // format: YYYY-MM-DD
    val purchasePoints: Double = 0.0,
    val notes: String = ""
)

