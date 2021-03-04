/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.androiddevchallenge.meditationtimer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.androiddevchallenge.meditationtimer.ui.theme.MyTheme
import com.androiddevchallenge.meditationtimer.ui.theme.buttonBgColor
import com.androiddevchallenge.meditationtimer.ui.theme.progressBarColor
import com.androiddevchallenge.meditationtimer.ui.theme.screenBgColorEnd
import com.androiddevchallenge.meditationtimer.ui.theme.screenBgColorStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

var showStart: MutableState<Boolean> = mutableStateOf(true)
var isTimerRunning: MutableState<Boolean> = mutableStateOf(false)
var isPause: MutableState<Boolean> = mutableStateOf(true)
var progress: MutableState<Float> = mutableStateOf(362f)
private const val DividerLengthInDegrees = 1.8f

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                CountDownTimerApp()
            }
        }
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        CountDownTimerApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        CountDownTimerApp()
    }
}

@Composable
fun CountDownTimerApp() {
    Surface(color = MaterialTheme.colors.background) {
        val coroutineScope = rememberCoroutineScope()
        Scaffold(
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colors.onPrimary,
                                    screenBgColorStart,
                                    screenBgColorEnd
                                )
                            )
                        )
                ) {
                    Text(
                        text = "3 Minute Meditation",
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier
                            .padding(20.dp)
                    )
                    ConstraintLayout(
                        Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .padding(top = 20.dp)
                    ) {
                        val (box, circle, innerBox, progressText) = createRefs()
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .clip(CircleShape)
                                .constrainAs(box) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        )
                        CircularProgressBar(
                            Modifier
                                .height(300.dp)
                                .fillMaxWidth()
                                .constrainAs(circle) {
                                    top.linkTo(box.top, margin = 5.dp)
                                    start.linkTo(box.start)
                                    end.linkTo(box.end)
                                    bottom.linkTo(box.bottom)
                                },
                            progress.value
                        )
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .constrainAs(innerBox) {
                                    top.linkTo(circle.top, margin = 4.dp)
                                    start.linkTo(circle.start, margin = 4.dp)
                                    end.linkTo(circle.end, margin = 4.dp)
                                    bottom.linkTo(circle.bottom, margin = 4.dp)
                                }
                                .background(color = buttonBgColor)
                        )
                        var seconds = (((progress.value - 2) % 120) / 2).toInt()
                        Text(
                            text = "0${((progress.value - 2) / 120).toInt()}:${if (seconds < 10) "0$seconds" else seconds}",
                            style = MaterialTheme.typography.h2.copy(Color.Black)
                                .copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier
                                .constrainAs(progressText) {
                                    top.linkTo(circle.top, margin = 4.dp)
                                    start.linkTo(circle.start, margin = 4.dp)
                                    end.linkTo(circle.end, margin = 4.dp)
                                    bottom.linkTo(circle.bottom, margin = 4.dp)
                                }
                        )
                    }
                    if (showStart.value) {
                        ShowStartButton(coroutineScope)
                    } else {
                        ShowBottomButtons(coroutineScope)
                    }
                    val image: Painter = painterResource(id = R.drawable.med_bottom)
                    Image(
                        painter = image, contentDescription = ""
                    )
                }
            }
        )
    }
}

@Composable
fun ShowStartButton(coroutineScope: CoroutineScope) {
    Row(
        modifier = Modifier
            .padding(30.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                showStart.value = false
                isTimerRunning.value = true
                coroutineScope.launch {
                    updateProgress()
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonBgColor),
            modifier = Modifier
                .clip(CircleShape)
                .size(70.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Start",
                tint = Color.Black,
                modifier = Modifier
                    .size(50.dp)
            )
        }
    }
}

@Composable
fun ShowBottomButtons(coroutineScope: CoroutineScope) {
    Row(
        modifier = Modifier
            .padding(30.dp)
            .fillMaxWidth()
    ) {
        Button(
            onClick = {
                showStart.value = true
                isTimerRunning.value = false
                isPause.value = true
                progress.value = 362f
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonBgColor),
            modifier = Modifier
                .clip(CircleShape)
                .size(70.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Stop,
                contentDescription = "Stop",
                tint = Color.Black,
                modifier = Modifier
                    .size(50.dp)
            )
        }
        Spacer(
            modifier = Modifier
                .weight(1f)
        )
        Button(
            onClick = {
                isTimerRunning.value = !isPause.value
                coroutineScope.launch {
                    updateProgress()
                }
                isPause.value = !isPause.value
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonBgColor),
            modifier = Modifier
                .clip(CircleShape)
                .size(70.dp)
        ) {
            Icon(
                imageVector = if (isPause.value) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPause.value) "Pause" else "Resume",
                tint = Color.Black,
                modifier = Modifier
                    .size(50.dp)
            )
        }
    }
}

@Composable
fun CircularProgressBar(
    modifier: Modifier = Modifier,
    sweep: Float
) {
    val stroke = with(LocalDensity.current) { Stroke(8.dp.toPx()) }
    Canvas(modifier) {
        val innerRadius = (size.minDimension - stroke.width) / 2
        drawArc(
            color = progressBarColor,
            startAngle = -90f + DividerLengthInDegrees / 2,
            sweepAngle = sweep - DividerLengthInDegrees,
            topLeft = Offset(
                (size / 2.0f).width - innerRadius,
                (size / 2.0f).height - innerRadius
            ),
            size = Size(innerRadius * 2, innerRadius * 2),
            useCenter = false,
            style = stroke
        )
    }
}

suspend fun updateProgress() {
    while (isTimerRunning.value) {
        progress.value -= 1
        if (progress.value <= 2) {
            progress.value = 362f
            isTimerRunning.value = false
            showStart.value = true
        }
        delay(500)
    }
}
