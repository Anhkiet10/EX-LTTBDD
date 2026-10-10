
package com.example.btvn02

import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.btvn02.ui.theme.Btvn02Theme
import java.nio.file.WatchEvent

class  Student(
    var name: String="",
    var email: String? = null,
    var phone: String?=null
)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Btvn02Theme {
                danhsachsv()
            }
        }
    }
}

@Composable
fun danhsachsv() {
    var student=listOf(
        Student("nguyễn văn A","a11@gmail.com","0123456789"),
        Student("nguyễn văn B",null,"0123456789"),
        Student("nguyễn văn C","a11@gmail.com",null),
        Student("nguyễn văn D",null,null),
        Student("nguyễn văn E",null,"0123456789")
    )
    Column() {
        Row(modifier = Modifier.fillMaxWidth().background(color = Color.Blue).padding(
            start = 20.dp,
            top = 50.dp,
            bottom = 30.dp
        )) {
            Text("Danh sách sinh viên", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        }
        for (x in student)
        {
            Card(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("${x.name}", fontWeight = FontWeight.Bold)
                    Row {
                        Text("email:")
                        Text("${x.email?:"Chưa cập nhật"}", color = if(x.email==null)
                        {
                            Color.Red
                        }
                        else
                        {
                            Color.Gray
                        })
                    }
                    Row {
                        Text("SĐT:")
                        Text("${x.phone?:"Chưa cập nhật"}", color = if(x.phone==null)
                        {
                            Color.Red
                        }
                        else
                        {
                            Color.Gray
                        })
                    }
                }
            }

        }
        Row(modifier = Modifier.fillMaxWidth().background(color = Color.Cyan,
            shape = RoundedCornerShape(8.dp)).padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Row {
                Text("Tổng:")
                Text("${student.size} sinh viên", fontWeight = FontWeight.Bold)
            }
            Row {
                Text("Thiếu email :")
                Text("${student.count({it.email==null})}", color = Color.Red)
            }
        }
    }

}

