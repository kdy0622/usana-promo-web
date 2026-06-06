package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.CheckpointEntity
import com.example.data.database.PromotionEntity
import com.example.data.database.PromotionWithCheckpoints
import com.example.data.repository.PromotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PromotionViewModel(private val repository: PromotionRepository) : ViewModel() {

    // Keep track of current system time to dynamically judge expiry status
    val systemTime = MutableStateFlow(System.currentTimeMillis())

    val promotionsState: StateFlow<List<PromotionWithCheckpoints>> = repository.allPromotions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getPromotionById(id: Long): Flow<PromotionWithCheckpoints?> {
        return repository.getPromotionById(id)
    }

    fun updateCheckpoint(checkpointId: Long, value: Double) {
        viewModelScope.launch {
            repository.updateCheckpointValue(checkpointId, value)
        }
    }

    fun addPromotion(
        title: String,
        category: String,
        description: String,
        qualifications: String,
        startDate: Long,
        endDate: Long,
        checkpoints: List<Pair<String, Double>> // Pair of title, targetValue (with unit separate or default unit)
    ) {
        viewModelScope.launch {
            val promotion = PromotionEntity(
                title = title,
                category = category,
                description = description,
                qualifications = qualifications,
                startDate = startDate,
                endDate = endDate,
                isDefault = false
            )
            val checkpointEntities = checkpoints.map { (ckptTitle, targetVal) ->
                // Automatically parse unit from title or default to standard count units
                val unit = when {
                    ckptTitle.contains("원") || ckptTitle.contains("SVP") || ckptTitle.contains("CVP") -> "SVP"
                    ckptTitle.contains("명") || ckptTitle.contains("파트너") -> "명"
                    ckptTitle.contains("주") || ckptTitle.contains("기간") -> "주"
                    ckptTitle.contains("회") || ckptTitle.contains("횟수") -> "회"
                    ckptTitle.contains("점") || ckptTitle.contains("포인트") -> "점"
                    else -> "개"
                }
                CheckpointEntity(
                    promotionId = 0, // Assigned by repository
                    title = ckptTitle,
                    targetValue = targetVal,
                    currentValue = 0.0,
                    unit = unit
                )
            }
            repository.insertPromotion(promotion, checkpointEntities)
        }
    }

    fun deletePromotion(promotionWithCheckpoints: PromotionWithCheckpoints) {
        viewModelScope.launch {
            repository.deletePromotion(promotionWithCheckpoints)
        }
    }

    fun updatePromotion(
        id: Long,
        title: String,
        category: String,
        description: String,
        qualifications: String,
        startDate: Long,
        endDate: Long,
        checkpoints: List<Triple<String, Double, Double>> // title, targetValue, currentValue
    ) {
        viewModelScope.launch {
            val promotion = PromotionEntity(
                id = id,
                title = title,
                category = category,
                description = description,
                qualifications = qualifications,
                startDate = startDate,
                endDate = endDate,
                isDefault = false
            )
            val checkpointEntities = checkpoints.map { (ckptTitle, targetVal, currentVal) ->
                val unit = when {
                    ckptTitle.contains("원") || ckptTitle.contains("SVP") || ckptTitle.contains("CVP") -> "SVP"
                    ckptTitle.contains("명") || ckptTitle.contains("파트너") -> "명"
                    ckptTitle.contains("주") || ckptTitle.contains("기간") -> "주"
                    ckptTitle.contains("회") || ckptTitle.contains("횟수") -> "회"
                    ckptTitle.contains("점") || ckptTitle.contains("포인트") -> "점"
                    else -> "개"
                }
                CheckpointEntity(
                    promotionId = id,
                    title = ckptTitle,
                    targetValue = targetVal,
                    currentValue = currentVal,
                    unit = unit
                )
            }
            repository.updatePromotion(promotion, checkpointEntities)
        }
    }

    fun getPartnersForPromotion(promotionId: Long): Flow<List<com.example.data.database.PartnerEntity>> {
        return repository.getPartnersByPromotionId(promotionId)
    }

    fun addPartner(promotionId: Long, name: String, joinDate: String, purchasePoints: Double, notes: String) {
        viewModelScope.launch {
            repository.insertPartner(
                com.example.data.database.PartnerEntity(
                    promotionId = promotionId,
                    name = name,
                    joinDate = joinDate,
                    purchasePoints = purchasePoints,
                    notes = notes
                )
            )
        }
    }

    fun updatePartner(partner: com.example.data.database.PartnerEntity) {
        viewModelScope.launch {
            repository.updatePartner(partner)
        }
    }

    fun addPurchasePoints(partner: com.example.data.database.PartnerEntity, pointsToAdd: Double) {
        viewModelScope.launch {
            val noteAddition = if (partner.notes.isEmpty()) "[가산: +${pointsToAdd}점]" else "${partner.notes}\n[가산: +${pointsToAdd}점]"
            val updated = partner.copy(
                purchasePoints = partner.purchasePoints + pointsToAdd,
                notes = noteAddition
            )
            repository.updatePartner(updated)
        }
    }

    fun deletePartner(partner: com.example.data.database.PartnerEntity) {
        viewModelScope.launch {
            repository.deletePartner(partner)
        }
    }

    fun resetPromotion(promotionId: Long) {
        viewModelScope.launch {
            repository.resetPromotionData(promotionId)
        }
    }

    fun refreshTime() {
        systemTime.value = System.currentTimeMillis()
    }

    class Factory(private val repository: PromotionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PromotionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PromotionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
