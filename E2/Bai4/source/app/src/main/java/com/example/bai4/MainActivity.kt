package com.example.bai4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bai4.ui.theme.Bai4Theme
import java.nio.file.WatchEvent

class Student (
    var name: String = "",
    var score: Double?=null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Bai4Theme {

                bangdiemlop()
            }
        }
    }
}

@Composable
fun bangdiemlop()
{
    var danhsachdiem = listOf(
        Student("Nguyễn Văn An",9.0),
        Student("Trần Thị Bình",7.2),
        Student("Lê Minh Chi",null),
        Student("Phạm Quốc Dũng",4.5),
        Student("Võ Thanh EM",5.8)
    )
    var sum =0.0
    var cnt =0
    var cntgioi=0
    var max=0.0
    for (x in danhsachdiem)
    {
        if(x.score != null)
        {
            x.score?.let { diem ->
                sum +=diem
                cnt++
                if (max<diem)
                {
                    max=diem
                }
            }
            var bo = false
            x.score?.let {diem ->
                bo =diem>= 8.5
            }
            if(bo)
            {
                cntgioi++
            }

        }
    }
    Column(modifier = Modifier.fillMaxSize().background(color = Color.LightGray)) {
        Row(modifier = Modifier.fillMaxWidth().background(color = Color.Blue).padding(start = 20.dp, top = 50.dp, bottom = 30.dp)) {
            Text("Danh sách điểm", fontWeight = FontWeight.Bold, fontSize = 25.sp, color = Color.White)
        }
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).background(color = Color.Blue, shape = RoundedCornerShape(10.dp)).padding(16.dp)) {
            Text("Điểm trung bình lớp",color = Color.White)
            Text("${sum/cnt}", fontSize = 30.sp,fontWeight = FontWeight.Bold,color = Color.White)
            HorizontalDivider(color = Color.White)
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column() {
                    Text("Giỏi",color = Color.White)
                    Text("${cntgioi}",color = Color.White)
                }
                Column() {
                    Text("Chưa có điểm",color = Color.White)
                    Text("${danhsachdiem.size-cnt}",color = Color.White)
                }
                Column() {
                    Text("Cao nhất",color = Color.White)
                    Text("${max}",color = Color.White)
                }
            }
        }
        Text("DANH SÁCH - ${danhsachdiem.size} SINH VIÊN", Modifier.padding(16.dp))
        Column() {
            for (x in danhsachdiem)
            {
                var xephang = if(x.score != null )
                {
                    when
                    {
                        x.score!! >= 8.5 ->"Giỏi"
                        x.score!! >= 7.0 ->"Khá"
                        x.score!! >= 5.0 ->"Trung bình"
                        else -> "Yếu"

                    }
                }
                else
                {
                    "Chưa có điểm"
                }
                var mau = if(x.score != null )
                {
                    when
                    {
                        x.score!! >= 8.5 ->Color.Green
                        x.score!! >= 7.0 -> Color.Blue
                        x.score!! >= 5.0 -> Color.Yellow
                        else -> Color.Red

                    }
                }
                else
                {
                    Color.Gray
                }
                Row(modifier =  Modifier.fillMaxWidth().padding(16.dp).background(color = Color.White, shape = RoundedCornerShape(10.dp)).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column() {
                        Text("${x.name}", fontWeight = FontWeight.Bold)
                        Text(xephang, color = mau)
                    }
                    if (x.score != null)
                    {
                        Text("${x.score}", fontWeight = FontWeight.Bold)
                    }
                    else
                    {
                        Text("--")
                    }
                }
            }

        }


    }

}