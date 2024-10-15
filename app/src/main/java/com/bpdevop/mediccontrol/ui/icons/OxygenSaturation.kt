package com.bpdevop.mediccontrol.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val OxygenSaturation: ImageVector
    get() {
        if (oxygenSaturation != null) {
            return oxygenSaturation!!
        }
        oxygenSaturation = ImageVector.Builder(
            name = "OxygenSaturation",
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
                moveTo(40f, 560f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(160f)
                quadToRelative(0f, 17f, 11.5f, 28.5f)
                reflectiveQuadTo(160f, 760f)
                horizontalLineToRelative(91f)
                quadToRelative(11f, -19f, 29.5f, -29.5f)
                reflectiveQuadTo(320f, 720f)
                horizontalLineToRelative(40f)
                verticalLineToRelative(-40f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(400f, 640f)
                reflectiveQuadToRelative(28.5f, 11.5f)
                reflectiveQuadTo(440f, 680f)
                verticalLineToRelative(40f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(-40f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(560f, 640f)
                reflectiveQuadToRelative(28.5f, 11.5f)
                reflectiveQuadTo(600f, 680f)
                verticalLineToRelative(40f)
                horizontalLineToRelative(40f)
                quadToRelative(21f, 0f, 39.5f, 10.5f)
                reflectiveQuadTo(709f, 760f)
                horizontalLineToRelative(91f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(840f, 720f)
                verticalLineToRelative(-160f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(160f)
                quadToRelative(0f, 50f, -35f, 85f)
                reflectiveQuadToRelative(-85f, 35f)
                horizontalLineToRelative(-91f)
                quadToRelative(-11f, 19f, -29.5f, 29.5f)
                reflectiveQuadTo(640f, 880f)
                horizontalLineTo(320f)
                quadToRelative(-21f, 0f, -39.5f, -10.5f)
                reflectiveQuadTo(251f, 840f)
                horizontalLineToRelative(-91f)
                quadToRelative(-50f, 0f, -85f, -35f)
                reflectiveQuadToRelative(-35f, -85f)
                close()
                moveToRelative(280f, -520f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(160f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(320f, 280f)
                horizontalLineToRelative(-40f)
                quadToRelative(-33f, 0f, -56.5f, 23.5f)
                reflectiveQuadTo(200f, 360f)
                reflectiveQuadToRelative(23.5f, 56.5f)
                reflectiveQuadTo(280f, 440f)
                verticalLineToRelative(80f)
                quadToRelative(-66f, 0f, -113f, -47f)
                reflectiveQuadToRelative(-47f, -113f)
                reflectiveQuadToRelative(47f, -113f)
                reflectiveQuadToRelative(113f, -47f)
                horizontalLineToRelative(40f)
                close()
                moveToRelative(240f, 0f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(160f)
                horizontalLineToRelative(40f)
                quadToRelative(66f, 0f, 113f, 47f)
                reflectiveQuadToRelative(47f, 113f)
                reflectiveQuadToRelative(-47f, 113f)
                reflectiveQuadToRelative(-113f, 47f)
                verticalLineToRelative(-80f)
                quadToRelative(33f, 0f, 56.5f, -23.5f)
                reflectiveQuadTo(760f, 360f)
                reflectiveQuadToRelative(-23.5f, -56.5f)
                reflectiveQuadTo(680f, 280f)
                horizontalLineToRelative(-40f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(560f, 200f)
                close()
                moveToRelative(-91f, 520f)
                quadToRelative(-44f, 0f, -79.5f, -25.5f)
                reflectiveQuadTo(340f, 467f)
                quadToRelative(-5f, -12f, -15f, -19.5f)
                reflectiveQuadToRelative(-23f, -7.5f)
                horizontalLineToRelative(-22f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(22f)
                quadToRelative(39f, 0f, 70.5f, 22.5f)
                reflectiveQuadTo(416f, 442f)
                quadToRelative(5f, 17f, 20f, 27.5f)
                reflectiveQuadToRelative(33f, 10.5f)
                horizontalLineToRelative(22f)
                quadToRelative(18f, 0f, 33f, -10.5f)
                reflectiveQuadToRelative(20f, -27.5f)
                quadToRelative(12f, -37f, 43.5f, -59.5f)
                reflectiveQuadTo(658f, 360f)
                horizontalLineToRelative(22f)
                verticalLineToRelative(80f)
                horizontalLineToRelative(-22f)
                quadToRelative(-13f, 0f, -23.5f, 7.5f)
                reflectiveQuadTo(620f, 467f)
                quadToRelative(-14f, 42f, -49.5f, 67.5f)
                reflectiveQuadTo(491f, 560f)
                close()
            }
        }.build()
        return oxygenSaturation!!
    }

private var oxygenSaturation: ImageVector? = null
