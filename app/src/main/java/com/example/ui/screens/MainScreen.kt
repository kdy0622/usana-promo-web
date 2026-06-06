package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.PromotionWithCheckpoints
import com.example.ui.viewmodel.PromotionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PromotionViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val promotions by viewModel.promotionsState.collectAsState()
    val currentTime by viewModel.systemTime.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "ACTIVE", "EXPIRED"

    // Automatically check timers on launch and on refresh
    LaunchedEffect(Unit) {
        viewModel.refreshTime()
    }

    // USANA Premium Colors mapping
    val usanaBlue = Color(0xFF4F46E5) // Brand Indigo
    val usanaTeal = Color(0xFF0EA5E9) // Sky Blue
    val goldAccent = Color(0xFFF59E0B) // Golden Achievement Label
    val darkGold = Color(0xFFB45309)

    val orderedPromotions = remember(promotions) {
        promotions.sortedWith(compareBy({ 
            val title = it.promotion.title
            val category = it.promotion.category
            when {
                title.contains("제주") || category == "UFI" -> 1
                title.contains("푸꾸옥") || category == "PHU_QUOC" -> 2
                title.contains("프라하") || category == "CZECH" -> 3
                title.contains("사노비브") || category == "SANOVIV" -> 4
                else -> 5
            }
        }, { it.promotion.id }))
    }

    val filteredPromotions = remember(orderedPromotions, selectedFilter, currentTime) {
        when (selectedFilter) {
            "ACTIVE" -> orderedPromotions.filter { !it.isExpired(currentTime) }
            "EXPIRED" -> orderedPromotions.filter { it.isExpired(currentTime) }
            else -> orderedPromotions
        }
    }

    // Consolidated Metrics Calculations
    val totalCount = promotions.size
    val activeCount = promotions.count { !it.isExpired(currentTime) }
    val achievedCount = promotions.count { it.isAchieved }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Color.White,
                                        radius = size.maxDimension / 2,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )
                                },
                            shape = CircleShape,
                            color = usanaBlue.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "U",
                                    color = usanaBlue,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Column {
                            Text(
                                "USANA CHALLENGE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = usanaBlue,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "유사나 프로모션 대시보드",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier
                            .testTag("admin_dashboard_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "관리자 대시보드",
                            tint = usanaBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.45f)
                ),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // High-Profile Header Performance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4F46E5), // Indigo 600
                                        Color(0xFF1D4ED8)  // Blue 700
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(1000f, 1000f)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "유사나 비즈니스 인센티브",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.7f),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "내 달성 요약 대시보드",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$totalCount",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(text = "전체 도전", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$activeCount",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF60A5FA)
                                    )
                                    Text(text = "진행 중", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$achievedCount",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFBBF24)
                                    )
                                    Text(text = "조건 만족", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val completedPastCount = totalCount - activeCount
                                    val rate = if (completedPastCount <= 0) {
                                        if (totalCount > 0) (achievedCount.toDouble() / totalCount * 100).toInt() else 0
                                    } else {
                                        (promotions.count { it.isExpired(currentTime) && it.isAchieved }.toDouble() / completedPastCount * 100).toInt()
                                    }
                                    Text(
                                        text = "$rate%",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF34D399)
                                    )
                                    Text(text = "최종 통과율", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "안내",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "달성 마감일이 지나면 실시간으로 만료 상태가 자동 업데이트됩니다.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Tabs / Filters
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "전체 프로모션", "ACTIVE" to "진행 중", "EXPIRED" to "기간 종료").forEach { (filterVal, filterTitle) ->
                        val isSelected = selectedFilter == filterVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) usanaBlue else Color.White.copy(alpha = 0.5f))
                                .clickable { selectedFilter = filterVal }
                                .drawBehind {
                                    if (!isSelected) {
                                        drawRoundRect(
                                            color = Color.White,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                        )
                                    }
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filterTitle,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            if (filteredPromotions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = "비어있음",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "해당하는 유사나 프로모션 항목이 존재하지 않습니다.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "원하는 신규 캠페인을 대시보드에서 등록해 보세요.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Cards Grid
            items(filteredPromotions, key = { it.promotion.id }) { item ->
                PromotionCard(
                    item = item,
                    currentTime = currentTime,
                    onNavigateToDetail = onNavigateToDetail,
                    usanaBlue = usanaBlue,
                    usanaTeal = usanaTeal,
                    goldAccent = goldAccent,
                    darkGold = darkGold
                )
            }
        }
    }
}

@Composable
fun PromotionCard(
    item: PromotionWithCheckpoints,
    currentTime: Long,
    onNavigateToDetail: (Long) -> Unit,
    usanaBlue: Color,
    usanaTeal: Color,
    goldAccent: Color,
    darkGold: Color
) {
    val isExpired = item.isExpired(currentTime)
    val isAchieved = item.isAchieved
    val progress = item.progressPercent
    val title = item.promotion.title
    val category = item.promotion.category

    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    val startStr = sdf.format(Date(item.promotion.startDate))
    val endStr = sdf.format(Date(item.promotion.endDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .clickable { onNavigateToDetail(item.promotion.id) }
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .testTag("promo_card_${item.promotion.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (category) {
                                "UFI" -> usanaBlue.copy(alpha = 0.15f)
                                "PHU_QUOC" -> usanaTeal.copy(alpha = 0.15f)
                                "CZECH" -> Color(0xFFE0F2FE)
                                "SANOVIV" -> Color(0xFFF0FDF4)
                                else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (category) {
                            "UFI" -> "UFI 제주"
                            "PHU_QUOC" -> "고 게터 푸꾸옥"
                            "CZECH" -> "리더십 프라하"
                            "SANOVIV" -> "다이아몬드 사노비브"
                            else -> "맞춤 인센티브"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (category) {
                            "UFI" -> usanaBlue
                            "PHU_QUOC" -> usanaTeal
                            "CZECH" -> Color(0xFF0369A1)
                            "SANOVIV" -> Color(0xFF15803D)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }

                // Dynamic Status Badge
                StatusBadge(
                    isExpired = isExpired,
                    isAchieved = isAchieved,
                    goldAccent = goldAccent,
                    darkGold = darkGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Navigation Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val emojiPrefix = when (category) {
                    "UFI" -> "🍊 "
                    "PHU_QUOC" -> "🏖️ "
                    "CZECH" -> "🏰 "
                    "SANOVIV" -> "💎 "
                    else -> "🏆 "
                }
                Text(
                    text = "$emojiPrefix$title",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "자세히 보기",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress percentage & labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "기간",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$startStr ~ $endStr",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!isExpired) {
                        val diff = item.promotion.endDate - currentTime
                        val days = (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0).toInt()
                        Text(
                            text = "남은 달성기간: D-${days}일",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (days < 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "기간 종료됨 (평가 마감)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Progress Text Label
                Text(
                    text = String.format(Locale.US, "%.1f%%", progress),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isAchieved) darkGold else usanaBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Elegant indicator bar
            LinearProgressIndicator(
                progress = { (progress / 100.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    isAchieved -> goldAccent
                    isExpired -> MeterExpiredColor()
                    else -> usanaBlue
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Short summary of requirements
            if (item.checkpoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item.checkpoints.take(2).forEach { cp ->
                        Text(
                            text = "• ${cp.title}: ${cp.currentValue.toInt()}/${cp.targetValue.toInt()}${cp.unit}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    isExpired: Boolean,
    isAchieved: Boolean,
    goldAccent: Color,
    darkGold: Color
) {
    val (bgColor, textColor, label) = when {
        isAchieved -> Triple(
            goldAccent.copy(alpha = 0.15f),
            darkGold,
            "달성 완료"
        )
        isExpired -> Triple(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.onErrorContainer,
            "기간 종료"
        )
        else -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "진행 중"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

@Composable
fun MeterExpiredColor(): Color {
    return MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
}
