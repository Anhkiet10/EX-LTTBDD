package com.example.bai01

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Navigation: Start → Onboarding → Đăng nhập (Google / Email / SĐT / Dùng thử)
 *             → Trang chủ ⇄ Hồ sơ
 * Route lấy từ sealed class [Screen], dữ liệu lấy từ [UthRepository],
 * đăng nhập qua [AuthService] (Firebase).
 */
@Composable
fun UthNavHost(
    repository: UthRepository = remember { LocalUthRepository() },
    // Đăng nhập thật bằng Firebase. Muốn chạy giao diện không cần mạng thì đổi thành
    // remember { FakeAuthService() }  (hoặc FakeAuthService(googleShouldFail = true) để xem luồng lỗi)
    authService: AuthService = LocalContext.current.applicationContext.let { app ->
        remember { FirebaseAuthService(app) }
    },
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val appInfo = remember { repository.getAppInfo() }
    val flow = remember { repository.getOnboardingFlow() }
    val loginContent = remember { repository.getLoginContent() }

    var user by remember { mutableStateOf(authService.currentUser) }

    // Analytics: gắn người dùng + ghi lại mỗi lần chuyển màn hình
    LaunchedEffect(user?.uid) {
        Telemetry.setUser(user)
        user?.uid?.let { PushHelper.registerUser(it) } // FCM: lưu token + đăng ký chủ đề
    }

    // Android 13+: xin quyền hiện thông báo (hỏi 1 lần khi đã đăng nhập)
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, "Bạn sẽ không nhận được thông báo nhắc việc", Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(user?.uid) {
        if (user != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !PushHelper.hasPermission(context)) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            entry.destination.route?.let { Telemetry.screen(it) }
        }
    }

    // ---------------- Firestore: công việc của người dùng đang đăng nhập ----------------
    val taskRepo: TaskRepository = remember { FirestoreTaskRepository() }
    val today = remember { todayKey() }
    val weekDates = remember { weekDateKeys() }
    var taskError by remember { mutableStateOf<String?>(null) }
    val uid = user?.uid

    // Danh sách việc hôm nay (null = đang tải lần đầu), tự cập nhật realtime
    val tasksOrNull by remember(uid) {
        if (uid == null) flowOf<List<TodoTask>>(emptyList())
        else taskRepo.observeDay(uid, today).catch { e ->
            Telemetry.nonFatal(e, "observeDay")
            taskError = firestoreMessage(e)
            emit(emptyList())
        }
    }.collectAsState(initial = null as List<TodoTask>?)
    val tasks = tasksOrNull.orEmpty()

    // Số việc xong mỗi ngày trong tuần (biểu đồ ở Hồ sơ)
    val doneByDate by remember(uid) {
        if (uid == null) flowOf<Map<String, Int>>(emptyMap())
        else taskRepo.observeDoneByDate(uid, weekDates).catch { emit(emptyMap()) }
    }.collectAsState(initial = emptyMap())
    val weekly = WeeklyStats(doneByDate)

    // Remote Config: chữ / banner / bật tắt nút – đổi trên Console là app tự cập nhật
    val appConfig by RemoteConfigService.config.collectAsState()
    // Dự phòng: tự tải lại mỗi 60 giây khi đang mở app (Realtime thường cập nhật ngay)
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            RemoteConfigService.refresh()
        }
    }

    // Thêm / sửa việc
    var showEditor by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TodoTask?>(null) }

    LaunchedEffect(taskError) {
        taskError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            taskError = null
        }
    }

    val onTaskError: (String) -> Unit = { taskError = it }

    fun toggle(task: TodoTask) {
        val id = uid ?: return
        taskRepo.setDone(id, task.id, !task.isDone, onTaskError)
        Telemetry.taskToggled(!task.isDone)
    }

    /** Đăng nhập xong (bất kỳ cách nào) → vào Trang chủ, xoá các màn đăng nhập khỏi back stack. */
    fun goHome(signedIn: UthUser) {
        user = signedIn
        taskRepo.saveProfile(signedIn) // lưu hồ sơ vào Firestore: users/{uid}
        Telemetry.login(signedIn)
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                appInfo = appInfo,
                onFinished = {
                    // Đã đăng nhập từ trước → vào thẳng Trang chủ
                    val next = if (user != null) Screen.Home.route else Screen.Onboarding.route
                    navController.navigate(next) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                appInfo = appInfo,
                flow = flow,
                onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                content = loginContent,
                onGoogleSignIn = { authService.signInWithGoogle(context) },
                onMicrosoftSignIn = {
                    val activity = context.findActivity()
                    if (activity != null) authService.signInWithMicrosoft(activity)
                    else AuthResult.Error("Không mở được trang đăng nhập Microsoft.")
                },
                onGitHubSignIn = {
                    val activity = context.findActivity()
                    if (activity != null) authService.signInWithGitHub(activity)
                    else AuthResult.Error("Không mở được trang đăng nhập GitHub.")
                },
                onFacebookSignIn = { token -> authService.signInWithFacebook(token) },
                onTwitterSignIn = {
                    val activity = context.findActivity()
                    if (activity != null) authService.signInWithTwitter(activity)
                    else AuthResult.Error("Không mở được trang đăng nhập X.")
                },
                onYahooSignIn = {
                    val activity = context.findActivity()
                    if (activity != null) authService.signInWithYahoo(activity)
                    else AuthResult.Error("Không mở được trang đăng nhập Yahoo.")
                },
                onGuestSignIn = { authService.signInAsGuest() },
                onOpenEmail = { navController.navigate(Screen.EmailLogin.route) },
                onOpenPhone = { navController.navigate(Screen.PhoneLogin.route) },
                onSignedIn = { goHome(it) },
            )
        }

        composable(Screen.EmailLogin.route) {
            EmailLoginScreen(
                authService = authService,
                onBack = { navController.popBackStack() },
                onSignedIn = { goHome(it) },
            )
        }

        composable(Screen.PhoneLogin.route) {
            PhoneLoginScreen(
                authService = authService,
                onBack = { navController.popBackStack() },
                onSignedIn = { goHome(it) },
            )
        }

        composable(Screen.Home.route) {
            user?.let { current ->
                HomeScreen(
                    user = current,
                    tasks = tasks,
                    loading = tasksOrNull == null,
                    onToggleTask = { toggle(it) },
                    onOpenProfile = { navController.navigate(Screen.Profile.route) },
                    onAddTask = { editingTask = null; showEditor = true },
                    onEditTask = { editingTask = it; showEditor = true },
                    config = appConfig,
                )

                if (showEditor) {
                    val editing = editingTask
                    TaskEditorSheet(
                        initial = editing,
                        onDismiss = { showEditor = false },
                        onSave = { title, category, time ->
                            if (editing == null) {
                                taskRepo.add(
                                    current.uid,
                                    TodoTask(id = "", title = title, category = category, time = time, isDone = false, date = today),
                                    onTaskError
                                )
                                Telemetry.taskAdded(category)
                                Toast.makeText(context, "Đã thêm \"$title\"", Toast.LENGTH_SHORT).show()
                            } else {
                                taskRepo.update(
                                    current.uid,
                                    editing.copy(title = title, category = category, time = time),
                                    onTaskError
                                )
                                Telemetry.taskEdited()
                                Toast.makeText(context, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show()
                            }
                            showEditor = false
                        },
                        onDelete = editing?.let { task ->
                            {
                                taskRepo.delete(current.uid, task.id, onTaskError)
                                Telemetry.taskDeleted()
                                Toast.makeText(context, "Đã xoá \"${task.title}\"", Toast.LENGTH_SHORT).show()
                                showEditor = false
                            }
                        },
                    )
                }
            }
        }

        composable(Screen.Profile.route) {
            user?.let { current ->
                ProfileScreen(
                    user = current,
                    tasks = tasks,
                    weekly = weekly,
                    onBack = { navController.popBackStack() },
                    onCopyFcmToken = {
                        PushHelper.copyToken(context) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onTestCrash = if (appConfig.showCrashButton) ({ Telemetry.testCrash() }) else null,
                    onSignOut = {
                        scope.launch {
                            Telemetry.logout()
                            authService.signOut()
                            user = null
                            Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}
