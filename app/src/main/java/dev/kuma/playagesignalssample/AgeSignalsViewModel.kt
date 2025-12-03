package dev.kuma.playagesignalssample

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 * User status constants from AgeSignalsVerificationStatus
 * Based on: https://developer.android.com/google/play/age-signals/use-age-signals-api
 */
object UserStatus {
    const val UNKNOWN = 0
    const val VERIFIED = 1
    const val SUPERVISED = 2
    const val SUPERVISED_APPROVAL_PENDING = 3
    const val SUPERVISED_APPROVAL_DENIED = 4
}

/**
 * ViewModel for handling Play Age Signals API calls
 */
class AgeSignalsViewModel(
    private val ageSignalsManager: AgeSignalsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgeSignalsUiState>(AgeSignalsUiState.Idle)
    val uiState: StateFlow<AgeSignalsUiState> = _uiState.asStateFlow()

    /**
     * Request age signals from the Play Age Signals API
     */
    fun checkAgeSignals() {
        viewModelScope.launch {
            _uiState.value = AgeSignalsUiState.Loading
            try {
                val request = AgeSignalsRequest.builder().build()
                val result = ageSignalsManager.checkAgeSignals(request).await()
                val status = result.userStatus() ?: UserStatus.UNKNOWN
                _uiState.value = AgeSignalsUiState.Success(
                    installId = result.installId() ?: "",
                    userStatus = status,
                    userStatusText = mapUserStatus(status)
                )
            } catch (e: Exception) {
                _uiState.value = AgeSignalsUiState.Error(
                    message = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Reset the UI state to idle
     */
    fun resetState() {
        _uiState.value = AgeSignalsUiState.Idle
    }

    /**
     * Map user status integer to human-readable string
     */
    private fun mapUserStatus(status: Int): String {
        return when (status) {
            UserStatus.VERIFIED -> "VERIFIED"
            UserStatus.SUPERVISED -> "SUPERVISED"
            UserStatus.SUPERVISED_APPROVAL_PENDING -> "SUPERVISED_APPROVAL_PENDING"
            UserStatus.SUPERVISED_APPROVAL_DENIED -> "SUPERVISED_APPROVAL_DENIED"
            UserStatus.UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN ($status)"
        }
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
