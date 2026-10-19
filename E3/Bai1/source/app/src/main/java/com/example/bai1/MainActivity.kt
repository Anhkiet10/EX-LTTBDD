package com.example.bai1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bai1.ui.theme.Bai1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Bai1Theme {
                AppNavigation()
            }
        }
    }
}

// Màu dùng trong giao diện
val TitleBlue = Color(0xFF2196F3)
val CardBlue = Color(0xFFBBDEFB)
val CardPink = Color(0xFFF8B4B4)
val BrownColor = Color(0xFFC77D22)


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            UIComponentsListScreen(navController)
        }
        composable("text_detail") {
            TextDetailScreen(navController)
        }
        composable("image_detail") {
            ImageDetailScreen(navController)
        }
        composable("textfield_detail") {
            TextFieldDetailScreen(navController)
        }
        composable("row_detail") {
            RowLayoutScreen(navController)
        }
        composable("password_detail") {
            PasswordDetailScreen(navController)
        }
        composable("column_detail") {
            ColumnLayoutScreen(navController)
        }
        // Màn hình mới sau này thì thêm tiếp ở đây, ví dụ:
        // composable("image_detail") { ImageDetailScreen(navController) }
    }
}


@Composable
fun UIComponentsListScreen(navController: NavHostController) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        UIComponentsList(
            modifier = Modifier.padding(innerPadding),
            onTextClick = { navController.navigate("text_detail") },
            onImageClick = { navController.navigate("image_detail") },
            onTextFieldClick = { navController.navigate("textfield_detail") },
            onRowClick = { navController.navigate("row_detail") },
            onPasswordClick = { navController.navigate("password_detail") },
            onColumnClick = { navController.navigate("column_detail") }
        )
    }
}

@Composable
fun UIComponentsList(
    modifier: Modifier = Modifier,
    onTextClick: () -> Unit = {},
    onImageClick: () -> Unit = {},
    onTextFieldClick: () -> Unit = {},
    onRowClick: () -> Unit = {},
    onPasswordClick: () -> Unit = {},
    onColumnClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "UI Components List",
            color = TitleBlue,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        SectionTitle("Display")
        ComponentCard("Text", "Displays text", onClick = onTextClick)   // (MỚI) bấm để chuyển màn
        ComponentCard("Image", "Displays an image", onClick = onImageClick)

        SectionTitle("Input")
        ComponentCard("TextField", "Input field for text", onClick = onTextFieldClick)
        ComponentCard("PasswordField", "Input field for passwords", onClick = onPasswordClick)

        SectionTitle("Layout")
        ComponentCard("Column", "Arranges elements vertically", onClick = onColumnClick)
        ComponentCard("Row", "Arranges elements horizontally", onClick = onRowClick)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun ComponentCard(
    title: String,
    description: String,
    backgroundColor: Color = CardBlue,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = description, fontSize = 15.sp)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDetailScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Text Detail", color = TitleBlue, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("<", fontSize = 24.sp, color = TitleBlue)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    append("The ")

                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append("quick")
                    }
                    append(" ")

                    withStyle(SpanStyle(color = BrownColor, fontSize = 44.sp)) {
                        append("Brown")
                    }
                    append("\nfox ")

                    withStyle(SpanStyle(letterSpacing = 8.sp)) {
                        append("jumps")
                    }
                    append(" ")

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                    ) {
                        append("over")
                    }
                    append("\n")

                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("the")
                    }
                    append(" ")

                    withStyle(
                        SpanStyle(fontFamily = FontFamily.Cursive, fontStyle = FontStyle.Italic)
                    ) {
                        append("lazy")
                    }
                    append(" dog.")
                },
                fontSize = 32.sp,
                lineHeight = 52.sp
            )

            HorizontalDivider(modifier = Modifier.padding(top = 24.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(title: String, navController: NavHostController) {
    CenterAlignedTopAppBar(
        title = { Text(title, color = TitleBlue, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("<", fontSize = 24.sp, color = TitleBlue)
            }
        }
    )
}


const val IMAGE_URL =
    "https://giaothongvantaitphcm.edu.vn/wp-content/uploads/2025/01/Logo-GTVT.png"

@Composable
fun ImageDetailScreen(navController: NavHostController) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = { DetailTopBar("Images", navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AsyncImage(
                model = IMAGE_URL,
                contentDescription = "Ảnh từ internet",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = IMAGE_URL,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { uriHandler.openUri(IMAGE_URL) }
            )

            Spacer(modifier = Modifier.height(8.dp))


            AsyncImage(
                model = R.drawable.uth,
                contentDescription = "Ảnh trong app",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(text = "In app", fontSize = 15.sp, color = Color.DarkGray)
        }
    }
}


@Composable
fun TextFieldDetailScreen(navController: NavHostController) {
    // Biến lưu nội dung người dùng nhập
    var text by remember { mutableStateOf("") }

    Scaffold(
        topBar = { DetailTopBar("TextField", navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Thông tin nhập", color = Color.Gray) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = text.ifEmpty { "Tự động cập nhật dữ liệu theo textfield" },
                color = Color(0xFFD32F2F),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


val LightBlueBox = Color(0xFFA3BFF0)
val DarkBlueBox = Color(0xFF5588EE)
val RowBackground = Color(0xFFF2F3F5)

@Composable
fun RowLayoutScreen(navController: NavHostController) {
    Scaffold(
        topBar = { DetailTopBar("Row Layout", navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            repeat(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RowBackground, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorBox(LightBlueBox, Modifier.weight(1f))
                    ColorBox(DarkBlueBox, Modifier.weight(1f))
                    ColorBox(LightBlueBox, Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
fun PasswordDetailScreen(navController: NavHostController) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }   // true = hiện mật khẩu

    Scaffold(
        topBar = { DetailTopBar("PasswordField", navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Nhập mật khẩu", color = Color.Gray) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,

                visualTransformation = if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(if (showPassword) "Ẩn" else "Hiện", color = TitleBlue)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (password.isEmpty()) "Mật khẩu sẽ được ẩn khi nhập"
                else "Độ dài mật khẩu: ${password.length} ký tự",
                color = Color(0xFFD32F2F),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun ColumnLayoutScreen(navController: NavHostController) {
    Scaffold(
        topBar = { DetailTopBar("Column Layout", navController) }
    ) { innerPadding ->
        // Row chứa 3 cột đặt cạnh nhau
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            repeat(3) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(RowBackground, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorBox(LightBlueBox, Modifier.fillMaxWidth())
                    ColorBox(DarkBlueBox, Modifier.fillMaxWidth())
                    ColorBox(LightBlueBox, Modifier.fillMaxWidth())
                    ColorBox(DarkBlueBox, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun ColorBox(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(72.dp)
            .background(color, RoundedCornerShape(12.dp))
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    Bai1Theme {
        AppNavigation()
    }
}