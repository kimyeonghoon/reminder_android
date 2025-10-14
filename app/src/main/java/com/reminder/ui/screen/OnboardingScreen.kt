package com.reminder.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.reminder.utils.rememberHapticFeedback
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.CheckCircle,
        title = "환영합니다!",
        description = "스마트한 리마인더 앱으로 생산성을 극대화하세요. 작업 관리부터 집중 모드까지 모든 기능을 제공합니다."
    ),
    OnboardingPage(
        icon = Icons.Default.Notifications,
        title = "알림과 리마인더",
        description = "중요한 작업을 절대 놓치지 마세요. 알람, 반복 작업, 위치 기반 알림까지 지원합니다."
    ),
    OnboardingPage(
        icon = Icons.Default.GridOn,
        title = "아이젠하워 매트릭스",
        description = "중요도×긴급도로 작업을 4개 쿼드런트로 분류하세요. AI가 긴급도를 자동으로 예측해드립니다."
    ),
    OnboardingPage(
        icon = Icons.Default.Adjust,
        title = "포커스 모드",
        description = "집중 타이머로 생산성을 높이세요. 방해 금지 모드가 자동으로 활성화되어 몰입을 도와줍니다."
    ),
    OnboardingPage(
        icon = Icons.Default.FilterList,
        title = "스마트 필터링",
        description = "우선순위, 카테고리, 태그로 작업을 정리하세요. 고급 필터와 저장된 필터로 빠르게 찾을 수 있습니다."
    ),
    OnboardingPage(
        icon = Icons.Default.Cloud,
        title = "실시간 동기화",
        description = "Firebase로 모든 기기에서 실시간 동기화됩니다. 언제 어디서나 작업을 이어가세요."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pager with page transition animations
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                // 페이지 전환 애니메이션을 위한 offset 계산
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val scale = lerp(
                    start = 0.85f,
                    stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )
                val alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )

                OnboardingPageContent(
                    page = onboardingPages[page],
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .alpha(alpha)
                )
            }

            // Page Indicator with animation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val size by animateFloatAsState(
                        targetValue = if (isSelected) 12f else 8f,
                        animationSpec = tween(300),
                        label = "indicator_size"
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.3f,
                        animationSpec = tween(300),
                        label = "indicator_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(size.dp)
                            .alpha(alpha)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip button (on all pages except last)
                if (pagerState.currentPage < onboardingPages.size - 1) {
                    TextButton(onClick = {
                        haptic.click()
                        onFinished()
                    }) {
                        Text("Skip")
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                // Next/Get Started button
                Button(
                    onClick = {
                        haptic.click()
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinished()
                        }
                    }
                ) {
                    Text(
                        if (pagerState.currentPage < onboardingPages.size - 1)
                            "Next"
                        else
                            "Get Started"
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
