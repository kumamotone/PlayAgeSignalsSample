package dev.kuma.playagesignalssample

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * UI state for the Age Signals screen
 */
sealed class AgeSignalsUiState {
    data object Idle : AgeSignalsUiState()
    data object Loading : AgeSignalsUiState()
    data class Success(
        val installId: String,
        val userStatus: Int,
        val userStatusText: String
    ) : AgeSignalsUiState()
    data class Error(val message: String, val errorCode: Int? = null) : AgeSignalsUiState()
}

/**
 * User status constants matching AgeSignalsVerificationStatus values
 * Based on: https://developer.android.com/google/play/age-signals/use-age-signals-api
 */
object UserStatus {
    const val UNKNOWN = 0
    const val VERIFIED = 1
    const val SUPERVISED = 2
    const val SUPERVISED_APPROVAL_PENDING = 3
    const val SUPERVISED_APPROVAL_DENIED = 4
    
    val allStatuses = listOf(UNKNOWN, VERIFIED, SUPERVISED, SUPERVISED_APPROVAL_PENDING, SUPERVISED_APPROVAL_DENIED)
    
    fun toDisplayName(status: Int): String {
        return when (status) {
            VERIFIED -> "VERIFIED"
            SUPERVISED -> "SUPERVISED"
            SUPERVISED_APPROVAL_PENDING -> "SUPERVISED_APPROVAL_PENDING"
            SUPERVISED_APPROVAL_DENIED -> "SUPERVISED_APPROVAL_DENIED"
            UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN ($status)"
        }
    }
}

/**
 * Error codes matching AgeSignalsErrorCode values
 * Based on: https://developer.android.com/google/play/age-signals/test-age-signals-api
 */
object SimulatedError {
    const val NONE = 0
    const val API_NOT_AVAILABLE = -1
    const val NETWORK_ERROR = -2
    const val TOO_MANY_REQUESTS = -6
    const val PLAY_SERVICES_VERSION_OUTDATED = -7
    const val CLIENT_TRANSIENT_ERROR = -8
    const val APP_NOT_OWNED = -9
    const val INTERNAL_ERROR = -100
    
    val allErrors = listOf(
        NONE to "None",
        API_NOT_AVAILABLE to "API_NOT_AVAILABLE (-1)",
        NETWORK_ERROR to "NETWORK_ERROR (-2)",
        TOO_MANY_REQUESTS to "TOO_MANY_REQUESTS (-6)",
        PLAY_SERVICES_VERSION_OUTDATED to "PLAY_SERVICES_OUTDATED (-7)",
        CLIENT_TRANSIENT_ERROR to "CLIENT_TRANSIENT_ERROR (-8)",
        APP_NOT_OWNED to "APP_NOT_OWNED (-9)",
        INTERNAL_ERROR to "INTERNAL_ERROR (-100)"
    )
    
    fun toDisplayName(code: Int): String {
        return allErrors.find { it.first == code }?.second ?: "Unknown ($code)"
    }
}

/**
 * Configuration for fake mode testing
 */
data class FakeConfig(
    val enabled: Boolean = false,
    val userStatus: Int = UserStatus.VERIFIED,
    val installId: String = "fake-install-id-12345",
    val simulatedError: Int = SimulatedError.NONE
)

/**
 * ViewModel for handling Play Age Signals API calls
 */
class AgeSignalsViewModel(
    private val realManager: AgeSignalsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgeSignalsUiState>(AgeSignalsUiState.Idle)
    val uiState: StateFlow<AgeSignalsUiState> = _uiState.asStateFlow()
    
    private val _fakeConfig = MutableStateFlow(FakeConfig())
    val fakeConfig: StateFlow<FakeConfig> = _fakeConfig.asStateFlow()

    /**
     * Update fake mode enabled state
     */
    fun setFakeModeEnabled(enabled: Boolean) {
        _fakeConfig.update { it.copy(enabled = enabled) }
    }
    
    /**
     * Update fake user status
     */
    fun setFakeUserStatus(status: Int) {
        _fakeConfig.update { it.copy(userStatus = status) }
    }
    
    /**
     * Update fake install ID
     */
    fun setFakeInstallId(installId: String) {
        _fakeConfig.update { it.copy(installId = installId) }
    }
    
    /**
     * Update simulated error
     */
    fun setSimulatedError(errorCode: Int) {
        _fakeConfig.update { it.copy(simulatedError = errorCode) }
    }

    /**
     * Request age signals from the Play Age Signals API
     */
    fun checkAgeSignals() {
        viewModelScope.launch {
            _uiState.value = AgeSignalsUiState.Loading
            
            val config = _fakeConfig.value
            
            try {
                if (config.enabled) {
                    // Use FakeAgeSignalsManager for testing
                    checkWithFakeManager(config)
                } else {
                    // Use real AgeSignalsManager
                    checkWithRealManager()
                }
            } catch (e: AgeSignalsException) {
                _uiState.value = AgeSignalsUiState.Error(
                    message = e.message ?: "Age Signals error occurred",
                    errorCode = e.errorCode
                )
            } catch (e: Exception) {
                _uiState.value = AgeSignalsUiState.Error(
                    message = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    private suspend fun checkWithRealManager() {
        val request = AgeSignalsRequest.builder().build()
        val result = realManager.checkAgeSignals(request).await()
        val status = result.userStatus() ?: UserStatus.UNKNOWN
        _uiState.value = AgeSignalsUiState.Success(
            installId = result.installId() ?: "",
            userStatus = status,
            userStatusText = UserStatus.toDisplayName(status)
        )
    }
    
    /**
     * Use FakeAgeSignalsManager to simulate API responses.
     * Based on: https://developer.android.com/google/play/age-signals/test-age-signals-api
     */
    private suspend fun checkWithFakeManager(config: FakeConfig) {
        val fakeManager = FakeAgeSignalsManager()
        
        if (config.simulatedError != SimulatedError.NONE) {
            // Set up fake error using the error code integer value directly
            fakeManager.setNextAgeSignalsException(AgeSignalsException(config.simulatedError))
        } else {
            // Set up fake success result using user status integer value directly
            val fakeResult = AgeSignalsResult.builder()
                .setUserStatus(config.userStatus)
                .setInstallId(config.installId)
                .build()
            fakeManager.setNextAgeSignalsResult(fakeResult)
        }
        
        val request = AgeSignalsRequest.builder().build()
        val result = fakeManager.checkAgeSignals(request).await()
        val status = result.userStatus() ?: UserStatus.UNKNOWN
        _uiState.value = AgeSignalsUiState.Success(
            installId = result.installId() ?: "",
            userStatus = status,
            userStatusText = UserStatus.toDisplayName(status)
        )
    }

    /**
     * Reset the UI state to idle
     */
    fun resetState() {
        _uiState.value = AgeSignalsUiState.Idle
    }

    companion object {
        /**
         * Get the description for each user status
         */
        fun getStatusDescription(status: Int): String {
            return when (status) {
                UserStatus.VERIFIED -> 
                    "User is verified as 18 years or older."
                UserStatus.SUPERVISED -> 
                    "User has a supervised Google account with age set by a parent."
                UserStatus.SUPERVISED_APPROVAL_PENDING -> 
                    "User has a supervised account, and a significant change is pending parental approval."
                UserStatus.SUPERVISED_APPROVAL_DENIED -> 
                    "User has a supervised account, and a significant change was denied by the parent."
                UserStatus.UNKNOWN -> 
                    "The user's verification status is unknown."
                else -> 
                    "Unrecognized verification status."
            }
        }

        /**
         * Error codes from the Age Signals API
         */
        object ErrorCodes {
            const val API_NOT_AVAILABLE = -1
            const val INVALID_PACKAGE_NAME = -2
            const val APP_NOT_INSTALLED = -3
            const val APP_UID_MISMATCH = -4
            const val USER_CANCELLED = -5
            const val TOO_MANY_REQUESTS = -6
            const val PLAY_SERVICES_VERSION_OUTDATED = -7
            const val CLIENT_TRANSIENT_ERROR = -8
            const val APP_NOT_OWNED = -9
            const val INTERNAL_ERROR = -100
        }
    }

    /**
     * Factory for creating AgeSignalsViewModel with the AgeSignalsManager
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AgeSignalsViewModel::class.java)) {
                val manager = AgeSignalsManagerFactory.create(context)
                return AgeSignalsViewModel(manager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
