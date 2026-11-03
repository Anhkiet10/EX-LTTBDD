package com.example.bai01

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/* ============================================================
 * Icon nét (khung 24 × 24, giống bộ icon trong Figma)
 * Vẽ bằng Canvas nên không cần thêm thư viện material-icons.
 * ============================================================ */
object UthIcons {
    private fun icon(vararg d: String): List<Path> =
        d.map { PathParser().parsePathString(it).toPath() }

    val Bell = icon(
        "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9",
        "M10.3 21a1.94 1.94 0 0 0 3.4 0",
    )
    val GraduationCap = icon(
        "M22 10v6",
        "M2 10l10-5 10 5-10 5z",
        "M6 12v5c3 3 9 3 12 0v-5",
    )
    val Home = icon(
        "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8",
        "M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z",
    )
    val List = icon(
        "M8 6h13", "M8 12h13", "M8 18h13",
        "M3 6h.01", "M3 12h.01", "M3 18h.01",
    )
    val Mail = icon(
        "M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z",
        "M22 6l-10 7L2 6",
    )
    val Phone = icon(
        "M7 2h10a2 2 0 0 1 2 2v16a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z",
        "M12 18h.01",
    )
    val Plus = icon("M5 12h14", "M12 5v14")
    val Sliders = icon(
        "M21 4h-7", "M10 4H3", "M21 12h-9", "M8 12H3", "M21 20h-5", "M12 20H3",
        "M14 2v4", "M8 10v4", "M16 18v4",
    )
    val ChevronRight = icon("M9 18l6-6-6-6")
    val ChevronLeft = icon("M15 18l-6-6 6-6")
    val Check = icon("M20 6L9 17l-5-5")
    val LogOut = icon(
        "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4",
        "M16 17l5-5-5-5",
        "M21 12H9",
    )
}

/**
 * Vẽ một icon nét.
 * @param strokeWidth độ dày nét theo đơn vị của khung 24 (mặc định 2 như Figma).
 */
@Composable
fun LineIcon(
    paths: List<Path>,
    color: Color,
    size: Dp = 24.dp,
    strokeWidth: Float = 2f,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.size(size)) {
        val k = this.size.width / 24f
        scale(k, pivot = Offset.Zero) {
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            paths.forEach { drawPath(it, color, style = stroke) }
        }
    }
}
