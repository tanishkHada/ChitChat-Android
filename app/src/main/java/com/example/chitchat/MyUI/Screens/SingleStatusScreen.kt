package com.example.chitchat.MyUI.Screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.chitchat.MyUI.components.commonImage
import com.example.chitchat.R
import com.example.chitchat.ui.theme.myTrackColor
import kotlinx.coroutines.delay

@Composable
fun SingleStatusScreen(
    username: String,
    imageUrl: String,
    statusList: ArrayList<String>,
    timestampList: ArrayList<String>
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(0) }
    val totalImages = statusList.size
    val progress = remember { mutableStateListOf(*Array(totalImages) { 0f }) }

    // Start the progress animation
    LaunchedEffect(currentIndex) {
        while (progress[currentIndex] < 1f) {
            progress[currentIndex] += 0.006f
            delay(16)
        }
        if (currentIndex < totalImages - 1) {
            currentIndex++
        } else {
            finishActivity(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        Image(
            painter = rememberAsyncImagePainter(model = statusList[currentIndex]),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                progress.forEachIndexed { index, progressValue ->
                    LinearProgressIndicator(
                        progress = progressValue,
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 2.dp),
                        color = Color.White,
                        trackColor = myTrackColor
                    )
                }
            }

            //back arrow, image, username
            Row(
                modifier = Modifier.padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { (context as Activity).finish() },
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_icon),
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                commonImage(
                    data = imageUrl,
                    modifier = Modifier
                        .padding(start = 3.dp, end = 8.dp)
                        .size(50.dp)
                        .clip(CircleShape)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = username,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                    Text(
                        text = timestampList[currentIndex],
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 5.dp, top = 5.dp)
                    )
                }
            }

            // Navigation Click Areas
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (currentIndex > 0) {
                                progress[currentIndex] = 0f
                                progress[currentIndex - 1] = 0f
                                currentIndex--
                            }
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (currentIndex < totalImages - 1) {
                                progress[currentIndex] = 1f
                                currentIndex++
                            } else {
                                finishActivity(context)
                            }
                        }
                )
            }
        }
    }
}

fun finishActivity(context: Context) {
    (context as Activity).finish()
}