package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("도움말") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 주요 기능 섹션
            HelpSection(
                title = "주요 기능",
                content = {
                    HelpItem(
                        title = "할 일 추가하기",
                        description = "홈 화면 우측 하단의 + 버튼을 눌러 새로운 할 일을 추가할 수 있습니다."
                    )
                    HelpItem(
                        title = "할 일 완료하기",
                        description = "할 일 카드의 체크박스를 누르면 완료 표시가 됩니다."
                    )
                    HelpItem(
                        title = "할 일 수정하기",
                        description = "할 일 카드를 터치하면 수정 화면으로 이동합니다."
                    )
                    HelpItem(
                        title = "할 일 삭제하기",
                        description = "할 일 카드의 휴지통 아이콘을 누르면 삭제됩니다."
                    )
                    HelpItem(
                        title = "우선순위 설정",
                        description = "할 일 추가/수정 시 높음(빨강), 중간(주황), 낮음(초록) 중 선택할 수 있습니다."
                    )
                    HelpItem(
                        title = "날짜/시간 설정",
                        description = "할 일에 마감 날짜와 시간을 설정하면 알림을 받을 수 있습니다."
                    )
                    HelpItem(
                        title = "반복 일정",
                        description = "매일, 매주, 매월 반복되는 일정을 설정할 수 있습니다."
                    )
                    HelpItem(
                        title = "위젯 사용",
                        description = "홈 화면을 길게 눌러 위젯을 추가하면 할 일을 빠르게 확인할 수 있습니다."
                    )
                }
            )

            HorizontalDivider()

            // 설정 기능 섹션
            HelpSection(
                title = "설정 기능",
                content = {
                    HelpItem(
                        title = "테마 변경",
                        description = "설정에서 라이트/다크/시스템 테마를 선택할 수 있습니다."
                    )
                    HelpItem(
                        title = "글씨 크기 조절",
                        description = "설정에서 작게/보통/크게/아주 크게 중 선택하여 글씨 크기를 변경할 수 있습니다."
                    )
                    HelpItem(
                        title = "간편 모드",
                        description = "복잡한 기능을 숨기고 더 큰 버튼으로 사용할 수 있는 모드입니다."
                    )
                }
            )

            HorizontalDivider()

            // 자주 묻는 질문
            HelpSection(
                title = "자주 묻는 질문 (FAQ)",
                content = {
                    FAQItem(
                        question = "알림이 오지 않아요",
                        answer = "설정 → 앱 → Reminder → 알림 권한을 확인해주세요. Android 12 이상에서는 정확한 알람 권한도 필요합니다."
                    )
                    FAQItem(
                        question = "완료한 할 일은 어디서 볼 수 있나요?",
                        answer = "홈 화면에서 필터 칩을 사용하여 '완료됨'을 선택하면 완료한 할 일을 볼 수 있습니다."
                    )
                    FAQItem(
                        question = "데이터를 백업하고 싶어요",
                        answer = "현재 Firebase를 통해 자동으로 동기화됩니다. 같은 계정으로 로그인하면 다른 기기에서도 동일한 데이터를 볼 수 있습니다."
                    )
                    FAQItem(
                        question = "위젯이 업데이트되지 않아요",
                        answer = "위젯을 한 번 터치하거나, 위젯을 제거하고 다시 추가해보세요."
                    )
                    FAQItem(
                        question = "글씨가 너무 작아요",
                        answer = "설정에서 글씨 크기를 '크게' 또는 '아주 크게'로 변경해보세요."
                    )
                }
            )

            HorizontalDivider()

            // 문의하기
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "문제가 해결되지 않으셨나요?",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "앱을 개선하는데 도움을 주셔서 감사합니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HelpSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
fun HelpItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "• $title",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQItem(
    question: String,
    answer: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Q. $question",
                style = MaterialTheme.typography.titleMedium
            )
            if (expanded) {
                Text(
                    text = "A. $answer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
