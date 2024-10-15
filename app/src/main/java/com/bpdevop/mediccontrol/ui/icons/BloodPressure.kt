package com.bpdevop.mediccontrol.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BloodPressure: ImageVector
    get() {
        if (bloodPressure != null) {
            return bloodPressure!!
        }
        bloodPressure = ImageVector.Builder(
            name = "Blood_pressure",
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
                moveTo(80f, 360f)
                verticalLineToRelative(-120f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(160f, 160f)
                horizontalLineToRelative(640f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(880f, 240f)
                verticalLineToRelative(220f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(-220f)
                horizontalLineTo(160f)
                verticalLineToRelative(120f)
                close()
                moveToRelative(200f, 320f)
                quadToRelative(-11f, 0f, -21f, -5.5f)
                reflectiveQuadTo(244f, 658f)
                lineToRelative(-69f, -138f)
                horizontalLineTo(80f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(120f)
                quadToRelative(11f, 0f, 21f, 5.5f)
                reflectiveQuadToRelative(15f, 16.5f)
                lineToRelative(44f, 88f)
                lineToRelative(124f, -248f)
                quadToRelative(5f, -10f, 15f, -15f)
                reflectiveQuadToRelative(21f, -5f)
                reflectiveQuadToRelative(21f, 5f)
                reflectiveQuadToRelative(15f, 15f)
                lineToRelative(67f, 134f)
                quadToRelative(-18f, 11f, -34.5f, 23f)
                reflectiveQuadTo(478f, 486f)
                lineToRelative(-38f, -76f)
                lineToRelative(-124f, 248f)
                quadToRelative(-5f, 11f, -15f, 16.5f)
                reflectiveQuadToRelative(-21f, 5.5f)
                moveToRelative(147f, 120f)
                horizontalLineTo(160f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(80f, 720f)
                verticalLineToRelative(-120f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(120f)
                horizontalLineToRelative(243f)
                quadToRelative(3f, 21f, 9f, 41f)
                reflectiveQuadToRelative(15f, 39f)
                moveToRelative(253f, 80f)
                quadToRelative(-83f, 0f, -141.5f, -58.5f)
                reflectiveQuadTo(480f, 680f)
                reflectiveQuadToRelative(58.5f, -141.5f)
                reflectiveQuadTo(680f, 480f)
                reflectiveQuadToRelative(141.5f, 58.5f)
                reflectiveQuadTo(880f, 680f)
                reflectiveQuadToRelative(-58.5f, 141.5f)
                reflectiveQuadTo(680f, 880f)
                moveToRelative(8f, -180f)
                lineToRelative(91f, -91f)
                lineToRelative(-28f, -28f)
                lineToRelative(-91f, 91f)
                close()
            }
        }.build()
        return bloodPressure!!
    }

private var bloodPressure: ImageVector? = null
