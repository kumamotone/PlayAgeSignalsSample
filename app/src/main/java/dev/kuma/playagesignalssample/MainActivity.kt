package dev.kuma.playagesignalssample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.kuma.playagesignalssample.ui.theme.PlayAgeSignalsSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlayAgeSignalsSampleTheme {
                val viewModel: AgeSignalsViewModel = viewModel(
                    factory = AgeSignalsViewModel.Factory(applicationContext)
                )
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AgeSignalsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AgeSignalsScreen(
    viewModel: AgeSignalsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Play Age Signals API",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sample Application",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The Play Age Signals API provides information about the user's age verification status. This helps apps provide age-appropriate content and experiences.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button
        when (uiState) {
            is AgeSignalsUiState.Idle -> {
                Button(
                    onClick = { viewModel.checkAgeSignals() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check Age Signals")
                }
            }
            is AgeSignalsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Checking age signals...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is AgeSignalsUiState.Success -> {
                val state = uiState as AgeSignalsUiState.Success
                SuccessResult(
                    installId = state.installId,
                    userStatus = state.userStatus,
                    userStatusText = state.userStatusText,
                    onReset = { viewModel.resetState() }
                )
            }
            is AgeSignalsUiState.Error -> {
                val state = uiState as AgeSignalsUiState.Error
                ErrorResult(
                    errorMessage = state.message,
                    errorCode = state.errorCode,
                    onRetry = { viewModel.checkAgeSignals() },
                    onReset = { viewModel.resetState() }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // User Status Reference
        AnimatedVisibility(
            visible = uiState is AgeSignalsUiState.Idle || uiState is AgeSignalsUiState.Success,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            UserStatusReference()
        }
    }
}

@Composable
fun SuccessResult(
    installId: String,
    userStatus: Int,
    userStatusText: String,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Age Signals Retrieved",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Status
            ResultRow(
                label = "User Status",
                value = userStatusText
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Install ID
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Install ID",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = installId,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Description
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Text(
                    text = AgeSignalsViewModel.getStatusDescription(userStatus),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(onClick = onReset) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun ErrorResult(
    errorMessage: String,
    errorCode: Int?,
    onRetry: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            if (errorCode != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Code: $errorCode",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onReset) {
                    Text("Cancel")
                }
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun ResultRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun UserStatusReference() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "User Status Reference",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        StatusReferenceItem(
            status = "VERIFIED",
            description = "User is verified as 18+"
        )
        StatusReferenceItem(
            status = "SUPERVISED",
            description = "Supervised account with parent-set age"
        )
        StatusReferenceItem(
            status = "SUPERVISED_APPROVAL_PENDING",
            description = "Pending parental approval"
        )
        StatusReferenceItem(
            status = "SUPERVISED_APPROVAL_DENIED",
            description = "Parental approval denied"
        )
        StatusReferenceItem(
            status = "UNKNOWN",
            description = "User status unknown"
        )
    }
}

@Composable
fun StatusReferenceItem(
    status: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuccessResultPreview() {
    PlayAgeSignalsSampleTheme {
        SuccessResult(
            installId = "abc123def456-xyz789",
            userStatus = 1, // AgeSignalsVerificationStatus.VERIFIED
            userStatusText = "VERIFIED",
            onReset = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorResultPreview() {
    PlayAgeSignalsSampleTheme {
        ErrorResult(
            errorMessage = "Failed to retrieve age signals. Please try again.",
            errorCode = -100,
            onRetry = {},
            onReset = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserStatusReferencePreview() {
    PlayAgeSignalsSampleTheme {
        UserStatusReference()
    }
}
