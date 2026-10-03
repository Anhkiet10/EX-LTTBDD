package com.example.myapplication

import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                profile()
            }
        }
    }
}

@Composable
fun profile()
{
    Box(modifier = Modifier.fillMaxSize())
    {
        Image(painter = painterResource(R.drawable.back),
            contentDescription = "back", modifier = Modifier.align(Alignment.TopStart).padding(30.dp).size(40.dp))
        Image(painter = painterResource(R.drawable.edit),
            contentDescription = "edit", modifier = Modifier.align(Alignment.TopEnd).padding(30.dp).size(40.dp))
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Image(painter = painterResource(R.drawable.avat2),
            contentDescription = "avt", modifier = Modifier.size(240.dp).clip(CircleShape))

        Text("HOÀNG ANH KIỆT", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("077206008228")
    }

}