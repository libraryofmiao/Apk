package com.libraryofmiao.membership.ui.membercard

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.libraryofmiao.membership.data.model.Member
import com.libraryofmiao.membership.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MemberCardUiState(
    val loading: Boolean = true,
    val member: Member? = null,
    val qrBitmap: Bitmap? = null,
    val error: String? = null
)

class MemberCardViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(MemberCardUiState())
    val uiState: StateFlow<MemberCardUiState> = _uiState

    fun load(memberId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.membershipApi.getMember(memberId)
                val member = response.body()?.member
                if (response.isSuccessful && member != null) {
                    val qr = member.verify?.let { buildQrBitmap(it) }
                    _uiState.value = MemberCardUiState(loading = false, member = member, qrBitmap = qr)
                } else {
                    _uiState.value = MemberCardUiState(loading = false, error = "Member not found")
                }
            } catch (e: Exception) {
                _uiState.value = MemberCardUiState(loading = false, error = "Server connection error")
            }
        }
    }

    /** QR encodes the same verify code the web app's verify.html expects, so a scan from either
     *  the app-generated card or a printed card lands on the same /api/verify flow. */
    private fun buildQrBitmap(verifyCode: String, size: Int = 480): Bitmap {
        val writer = QRCodeWriter()
        val matrix = writer.encode("$verifyCode", BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
}
