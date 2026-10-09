package com.example.bai1

import android.os.Bundle
import android.renderscript.Sampler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.bai1.ui.theme.Bai1Theme
import kotlinx.coroutines.handleCoroutineException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Bai1Theme {
                maytinh()
            }
        }
    }
}

@Composable

fun maytinh()
{
    var bien1 by remember {
        mutableStateOf("")
    }
    var bien2 by remember {
        mutableStateOf("")
    }
    var ketqua by remember {
        mutableStateOf("")
    }
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
        Text("Thực Hành 03", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = bien1,
            onValueChange = {bien1=it},
            label = {Text("nhập số đầu")},
            shape = RoundedCornerShape(16.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)

        ) {
            Button(onClick = {
                val a = bien1.toDoubleOrNull()
                val b = bien2.toDoubleOrNull()
                ketqua = if(a != null && b != null)
                {
                    (a+b).toString()
                }
                else
                {
                    "vui lòng nhập đủ 2 số"
                }
            }, shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )) {
                Text("+")
            }
            Button(onClick = {
                val a = bien1.toDoubleOrNull()
                val b = bien2.toDoubleOrNull()
                ketqua = if(a != null && b != null)
                {
                    (a-b).toString()
                }
                else
                {
                    "vui lòng nhập đủ 2 số"
                }
            }, shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Yellow,
                    contentColor = Color.Black
                )) {
                Text("-")
            }
            Button(onClick = {
                val a = bien1.toDoubleOrNull()
                val b = bien2.toDoubleOrNull()
                ketqua = if(a != null && b != null)
                {
                    (a*b).toString()
                }
                else
                {
                    "vui lòng nhập đủ 2 số"
                }
            }, shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Magenta,
                    contentColor = Color.White
                )) {
                Text("*")
            }
            Button(onClick = {
                val a = bien1.toDoubleOrNull()
                val b = bien2.toDoubleOrNull()
                ketqua = if(a != null && b != null)
                {
                    (a/b).toString()
                }
                else
                {
                    "vui lòng nhập đủ 2 số"
                }
            }, shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )) {
                Text("/")
            }

        }
        OutlinedTextField(
            value = bien2,
            onValueChange = {bien2=it},
            label = {Text("nhập số sau")},
            shape = RoundedCornerShape(16.dp)
        )
        Text("Kết quả : $ketqua")
    }
}
