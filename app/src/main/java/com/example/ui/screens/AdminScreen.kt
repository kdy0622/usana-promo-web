package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PromotionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: PromotionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val promotions by viewModel.promotionsState.collectAsState()
    val currentTime by viewModel.systemTime.collectAsState()

    // Form states
    var isEditing by remember { mutableStateOf(false) }
    var editingPromotionId by remember { mutableStateOf(0L) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var qualifications by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("CUSTOM") } // "UFI", "PHU_QUOC", "CZECH", "SANOVIV", "CUSTOM"

    // Automated Duration Setting Sliders
    var durationMonths by remember { mutableStateOf(3f) } // default 3 months
    var startMonthOffset by remember { mutableStateOf(0f) } // default 0 (starts this month)

    // Checkpoint Form States
    var checkpointTitle by remember { mutableStateOf("") }
    var checkpointTarget by remember { mutableStateOf(5.0) }
    val localCheckpoints = remember { mutableStateListOf<Triple<String, Double, Double>>() }

    // Computations block for duration parameters
    val computedDates = remember(startMonthOffset, durationMonths) {
        val cal = Calendar.getInstance()
        // start date computed
        cal.add(Calendar.MONTH, startMonthOffset.toInt())
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startEpoch = cal.timeInMillis

        // end date computed
        cal.add(Calendar.MONTH, durationMonths.toInt())
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endEpoch = cal.timeInMillis

        startEpoch to endEpoch
    }

    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    val startStr = sdf.format(Date(computedDates.first))
    val endStr = sdf.format(Date(computedDates.second))

    val usanaBlue = Color(0xFF4F46E5) // Brand Indigo
    val usanaTeal = Color(0xFF0EA5E9) // Sky Blue

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프로모션 관리자 대시보드", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("admin_back_button")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section Header: Register New Promotion
            item {
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
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.PlaylistAdd, contentDescription = if (isEditing) "수정" else "등록", tint = usanaBlue)
                            Text(text = if (isEditing) "선택한 프로모션 상세 수정" else "새 프로모션 캠페인 등록", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        // Form input: Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("프로모션 명칭") },
                            placeholder = { Text("예: 하반기 직급 골드 클럽 도전") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_title_input"),
                            singleLine = true
                        )

                        // Segmented Category Selection Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "분류 카테고리 기안", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("UFI", "PHU_QUOC", "CZECH", "SANOVIV", "CUSTOM").forEach { cat ->
                                    val isSelected = selectedCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) usanaBlue else Color.White.copy(alpha = 0.5f))
                                            .clickable { selectedCategory = cat }
                                            .drawBehind {
                                                if (!isSelected) {
                                                    drawRoundRect(
                                                        color = Color.White.copy(alpha = 0.5f),
                                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                                    )
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (cat) {
                                                "UFI" -> "UFI"
                                                "PHU_QUOC" -> "푸쿠옥"
                                                "CZECH" -> "체코"
                                                "SANOVIV" -> "사노비브"
                                                else -> "맞춤"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Form input: Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("프로모션 개요 및 설명") },
                            placeholder = { Text("파트너 리더들과의 연대 및 성장의 기회를 상세히 기재 수록") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("form_desc_input"),
                            maxLines = 4
                        )

                        // Form input: Qualifications
                        OutlinedTextField(
                            value = qualifications,
                            onValueChange = { qualifications = it },
                            label = { Text("상세 자격 조건 (줄바꿈 구분)") },
                            placeholder = { Text("1. 디렉터 이상 지원\n2. 후원 파트너 전원 등록\n3. 누적 SVP 볼륨 2000점") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("form_qual_input"),
                            maxLines = 4
                        )

                        // Dynamic Month Timeline Sliders
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.EventNote, contentDescription = "달력", tint = usanaBlue, modifier = Modifier.size(16.dp))
                                    Text(text = "도전 평가 기간 자동 계산 설정", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Column {
                                    Text(
                                        text = "시작 시점: ${if (startMonthOffset.toInt() == 0) "이번 달" else "${startMonthOffset.toInt()}개월 후"}",
                                        fontSize = 11.sp
                                    )
                                    Slider(
                                        value = startMonthOffset,
                                        onValueChange = { startMonthOffset = it },
                                        valueRange = 0f..6f,
                                        steps = 5,
                                        modifier = Modifier.height(24.dp)
                                    )
                                }

                                Column {
                                    Text(text = "도전 기간: ${durationMonths.toInt()}개월", fontSize = 11.sp)
                                    Slider(
                                        value = durationMonths,
                                        onValueChange = { durationMonths = it },
                                        valueRange = 1f..12f,
                                        steps = 10,
                                        modifier = Modifier.height(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "설정된 달성 기간: $startStr ~ $endStr",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = usanaBlue
                                )
                            }
                        }

                        // Form segment: Checkpoint Creation
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(text = "달성을 위한 세부 체크포인트 추가", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = checkpointTitle,
                                        onValueChange = { checkpointTitle = it },
                                        label = { Text("체크 과제 명칭") },
                                        placeholder = { Text("예: 파트너 후원") },
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .testTag("ckpt_title_input"),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = if (checkpointTarget <= 0.0) "" else checkpointTarget.toInt().toString(),
                                        onValueChange = {
                                            val doubleVal = it.toDoubleOrNull() ?: 0.0
                                            checkpointTarget = doubleVal
                                        },
                                        label = { Text("목표치") },
                                        placeholder = { Text("5") },
                                        modifier = Modifier
                                            .weight(0.8f)
                                            .testTag("ckpt_val_input"),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (checkpointTitle.isNotEmpty() && checkpointTarget > 0.0) {
                                                localCheckpoints.add(Triple(checkpointTitle, checkpointTarget, 0.0))
                                                // reset form
                                                checkpointTitle = ""
                                                checkpointTarget = 5.0
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .testTag("add_ckpt_pill_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = usanaTeal)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "추가")
                                    }
                                }

                                // Interactive listing of currently added checkpoint rows
                                if (localCheckpoints.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "추가 예정 조건 목록:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        localCheckpoints.forEachIndexed { index, triple ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "${triple.first} (${triple.second.toInt()})",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Cancel,
                                                        contentDescription = "삭제",
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { localCheckpoints.removeAt(index) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Action Submit Button / Edit Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isEditing) {
                                Button(
                                    onClick = {
                                        isEditing = false
                                        editingPromotionId = 0L
                                        title = ""
                                        description = ""
                                        qualifications = ""
                                        selectedCategory = "CUSTOM"
                                        localCheckpoints.clear()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("cancel_edit_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text(text = "수정 취소", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Button(
                                onClick = {
                                    if (title.isNotEmpty()) {
                                        if (isEditing) {
                                            viewModel.updatePromotion(
                                                id = editingPromotionId,
                                                title = title,
                                                category = selectedCategory,
                                                description = description,
                                                qualifications = qualifications.ifBlank { "자체 기획 맞춤 인센티브" },
                                                startDate = computedDates.first,
                                                endDate = computedDates.second,
                                                checkpoints = localCheckpoints.toList()
                                            )
                                        } else {
                                            viewModel.addPromotion(
                                                title = title,
                                                category = selectedCategory,
                                                description = description,
                                                qualifications = qualifications.ifBlank { "자체 기획 맞춤 인센티브" },
                                                startDate = computedDates.first,
                                                endDate = computedDates.second,
                                                checkpoints = localCheckpoints.map { it.first to it.second }
                                            )
                                        }
                                        // Reset Form
                                        isEditing = false
                                        editingPromotionId = 0L
                                        title = ""
                                        description = ""
                                        qualifications = ""
                                        selectedCategory = "CUSTOM"
                                        localCheckpoints.clear()
                                        onNavigateBack()
                                    }
                                },
                                enabled = title.isNotEmpty(),
                                modifier = Modifier
                                    .weight(2.9f)
                                    .height(48.dp)
                                    .testTag("submit_promo_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = usanaBlue)
                            ) {
                                Text(
                                    text = if (isEditing) "수정 사항 반영하기" else "신규 프로모션 캠페인 등록하기",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Section Header: Manage Existing Challenge Items
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(top = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ListAlt, contentDescription = "대장", tint = usanaBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "참가 중인 프로모션 삭제 및 취소 관리", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (promotions.isEmpty()) {
                item {
                    Text(
                        text = "현재 진행 및 도전 중인 유사나 프로모션 내역이 존재하지 않습니다.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(promotions, key = { it.promotion.id }) { item ->
                AdminPromotionManageItem(
                    item = item,
                    currentTime = currentTime,
                    onEdit = {
                        isEditing = true
                        editingPromotionId = item.promotion.id
                        title = item.promotion.title
                        description = item.promotion.description
                        qualifications = item.promotion.qualifications
                        selectedCategory = item.promotion.category
                        
                        localCheckpoints.clear()
                        localCheckpoints.addAll(
                            item.checkpoints.map { Triple(it.title, it.targetValue, it.currentValue) }
                        )
                    },
                    onDelete = {
                        viewModel.deletePromotion(item)
                        if (isEditing && editingPromotionId == item.promotion.id) {
                            isEditing = false
                            editingPromotionId = 0L
                            title = ""
                            description = ""
                            qualifications = ""
                            selectedCategory = "CUSTOM"
                            localCheckpoints.clear()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdminPromotionManageItem(
    item: com.example.data.database.PromotionWithCheckpoints,
    currentTime: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isExpired = item.isExpired(currentTime)
    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    val endStr = sdf.format(Date(item.promotion.endDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.promotion.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "마감: $endStr",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isExpired) "| 종료" else "| 진행 중",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpired) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("edit_promo_${item.promotion.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "수정하기",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_promo_${item.promotion.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "삭제하기",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

