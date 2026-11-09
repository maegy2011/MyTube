package com.mohamedadel.mytube.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// Brand logo drawn in code (no asset files): rounded red badge with a white play triangle.
// Used in the top bar and anywhere the app identity appears.
fun myTubeLogo(): ImageVector = ImageVector.Builder(
    name = "MyTubeLogo",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    addPath(
        pathData = PathParser().parsePathString(
            "M6,2 h12 a4,4 0 0 1 4,4 v12 a4,4 0 0 1 -4,4 H6 a4,4 0 0 1 -4,-4 V6 a4,4 0 0 1 4,-4 z"
        ).toNodes(),
        fill = SolidColor(com.mohamedadel.mytube.ui.theme.BrandMark)
    )
    addPath(
        pathData = PathParser().parsePathString("M10,8.5 v7 l6,-3.5 z").toNodes(),
        fill = SolidColor(Color.White)
    )
}.build()
