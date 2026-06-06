package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(entities = [PromotionEntity::class, CheckpointEntity::class, PartnerEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promotionDao(): PromotionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "usana_promotions_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDatabase(dao: PromotionDao) {
            val calendar = Calendar.getInstance()

            // 1. Jeju (Active - finishes end of August 2026)
            calendar.set(2026, Calendar.MARCH, 2, 0, 0, 0)
            val jejuStart = calendar.timeInMillis
            calendar.set(2026, Calendar.AUGUST, 28, 23, 59, 59)
            val jejuEnd = calendar.timeInMillis

            val jejuId = dao.insertPromotion(
                PromotionEntity(
                    title = "2026 유사나 패밀리 인센티브 시월애 제주",
                    category = "UFI",
                    description = "따뜻한 10월의 동행, 가을빛에 잠긴 제주! 유사나와 함께하는 모두의 특별한 목적지, 2026 유사나 패밀리 인센티브 '시월애 제주' 캠페인입니다.",
                    qualifications = "1. 자격취득 요건 (1인/2인 자격):\n - 1인 자격: 신규 BC 직접 후원 2인 이상 + 개인실적(SP) 총합 4,000점\n - 2인 자격: 신규 BC 직접 후원 2인 이상 + 개인실적(SP) 총합 8,000점\n2. 첫 도전 자격 요건 (UFI 취득 이력 없는 FP):\n - 1인 자격: 신규 BC 직접 후원 2인 이상 + 개인실적(SP) 총합 3,000점\n - 2인 자격: 신규 BC 직접 후원 2인 이상 + 개인실적(SP) 총합 6,000점\n3. 더블 프로모션:\n - 1차 자격기간 첫 4주 내 실적점수 2배 인정 (최대 일반 4,000점 / 첫도전 3,000점 까지만 적용)",
                    startDate = jejuStart,
                    endDate = jejuEnd,
                    isDefault = true
                )
            )
            dao.insertCheckpoints(
                listOf(
                    CheckpointEntity(promotionId = jejuId, title = "신규 브랜드파트너(BC) 직접 후원인 수", targetValue = 2.0, currentValue = 2.0, unit = "명"),
                    CheckpointEntity(promotionId = jejuId, title = "일반 자격 신규 개인실적점수(SP) 합계", targetValue = 8000.0, currentValue = 4500.0, unit = "점"),
                    CheckpointEntity(promotionId = jejuId, title = "첫 도전 신규 개인실적점수(SP) 합계", targetValue = 6000.0, currentValue = 4500.0, unit = "점"),
                    CheckpointEntity(promotionId = jejuId, title = "더블 프로모션 반영 실적점수", targetValue = 4000.0, currentValue = 3000.0, unit = "점")
                )
            )

            // 2. Phu Quoc (Active - finishes end of Sept 2026)
            calendar.set(2026, Calendar.MARCH, 30, 0, 0, 0)
            val pqStart = calendar.timeInMillis
            calendar.set(2026, Calendar.SEPTEMBER, 25, 23, 59, 59)
            val pqEnd = calendar.timeInMillis

            val pqId = dao.insertPromotion(
                PromotionEntity(
                    title = "2026 고 게터 디스커버리 푸꾸옥",
                    category = "PHU_QUOC",
                    description = "에메랄드빛 바다가 스며든 베트남의 진주, 푸꾸옥! 3박 4일 간의 인센티브 여행 및 호텔/항공 지원, 리더십 마인드셋 트레이닝 혜택이 주어집니다.",
                    qualifications = "1. 실버 디렉터 이하 직급자 자격 대상\n2. 1인 자격 취득: 누적 30점 이상 달성 시 (최대 2인 자격)\n3. 점수 획득 공식:\n - 후원수당점수 성장: 직전 26주 대비 4,000CP 성장시 +20점, 8,000CP 성장시 +40점\n - 신규 디렉터 육성: 직접 후원 BP가 디렉터 승급 후 4주 50CP 유지 시 +10점 (최대 10점)\n - 신규 페이스세터 직접 후원: 직접 후원 시 +5점 (제한 없음)",
                    startDate = pqStart,
                    endDate = pqEnd,
                    isDefault = true
                )
            )
            dao.insertCheckpoints(
                listOf(
                    CheckpointEntity(promotionId = pqId, title = "누적 획득 점수 (요건: 30점)", targetValue = 30.0, currentValue = 25.0, unit = "점"),
                    CheckpointEntity(promotionId = pqId, title = "후원수당점수 성장 수치", targetValue = 4000.0, currentValue = 3200.0, unit = "CP"),
                    CheckpointEntity(promotionId = pqId, title = "파트너 신규 디렉터 승급/유지 수", targetValue = 1.0, currentValue = 1.0, unit = "명"),
                    CheckpointEntity(promotionId = pqId, title = "직접 후원 신규 페이스세터 수", targetValue = 2.0, currentValue = 1.0, unit = "명")
                )
            )

            // 3. Prague (Active - finishes December 25, 2026)
            calendar.set(2025, Calendar.DECEMBER, 29, 0, 0, 0)
            val czStart = calendar.timeInMillis
            calendar.set(2026, Calendar.DECEMBER, 25, 23, 59, 59)
            val czEnd = calendar.timeInMillis

            val czId = dao.insertPromotion(
                PromotionEntity(
                    title = "2027 리더십 비즈니스 리트리트 프라하",
                    category = "CZECH",
                    description = "예술과 낭만의 도시, 체코 프라하로 떠나는 5박 6일 리트리트! 골드 디렉터 이상 리더들을 위한 최고급 현지 프로그램 및 비즈니스 클래스 왕복항공권(TOP 10) 기회!",
                    qualifications = "1. 골드 디렉터 이상 직급자 대상\n2. 자격기간 내 150점 이상 취득 시, 2인 자격 달성!\n3. 스스로의 성장 요건:\n - 주별 1,000CP 이상 달성: 매주 1점 (최대 52점)\n - 골드 디렉터 이상 승급 완료: 승급당 +30점 (최대 90점)\n - 신규 이그제큐티브 취득: +80점 (연 1회)\n - 이그제큐티브 갱신 및 재취득: +50점 (연 1회)\n4. 파트너의 성장 요건:\n - 직접 후원 파트너 디렉터 승급 & 유지 완료: +10점 (최대 40점)\n - 직접 후원 파트너 골드 디렉터 승급: +20점 (제한 없음)",
                    startDate = czStart,
                    endDate = czEnd,
                    isDefault = true
                )
            )
            dao.insertCheckpoints(
                listOf(
                    CheckpointEntity(promotionId = czId, title = "누적 획득 점수 (요건: 150점)", targetValue = 150.0, currentValue = 95.0, unit = "점"),
                    CheckpointEntity(promotionId = czId, title = "주별 1000CP 이상 달성 주 수", targetValue = 52.0, currentValue = 28.0, unit = "주"),
                    CheckpointEntity(promotionId = czId, title = "파트너 신규 디렉터 육성 수", targetValue = 4.0, currentValue = 2.0, unit = "명"),
                    CheckpointEntity(promotionId = czId, title = "파트너 신규 골드 디렉터 육성 수", targetValue = 2.0, currentValue = 1.0, unit = "명")
                )
            )

            // 4. Sanoviv (Active - finishes December 25, 2026)
            calendar.set(2025, Calendar.DECEMBER, 29, 0, 0, 0)
            val saStart = calendar.timeInMillis
            calendar.set(2026, Calendar.DECEMBER, 25, 23, 59, 59)
            val saEnd = calendar.timeInMillis

            val saId = dao.insertPromotion(
                PromotionEntity(
                    title = "다이아몬드 디스커버리",
                    category = "SANOVIV",
                    description = "멕시코 사노비브 디톡스 & 웰니스 프로그램 및 미국 유사나 본사 비전 투어! 신규 이그제큐티브 취득을 위한 최고의 명예 인센티브 트립입니다.",
                    qualifications = "1. 신규 다이아몬드 디렉터 승급 완료 후 요건 달성:\n - 승급 완료 후 13주 연속 4,000CP 이상 달성\n - 승급 완료 후 누적 52,000CP 이상 달성\n - 7일간 사노비브 디톡스 & 웰니스 프로그램\n - 3일간 미국 유사나 본사 비전 트립",
                    startDate = saStart,
                    endDate = saEnd,
                    isDefault = true
                )
            )
            dao.insertCheckpoints(
                listOf(
                    CheckpointEntity(promotionId = saId, title = "다이아몬드 승급 후 13주 연속 달성 주수", targetValue = 13.0, currentValue = 8.0, unit = "주"),
                    CheckpointEntity(promotionId = saId, title = "다이아몬드 승급 후 누적 획득 점수", targetValue = 52000.0, currentValue = 34000.0, unit = "CP")
                )
            )
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.promotionDao())
                }
            }
        }
    }
}
