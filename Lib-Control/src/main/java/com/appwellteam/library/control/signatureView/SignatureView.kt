package com.appwellteam.library.control.signatureView

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.appwellteam.library.control.AWTView
import java.io.ByteArrayOutputStream

@Suppress("unused")
class SignatureView : View, AWTView {
    
    private var mSignatureWidth = 8f
    private var mSignatureColor = Color.BLACK

    private var isCapturing = true
    private var mSignature: Bitmap? = null

    private val mPaint = Paint()
    private val mPath = Path()

    private val mInvalidRect = Rect()

    private var mX: Float = 0.toFloat()
    private var mY: Float = 0.toFloat()

    private var mCurveEndX: Float = 0.toFloat()
    private var mCurveEndY: Float = 0.toFloat()

    private val mInvalidateExtraBorder = 10

    private var signatureBitmap: Bitmap?
        get() {
            when {
                mSignature != null -> {
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val tC = Canvas(bmp)
                    tC.drawBitmap(mSignature!!, null, Rect(0, 0, width,
                            height), null)
                    tC.drawPath(mPath, mPaint)
                    return bmp
                }
                mPath.isEmpty -> return null
                else -> {
                    val bmp = Bitmap.createBitmap(width, height,
                            Bitmap.Config.ARGB_8888)
                    val c = Canvas(bmp)
                    c.drawColor(Color.TRANSPARENT)
                    c.drawPath(mPath, mPaint)
                    return bmp
                }
            }
        }
        set(signature) {
            mSignature = signature
            invalidate()
        }

    var signatureWidth: Float
        get() = mPaint.strokeWidth
        set(width) {
            mSignatureWidth = width
            mPaint.strokeWidth = mSignatureWidth
            invalidate()
        }

    /**
     * @return the byte array representing the signature as a PNG file format
     */
    //    public byte[] getSignaturePNG(int quality) {
    //    	return getSignatureBytes(CompressFormat.PNG, quality);
    val signaturePNG: ByteArray?
        get() = getSignatureBytes(CompressFormat.PNG, 100)

    constructor(pContext: Context) : super(pContext) {
        initialize(pContext)
    }

    constructor(pContext: Context, attrs: AttributeSet) : super(pContext, attrs) {
        initialize(pContext)
    }

    constructor(pContext: Context, attrs: AttributeSet, defStyleAttr: Int) : super(pContext, attrs, defStyleAttr) {
        initialize(pContext)
    }

    override fun initialize(context: Context) {
        setWillNotDraw(false)

        mPaint.isAntiAlias = GESTURE_RENDERING_ANTIALIAS
        mPaint.color = mSignatureColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = mSignatureWidth
        mPaint.isDither = DITHER_FLAG
        mPath.reset()
    }

    override fun onDraw(canvas: Canvas) {
        val bitmap = signatureBitmap
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0f,0f, null)
        }
        canvas.drawPath(mPath, mPaint)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (isCapturing) {
            processEvent(event)
            Log.d(VIEW_LOG_TAG, "dispatchTouchEvent")
            true
        } else {
            false
        }
    }

    private fun processEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDown(event)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val rect = touchMove(event)
                @Suppress("DEPRECATION")
                invalidate(rect)
                return true
            }

            MotionEvent.ACTION_UP -> {
                touchUp(event, false)
                invalidate()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                touchUp(event, true)
                invalidate()
                return true
            }
        }
        return false
    }

    @Suppress("UNUSED_PARAMETER")
    private fun touchUp(event: MotionEvent, b: Boolean) {

    }

    private fun touchMove(event: MotionEvent): Rect {
        val x = event.x
        val y = event.y

        val previousX = mX
        val previousY = mY

        val areaToRefresh = mInvalidRect

        // start with the curve end
        val border = mInvalidateExtraBorder
        areaToRefresh.set(mCurveEndX.toInt() - border, mCurveEndY.toInt() - border,
                mCurveEndX.toInt() + border, mCurveEndY.toInt() + border)

        mCurveEndX = (x + previousX) / 2
        val cX = mCurveEndX
        mCurveEndY = (y + previousY) / 2
        val cY = mCurveEndY

        mPath.quadTo(previousX, previousY, cX, cY)

        // union with the control point of the new curve
        areaToRefresh.union(previousX.toInt() - border, previousY.toInt() - border,
                previousX.toInt() + border, previousY.toInt() + border)

        // union with the end point of the new curve
        areaToRefresh.union(cX.toInt() - border, cY.toInt() - border, cX.toInt() + border, cY.toInt() + border)

        mX = x
        mY = y

        return areaToRefresh
    }

    private fun touchDown(event: MotionEvent) {
        val x = event.x
        val y = event.y

        mX = x
        mY = y
        mPath.moveTo(x, y)

        val border = mInvalidateExtraBorder
        mInvalidRect.set(x.toInt() - border, y.toInt() - border, x.toInt() + border,
                y.toInt() + border)

        mCurveEndX = x
        mCurveEndY = y
    }

    /**
     * Erases the signature.
     */
    fun clear() {
        mSignature = null
        mPath.rewind()
        // Repaints the entire view.
        invalidate()
    }

    fun setSignatureColor(color: Int) {
        mSignatureColor = color
    }

    /**
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for small
     * size, 100 meaning compress for max quality.
     * @return the byte array representing the signature as a JPEG file format
     */
    fun getSignatureJPEG(quality: Int): ByteArray? {
        return getSignatureBytes(CompressFormat.JPEG, quality)
    }

    private fun getSignatureBytes(format: CompressFormat, quality: Int): ByteArray? {
        //        Log.d(LOG_TAG, "getSignatureBytes() path is empty: " + mPath.isEmpty());
        val bmp = signatureBitmap ?: return null
        return run {
            val stream = ByteArrayOutputStream()
            bmp.compress(format, quality, stream)
            stream.toByteArray()
        }
    }

    companion object {

        private const val GESTURE_RENDERING_ANTIALIAS = true
        private const val DITHER_FLAG = true
    }
}
