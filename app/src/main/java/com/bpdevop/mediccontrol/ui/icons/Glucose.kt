package com.bpdevop.mediccontrol.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Glucose: ImageVector
    get() {
        if (glucose != null) {
            return glucose!!
        }
        glucose = ImageVector.Builder(
            name = "Glucose",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(539f, 880f)
                quadToRelative(-28f, 0f, -52.5f, -12f)
                reflectiveQuadTo(445f, 834f)
                lineTo(227f, 557f)
                lineToRelative(19f, -20f)
                quadToRelative(20f, -21f, 48f, -25f)
                reflectiveQuadToRelative(52f, 11f)
                lineToRelative(74f, 45f)
                verticalLineToRelative(-408f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(460f, 120f)
                reflectiveQuadToRelative(29f, 11.5f)
                reflectiveQuadToRelative(12f, 28.5f)
                verticalLineToRelative(552f)
                lineToRelative(-97f, -60f)
                lineToRelative(104f, 133f)
                quadToRelative(6f, 8f, 14f, 11.5f)
                reflectiveQuadToRelative(17f, 3.5f)
                horizontalLineToRelative(221f)
                quadToRelative(33f, 0f, 56.5f, -23.5f)
                reflectiveQuadTo(840f, 720f)
                verticalLineToRelative(-280f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(880f, 400f)
                reflectiveQuadToRelative(28.5f, 11.5f)
                reflectiveQuadTo(920f, 440f)
                verticalLineToRelative(280f)
                quadToRelative(0f, 66f, -47f, 113f)
                reflectiveQuadTo(760f, 880f)
                close()
                moveToRelative(21f, -360f)
                verticalLineToRelative(-200f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(600f, 280f)
                reflectiveQuadToRelative(28.5f, 11.5f)
                reflectiveQuadTo(640f, 320f)
                verticalLineToRelative(200f)
                close()
                moveToRelative(140f, 0f)
                verticalLineToRelative(-160f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(740f, 320f)
                reflectiveQuadToRelative(28.5f, 11.5f)
                reflectiveQuadTo(780f, 360f)
                verticalLineToRelative(160f)
                close()
                moveTo(180f, 400f)
                quadToRelative(-58f, 0f, -99f, -40f)
                reflectiveQuadToRelative(-41f, -98f)
                quadToRelative(0f, -42f, 25f, -75.5f)
                reflectiveQuadToRelative(52f, -65.5f)
                lineToRelative(63f, -72f)
                lineToRelative(63f, 73f)
                quadToRelative(27f, 32f, 52f, 65f)
                reflectiveQuadToRelative(25f, 75f)
                quadToRelative(0f, 58f, -41f, 98f)
                reflectiveQuadToRelative(-99f, 40f)
                moveToRelative(0f, -80f)
                quadToRelative(25f, 0f, 42.5f, -17f)
                reflectiveQuadToRelative(17.5f, -41f)
                quadToRelative(0f, -27f, -18.5f, -46.5f)
                reflectiveQuadTo(185f, 176f)
                lineToRelative(-5f, -5f)
                lineToRelative(-5f, 5f)
                quadToRelative(-18f, 20f, -36.5f, 39.5f)
                reflectiveQuadTo(120f, 262f)
                quadToRelative(0f, 24f, 17.5f, 41f)
                reflectiveQuadToRelative(42.5f, 17f)
                moveToRelative(0f, -75f)
            }
        }.build()
        return glucose!!
    }

private var glucose: ImageVector? = null
