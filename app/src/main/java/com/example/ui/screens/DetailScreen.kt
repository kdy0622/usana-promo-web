package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TaskAlt
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.CheckpointEntity
import com.example.data.database.PartnerEntity
import com.example.data.database.PromotionWithCheckpoints
import com.example.ui.viewmodel.PromotionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Reporting and pdf generation imports
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.content.Context
import android.widget.Toast
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import android.content.Intent
import androidx.core.content.FileProvider
import android.net.Uri
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.DeleteForever


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    promotionId: Long,
    viewModel: PromotionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsFlow = remember(promotionId) { viewModel.getPromotionById(promotionId) }
    val itemWithCheckpoints by itemsFlow.collectAsState(initial = null)
    val currentTime by viewModel.systemTime.collectAsState()

    val partnersFlow = remember(promotionId) { viewModel.getPartnersForPromotion(promotionId) }
    val partnersList by partnersFlow.collectAsState(initial = emptyList())

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showReportPreviewDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Premium Theme colors
    val usanaBlue = Color(0xFF4F46E5) // Brand Indigo
    val usanaTeal = Color(0xFF0EA5E9) // Sky Blue
    val goldAccent = Color(0xFFF59E0B) // Golden Achievement Label
    val darkGold = Color(0xFFB45309)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("상세 도전 진척도", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("back_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
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
        val currentItem = itemWithCheckpoints

        if (currentItem == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val isExpired = currentItem.isExpired(currentTime)
            val isAchieved = currentItem.isAchieved
            val progressPercent = currentItem.progressPercent

            val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
            val startDateStr = sdf.format(Date(currentItem.promotion.startDate))
            val endDateStr = sdf.format(Date(currentItem.promotion.endDate))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Ring Progress Gauge
                item {
                    val emojiPrefix = when (currentItem.promotion.category) {
                        "UFI" -> "🍊 "
                        "PHU_QUOC" -> "🏖️ "
                        "CZECH" -> "🏰 "
                        "SANOVIV" -> "💎 "
                        else -> "🏆 "
                    }
                    DetailProgressGaugeCard(
                        title = "$emojiPrefix${currentItem.promotion.title}",
                        progressPercent = progressPercent,
                        isExpired = isExpired,
                        isAchieved = isAchieved,
                        startDateStr = startDateStr,
                        endDateStr = endDateStr,
                        usanaBlue = usanaBlue,
                        usanaTeal = usanaTeal,
                        goldAccent = goldAccent,
                        darkGold = darkGold
                    )
                }

                // Celebrate fully achieved state
                if (isAchieved) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)) // Gold cream
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = "성공",
                                    tint = darkGold,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(
                                        text = "대단하십니다! 모든 요건 달성 완료!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = darkGold
                                    )
                                    Text(
                                        text = "성공 파트너로서 우뚝 서신 것을 진심으로 축하드립니다. 파라다이스에서 만나요!",
                                        fontSize = 12.sp,
                                        color = darkGold.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 1: Overview (개요)
                item {
                    ExpandableInfoCard(
                        headerTitle = "프로모션 개요",
                        content = currentItem.promotion.description,
                        isDefaultExpanded = true,
                        iconColor = usanaBlue
                    )
                }

                // Section 2: Requirements (자격요건)
                item {
                    ExpandableInfoCard(
                        headerTitle = "자격 및 요건 상세",
                        content = currentItem.promotion.qualifications,
                        isDefaultExpanded = true,
                        iconColor = usanaTeal
                    )
                }

                // Section 3: Interactive Inputs (달성을 위한 조건별 입력사항 / 달성진척도)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "조건",
                            tint = usanaBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "달성을 위한 조건별 수치 조절",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (currentItem.checkpoints.isEmpty()) {
                    item {
                        Text(
                            text = "등록된 달성 세부 조절 항목이 없습니다.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                items(currentItem.checkpoints, key = { it.id }) { checkpoint ->
                    CheckpointInputCard(
                        checkpoint = checkpoint,
                        isExpired = isExpired,
                        onUpdateValue = { newValue ->
                            viewModel.updateCheckpoint(checkpoint.id, newValue)
                        },
                        usanaBlue = usanaBlue,
                        usanaTeal = usanaTeal,
                        goldAccent = goldAccent
                    )
                }

                // Section 4: 신규 브랜드파트너(BP) 실적 관리
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .padding(top = 12.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "추적",
                            tint = usanaBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "신규 브랜드파트너(BP) 실적 기록",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                item {
                    PartnerTrackingCard(
                        partnersList = partnersList,
                        onAddPartner = { name, joinDate, pts, notes ->
                            viewModel.addPartner(promotionId, name, joinDate, pts, notes)
                        },
                        onAddPoints = { partner, points ->
                            viewModel.addPurchasePoints(partner, points)
                        },
                        onDeletePartner = { partner ->
                            viewModel.deletePartner(partner)
                        },
                        usanaBlue = usanaBlue,
                        usanaTeal = usanaTeal,
                        goldAccent = goldAccent
                    )
                }

                // Section 4.5: 보고서 및 데이터 관리
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .padding(top = 12.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "보고서",
                            tint = usanaBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "보고서 및 데이터 관리",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "📊 실적 및 목표 현황 성취 보고서",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "현재까지 입력된 도전 조건별 실적 및 가입된 브랜드파트너(BP) 후원 현황을 종합한 보고서를 한눈에 확인하고, PDF 파일로 다운로드 하거나 카카오톡 전송 양식으로 즉시 보낼 수 있습니다.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showReportPreviewDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = usanaBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("open_report_dialog_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "리포트",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("보고서 열기 📊", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Button(
                                    onClick = { showResetConfirmDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(44.dp).testTag("reset_promotion_data_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = "초기화",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("전체 초기화 ⚠️", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Section 5: "홈으로" (Home) Button
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = usanaBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .height(52.dp)
                            .testTag("home_button_bottom")
                    ) {
                        Text(
                            text = "홈으로 돌아가기",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Dialog overlay elements for data management
            if (showResetConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showResetConfirmDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "경고",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "프로모션 데이터 전체 초기화",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "⚠️ 정말로 현재 진행 중인 모든 실적 데이터를 초기화하시겠습니까?\n\n이 작업을 수행하면 현재 프로모션에 맞춰 조절해 놓은 조건 수치들은 모두 0으로 재설정되며, 정성스레 등록한 신규 브랜드파트너(BP) 목록 및 재구매 가산 실정 정보까지 전부 완전히 지워집니다. 이 데이터는 영구 삭제되며 복구가 절대 불가능함을 인지하시고 주의해 주십시오.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetPromotion(promotionId)
                                showResetConfirmDialog = false
                                Toast.makeText(context, "해당 프로모션의 모든 실적 데이터가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("confirm_reset_button")
                        ) {
                            Text("모두 초기화 진행", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showResetConfirmDialog = false },
                            modifier = Modifier.testTag("cancel_reset_button")
                        ) {
                            Text("취소")
                        }
                    }
                )
            }

            if (showReportPreviewDialog) {
                AlertDialog(
                    onDismissRequest = { showReportPreviewDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📑 프로모션 성취 보고서", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        val kakaoMsg = getKakaoMessage(
                            promotionTitle = currentItem.promotion.title,
                            progressPercent = progressPercent,
                            isAchieved = isAchieved,
                            startDateStr = startDateStr,
                            endDateStr = endDateStr,
                            checkpoints = currentItem.checkpoints,
                            partners = partnersList
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "💬 아래 양식은 카톡 전송 또는 클립보드 복사용 텍스트 프리뷰입니다:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = usanaBlue
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item {
                                        Text(
                                            text = kakaoMsg,
                                            fontSize = 11.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = "💡 'PDF 리포트'를 저장하면 규격 A4 형태의 깔끔한 문서로 인쇄하거나 공식 서류 공유가 가능합니다.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        }
                    },
                    confirmButton = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val kakaoMsg = getKakaoMessage(
                                promotionTitle = currentItem.promotion.title,
                                progressPercent = progressPercent,
                                isAchieved = isAchieved,
                                startDateStr = startDateStr,
                                endDateStr = endDateStr,
                                checkpoints = currentItem.checkpoints,
                                partners = partnersList
                            )

                            Button(
                                onClick = {
                                    generateAndSharePdf(
                                        context = context,
                                        promotionTitle = currentItem.promotion.title,
                                        progressPercent = progressPercent,
                                        isAchieved = isAchieved,
                                        startDateStr = startDateStr,
                                        endDateStr = endDateStr,
                                        checkpoints = currentItem.checkpoints,
                                        partners = partnersList
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = usanaBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("download_pdf_report_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = "PDF", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("📄 PDF 보고서 저장 및 인쇄/공유", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    generateAndShareHtml(
                                        context = context,
                                        promotionTitle = currentItem.promotion.title,
                                        progressPercent = progressPercent,
                                        isAchieved = isAchieved,
                                        startDateStr = startDateStr,
                                        endDateStr = endDateStr,
                                        checkpoints = currentItem.checkpoints,
                                        partners = partnersList
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = usanaTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("download_html_report_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = "HTML", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🌐 HTML 웹 보고서 저장 및 공유", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        shareToKakao(context, kakaoMsg)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE500)), // Yellow
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(42.dp).testTag("share_kakaotalk_btn")
                                ) {
                                    Text("💬 카톡 전송", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3C1E1E))
                                }

                                OutlinedButton(
                                    onClick = {
                                        copyToClipboard(context, kakaoMsg)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(42.dp).testTag("copy_clipboard_report_btn")
                                ) {
                                    Text("📋 본문 복사", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = usanaBlue)
                                }
                            }
                            
                            TextButton(
                                onClick = { showReportPreviewDialog = false },
                                modifier = Modifier.fillMaxWidth().testTag("close_report_dialog_btn")
                            ) {
                                Text("닫기", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }
        }
    }
}

// Dialog Sharing Utilities (PDF report generator, Kakao text template builder, and share controllers)
fun getKakaoMessage(
    promotionTitle: String,
    progressPercent: Double,
    isAchieved: Boolean,
    startDateStr: String,
    endDateStr: String,
    checkpoints: List<CheckpointEntity>,
    partners: List<PartnerEntity>
): String {
    val statusSymbol = if (isAchieved) "🎉 [도전 달성 완료!!]" else "🔥 [도전 진행 중]"
    val checkpointsText = if (checkpoints.isEmpty()) "  - 세부 항목 없음" else checkpoints.joinToString("\n") { cp ->
        val cpPercent = if (cp.targetValue <= 0.0) 0.0 else (cp.currentValue / cp.targetValue * 100).roundToInt()
        "  • ${cp.title}: ${cp.currentValue.roundToInt()}/${cp.targetValue.roundToInt()} ${cp.unit} (${cpPercent}%)"
    }
    
    val partnersText = if (partners.isEmpty()) "  - 신규 후원 BP 없음" else partners.joinToString("\n") { p ->
        val suffix = if (p.notes.isNotEmpty()) " (${p.notes.replace("\n", " ")})" else ""
        "  • ${p.name}: ${p.purchasePoints.roundToInt()} SVP$suffix"
    }
    
    return """
🚀 [유사나(USANA) 프로모션 비즈니스 보고서] 🚀

${statusSymbol}

📌 도전 과제: ${promotionTitle}
🗓️ 도전 기간: ${startDateStr} ~ ${endDateStr}
📊 전체 진척도: ${String.format(Locale.US, "%.1f%%", progressPercent)}

=======================
🔍 조건 단위별 세부 진척 현황:
${checkpointsText}

=======================
👥 신규 브랜드파트너(BP) 후원 실적:
${partnersText}

-----------------------
💙 유사나 리더님의 꿈과 도전을 늘 응원합니다! 파라다이스에서 축배를 나눕시다! 🥂 파이팅! 💙
    """.trimIndent()
}

fun shareToKakao(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    intent.`package` = "com.kakao.talk"
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, "보고서 카톡/일반 공유")
        context.startActivity(chooser)
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("USANA Promotion Report", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "보고서가 클립보드에 복사되었습니다! 카톡창 등에 복사-붙여넣기 하세요.", Toast.LENGTH_SHORT).show()
}

fun generateAndSharePdf(
    context: Context,
    promotionTitle: String,
    progressPercent: Double,
    isAchieved: Boolean,
    startDateStr: String,
    endDateStr: String,
    checkpoints: List<CheckpointEntity>,
    partners: List<PartnerEntity>
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        val primaryColor = 0xFF4F46E5.toInt() // Indigo Brand
        val textColor = 0xFF1F2937.toInt()    // Dark Gray
        val mutedColor = 0xFF4B5563.toInt()    // Light Gray
        val successColor = 0xFF15803D.toInt()  // Green
        val boundaryColor = 0xFFE5E7EB.toInt() // Gray line
        
        var yPos = 40f

        paint.color = primaryColor
        canvas.drawRect(30f, yPos, 565f, yPos + 60f, paint)

        paint.color = android.graphics.Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("유사나(USANA) 프로모션 성취 보고서", 297f, yPos + 36f, paint)
        yPos += 85f

        paint.color = textColor
        paint.textSize = 16f
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🏆 $promotionTitle", 40f, yPos, paint)
        yPos += 20f

        paint.color = mutedColor
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("🗓️ 자격 기간: $startDateStr ~ $endDateStr", 40f, yPos, paint)
        yPos += 25f

        paint.color = primaryColor
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("📊 전체 달성 진척율", 40f, yPos, paint)
        yPos += 10f

        paint.color = boundaryColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas.drawLine(40f, yPos, 555f, yPos, paint)

        paint.color = if (isAchieved) successColor else primaryColor
        paint.style = Paint.Style.FILL
        val fillWidth = (515f * (progressPercent / 100.0).coerceIn(0.0, 1.0)).toFloat()
        if (fillWidth > 0) {
            canvas.drawLine(40f, yPos, 40f + fillWidth,  yPos, paint)
        }
        yPos += 25f

        paint.color = textColor
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val statusText = if (isAchieved) "달성 완료 🎉" else "진행 중 (${String.format(Locale.US, "%.1f%%", progressPercent)})"
        canvas.drawText("현재 달성도: $statusText", 40f, yPos, paint)
        yPos += 30f

        paint.color = primaryColor
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("📋 세부 조건 항목별 진척율", 40f, yPos, paint)
        yPos += 15f

        paint.color = textColor
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        if (checkpoints.isEmpty()) {
            canvas.drawText("- 등록된 세부 설정 항목이 없습니다.", 50f, yPos, paint)
            yPos += 18f
        } else {
            checkpoints.forEach { cp ->
                val cpPercent = if (cp.targetValue <= 0.0) 0.0 else (cp.currentValue / cp.targetValue * 100.0).coerceAtMost(150.0)
                val statusSymbol = if (cp.currentValue >= cp.targetValue) "(충족 ✔️)" else ""
                val cpLine = "${cp.title}: ${cp.currentValue.roundToInt()}/${cp.targetValue.roundToInt()} ${cp.unit} (${cpPercent.roundToInt()}%) $statusSymbol"
                canvas.drawText("• $cpLine", 50f, yPos, paint)
                yPos += 18f
            }
        }
        yPos += 15f

        paint.color = primaryColor
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("👥 신규 브랜드 파트너(BP) 후원 실적", 40f, yPos, paint)
        yPos += 15f

        paint.color = textColor
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        if (partners.isEmpty()) {
            canvas.drawText("- 등록된 신규 BP 후원 및 실적 정보가 없습니다.", 50f, yPos, paint)
            yPos += 18f
        } else {
            partners.forEach { partner ->
                val pLine = "${partner.name} (가입일: ${partner.joinDate}) : ${partner.purchasePoints.roundToInt()} SVP"
                canvas.drawText("• $pLine", 50f, yPos, paint)
                yPos += 16f
                if (partner.notes.isNotEmpty()) {
                    val formattedNotes = partner.notes.replace("\n", " ")
                    canvas.drawText("   메모: $formattedNotes", 50f, yPos, paint)
                    yPos += 16f
                }
            }
        }

        yPos += 30f
        paint.color = boundaryColor
        paint.strokeWidth = 1f
        canvas.drawLine(40f, yPos, 555f, yPos, paint)
        yPos += 25f

        paint.color = mutedColor
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("유사나(USANA) 리더님의 성공적인 비즈니스와 파라다이스 도전을 진심으로 격려합니다!", 297f, yPos, paint)
        yPos += 16f
        
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN).format(Date())
        paint.textSize = 8f
        canvas.drawText("보고서 생성일: $nowStr | 본 문서는 성취 도우미 앱을 통해 작성되었습니다.", 297f, yPos, paint)

        pdfDocument.finishPage(page)

        val fileName = "USANA_Report_${System.currentTimeMillis()}.pdf"
        val reportDir = context.getExternalFilesDir("Reports") ?: context.cacheDir
        if (reportDir != null && !reportDir.exists()) {
            reportDir.mkdirs()
        }
        val reportFile = File(reportDir, fileName)
        FileOutputStream(reportFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        Toast.makeText(context, "Reports 폴더에 PDF가 정상 저장되었습니다.", Toast.LENGTH_SHORT).show()

        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            reportFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "[$promotionTitle] 성취 보고서")
            putExtra(Intent.EXTRA_TEXT, "유사나 프로모션 성취 보고서를 전달합니다.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "PDF 보고서 파일 공유"))

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "PDF 생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun generateAndShareHtml(
    context: Context,
    promotionTitle: String,
    progressPercent: Double,
    isAchieved: Boolean,
    startDateStr: String,
    endDateStr: String,
    checkpoints: List<CheckpointEntity>,
    partners: List<PartnerEntity>
) {
    try {
        val nowStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.KOREAN).format(java.util.Date())
        val statusText = if (isAchieved) "성취 완료 🎉" else "도전 진행 중"
        val statusColor = if (isAchieved) "#10B981" else "#4F46E5"
        
        val checkpointsHtml = if (checkpoints.isEmpty()) """
            <div class="no-data">등록된 세부 조건 항목이 없습니다.</div>
        """.trimIndent() else checkpoints.joinToString("\n") { cp ->
            val cpPercent = if (cp.targetValue <= 0.0) 0 else (cp.currentValue / cp.targetValue * 100).roundToInt()
            val badge = if (cp.currentValue >= cp.targetValue) "<span class='badge success'>충족 완료</span>" else "<span class='badge warning'>진행중</span>"
            """
            <div class="card">
                <div class="card-header">
                    <span class="card-title">${cp.title}</span>
                    $badge
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar" style="width: ${if (cpPercent > 100) 100 else cpPercent}%;"></div>
                </div>
                <div class="card-footer">
                    <span>현재: <strong>${cp.currentValue.roundToInt()}</strong> / 목표: ${cp.targetValue.roundToInt()} ${cp.unit}</span>
                    <span>${cpPercent}%</span>
                </div>
            </div>
            """.trimIndent()
        }
        
        val partnersHtml = if (partners.isEmpty()) """
            <div class="no-data">신규 유치 브랜드파트너(BP) 및 후원실적 내역이 없습니다.</div>
        """.trimIndent() else partners.joinToString("\n") { p ->
            val noteSection = if (p.notes.isNotEmpty()) """
                <div class="partner-notes">📝 메모: ${p.notes.replace("\n", "<br>")}</div>
            """.trimIndent() else ""
            """
            <div class="partner-card">
                <div class="partner-info">
                    <strong>${p.name}</strong> 
                    <span class="partner-date">가입일: ${p.joinDate}</span>
                </div>
                <div class="partner-points">${p.purchasePoints.roundToInt()} SVP</div>
                $noteSection
            </div>
            """.trimIndent()
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>유사나(USANA) 프로모션 보고서</title>
                <style>
                    body {
                        font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif;
                        background-color: #F3F4F6;
                        color: #1F2937;
                        margin: 0;
                        padding: 16px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: #FFFFFF;
                        border-radius: 16px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.08);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #4F46E5 0%, #0EA5E9 100%);
                        color: #FFFFFF;
                        padding: 24px;
                        text-align: center;
                    }
                    .header h1 {
                        font-size: 20px;
                        margin: 0 0 8px 0;
                        font-weight: 800;
                    }
                    .header p {
                        font-size: 13px;
                        margin: 0;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 20px;
                    }
                    .section-title {
                        font-size: 15px;
                        font-weight: 700;
                        color: #4F46E5;
                        margin: 20px 0 10px 0;
                        border-left: 4px solid #4F46E5;
                        padding-left: 8px;
                    }
                    .overview-card {
                        background: #F9FAFB;
                        border: 1px solid #E5E7EB;
                        border-radius: 12px;
                        padding: 16px;
                        margin-bottom: 20px;
                    }
                    .overview-row {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 8px;
                        font-size: 13px;
                    }
                    .overview-row:last-child {
                        margin-bottom: 0;
                    }
                    .overview-label {
                        color: #6B7280;
                    }
                    .overview-value {
                        font-weight: 700;
                    }
                    .main-progress {
                        position: relative;
                        height: 24px;
                        background: #E5E7EB;
                        border-radius: 12px;
                        overflow: hidden;
                        margin-top: 12px;
                    }
                    .main-progress-bar {
                        height: 100%;
                        background: $statusColor;
                        border-radius: 12px;
                    }
                    .main-progress-text {
                        position: absolute;
                        top: 50%;
                        left: 50%;
                        transform: translate(-50%, -50%);
                        font-size: 11px;
                        font-weight: 800;
                        color: #FFFFFF;
                        text-shadow: 0 1px 2px rgba(0,0,0,0.4);
                    }
                    .card {
                        background: #FFFFFF;
                        border: 1px solid #E5E7EB;
                        border-radius: 12px;
                        padding: 14px;
                        margin-bottom: 12px;
                    }
                    .card-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 8px;
                    }
                    .card-title {
                        font-size: 13px;
                        font-weight: 700;
                    }
                    .badge {
                        font-size: 10px;
                        padding: 2px 6px;
                        border-radius: 6px;
                        font-weight: 700;
                    }
                    .badge.success {
                        background: #D1FAE5;
                        color: #065F46;
                    }
                    .badge.warning {
                        background: #FEF3C7;
                        color: #92400E;
                    }
                    .progress-bar-container {
                        height: 8px;
                        background: #F3F4F6;
                        border-radius: 4px;
                        overflow: hidden;
                        margin-bottom: 6px;
                    }
                    .progress-bar {
                        height: 100%;
                        background: #4F46E5;
                    }
                    .card-footer {
                        display: flex;
                        justify-content: space-between;
                        font-size: 11px;
                        color: #6B7280;
                    }
                    .partner-card {
                        border-bottom: 1px solid #F3F4F6;
                        padding: 10px 0;
                        display: flex;
                        flex-direction: column;
                    }
                    .partner-card:last-child {
                        border-bottom: none;
                    }
                    .partner-info {
                        display: flex;
                        justify-content: space-between;
                        font-size: 13px;
                    }
                    .partner-date {
                        font-size: 11px;
                        color: #9CA3AF;
                    }
                    .partner-points {
                        font-size: 12px;
                        color: #0369A1;
                        font-weight: 700;
                        margin-top: 2px;
                    }
                    .partner-notes {
                        font-size: 11px;
                        color: #6B7280;
                        background: #F9FAFB;
                        padding: 6px 10px;
                        border-radius: 6px;
                        margin-top: 4px;
                        line-height: 1.4;
                    }
                    .no-data {
                        text-align: center;
                        font-size: 12px;
                        color: #9CA3AF;
                        padding: 20px 0;
                    }
                    .footer {
                        background: #F9FAFB;
                        padding: 20px;
                        text-align: center;
                        font-size: 11px;
                        color: #9CA3AF;
                        border-top: 1px solid #E5E7EB;
                    }
                    .footer p {
                        margin: 4px 0;
                        line-height: 1.4;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏆 유사나 프로모션 성취 보고서</h1>
                        <p>USANA PROMOTION BUSINESS ACHIEVEMENT REPORT</p>
                    </div>
                    <div class="content">
                        <div class="overview-card">
                            <div class="overview-row">
                                <span class="overview-label">도전 프로모션</span>
                                <span class="overview-value" style="color: #4F46E5;">$promotionTitle</span>
                            </div>
                            <div class="overview-row">
                                <span class="overview-label">자격 도전 기간</span>
                                <span class="overview-value">$startDateStr ~ $endDateStr</span>
                            </div>
                            <div class="overview-row">
                                <span class="overview-label">현재 진척 상태</span>
                                <span class="overview-value" style="color: $statusColor;">$statusText</span>
                            </div>
                            <div class="main-progress">
                                <div class="main-progress-bar" style="width: ${if (progressPercent > 100.0) 100.0 else progressPercent}%;"></div>
                                <div class="main-progress-text">${String.format(java.util.Locale.US, "%.1f%%", progressPercent)} 성취</div>
                            </div>
                        </div>
                        
                        <div class="section-title">📊 세부 조건 항목별 현황</div>
                        $checkpointsHtml
                        
                        <div class="section-title">👥 신규 브랜드 파트너(BP) 후원 실적</div>
                        <div class="card" style="margin-bottom:0;">
                            $partnersHtml
                        </div>
                    </div>
                    <div class="footer">
                        <p style="color: #4B5563; font-weight: 700;">유사나(USANA)</p>
                        <p>리더님의 열정과 성공 도전을 진심으로 응원합니다! 파라다이스에서 만나요!</p>
                        <p style="margin-top: 12px; font-size: 9px;">보고서 생성일시: $nowStr | 본 문서는 성취 도우미 모바일 앱에서 자동 생성되었습니다.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        val fileName = "USANA_Report_${System.currentTimeMillis()}.html"
        val reportDir = context.getExternalFilesDir("Reports") ?: context.cacheDir
        if (reportDir != null && !reportDir.exists()) {
            reportDir.mkdirs()
        }
        val reportFile = File(reportDir, fileName)
        java.io.FileOutputStream(reportFile).use { out ->
            out.write(htmlContent.toByteArray(Charsets.UTF_8))
        }

        Toast.makeText(context, "보고서가 HTML 파일로 내장저장소에 저장되었습니다.", Toast.LENGTH_SHORT).show()

        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            reportFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "[$promotionTitle] 성취 HTML 보고서")
            putExtra(Intent.EXTRA_TEXT, "유사나 프로모션 성취 보고서(HTML 포맷)를 공유합니다.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "HTML 보고서 파일 공유"))

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "HTML 생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun DetailProgressGaugeCard(
    title: String,
    progressPercent: Double,
    isExpired: Boolean,
    isAchieved: Boolean,
    startDateStr: String,
    endDateStr: String,
    usanaBlue: Color,
    usanaTeal: Color,
    goldAccent: Color,
    darkGold: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$startDateStr ~ $endDateStr",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Beautiful Circular Progress Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(174.dp)
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = (progressPercent / 100.0).toFloat(),
                    animationSpec = tween(durationMillis = 800),
                    label = "circularProgress"
                )

                Canvas(modifier = Modifier.size(160.dp)) {
                    // Gray underlying track
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = -210f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Active ring brush
                    val activeBrush = Brush.sweepGradient(
                        colors = if (isAchieved) {
                            listOf(goldAccent, Color(0xFFFCD34D), goldAccent)
                        } else {
                            listOf(usanaBlue, usanaTeal, usanaBlue)
                        }
                    )

                    drawArc(
                        brush = activeBrush,
                        startAngle = -210f,
                        sweepAngle = animatedProgress * 240f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inside statistics values
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.1f%%", progressPercent),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isAchieved -> darkGold
                            isExpired -> Color.Gray
                            else -> usanaBlue
                        }
                    )
                    Text(
                        text = if (isAchieved) "목표 충족" else if (isExpired) "달성 실패종료" else "달성 진척율",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub Status Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isAchieved -> goldAccent.copy(alpha = 0.15f)
                            isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else -> Color(0xFFE8F5E9)
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when {
                        isAchieved -> "축하드립니다! 도전 과제를 완료했습니다."
                        isExpired -> "기간이 마감되어 상태가 종료로 변경되었습니다."
                        else -> "조건을 조절하면서 진행률을 100%까지 달성해 보세요!"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isAchieved -> darkGold
                        isExpired -> MaterialTheme.colorScheme.onErrorContainer
                        else -> Color(0xFF2E7D32)
                    }
                )
            }
        }
    }
}

@Composable
fun ExpandableInfoCard(
    headerTitle: String,
    content: String,
    isDefaultExpanded: Boolean,
    iconColor: Color
) {
    var expanded by remember { mutableStateOf(isDefaultExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(iconColor, CircleShape))
                    Text(text = headerTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = if (expanded) "접기" else "자세히",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = content,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CheckpointInputCard(
    checkpoint: CheckpointEntity,
    isExpired: Boolean,
    onUpdateValue: (Double) -> Unit,
    usanaBlue: Color,
    usanaTeal: Color,
    goldAccent: Color
) {
    val isSatisfied = checkpoint.currentValue >= checkpoint.targetValue
    val localProgress = if (checkpoint.targetValue <= 0.0) 0.0 else (checkpoint.currentValue / checkpoint.targetValue).coerceIn(0.0, 1.0)

    val currentValInt = checkpoint.currentValue.roundToInt()
    val targetValInt = checkpoint.targetValue.roundToInt()

    var showDirectInput by remember { mutableStateOf(false) }
    var typedValue by remember(checkpoint.currentValue) { mutableStateOf(currentValInt.toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .testTag("checkpoint_card_${checkpoint.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSatisfied) Color(0xFFF0FDF4).copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSatisfied) Color(0xFFBBF7D0).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = checkpoint.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "목표 수치: $targetValInt ${checkpoint.unit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Mini Indicator Icon
                if (isSatisfied) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("충족", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("진행중", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slider & Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Increment/Decrement Controls
                IconButton(
                    onClick = {
                        if (!isExpired && checkpoint.currentValue > 0.0) {
                            onUpdateValue(checkpoint.currentValue - 1.0)
                        }
                    },
                    enabled = !isExpired && checkpoint.currentValue > 0.0,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .testTag("decrement_button_${checkpoint.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "감소",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Central Active Slider
                Slider(
                    value = checkpoint.currentValue.toFloat(),
                    onValueChange = {
                        if (!isExpired) {
                            onUpdateValue(it.toDouble())
                        }
                    },
                    valueRange = 0f..checkpoint.targetValue.toFloat().coerceAtLeast(1f) * 1.5f, // Allow sliding beyond target
                    modifier = Modifier
                        .weight(1f)
                        .testTag("slider_${checkpoint.id}"),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isSatisfied) Color(0xFF16A34A) else usanaBlue,
                        activeTrackColor = if (isSatisfied) Color(0xFF4ADE80) else usanaTeal
                    ),
                    enabled = !isExpired
                )

                IconButton(
                    onClick = {
                        if (!isExpired) {
                            onUpdateValue(checkpoint.currentValue + 1.0)
                        }
                    },
                    enabled = !isExpired,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .testTag("increment_button_${checkpoint.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "증가",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = showDirectInput) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = typedValue,
                            onValueChange = { typedValue = it },
                            placeholder = { Text("수치 예: 10") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("direct_input_${checkpoint.id}"),
                            singleLine = true,
                            enabled = !isExpired,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = usanaBlue,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            label = { Text("실적 직접 등록 (${checkpoint.unit})", fontSize = 11.sp) }
                        )

                        Button(
                            onClick = {
                                val inputDouble = typedValue.toDoubleOrNull()
                                if (inputDouble != null && inputDouble >= 0.0) {
                                    onUpdateValue(inputDouble)
                                    showDirectInput = false
                                }
                            },
                            enabled = !isExpired,
                            colors = ButtonDefaults.buttonColors(containerColor = usanaBlue),
                            modifier = Modifier.testTag("direct_save_button_${checkpoint.id}")
                        ) {
                            Text("저장", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // Stats footer inside Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "현재 진행률: ${(localProgress * 100).roundToInt()}%",
                        fontSize = 11.sp,
                        color = if (isSatisfied) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (!isExpired) {
                        Text(
                            text = if (showDirectInput) "✎ 닫기" else "✎ 수치 직접 기입",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = usanaBlue,
                            modifier = Modifier.clickable { showDirectInput = !showDirectInput }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$currentValInt",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSatisfied) Color(0xFF15803D) else usanaBlue
                    )
                    Text(
                        text = "/ $targetValInt ${checkpoint.unit}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PartnerTrackingCard(
    partnersList: List<PartnerEntity>,
    onAddPartner: (String, String, Double, String) -> Unit,
    onAddPoints: (PartnerEntity, Double) -> Unit,
    onDeletePartner: (PartnerEntity) -> Unit,
    usanaBlue: Color,
    usanaTeal: Color,
    goldAccent: Color
) {
    // fields for new BP
    var newName by remember { mutableStateOf("") }
    var newJoinDate by remember { mutableStateOf("") }
    var newPoints by remember { mutableStateOf("") }
    var newNotes by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    // Repurchase addition state
    var selectedPartnerForPointsArc by remember { mutableStateOf<PartnerEntity?>(null) }
    var pointsToAddField by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(usanaTeal, CircleShape))
                    Text(
                        text = "신규 브랜드파트너(BP) 실적 관리",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(
                    onClick = { showAddForm = !showAddForm },
                    colors = ButtonDefaults.textButtonColors(contentColor = usanaBlue)
                ) {
                    Text(if (showAddForm) "닫기" else "신규 등록 +", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Register form
            AnimatedVisibility(visible = showAddForm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("새로운 신규 후원 BP 등록", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = usanaBlue)
                    
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("성명 (예: 홍길동)", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("partner_name_input"),
                        singleLine = true
                    )

                    val todayStr = remember { 
                        SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).format(Date()) 
                    }
                    if (newJoinDate.isEmpty()) {
                        newJoinDate = todayStr
                    }

                    OutlinedTextField(
                        value = newJoinDate,
                        onValueChange = { newJoinDate = it },
                        label = { Text("가입일 (YYYY-MM-DD)", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("partner_date_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newPoints,
                        onValueChange = { newPoints = it },
                        label = { Text("최초 가입 구매점수 (볼륨)", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("partner_points_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("메모 (예: 1BC 개설)", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("partner_notes_input"),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val pts = newPoints.toDoubleOrNull() ?: 0.0
                            if (newName.isNotBlank()) {
                                onAddPartner(newName, newJoinDate, pts, newNotes)
                                newName = ""
                                newPoints = ""
                                newNotes = ""
                                showAddForm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = usanaBlue),
                        modifier = Modifier.fillMaxWidth().testTag("save_partner_button")
                    ) {
                        Text("BP 등록 및 추적 시작", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Partner list
            if (partnersList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "💡 등록된 신규 브랜드파트너(BP)가 없습니다.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "우측 상단 '신규 등록'으로 대상자를 추가해 보세요!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    partnersList.forEach { partner ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("partner_card_${partner.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // Custom initials avatar
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(usanaBlue.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = partner.name.take(1),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = usanaBlue
                                            )
                                        }

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(partner.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(usanaBlue.copy(alpha = 0.1f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("신규 BP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = usanaBlue)
                                                }
                                            }
                                            Text("가입일: ${partner.joinDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    // Delete btn
                                    IconButton(
                                        onClick = { onDeletePartner(partner) },
                                        modifier = Modifier.size(32.dp).testTag("delete_partner_${partner.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "삭제",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("누적 구매점수:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "${partner.purchasePoints.roundToInt()} 점",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF16A34A)
                                            )
                                        }
                                        if (partner.notes.isNotEmpty()) {
                                            Text(
                                                text = "메모: ${partner.notes}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }

                                    // Repurchase point addition button!
                                    Button(
                                        onClick = { selectedPartnerForPointsArc = partner },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp).testTag("add_points_btn_${partner.id}")
                                    ) {
                                        Text("재구매 점수 가산+", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue for adding repurchase calculation
    if (selectedPartnerForPointsArc != null) {
        val partner = selectedPartnerForPointsArc!!
        AlertDialog(
            onDismissRequest = { 
                selectedPartnerForPointsArc = null
                pointsToAddField = ""
            },
            title = {
                Text(
                    text = "${partner.name}님 재구매 점수 누적 가산",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "프로모션 기간 내 재구매로 발생한 실적 점수를 입력하세요. 기존 누적 점수(${partner.purchasePoints.roundToInt()}점)에 추가 계산되어 영구 가산됩니다.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = pointsToAddField,
                        onValueChange = { pointsToAddField = it },
                        placeholder = { Text("예: 200") },
                        singleLine = true,
                        label = { Text("가산할 재구매 점수", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("points_add_input")
                    )

                    // Quick-add badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(100.0, 200.0, 500.0, 1000.0).forEach { amt ->
                            SuggestionChip(
                                onClick = { pointsToAddField = amt.roundToInt().toString() },
                                label = { Text("+${amt.roundToInt()}", fontSize = 10.sp) },
                                modifier = Modifier.testTag("quick_add_${amt.roundToInt()}")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val addVal = pointsToAddField.toDoubleOrNull()
                        if (addVal != null && addVal > 0.0) {
                            onAddPoints(partner, addVal)
                            selectedPartnerForPointsArc = null
                            pointsToAddField = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.testTag("confirm_points_add_button")
                ) {
                    Text("가산 등록", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        selectedPartnerForPointsArc = null
                        pointsToAddField = ""
                    },
                    modifier = Modifier.testTag("cancel_points_add_button")
                ) {
                    Text("취소")
                }
            }
        )
    }
}
