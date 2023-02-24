package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.utils.random

@Composable
@ExperimentalTextApi
fun ProfileImage(name: String, modifier: Modifier = Modifier) {
    val color = remember { Color.random() }
    val textColor = if((color.red + color.blue + color.green) / 3 >= 128)
        Color.Black
    else
        Color.White

    val measurer = rememberTextMeasurer()

    Canvas(Modifier.size(24.dp).then(modifier)) {
        // Fill with random color
        drawRect(color)
        // Draw the first letter
        drawText(
            text = name[0].toString(),
            textMeasurer = measurer,
            style = TextStyle(
                fontSize = 18.sp,
                color = textColor,
            ),
            topLeft = Offset(18f,0f)
        )
    }
}

@Preview
@Composable
@OptIn(ExperimentalTextApi::class)
fun ProfileImagePreview() {
    ProfileImage(name = "Test")
}
