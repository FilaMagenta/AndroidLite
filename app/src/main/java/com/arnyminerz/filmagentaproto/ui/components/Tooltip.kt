package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.delay

/**
 * Tooltip implementation for AndroidX Jetpack Compose.
 * Based on material [DropdownMenu] implementation
 *
 * A [Tooltip] behaves similarly to a [Popup], and will use the position of the parent layout
 * to position itself on screen. Commonly a [Tooltip] will be placed in a [Box] with a sibling
 * that will be used as the 'anchor'. Note that a [Tooltip] by itself will not take up any
 * space in a layout, as the tooltip is displayed in a separate window, on top of other content.
 *
 * The [content] of a [Tooltip] will typically be [Text], as well as custom content.
 *
 * [Tooltip] changes its positioning depending on the available space, always trying to be
 * fully visible. It will try to expand horizontally, depending on layout direction, to the end of
 * its parent, then to the start of its parent, and then screen end-aligned. Vertically, it will
 * try to expand to the bottom of its parent, then from the top of its parent, and then screen
 * top-aligned. An [offset] can be provided to adjust the positioning of the menu for cases when
 * the layout bounds of its parent do not coincide with its visual bounds. Note the offset will
 * be applied in the direction in which the menu will decide to expand.
 *
 * @param expanded Whether the tooltip is currently visible to the user
 * @param offset [DpOffset] to be added to the position of the tooltip
 *
 * @see androidx.compose.material.DropdownMenu
 * @see androidx.compose.material.DropdownMenuPositionProvider
 * @see androidx.compose.ui.window.Popup
 *
 * @author Artyom Krivolapov
 */
@Composable
fun Tooltip(
    expanded: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    timeoutMillis: Long = TooltipTimeout,
    backgroundColor: Color = Color.Black,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded.value

    if (expandedStates.currentState || expandedStates.targetState) {
        if (expandedStates.isIdle) {
            LaunchedEffect(timeoutMillis, expanded) {
                delay(timeoutMillis)
                expanded.value = false
            }
        }

        Popup(
            onDismissRequest = { expanded.value = false },
            popupPositionProvider = DropdownMenuPositionProvider(offset, LocalDensity.current),
            properties = properties,
        ) {
            Box(
                // Add space for elevation shadow
                modifier = Modifier.padding(TooltipElevationDp),
            ) {
                TooltipContent(expandedStates, backgroundColor, modifier, content)
            }
        }
    }
}

/**
 * Calculates the position of a Material [DropdownMenu].
 */
// TODO(popam): Investigate if this can/should consider the app window size rather than screen size
@Immutable
private data class DropdownMenuPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val onPositionCalculated: (IntRect, IntRect) -> Unit = { _, _ -> }
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // The min margin above and below the menu, relative to the screen.
        val verticalMargin = with(density) { 48.dp.roundToPx() }
        // The content offset specified using the dropdown offset parameter.
        val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

        // Compute horizontal position.
        val toRight = anchorBounds.left + contentOffsetX
        val toLeft = anchorBounds.right - contentOffsetX - popupContentSize.width
        val toDisplayRight = windowSize.width - popupContentSize.width
        val toDisplayLeft = 0
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            sequenceOf(
                toRight,
                toLeft,
                // If the anchor gets outside of the window on the left, we want to position
                // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                if (anchorBounds.left >= 0) toDisplayRight else toDisplayLeft
            )
        } else {
            sequenceOf(
                toLeft,
                toRight,
                // If the anchor gets outside of the window on the right, we want to position
                // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
                if (anchorBounds.right <= windowSize.width) toDisplayLeft else toDisplayRight
            )
        }.firstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: toLeft

        // Compute vertical position.
        val toBottom = maxOf(anchorBounds.bottom + contentOffsetY, verticalMargin)
        val toTop = anchorBounds.top - contentOffsetY - popupContentSize.height
        val toCenter = anchorBounds.top - popupContentSize.height / 2
        val toDisplayBottom = windowSize.height - popupContentSize.height - verticalMargin
        val y = sequenceOf(toBottom, toTop, toCenter, toDisplayBottom).firstOrNull {
            it >= verticalMargin &&
                    it + popupContentSize.height <= windowSize.height - verticalMargin
        } ?: toTop

        onPositionCalculated(
            anchorBounds,
            IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height)
        )
        return IntOffset(x, y)
    }
}


/** @see androidx.compose.material.DropdownMenuContent */
@Composable
private fun TooltipContent(
    expandedStates: MutableTransitionState<Boolean>,
    backgroundColor: Color,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Tooltip open/close animation.
    val transition = updateTransition(expandedStates, "Tooltip")

    val alpha by transition.animateFloat(
        label = "alpha",
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = InTransitionDuration)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        }
    ) { if (it) 1f else 0f }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.75f),
            contentColor = MaterialTheme.colorScheme.contentColorFor(backgroundColor)
                .takeOrElse { backgroundColor.onColor() },
        ),
        modifier = Modifier.alpha(alpha),
        elevation = TooltipElevation,
    ) {
        val p = TooltipPadding
        Column(
            modifier = modifier
                .padding(start = p, top = p * 0.5f, end = p, bottom = p * 0.7f)
                .width(IntrinsicSize.Max),
            content = content,
        )
    }
}

private val TooltipElevationDp = 16.dp

private val TooltipElevation: CardElevation
    @Composable
    get() = CardDefaults.cardElevation(defaultElevation = TooltipElevationDp)
private val TooltipPadding = 16.dp

// Tooltip open/close animation duration.
private const val InTransitionDuration = 64
private const val OutTransitionDuration = 240

// Default timeout before tooltip close
private const val TooltipTimeout = 2_000L - OutTransitionDuration


// Color utils

/**
 * Calculates an 'on' color for this color.
 *
 * @return [Color.Black] or [Color.White], depending on [isLightColor].
 */
fun Color.onColor(): Color {
    return if (isLightColor()) Color.Black else Color.White
}

/**
 * Calculates if this color is considered light.
 *
 * @return true or false, depending on the higher contrast between [Color.Black] and [Color.White].
 *
 */
fun Color.isLightColor(): Boolean {
    val contrastForBlack = calculateContrastFor(foreground = Color.Black)
    val contrastForWhite = calculateContrastFor(foreground = Color.White)
    return contrastForBlack > contrastForWhite
}

fun Color.calculateContrastFor(foreground: Color): Double {
    return ColorUtils.calculateContrast(foreground.toArgb(), toArgb())
}
