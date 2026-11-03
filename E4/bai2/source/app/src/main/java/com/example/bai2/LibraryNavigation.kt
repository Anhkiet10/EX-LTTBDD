package com.example.bai2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

/* ------------------------------------------------------------------
 * Các tab của bottom navigation – mỗi tab là một đối tượng (OOP)
 * ------------------------------------------------------------------ */
sealed class LibraryDestination(
    val route: String,
    val label: String,
    /** Đường vẽ icon (Material Icons, khung 24 × 24). */
    val iconPath: String,
) {
    data object Manage : LibraryDestination(
        route = "manage",
        label = "Quản lý",
        iconPath = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z",
    )

    data object Books : LibraryDestination(
        route = "books",
        label = "DS Sách",
        iconPath = "M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-1 9H9V9h10v2zm-4 4H9v-2h6v2zm4-8H9V5h10v2z",
    )

    data object Students : LibraryDestination(
        route = "students",
        label = "Sinh viên",
        iconPath = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z",
    )

    companion object {
        val tabs: List<LibraryDestination> by lazy { listOf(Manage, Books, Students) }
    }
}

@Composable
fun LibraryApp(
    library: LibraryService = remember { LibraryManager.withSampleData() },
    navController: NavHostController = rememberNavController(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (LibraryResult) -> Unit = { result ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(result.message)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun goTo(destination: LibraryDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            LibraryBottomBar(
                currentRoute = currentRoute,
                onSelect = { goTo(it) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LibraryDestination.Manage.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(LibraryDestination.Manage.route) {
                ManageScreen(library = library, onMessage = showMessage)
            }
            composable(LibraryDestination.Books.route) {
                BooksScreen(library = library, onMessage = showMessage)
            }
            composable(LibraryDestination.Students.route) {
                StudentsScreen(
                    library = library,
                    onMessage = showMessage,
                    onStudentSelected = { goTo(LibraryDestination.Manage) }
                )
            }
        }
    }
}

/* ------------------------------------------------------------------
 * Bottom bar giống Figma: nền trắng, đường kẻ trên, icon + nhãn
 * ------------------------------------------------------------------ */
@Composable
private fun LibraryBottomBar(currentRoute: String?, onSelect: (LibraryDestination) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = LibColors.Divider, thickness = 1.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            LibraryDestination.tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                val tint = if (selected) LibColors.TabActive else LibColors.TabInactive
                Column(
                    Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(tab) }
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PathIcon(tab.iconPath, tint, 28.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(tab.label, color = tint, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PathIcon(pathData: String, tint: Color, size: Dp) {
    val path = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    Box(Modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            scale(this.size.width / 24f, pivot = Offset.Zero) {
                drawPath(path, tint)
            }
        }
    }
}
