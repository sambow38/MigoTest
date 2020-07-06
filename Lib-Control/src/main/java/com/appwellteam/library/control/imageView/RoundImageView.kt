package com.appwellteam.library.control.imageView

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.appwellteam.library.control.AWTView
import com.appwellteam.library.control.R

/**
 * Created by Sambow on 16/9/6.
 */
class RoundImageView : AppCompatImageView, AWTView {

    private var type = ImageType.Normal
    private var border = false
    private var borderWidth: Int = 0
    @ColorInt
    private var borderColor = Color.TRANSPARENT
    private val paint: Paint = Paint()
    private var radius = -1

    private enum class ImageType(val key: Int) {
        Normal(0),
        Round(1),
        Circle(2);


        companion object {

            fun getType(key: Int): ImageType {
                val alignments = values()
                for (alignment in alignments) {
                    if (alignment.key == key) {
                        return alignment
                    }
                }
                return Normal
            }
        }
    }

    constructor(context: Context) : super(context) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs)
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        borderWidth = context.resources.getDimensionPixelSize(R.dimen.dp_1)
        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.RoundImageView, 0, 0)
            type = ImageType.getType(typedArray.getInt(R.styleable.RoundImageView_imageType, ImageType.Normal.key))
            border = typedArray.getBoolean(R.styleable.RoundImageView_roundBorder, false)
            borderWidth = typedArray.getDimensionPixelSize(R.styleable.RoundImageView_roundBorderWidth, borderWidth)
            borderColor = typedArray.getColor(R.styleable.RoundImageView_roundBorderColor, Color.TRANSPARENT)
            radius = typedArray.getDimensionPixelSize(R.styleable.RoundImageView_roundRadius, radius)
        }
        initialize(context)
    }

    private fun getDrawableBitmap(): Bitmap
    {
        val drawable = drawable
        val bitmap: Bitmap
        if (drawable is BitmapDrawable) {
            bitmap = drawable.bitmap
        } else {
            val bitmapCanvas = createBitmapCanvas(width, height)
            bitmap = bitmapCanvas.first
            val cv = bitmapCanvas.second
            drawable.setBounds(0, 0, cv.width, cv.height)
            drawable.draw(cv)
        }
        return bitmap
    }

    private fun createBitmapCanvas(width: Int, height: Int): Pair<Bitmap, Canvas>
    {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
         return Pair(bitmap, canvas)
    }

    private fun createBitmapShader(bitmap: Bitmap): BitmapShader
    {
        return BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    private fun createMatrix(scale: Float): Matrix
    {
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        return matrix
    }

    private fun createSizeRectF(width: Float, height: Float): RectF
    {
        return RectF(0f, 0f, width, height)
    }

    override fun initialize(context: Context) {
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.strokeWidth = borderWidth.toFloat()
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable
        if (type == ImageType.Normal || drawable == null || width == 0 || height == 0) {
            super.onDraw(canvas)
            return
        }

        val bitmap: Bitmap = getDrawableBitmap()
//        if (drawable is BitmapDrawable) {
//            bitmap = drawable.bitmap
//        } else {
//            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//            val cv = Canvas(bitmap)
//            drawable.setBounds(0, 0, width, height)
//            drawable.draw(cv)
//        }
//
//        if (bitmap == null) {
//            super.onDraw(canvas)
//            return
//        }

        val width = width
        val height = height
        val centerX = width / 2
        val centerY = height / 2
        val radius = if (this.radius < 0) {
            if (width > height) height / 2 else width / 2
        } else {
            this.radius
        }
        //		int radius = width > height ? height / 2 : width / 2;
        val diameter = radius * 2

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        var scale = 1f

        val bitmapShader = createBitmapShader(bitmap)
        if (type == ImageType.Circle) {
            val size = Math.min(bitmapWidth, bitmapHeight)

            scale = diameter * 1f / size
        } else if (type == ImageType.Round) {
            scale = Math.max(width * 1.0f / bitmapWidth, height * 1.0f / bitmapHeight)

        }

        val matrix = createMatrix(scale)

        val borderWidthOffset = borderWidth / 2

        if (type == ImageType.Circle) {
            val realRadius = radius - borderWidthOffset
            bitmapShader.setLocalMatrix(matrix)
            val bitmapCanvas = createBitmapCanvas(diameter, diameter)
            val dest = bitmapCanvas.first
            val cv = bitmapCanvas.second
            paint.style = Paint.Style.FILL
            paint.shader = bitmapShader
            cv.drawCircle(radius.toFloat(), realRadius.toFloat(), realRadius.toFloat(), paint)

            canvas.drawBitmap(dest, (centerX - radius).toFloat(), (centerY - radius).toFloat(), paint)
            if (border) {
                paint.style = Paint.Style.STROKE
                paint.shader = null
                canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), realRadius.toFloat(), paint)
            }
        } else if (type == ImageType.Round) {
            matrix.postTranslate((height - bitmapHeight * scale) / 2, (width - bitmapWidth * scale) / 2)
            bitmapShader.setLocalMatrix(matrix)

            val rectF = createSizeRectF((width - borderWidthOffset * 2).toFloat(), (height - borderWidthOffset * 2).toFloat())

            val bitmapCanvas = createBitmapCanvas(width, height)
            val dest = bitmapCanvas.first
            val cv = bitmapCanvas.second
            paint.style = Paint.Style.FILL
            paint.shader = bitmapShader
            cv.drawRoundRect(rectF, radius.toFloat(), radius.toFloat(), paint)

            canvas.drawBitmap(dest, centerX - rectF.centerX(), centerY - rectF.centerY(), paint)
            if (border) {
                paint.style = Paint.Style.STROKE
                paint.shader = null
                canvas.drawRoundRect(rectF, radius.toFloat(), radius.toFloat(), paint)
            }
        }
    }
}