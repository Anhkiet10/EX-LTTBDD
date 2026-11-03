package com.example.bai3.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bai3.ui.screens.ConfirmScreen
import com.example.bai3.ui.screens.ForgetPasswordScreen
import com.example.bai3.ui.screens.NewPasswordScreen
import com.example.bai3.ui.screens.VerifyCodeScreen
import com.example.bai3.viewmodel.ResetPasswordViewModel

/**
 * "Bản đồ" điều hướng của app: khai báo màn nào ứng với route nào
 * và bấm nút thì đi đến đâu.
 *
 * Forget Password -> Verify Code -> Create new password -> Confirm
 *        ^                                                  |
 *        |__________________ Summit (xoá back stack) _______|
 */
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Tạo 1 ViewModel duy nhất ở cấp NavGraph và truyền cho cả 4 màn hình
    // => dữ liệu nhập ở màn trước được giữ lại cho màn sau (data flow).
    val vm: ResetPasswordViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.ForgetPassword.route,
        modifier = modifier
    ) {
        composable(Screen.ForgetPassword.route) {
            ForgetPasswordScreen(
                vm = vm,
                onNext = { navController.navigate(Screen.VerifyCode.route) }
            )
        }

        composable(Screen.VerifyCode.route) {
            VerifyCodeScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Screen.NewPassword.route) }
            )
        }

        composable(Screen.NewPassword.route) {
            NewPasswordScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Screen.Confirm.route) }
            )
        }

        composable(Screen.Confirm.route) {
            ConfirmScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onSubmit = {
                    vm.submit()   // lưu thông tin + xoá form
                    navController.navigate(Screen.ForgetPassword.route) {
                        // Xoá toàn bộ back stack: bấm Back ở màn đầu sẽ thoát app
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}
