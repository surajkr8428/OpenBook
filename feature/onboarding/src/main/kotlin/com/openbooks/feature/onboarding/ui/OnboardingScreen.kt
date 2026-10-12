package com.openbooks.feature.onboarding.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openbooks.feature.onboarding.ui.screen.OnboardingScreen
import com.openbooks.feature.onboarding.ui.viewmodel.OnboardingViewModel
import com.openbooks.app.ui.theme.OpenBooksTheme

class OnboardingActivity : ComponentActivity() {
    private val viewModel: OnboardingViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenBooksTheme {
                OnboardingScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
    onFinish: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(uiState.pages) { page ->
                OnboardingPage(page = page)
            }
            
            item {
                androidx.compose.foundation.layout.Spacer(Modifier.height(32.dp))
                
                if (uiState.currentPage == uiState.pages.lastIndex) {
                    Button(
                        onClick = { 
                            viewModel.completeOnboarding()
                            onFinish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = com.openbooks.app.ui.theme.OpenBooksTheme.colorScheme.primary
                        )
                    ) {
                        Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        uiState.pages.forEachIndexed { index, _ ->
                            androidx.compose.foundation.shape.CircleShape
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(if (index == uiState.currentPage) 12.dp else 8.dp)
                                    .padding(4.dp)
                                    .background(
                                        color = if (index == uiState.currentPage) 
                                            com.openbooks.app.ui.theme.OpenBooksTheme.colorScheme.primary 
                                            else com.openbooks.app.ui.theme.OpenBooksTheme.colorScheme.outlineVariant,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.nextPage() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(page: OnboardingPageData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon/Image
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = com.openbooks.app.ui.theme.OpenBooksTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .padding(16.dp)
        ) {
            // Would show icon based on page type
        }
        
        androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
        
        Text(
            text = page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.TextAlign.Center
        )
        
        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
        
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = com.openbooks.app.ui.theme.OpenBooksTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.TextAlign.Center
        )
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: String
)