package com.buildzone.zonebu.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.buildzone.zonebu.presentation.navigation.Routes
import com.buildzone.zonebu.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private data class OnboardingPage(
    val icon: ImageVector,
    val iconTint: Color,
    val bgGradient: List<Color>,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(navController: NavController) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            icon = Icons.Filled.Thermostat,
            iconTint = Color(0xFF38BDF8),
            bgGradient = listOf(Color(0xFF0F172A), Color(0xFF1E3A5F)),
            title = "Track temperature zones",
            description = "Map every corner of your home and discover temperature patterns you never noticed before."
        ),
        OnboardingPage(
            icon = Icons.Filled.Search,
            iconTint = Color(0xFFEF4444),
            bgGradient = listOf(Color(0xFF0F172A), Color(0xFF450A0A)),
            title = "Find cold and hot spots",
            description = "Automatically identify problem areas — cold drafts, overheated walls, and moisture traps."
        ),
        OnboardingPage(
            icon = Icons.Filled.Construction,
            iconTint = Color(0xFF34D399),
            bgGradient = listOf(Color(0xFF0F172A), Color(0xFF064E3B)),
            title = "Improve insulation",
            description = "Get personalized recommendations to fix issues, save energy, and boost your comfort."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            val page = pages[index]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(page.bgGradient)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                page.iconTint.copy(alpha = 0.1f),
                                RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            page.icon,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = page.iconTint
                        )
                    }
                    Spacer(Modifier.height(40.dp))
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { i ->
                    val isSelected = pagerState.currentPage == i
                    val color by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF3B82F6) else Color(0xFF334155),
                        label = "dot"
                    )
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                            .background(color, CircleShape)
                    )
                }
            }

            if (pagerState.currentPage < pages.size - 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            settingsViewModel.setOnboardingDone()
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Skip", color = Color(0xFF64748B))
                    }
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("Next", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Button(
                    onClick = {
                        settingsViewModel.setOnboardingDone()
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text(
                        "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
