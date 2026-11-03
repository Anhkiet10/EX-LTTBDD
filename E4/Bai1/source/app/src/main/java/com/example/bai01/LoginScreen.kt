package com.example.bai01

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/* ============================================================
 * Màn 9 – Đăng nhập
 *  Google / Email / Số điện thoại / Dùng thử
 *  - Đăng nhập lỗi       → hộp thoại báo lỗi
 *  - Đăng nhập thành công → Toast "Đăng nhập thành công" + vào Trang chủ
 * ============================================================ */

val UthBorder = Color(0xFFE4E4EA)
val UthMuted = Color(0xFF8E8E93)

@Composable
fun LoginScreen(
    content: LoginContent,
    onGoogleSignIn: suspend () -> AuthResult,
    onMicrosoftSignIn: suspend () -> AuthResult,
    onGitHubSignIn: suspend () -> AuthResult,
    onFacebookSignIn: suspend (accessToken: String) -> AuthResult,
    onTwitterSignIn: suspend () -> AuthResult,
    onYahooSignIn: suspend () -> AuthResult,
    onGuestSignIn: suspend () -> AuthResult,
    onOpenEmail: () -> Unit,
    onOpenPhone: () -> Unit,
    onSignedIn: (UthUser) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loadingKind by remember { mutableStateOf<SignInKind?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastKind by remember { mutableStateOf(SignInKind.GOOGLE) }
    val busy = loadingKind != null

    fun finish(result: AuthResult) {
        loadingKind = null
        when (result) {
            is AuthResult.Success -> {
                showSignedInToast(context, result.user)
                onSignedIn(result.user)
            }
            is AuthResult.Error -> errorMessage = result.message
        }
    }

    // Facebook đi qua Facebook SDK trước, lấy token rồi mới đăng nhập Firebase
    val startFacebookLogin = rememberFacebookLogin(
        onToken = { token -> scope.launch { finish(onFacebookSignIn(token)) } },
        onCancel = { finish(AuthResult.Error("Bạn đã huỷ đăng nhập Facebook.")) },
        onError = { message -> finish(AuthResult.Error(message)) },
    )

    fun runSignIn(kind: SignInKind) {
        if (busy) return
        lastKind = kind
        loadingKind = kind
        if (kind == SignInKind.FACEBOOK) {
            startFacebookLogin()
            return
        }
        scope.launch {
            val result = when (kind) {
                SignInKind.GOOGLE -> onGoogleSignIn()
                SignInKind.MICROSOFT -> onMicrosoftSignIn()
                SignInKind.GITHUB -> onGitHubSignIn()
                SignInKind.TWITTER -> onTwitterSignIn()
                SignInKind.YAHOO -> onYahooSignIn()
                SignInKind.GUEST -> onGuestSignIn()
                SignInKind.FACEBOOK -> AuthResult.Error("")
            }
            finish(result)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()) // nhiều nút → cho cuộn trên máy màn hình nhỏ
            .padding(horizontal = 28.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        LogoMark(
            size = 76.dp,
            corner = 20.dp,
            background = UthBlue,
            foreground = Color.White,
            secondCircleAlpha = 0.7f,
            secondLineAlpha = 0.55f,
            modifier = Modifier.cssShadow(
                RoundedCornerShape(20.dp), y = 10.dp, blur = 24.dp, color = Color(0x402F5BEA)
            )
        )
        Spacer(Modifier.height(24.dp))
        Text(content.title, style = uthText(26.sp, FontWeight.Bold, UthDark, 34.sp, (-0.4).sp))
        Spacer(Modifier.height(10.dp))
        Text(content.description, style = uthText(14.sp, FontWeight.Normal, UthBody, 21.sp))

        Spacer(Modifier.height(36.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AuthOptionButton(
                label = content.googleLabel,
                onClick = { runSignIn(SignInKind.GOOGLE) },
                enabled = !busy,
                loading = loadingKind == SignInKind.GOOGLE,
                loadingLabel = content.loadingLabel,
            ) { GoogleBadge() }

            AuthOptionButton(
                label = content.microsoftLabel,
                onClick = { runSignIn(SignInKind.MICROSOFT) },
                enabled = !busy,
                loading = loadingKind == SignInKind.MICROSOFT,
                loadingLabel = content.loadingLabel,
            ) { MicrosoftLogo() }

            AuthOptionButton(
                label = content.githubLabel,
                onClick = { runSignIn(SignInKind.GITHUB) },
                enabled = !busy,
                loading = loadingKind == SignInKind.GITHUB,
                loadingLabel = content.loadingLabel,
            ) { GitHubLogo() }

            AuthOptionButton(
                label = content.facebookLabel,
                onClick = { runSignIn(SignInKind.FACEBOOK) },
                enabled = !busy,
                loading = loadingKind == SignInKind.FACEBOOK,
                loadingLabel = content.loadingLabel,
            ) { FacebookLogo() }

            AuthOptionButton(
                label = content.twitterLabel,
                onClick = { runSignIn(SignInKind.TWITTER) },
                enabled = !busy,
                loading = loadingKind == SignInKind.TWITTER,
                loadingLabel = content.loadingLabel,
            ) { XLogo() }

            AuthOptionButton(
                label = content.yahooLabel,
                onClick = { runSignIn(SignInKind.YAHOO) },
                enabled = !busy,
                loading = loadingKind == SignInKind.YAHOO,
                loadingLabel = content.loadingLabel,
            ) { YahooLogo() }

            AuthOptionButton(
                label = content.emailLabel,
                onClick = onOpenEmail,
                enabled = !busy,
            ) { LineIcon(UthIcons.Mail, UthSkip, size = 20.dp) }

            AuthOptionButton(
                label = content.phoneLabel,
                onClick = onOpenPhone,
                enabled = !busy,
            ) { LineIcon(UthIcons.Phone, UthSkip, size = 20.dp) }
        }

        // ---- hoặc ----
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f).height(1.dp).background(UthBorder))
            Text(
                content.orLabel,
                style = uthText(12.sp, FontWeight.Normal, UthMuted, 15.sp),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Box(Modifier.weight(1f).height(1.dp).background(UthBorder))
        }

        Text(
            if (loadingKind == SignInKind.GUEST) content.loadingLabel else content.guestLabel,
            style = uthText(15.sp, FontWeight.SemiBold, UthBlue, 19.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = !busy) { runSignIn(SignInKind.GUEST) }
                .padding(vertical = 10.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            buildAnnotatedString {
                append(content.termsPrefix)
                // Bấm vào → mở trang trên Firebase Hosting
                val linkStyle = TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline, color = UthBody))
                withLink(LinkAnnotation.Url(HostingUrls.TERMS, linkStyle)) {
                    append(content.termsLink)
                }
                append(content.termsMiddle)
                withLink(LinkAnnotation.Url(HostingUrls.PRIVACY, linkStyle)) {
                    append(content.privacyLink)
                }
                append(content.termsSuffix)
            },
            style = uthText(11.sp, FontWeight.Normal, UthMuted, 16.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }

    errorMessage?.let { message ->
        AuthErrorDialog(
            message = message,
            onDismiss = { errorMessage = null },
            onRetry = { runSignIn(lastKind) }
        )
    }
}

private enum class SignInKind { GOOGLE, MICROSOFT, GITHUB, FACEBOOK, TWITTER, YAHOO, GUEST }

/** Logo Yahoo: ô tím với chữ Y!. */
@Composable
private fun YahooLogo() {
    Box(
        Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFF6001D2)),
        contentAlignment = Alignment.Center
    ) {
        Text("Y!", style = uthText(10.sp, FontWeight.ExtraBold, Color.White, 12.sp))
    }
}

/** Logo X: ô vuông đen bo góc với chữ X. */
@Composable
private fun XLogo() {
    Box(
        Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("X", style = uthText(12.sp, FontWeight.Bold, Color.White, 14.sp))
    }
}

/** Logo Facebook: hình tròn xanh với chữ f. */
@Composable
private fun FacebookLogo() {
    Box(
        Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color(0xFF1877F2)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text("f", style = uthText(15.sp, FontWeight.ExtraBold, Color.White, 17.sp))
    }
}

/** Logo GitHub (đặc) – nếu đường vẽ lỗi thì hiện vòng tròn đen. */
private val gitHubMark: Path? = runCatching {
    PathParser().parsePathString(
        "M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 " +
            "0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7" +
            "c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998" +
            ".108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22" +
            "-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405" +
            " 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 " +
            "0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 " +
            "0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"
    ).toPath()
}.getOrNull()

@Composable
private fun GitHubLogo() {
    Canvas(Modifier.size(20.dp)) {
        val mark = gitHubMark
        if (mark == null) {
            drawCircle(UthDark)
        } else {
            val k = size.width / 24f
            scale(k, pivot = Offset.Zero) { drawPath(mark, UthDark) }
        }
    }
}

/** Logo Microsoft: 4 ô vuông màu. */
@Composable
private fun MicrosoftLogo() {
    Column(verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
            Box(Modifier.size(8.dp).background(Color(0xFFF25022)))
            Box(Modifier.size(8.dp).background(Color(0xFF7FBA00)))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
            Box(Modifier.size(8.dp).background(Color(0xFF00A4EF)))
            Box(Modifier.size(8.dp).background(Color(0xFFFFB900)))
        }
    }
}

/** Huy hiệu chữ G nhỏ trong nút Google. */
@Composable
private fun GoogleBadge() {
    Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(1.dp, UthBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("G", style = uthText(11.sp, FontWeight.Bold, UthSkip, 13.sp))
    }
}

@Preview(name = "9 Đăng nhập", widthDp = 390, heightDp = 844)
@Composable
fun LoginPreview() {
    LoginScreen(
        content = LocalUthRepository().getLoginContent(),
        onGoogleSignIn = { AuthResult.Error("") },
        onMicrosoftSignIn = { AuthResult.Error("") },
        onGitHubSignIn = { AuthResult.Error("") },
        onFacebookSignIn = { AuthResult.Error("") },
        onTwitterSignIn = { AuthResult.Error("") },
        onYahooSignIn = { AuthResult.Error("") },
        onGuestSignIn = { AuthResult.Error("") },
        onOpenEmail = {},
        onOpenPhone = {},
        onSignedIn = {}
    )
}
