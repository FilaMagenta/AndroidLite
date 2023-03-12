package com.arnyminerz.filmagentaproto.ui.components

import android.text.Html
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

private val BoldStyle = SpanStyle(fontWeight = FontWeight.Bold)
private val ItalicStyle = SpanStyle(fontStyle = FontStyle.Italic)

@Composable
fun HtmlText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit) {
    val annotatedString = buildAnnotatedString {
        var counter = 0
        while (counter < text.length) {
            when (val char = text[counter]) {
                '<' -> {
                    val closing = text.indexOf('>', counter)
                    when (val tag = text.substring(counter, closing + 1)) {
                        "<b>", "<h3>", "<h4>" -> pushStyle(BoldStyle)
                        "</b>", "</h3>", "</h4>" -> pop()
                        "<em>", "<i>" -> pushStyle(ItalicStyle)
                        "</em>", "</i>" -> pop()
                        "<p>", "</p>" -> {}
                        else -> append(tag)
                    }
                    counter = closing + 1
                }
                '&' -> {
                    val end = text.indexOf(';', counter)
                    val specialCharacter = text.substring(counter, end)
                    val converted = Html.fromHtml(specialCharacter, Html.FROM_HTML_MODE_COMPACT)
                    append(converted)
                    counter = end + 1
                }
                else -> {
                    append(char)
                    counter++
                }
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        fontSize = fontSize,
    )
}
