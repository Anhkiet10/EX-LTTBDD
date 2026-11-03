
package com.example.bai1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private val Blue = Color(0xFF2196E8)
private val LightBlue = Color(0xFFB9E0F8)

private val quotes = listOf(
    "The only way to do great work is to love what you do.",
    "Success is the sum of small efforts.",
    "Believe you can and you're halfway there.",
    "It always seems impossible until it's done.",
    "Great things take time.",
    "1tr dòng sức mạnh"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "root"
    ) {
        // Trang 1
        composable("root") {
            RootScreen(
                onPush = {
                    navController.navigate("list")
                }
            )
        }

        // Trang 2
        composable("list") {
            ListScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSelect = { index ->
                    navController.navigate("detail/$index")
                }
            )
        }

        // Trang 3
        composable(
            route = "detail/{index}",
            arguments = listOf(
                navArgument("index") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            val index = entry.arguments?.getInt("index") ?: 0

            DetailScreen(
                quote = quotes.getOrElse(index) {
                    quotes[0]
                },
                onBackToRoot = {
                    navController.navigate("root") {
                        popUpTo("root") {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}


@Composable
fun RootScreen(onPush: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.uth),
                contentDescription = "Logo UTH",
                modifier = Modifier.size(130.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Navigation",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Navigation helps you move between " +
                    "different screens in your application.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onPush,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            )
        ) {
            Text("PUSH")
        }
    }
}


@Composable
fun ListScreen(
    onBack: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onBack() }
            ) {
                Text(
                    text = "❮",
                    color = Blue,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "LazyColumn",
                color = Blue,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold

            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(24.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            itemsIndexed(quotes) { index, quote ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            LightBlue,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            onSelect(index)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1} | $quote",
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Color.Black,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DetailScreen(
    quote: String,
    onBackToRoot: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onBack() }
            ) {
                Text(
                    text = "<",
                    color = Blue,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Detail",
                color = Blue,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(24.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "\"$quote\"",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFB4E7FA),
                            Color(0xFF39A5ED)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"$quote\"",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Steve Jobs",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBackToRoot,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            )
        ) {
            Text("BACK TO ROOT")
        }
    }
}
