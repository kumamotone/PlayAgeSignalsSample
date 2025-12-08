package dev.kuma.playagesignalssample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val fakeConfig by viewModel.fakeConfig.collectAsState()

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
        
        // Fake mode indicator
        AnimatedVisibility(visible = fakeConfig.enabled) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text(
                    text = "🧪 Testing Mode Active",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

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
                    Text(if (fakeConfig.enabled) "Check Age Signals (Fake)" else "Check Age Signals")
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
                    isFakeMode = fakeConfig.enabled,
                    onReset = { viewModel.resetState() }
                )
            }
            is AgeSignalsUiState.Error -> {
                val state = uiState as AgeSignalsUiState.Error
                ErrorResult(
                    errorMessage = state.message,
                    errorCode = state.errorCode,
                    isFakeMode = fakeConfig.enabled,
                    onRetry = { viewModel.checkAgeSignals() },
                    onReset = { viewModel.resetState() }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Testing Settings Panel
        TestingSettingsPanel(
            fakeConfig = fakeConfig,
            onFakeModeChanged = { viewModel.setFakeModeEnabled(it) },
            onUserStatusChanged = { viewModel.setFakeUserStatus(it) },
            onInstallIdChanged = { viewModel.setFakeInstallId(it) },
            onSimulatedErrorChanged = { viewModel.setSimulatedError(it) }
        )

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
fun TestingSettingsPanel(
    fakeConfig: FakeConfig,
    onFakeModeChanged: (Boolean) -> Unit,
    onUserStatusChanged: (Int) -> Unit,
    onInstallIdChanged: (String) -> Unit,
    onSimulatedErrorChanged: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (fakeConfig.enabled) 
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Header (clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🧪",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Testing Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            
            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    // Use Fake Manager Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Use Fake Manager",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = fakeConfig.enabled,
                            onCheckedChange = onFakeModeChanged
                        )
                    }
                    
                    // Settings only shown when fake mode is enabled
                    AnimatedVisibility(visible = fakeConfig.enabled) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // User Status Dropdown
                            Text(
                                text = "User Status",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusDropdown(
                                selectedStatus = fakeConfig.userStatus,
                                onStatusSelected = onUserStatusChanged
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Install ID Text Field
                            Text(
                                text = "Install ID",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = fakeConfig.installId,
                                onValueChange = onInstallIdChanged,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Simulated Error Dropdown
                            Text(
                                text = "Simulate Error",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ErrorDropdown(
                                selectedError = fakeConfig.simulatedError,
                                onErrorSelected = onSimulatedErrorChanged
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusDropdown(
    selectedStatus: Int,
    onStatusSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(8.dp)
                )
                .clickable { expanded = true }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = UserStatus.toDisplayName(selectedStatus),
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select status"
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            UserStatus.allStatuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(UserStatus.toDisplayName(status)) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ErrorDropdown(
    selectedError: Int,
    onErrorSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(8.dp)
                )
                .clickable { expanded = true }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SimulatedError.toDisplayName(selectedError),
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select error"
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SimulatedError.allErrors.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onErrorSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SuccessResult(
    installId: String,
    userStatus: Int,
    userStatusText: String,
    isFakeMode: Boolean = false,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Age Signals Retrieved",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
                if (isFakeMode) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(Fake)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
            
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
    isFakeMode: Boolean = false,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
                if (isFakeMode) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(Simulated)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
                    )
                }
            }
            
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
            userStatus = 1,
            userStatusText = "VERIFIED",
            isFakeMode = true,
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
            isFakeMode = true,
            onRetry = {},
            onReset = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TestingSettingsPanelPreview() {
    PlayAgeSignalsSampleTheme {
        TestingSettingsPanel(
            fakeConfig = FakeConfig(enabled = true),
            onFakeModeChanged = {},
            onUserStatusChanged = {},
            onInstallIdChanged = {},
            onSimulatedErrorChanged = {}
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
