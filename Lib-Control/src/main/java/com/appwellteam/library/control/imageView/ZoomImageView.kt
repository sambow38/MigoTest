package com.appwellteam.library.control.imageView

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.appwellteam.library.control.AWTView
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Sambow on 16/1/11.
 */
@Suppress("unused")
class ZoomImageView : AppCompatImageView, AWTView// implements GestureDetector.OnGestureListener, AWTView
{

    //region 監聽器
    /**
     * 外界點擊事件
     *
     * @see .setOnClickListener
     */
    private var mOnClickListener: OnClickListener? = null

    /**
     * 外界長按事件
     *
     * @see .setOnLongClickListener
     */
    private var mOnLongClickListener: OnLongClickListener? = null


    override fun setOnClickListener(l: OnClickListener?) {
        //默認的click會在任何點擊情況下都會觸發，所以搞成自己的
        mOnClickListener = l
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        //默認的long click會在任何長按情況下都會觸發，所以搞成自己的
        mOnLongClickListener = l
    }
    //endregion

    /**
     * 外層變換矩陣，如果是單位矩陣，那麼圖片是fit center狀態
     *
     * @see .getOuterMatrix
     * @see .outerMatrixTo
     */
    private val mOuterMatrix = Matrix()

    /**
     * 矩形遮罩
     *
     * @see .getMask
     * @see .zoomMaskTo
     */
    private var mMask: RectF? = null

    /**
     * 當前手勢狀態
     *
     * @see .getPinchMode
     * @see .PINCH_MODE_FREE
     *
     * @see .PINCH_MODE_SCROLL
     *
     * @see .PINCH_MODE_SCALE
     */
    /**
     * 獲取當前手勢狀態
     *
     * @see .PINCH_MODE_FREE
     *
     * @see .PINCH_MODE_SCROLL
     *
     * @see .PINCH_MODE_SCALE
     */
    var pinchMode = PINCH_MODE_FREE
        private set

    /**
     * 獲取當前設置的mask
     *
     * @return返回當前的mask對象副本,如果當前沒有設置mask則返回null
     */
    val mask: RectF?
        get() = if (mMask != null) {
            RectF(mMask)
        } else {
            null
        }

    /**
     * 所有OuterMatrixChangedListener监听列表
     *
     * @see .addOuterMatrixChangedListener
     * @see .removeOuterMatrixChangedListener
     */
    private var mOuterMatrixChangedListeners: MutableList<OuterMatrixChangedListener> = ArrayList()

    /**
     * 當mOuterMatrixChangedListeners被鎖定不允許修改時,臨時將修改寫到這個副本中
     *
     * @see .mOuterMatrixChangedListeners
     */
    private var mOuterMatrixChangedListenersCopy: MutableList<OuterMatrixChangedListener> = ArrayList()

    /**
     * mOuterMatrixChangedListeners的修改鎖定
     *
     * 當進入dispatchOuterMatrixChanged方法時,被加1,退出前被減1
     *
     * @see .dispatchOuterMatrixChanged
     * @see .addOuterMatrixChangedListener
     * @see .removeOuterMatrixChangedListener
     */
    private var mDispatchOuterMatrixChangedLock: Int = 0


    // //////////////////////////////用於重載定制///////////// ///////////////////

    /**
     * 獲取圖片最大可放大的比例
     *
     * 如果放大大於這個比例則不被允許.
     * 在雙手縮放過程中如果圖片放大比例大於這個值,手指釋放將回彈到這個比例.
     * 在雙擊放大過程中不允許放大比例大於這個值.
     * 覆蓋此方法可以定制不同情況使用不同的最大可放大比例.
     *
     * @return 縮放比例
     *
     * @see .scaleEnd
     * @see .doubleTap
     */
    @Suppress("ProtectedInFinal")
    protected val maxScale: Float
        get() = MAX_SCALE


    // //////////////////////////////有效性判斷/////////////// /////////////////

    /**
     * 判斷當前情況是否能執行手勢相關計算
     *
     * 包括:是否有圖片,圖片是否有尺寸,控件是否有尺寸.
     *
     * @return 是否能執行手勢相關計算
     */
    private val isReady: Boolean
        get() = (drawable != null && drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0
                && width > 0 && height > 0)


    // //////////////////////////////mask動畫處理/////////////// /////////////////

    /**
     * mask修改的動畫
     *
     * 和圖片的動畫相互獨立.
     *
     * @see .zoomMaskTo
     */
    private var mMaskAnimator: MaskAnimator? = null


    //region 手勢動畫處理
    /**
     * 在單指模式下:
     * 記錄上一次手指的位置,用於計算新的位置和上一次位置的差值.
     *
     * 雙指模式下:
     * 記錄兩個手指的中點,作為和mScaleCenter綁定的點.
     * 這個綁定可以保證mScaleCenter無論如何都會跟隨這個中點.
     *
     * @see .mScaleCenter
     *
     * @see .scale
     * @see .scaleEnd
     */
    private val mLastMovePoint = PointF()

    /**
     * 縮放模式下圖片的縮放中點.
     *
     * 為其指代的點經過innerMatrix變換之後的值.
     * 其指代的點在手勢過程中始終跟隨mLastMovePoint.
     * 通過雙指縮放時,其為縮放中心點.
     *
     * @see .saveScaleContext
     * @see .mLastMovePoint
     *
     * @see .scale
     */
    private val mScaleCenter = PointF()

    /**
     * 縮放模式下的基礎縮放比例
     *
     * 為外層縮放值除以開始縮放時兩指距離.
     * 其值乘上最新的兩指之間距離為最新的圖片縮放比例.
     *
     * @see .saveScaleContext
     * @see .scale
     */
    private var mScaleBase = 0f

    /**
     * 圖片縮放動畫
     *
     * 縮放模式把圖片的位置大小超出限制之後觸發.
     * 雙擊圖片放大或縮小時觸發.
     * 手動調用outerMatrixTo觸發.
     *
     * @see .scaleEnd
     * @see .doubleTap
     * @see .outerMatrixTo
     */
    private var mScaleAnimator: ScaleAnimator? = null

    /**
     * 滑動產生的慣性動畫
     *
     * @see .fling
     */
    private var mFlingAnimator: FlingAnimator? = null

    /**
     * 常用手勢處理
     *
     * 在onTouchEvent末尾被執行.
     */
    private val mGestureDetector = GestureDetector(this@ZoomImageView.context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            //只有在單指模式結束之後才允許執行fling
            if (pinchMode == PINCH_MODE_FREE && mScaleAnimator?.isRunning != true) {
                fling(velocityX, velocityY)
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            //觸發長按
            mOnLongClickListener?.onLongClick(this@ZoomImageView)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            //當手指快速第二次按下觸發,此時必須是單指模式才允許執行doubleTap
            if (pinchMode == PINCH_MODE_SCROLL && mScaleAnimator?.isRunning != true) {
                doubleTap(e.x, e.y)
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            //觸發點擊
            mOnClickListener?.onClick(this@ZoomImageView)
            return true
        }
    })
    //endregion

    /**
     * 獲取外部變換矩陣.
     *
     * 外部變換矩陣記錄了圖片手勢操作的最終結果,是相對於圖片fit center狀態的變換.
     * 默認值為單位矩陣,此時圖片為fit center狀態.
     *
     * @param matrix 用於填充結果的對象
     * @return如果傳了matrix參數則將matrix填充後返回,否則new一個填充返回
     */
    fun getOuterMatrix(matrix: Matrix?): Matrix {
        return if (matrix == null) {
            Matrix(mOuterMatrix)
        } else {
            matrix.set(mOuterMatrix)
            matrix
        }
    }

    /**
     * 獲取內部變換矩陣.
     *
     * 內部變換矩陣是原圖到fit center狀態的變換,當原圖尺寸變化或者控件大小變化都會發生改變
     * 當尚未佈局或者原圖不存在時,其值無意義.所以在調用前需要確保前置條件有效,否則將影響計算結果.
     *
     * @param matrix 用於填充結果的對象
     * @return如果傳了matrix參數則將matrix填充後返回,否則new一個填充返回
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getInnerMatrix(matrix: Matrix?): Matrix {
        val result =
                if (matrix != null) {
                    matrix.reset()
                    matrix
                } else {
                    Matrix()
                }

        if (isReady) {
            //原圖大小
            val tempSrc = MathUtils.rectFTake(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
            //控件大小
            val tempDst = MathUtils.rectFTake(0f, 0f, width.toFloat(), height.toFloat())
            //計算fit center矩陣
            result.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER)
            //釋放臨時對象
            MathUtils.rectFGiven(tempDst)
            MathUtils.rectFGiven(tempSrc)
        }
        return result
    }

    /**
     * 獲取圖片總變換矩陣.
     *
     * 總變換矩陣為內部變換矩陣x外部變換矩陣,決定了原圖到所見最終狀態的變換
     * 當尚未佈局或者原圖不存在時,其值無意義.所以在調用前需要確保前置條件有效,否則將影響計算結果.
     *
     * @param matrix 用於填充結果的對象
     * @return如果傳了matrix參數則將matrix填充後返回,否則new一個填充返回
     *
     * @see .getOuterMatrix
     * @see .getInnerMatrix
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getCurrentImageMatrix(matrix: Matrix): Matrix {
        var result = matrix
        //獲取內部變換矩陣
        result = getInnerMatrix(result)
        //乘上外部變換矩陣
        result.postConcat(mOuterMatrix)
        return result
    }

    /**
     * 獲取當前變換後的圖片位置和尺寸
     *
     * 當尚未佈局或者原圖不存在時,其值無意義.所以在調用前需要確保前置條件有效,否則將影響計算結果.
     *
     * @param rectF 用於填充結果的對象
     * @return如果傳了rectF參數則將rectF填充後返回,否則new一個填充返回
     *
     * @see .getCurrentImageMatrix
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getImageBound(rectF: RectF?): RectF {
        val result =
                if (rectF == null) {
                    RectF()
                } else {
                    rectF.setEmpty()
                    rectF
                }

        return if (!isReady) {
            result
        } else {
            //申請一個空matrix
            val matrix = MathUtils.matrixTake()
            //獲取當前總變換矩陣
            getCurrentImageMatrix(matrix)
            //對原圖矩形進行變換得到當前顯示矩形
            result.set(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
            matrix.mapRect(result)
            //釋放臨時matrix
            MathUtils.matrixGiven(matrix)
            result
        }
    }

    /**
     * 與ViewPager結合的時候使用
     * @param direction
     * @return
     */
    override fun canScrollHorizontally(direction: Int): Boolean {
        if (pinchMode == PINCH_MODE_SCALE) {
            return true
        }
        val bound = getImageBound(null)
        if (bound.isEmpty) {
            return false
        }
        return if (direction > 0) {
            bound.right > width
        } else {
            bound.left < 0
        }
    }

    /**
     * 與ViewPager結合的時候使用
     * @param direction
     * @return
     */
    override fun canScrollVertically(direction: Int): Boolean {
        if (pinchMode == PINCH_MODE_SCALE) {
            return true
        }
        val bound = getImageBound(null)
        if (bound.isEmpty) {
            return false
        }
        return if (direction > 0) {
            bound.bottom > height
        } else {
            bound.top < 0
        }
    }


    // //////////////////////////////公共狀態設置/////////////// /////////////////

    /**
     * 執行當前outerMatrix到指定outerMatrix漸變的動畫
     *
     * 調用此方法會停止正在進行中的手勢以及手勢動畫.
     * 當duration為0時,outerMatrix值會被立即設置而不會啟動動畫.
     *
     * @param endMatrix 動畫目標矩陣
     * @param duration 動畫持續時間
     *
     * @see .getOuterMatrix
     */
    fun outerMatrixTo(endMatrix: Matrix?, duration: Long) {
        if (endMatrix == null) {
            return
        }
        //將手勢設置為PINCH_MODE_FREE將停止後續手勢的觸發
        pinchMode = PINCH_MODE_FREE
        //停止所有正在進行的動畫
        cancelAllAnimator()
        //如果時間不合法立即執行結果
        if (duration <= 0) {
            mOuterMatrix.set(endMatrix)
            dispatchOuterMatrixChanged()
            invalidate()
        } else {
            //創建矩陣變化動畫
            mScaleAnimator = ScaleAnimator(mOuterMatrix, endMatrix, duration)
            mScaleAnimator?.start()
        }
    }

    /**
     * 執行當前mask到指定mask的變化動畫
     *
     * 調用此方法不會停止手勢以及手勢相關動畫,但會停止正在進行的mask動畫.
     * 當前mask為null時,則不執行動畫立即設置為目標mask.
     * 當duration為0時,立即將當前mask設置為目標mask,不會執行動畫.
     *
     * @param mask 動畫目標mask
     * @param duration 動畫持續時間
     *
     * @see .getMask
     */
    fun zoomMaskTo(mask: RectF?, duration: Long) {
        if (mask == null) {
            return
        }
        //停止mask動畫
        mMaskAnimator?.cancel()
        mMaskAnimator = null

        //如果duration為0或者之前沒有設置過mask,不執行動畫,立即設置
        if (duration <= 0 || mMask == null) {
            if (mMask == null) {
                mMask = RectF()
            }
            mMask?.set(mask)
            invalidate()
        } else {
            //執行mask動畫
            mMaskAnimator = MaskAnimator(mMask!!, mask, duration)
            mMaskAnimator?.start()
        }
    }

    /**
     * 重置所有狀態
     *
     * 重置位置到fit center狀態,清空mask,停止所有手勢,停止所有動畫.
     * 但不清空drawable,以及事件綁定相關數據.
     */
    fun reset() {
        //重置位置到fit
        mOuterMatrix.reset()
        dispatchOuterMatrixChanged()
        //清空mask
        mMask = null
        //停止所有手势
        pinchMode = PINCH_MODE_FREE
        mLastMovePoint.set(0f, 0f)
        mScaleCenter.set(0f, 0f)
        mScaleBase = 0f
        //停止所有动画
        mMaskAnimator?.cancel()
        mMaskAnimator = null

        cancelAllAnimator()
        //重繪
        invalidate()
    }


    ////////////////////////////////對外廣播事件////////////////////////////////

    /**
     * 外部矩阵变化事件通知监听器
     */
    interface OuterMatrixChangedListener {

        /**
         * 外部矩阵变化回调
         *
         * 外部矩阵的任何变化后都收到此回调.
         * 外部矩阵变化后,总变化矩阵,图片的展示位置都将发生变化.
         *
         * @param zoomImageView
         *
         * @see .getOuterMatrix
         * @see .getCurrentImageMatrix
         * @see .getImageBound
         */
        fun onOuterMatrixChanged(zoomImageView: ZoomImageView)
    }

    /**
     * 添加外部矩陣變化監聽
     *
     * @param listener
     */
    fun addOuterMatrixChangedListener(listener: OuterMatrixChangedListener?) {
        if (listener == null) {
            return
        }
        //如果監聽列表沒有被修改鎖定直接將監聽添加到監聽列表
        if (mDispatchOuterMatrixChangedLock == 0) {
            mOuterMatrixChangedListeners.add(listener)
        } else {
            //如果監聽列表修改被鎖定,那麼嘗試在監聽列表副本上添加
            //監聽列表副本將會在鎖定被解除時替換到監聽列表裡
            if (mOuterMatrixChangedListenersCopy.size == 0 && mOuterMatrixChangedListeners.size > 0) {
                mOuterMatrixChangedListenersCopy.addAll(mOuterMatrixChangedListeners)
            }
            mOuterMatrixChangedListenersCopy.add(listener)
        }
    }

    /**
     * 刪除外部矩陣變化監聽
     *
     * @param listener
     */
    fun removeOuterMatrixChangedListener(listener: OuterMatrixChangedListener?) {
        if (listener == null) {
            return
        }
        //如果監聽列表沒有被修改鎖定直接在監聽列表數據結構上修改
        if (mDispatchOuterMatrixChangedLock == 0) {
            mOuterMatrixChangedListeners.remove(listener)
        } else {
            //如果監聽列表被修改鎖定,那麼就在其副本上修改
            //其副本將會在鎖定解除時替換回監聽列表
            if (mOuterMatrixChangedListenersCopy.size == 0 && mOuterMatrixChangedListeners.size != 0) {
                mOuterMatrixChangedListenersCopy.addAll(mOuterMatrixChangedListeners)
            }
            if (mOuterMatrixChangedListenersCopy.size > 0) {
                mOuterMatrixChangedListenersCopy.remove(listener)
            }
        }
    }

    /**
     * 觸發外部矩陣修改事件
     *
     * 需要在每次給外部矩陣設置值時都調用此方法.
     *
     * @see .mOuterMatrix
     */
    private fun dispatchOuterMatrixChanged() {
        if (mOuterMatrixChangedListeners.size == 0) {
            return
        }
        //增加鎖
        //這里之所以用計數器做鎖定是因為可能在鎖定期間又間接調用了此方法產生遞歸
        //使用boolean無法判斷遞歸結束
        mDispatchOuterMatrixChangedLock++
        //在列表循環過程中不允許修改列表,否則將引發崩潰
        for (listener in mOuterMatrixChangedListeners) {
            listener.onOuterMatrixChanged(this)
        }
        //減鎖
        mDispatchOuterMatrixChangedLock--
        //如果是遞歸的情況,mDispatchOuterMatrixChangedLock可能大於1,只有減到0才能算列表的鎖定解除
        if (mDispatchOuterMatrixChangedLock == 0) {
            //如果期間有修改列表,那麼副本將不為null
            if (mOuterMatrixChangedListenersCopy.size > 0) {
                //將副本替換掉正式的列表
                mOuterMatrixChangedListeners.clear()
                mOuterMatrixChangedListeners.addAll(mOuterMatrixChangedListenersCopy)
                //清空副本
                mOuterMatrixChangedListenersCopy.clear()
            }
        }
    }

    /**
     * 計算雙擊之後圖片接下來應該被縮放的比例
     *
     * 如果值大於getMaxScale或者小於fit center尺寸，則實際使用取邊界值.
     * 通過覆蓋此方法可以定制不同的圖片被雙擊時使用不同的放大策略.
     *
     * @param innerScale 當前內部矩陣的縮放值
     * @param outerScale 當前外部矩陣的縮放值
     * @return接下來的縮放比例
     *
     * @see .doubleTap
     * @see .getMaxScale
     */
    @Suppress("ProtectedInFinal")
    protected fun calculateNextScale(innerScale: Float, outerScale: Float): Float {
        val currentScale = innerScale * outerScale
        return if (currentScale < MAX_SCALE) {
            MAX_SCALE
        } else {
            innerScale
        }
    }

    //region 初始化
    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initialize(context)
    }

    override fun initialize(context: Context) {
        super.setScaleType(ScaleType.MATRIX)
    }
    //endregion

    @Deprecated("不允許設置scaleType，只能用內部設置的matrix")
    override fun setScaleType(scaleType: ScaleType) {
    }

    // //////////////////////////////繪製///////////////// ///////////////
    override fun onDraw(canvas: Canvas) {
        //在繪製前設置變換矩陣
        if (isReady) {
            val matrix = MathUtils.matrixTake()
            imageMatrix = getCurrentImageMatrix(matrix)
            MathUtils.matrixGiven(matrix)
        }
        //對圖像做遮罩處理
        if (mMask == null) super.draw(canvas)
        else {
            canvas.save()
            canvas.clipRect(mMask!!)
            super.onDraw(canvas)
            canvas.restore()
        }
    }

    /**
     * mask變換動畫
     *
     * 將mask從一個rect動畫到另外一個rect
     */
    private inner class MaskAnimator
    /**
     * 创建mask变换动画
     *
     * @param start 动画起始状态
     * @param end 动画终点状态
     * @param duration 动画持续时间
     */
    (start: RectF, end: RectF, duration: Long) : ValueAnimator(), ValueAnimator.AnimatorUpdateListener {

        /**
         * 開始mask
         */
        private val mStart = FloatArray(4)

        /**
         * 結束mask
         */
        private val mEnd = FloatArray(4)

        /**
         * 中间结果mask
         */
        private val mResult = FloatArray(4)

        init {
            setFloatValues(0f, 1f)
            setDuration(duration)
            addUpdateListener(this)
            //将起点终点拷贝到数组方便计算
            mStart[0] = start.left
            mStart[1] = start.top
            mStart[2] = start.right
            mStart[3] = start.bottom
            mEnd[0] = end.left
            mEnd[1] = end.top
            mEnd[2] = end.right
            mEnd[3] = end.bottom
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            //获取动画进度,0-1范围
            val value = animation.animatedValue as Float
            //根据进度对起点终点之间做插值
            for (i in 0..3) {
                mResult[i] = mStart[i] + (mEnd[i] - mStart[i]) * value
            }
            //期間mask有可能被置空了,所以判斷一下
            if (mMask == null) {
                mMask = RectF()
            }
            //設置新的mask並繪製
            mMask?.set(mResult[0], mResult[1], mResult[2], mResult[3])
            invalidate()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val action = event.action and MotionEvent.ACTION_MASK
        //最後一個點抬起或者取消，結束所有模式
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            //如果之前是縮放模式,還需要觸發一下縮放結束動畫
            if (pinchMode == PINCH_MODE_SCALE) {
                scaleEnd()
            }
            pinchMode = PINCH_MODE_FREE
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            //多個手指情況下抬起一個手指,此時需要是縮放模式才觸發
            if (pinchMode == PINCH_MODE_SCALE) {
                //抬起的點如果大於2，那麼縮放模式還有效，但是有可能初始點變了，重新測量初始點
                if (event.pointerCount > 2) {
                    //如果還沒結束縮放模式，但是第一個點抬起了，那麼讓第二個點和第三個點作為縮放控制點
                    if (event.action shr 8 == 0) {
                        saveScaleContext(event.getX(1), event.getY(1), event.getX(2), event.getY(2))
                        //如果還沒結束縮放模式，但是第二個點抬起了，那麼讓第一個點和第三個點作為縮放控制點
                    } else if (event.action shr 8 == 1) {
                        saveScaleContext(event.getX(0), event.getY(0), event.getX(2), event.getY(2))
                    }
                }
                //如果抬起的點等於2,那麼此時只剩下一個點,也不允許進入單指模式,因為此時可能圖片沒有在正確的位置上
            }
            //第一個點按下，開啟滾動模式，記錄開始滾動的點
        } else if (action == MotionEvent.ACTION_DOWN) {
            //在矩陣動畫過程中不允許啟動滾動模式
            if (mScaleAnimator?.isRunning != true) {
                //停止所有動畫
                cancelAllAnimator()
                //切換到滾動模式
                pinchMode = PINCH_MODE_SCROLL
                //保存觸發點用於move計算差值
                mLastMovePoint.set(event.x, event.y)
            }
            //非第一個點按下，關閉滾動模式，開啟縮放模式，記錄縮放模式的一些初始數據
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            //停止所有動畫
            cancelAllAnimator()
            //切換到縮放模式
            pinchMode = PINCH_MODE_SCALE
            //保存縮放的兩個手指
            saveScaleContext(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mScaleAnimator?.isRunning != true) {
                //在滚动模式下移动
                if (pinchMode == PINCH_MODE_SCROLL) {
                    //每次移动产生一个差值累积到图片位置上
                    scrollBy(event.x - mLastMovePoint.x, event.y - mLastMovePoint.y)
                    //记录新的移动点
                    mLastMovePoint.set(event.x, event.y)
                    //在缩放模式下移动
                } else if (pinchMode == PINCH_MODE_SCALE && event.pointerCount > 1) {
                    //两个缩放点间的距离
                    val distance = MathUtils.getDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
                    //保存缩放点中点
                    val lineCenter = MathUtils.getCenterPoint(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
                    mLastMovePoint.set(lineCenter[0], lineCenter[1])
                    //处理缩放
                    scale(mScaleCenter, mScaleBase, distance, mLastMovePoint)
                }
            }
        }
        //无论如何都处理各种外部手势
        mGestureDetector.onTouchEvent(event)
        return true
    }

    /**
     * 让图片移动一段距离
     *
     * 不能移动超过可移动范围,超过了就到可移动范围边界为止.
     *
     * @param xDiff 移动距离
     * @param yDiff 移动距离
     * @return 是否改变了位置
     */
    private fun scrollBy(xDiff: Float, yDiff: Float): Boolean {
        var dx = xDiff
        var dy = yDiff
        if (!isReady) return false

        //原圖方框
        val bound = MathUtils.rectFTake()
        getImageBound(bound)
        //控件大小
        val displayWidth = width.toFloat()
        val displayHeight = height.toFloat()
        //如果當前圖片寬度小於控件寬度，則不能移動
        dx = when {
            bound.right - bound.left < displayWidth -> 0f
            //如果圖片左邊在移動後超出控件左邊
            bound.left + dx > 0 -> //如果在移動之前是沒超出的，計算應該移動的距離
                if (bound.left < 0) {
                    -bound.left
                    //否則無法移動
                } else {
                    0f
                }
            //如果圖片右邊在移動後超出控件右邊
            bound.right + dx < displayWidth -> //如果在移動之前是沒超出的，計算應該移動的距離
                if (bound.right > displayWidth) {
                    displayWidth - bound.right
                    //否則無法移動
                } else {
                    0f
                }
            else -> dx
        }
        //以下同理
        //應用移動變換
        //觸發重繪
        //檢查是否有變化
        //以下同理
        dy = when {
            bound.bottom - bound.top < displayHeight -> 0f
            bound.top + dy > 0 ->
                if (bound.top < 0) {
                    -bound.top
                } else {
                    0f
                }
            bound.bottom + dy < displayHeight ->
                if (bound.bottom > displayHeight) {
                    displayHeight - bound.bottom
                } else {
                    0f
                }
            else -> dy
        }
        MathUtils.rectFGiven(bound)
        //應用移動變換
        //觸發重繪
        //檢查是否有變化
        //應用移動變換
        mOuterMatrix.postTranslate(dx, dy)
        dispatchOuterMatrixChanged()
        //觸發重繪
        invalidate()
        //檢查是否有變化
        return dx != 0f || dy != 0f
    }

    /**
     * 記錄縮放前的一些信息
     *
     * 保存基礎縮放值​​.
     * 保存圖片縮放中點.
     *
     * @param x1 縮放第一個手指
     * @param y1 縮放第一個手指
     * @param x2 縮放第二個手指
     * @param y2 縮放第二個手指
     */
    private fun saveScaleContext(x1: Float, y1: Float, x2: Float, y2: Float) {
        //记录基础缩放值,其中图片缩放比例按照x方向来计算
        //理论上图片应该是等比的,x和y方向比例相同
        //但是有可能外部设定了不规范的值.
        //但是后续的scale操作会将xy不等的缩放值纠正,改成和x方向相同
        mScaleBase = MathUtils.getMatrixScale(mOuterMatrix)[0] / MathUtils.getDistance(x1, y1, x2, y2)
        //两手指的中点在屏幕上落在了图片的某个点上,图片上的这个点在经过总矩阵变换后和手指中点相同
        //现在我们需要得到图片上这个点在图片是fit center状态下在屏幕上的位置
        //因为后续的计算都是基于图片是fit center状态下进行变换
        //所以需要把两手指中点除以外层变换矩阵得到mScaleCenter
        val center = MathUtils.inverseMatrixPoint(MathUtils.getCenterPoint(x1, y1, x2, y2), mOuterMatrix)
        mScaleCenter.set(center[0], center[1])
    }

    /**
     * 对图片按照一些手势信息进行缩放
     *
     * @param scaleCenter mScaleCenter
     * @param scaleBase mScaleBase
     * @param distance 手指两点之间距离
     * @param lineCenter 手指两点之间中点
     *
     * @see .mScaleCenter
     *
     * @see .mScaleBase
     */
    private fun scale(scaleCenter: PointF, scaleBase: Float, distance: Float, lineCenter: PointF) {
        if (!isReady) {
            return
        }
        //计算图片从fit center状态到目标状态的缩放比例
        val scale = scaleBase * distance
        val matrix = MathUtils.matrixTake()
        //按照图片缩放中心缩放，并且让缩放中心在缩放点中点上
        matrix.postScale(scale, scale, scaleCenter.x, scaleCenter.y)
        //让图片的缩放中点跟随手指缩放中点
        matrix.postTranslate(lineCenter.x - scaleCenter.x, lineCenter.y - scaleCenter.y)
        //應用變換
        mOuterMatrix.set(matrix)
        MathUtils.matrixGiven(matrix)
        dispatchOuterMatrixChanged()
        //重繪
        invalidate()
    }

    /**
     * 雙擊後放大或者縮小
     *
     * 將圖片縮放比例縮放到nextScale指定的值.
     * 但nextScale值不能大於最大縮放值不能小於fit center情況下的縮放值.
     * 將雙擊的點盡量移動到控件中心.
     *
     * @param x 雙擊的點
     * @param y 雙擊的點
     *
     * @see .calculateNextScale
     * @see .getMaxScale
     */
    private fun doubleTap(x: Float, y: Float) {
        if (!isReady) {
            return
        }
        //獲取第一層變換矩陣
        val innerMatrix = MathUtils.matrixTake()
        getInnerMatrix(innerMatrix)
        //當前總的縮放比例
        val innerScale = MathUtils.getMatrixScale(innerMatrix)[0]
        val outerScale = MathUtils.getMatrixScale(mOuterMatrix)[0]
        val currentScale = innerScale * outerScale
        //控件大小
        val displayWidth = width.toFloat()
        val displayHeight = height.toFloat()
        //最大放大大小
        val maxScale = maxScale
        //接下來要放大的大小
        var nextScale = calculateNextScale(innerScale, outerScale)
        //如果接下來放大大於最大值或者小於fit center值，則取邊界
        if (nextScale > maxScale) {
            nextScale = maxScale
        }
        if (nextScale < innerScale) {
            nextScale = innerScale
        }
        //開始計算縮放動畫的結果矩陣
        val animEnd = MathUtils.matrixTake(mOuterMatrix)
        //計算還需縮放的倍數
        animEnd.postScale(nextScale / currentScale, nextScale / currentScale, x, y)
        //將放大點移動到控件中心
        animEnd.postTranslate(displayWidth / 2f - x, displayHeight / 2f - y)
        //得到放大之後的圖片方框
        val testMatrix = MathUtils.matrixTake(innerMatrix)
        testMatrix.postConcat(animEnd)
        val testBound = MathUtils.rectFTake(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        testMatrix.mapRect(testBound)
        //修正位置
        val postX = when {
            testBound.right - testBound.left < displayWidth -> displayWidth / 2f - (testBound.right + testBound.left) / 2f
            testBound.left > 0 -> -testBound.left
            testBound.right < displayWidth -> displayWidth - testBound.right
            else -> 0f
        }
        val postY = when {
            testBound.bottom - testBound.top < displayHeight -> displayHeight / 2f - (testBound.bottom + testBound.top) / 2f
            testBound.top > 0 -> -testBound.top
            testBound.bottom < displayHeight -> displayHeight - testBound.bottom
            else -> 0f
        }

        //應用修正位置
        //清理當前可能正在執行的動畫
        //啟動矩陣動畫
        //清理臨時變量
        //應用修正位置
        //清理當前可能正在執行的動畫
        //啟動矩陣動畫
        //清理臨時變量
        //應用修正位置
        //清理當前可能正在執行的動畫
        //啟動矩陣動畫
        //清理臨時變量
        //應用修正位置
        animEnd.postTranslate(postX, postY)
        //清理當前可能正在執行的動畫
        cancelAllAnimator()
        //啟動矩陣動畫
        mScaleAnimator = ScaleAnimator(mOuterMatrix, animEnd)
        mScaleAnimator?.start()
        //清理臨時變量
        MathUtils.rectFGiven(testBound)
        MathUtils.matrixGiven(testMatrix)
        MathUtils.matrixGiven(animEnd)
        MathUtils.matrixGiven(innerMatrix)
    }

    /**
     * 當縮放操作結束動畫
     *
     * 如果圖片超過邊界,找到最近的位置動畫恢復.
     * 如果圖片縮放尺寸超過最大值或者最小值,找到最近的值動畫恢復.
     */
    private fun scaleEnd() {
        if (!isReady) {
            return
        }
        //獲取圖片整體的變換矩陣
        val currentMatrix = MathUtils.matrixTake()
        getCurrentImageMatrix(currentMatrix)
        //整體縮放比例
        val currentScale = MathUtils.getMatrixScale(currentMatrix)[0]
        //第二層縮放比例
        val outerScale = MathUtils.getMatrixScale(mOuterMatrix)[0]
        //控件大小
        val displayWidth = width.toFloat()
        val displayHeight = height.toFloat()
        //最大縮放比例
        val maxScale = maxScale
        //比例修正
        var scalePost = 1f
        //如果整體縮放比例大於最大比例，進行縮放修正
        if (currentScale > maxScale) {
            scalePost = maxScale / currentScale
        }
        //如果缩放修正后整体导致第二层缩放小于1（就是图片比fit center状态还小），重新修正缩放
        if (outerScale * scalePost < 1f) {
            scalePost = 1f / outerScale
        }
        //尝试根据缩放点进行缩放修正
        val testMatrix = MathUtils.matrixTake(currentMatrix)
        testMatrix.postScale(scalePost, scalePost, mLastMovePoint.x, mLastMovePoint.y)
        val testBound = MathUtils.rectFTake(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        //获取缩放修正后的图片方框
        testMatrix.mapRect(testBound)
        //位置修正
        //检测缩放修正后位置有无超出，如果超出进行位置修正
        //計算結束矩陣
        //清理當前可能正在執行的動畫
        //啟動矩陣動畫
        //清理臨時變量
        //清理臨時變量
        val postX = when {
            testBound.right - testBound.left < displayWidth -> displayWidth / 2f - (testBound.right + testBound.left) / 2f
            testBound.left > 0 -> -testBound.left
            testBound.right < displayWidth -> displayWidth - testBound.right
            else -> 0f
        }
        //計算結束矩陣
        //清理當前可能正在執行的動畫
        //啟動矩陣動畫
        //清理臨時變量
        //清理臨時變量
        val postY = when {
            testBound.bottom - testBound.top < displayHeight -> displayHeight / 2f - (testBound.bottom + testBound.top) / 2f
            testBound.top > 0 -> -testBound.top
            testBound.bottom < displayHeight -> displayHeight - testBound.bottom
            else -> 0f
        }

//        //如果缩放修正不为1，说明进行了修正
//        if (scalePost != 1f) {
//            change = true
//        }
//        //如果位置修正不為0，說明進行了修正
//        //只有有執行修正才執行動畫
//        //如果位置修正不為0，說明進行了修正
//        //只有有執行修正才執行動畫
//        //如果位置修正不為0，說明進行了修正
//        //只有有執行修正才執行動畫
//        //如果位置修正不為0，說明進行了修正
//        if (postX != 0f || postY != 0f) {
//            change = true
//        }

        //只有有執行修正才執行動畫
        if (scalePost != 1f || postX != 0f || postY != 0f) {
            //計算結束矩陣
            val animEnd = MathUtils.matrixTake(mOuterMatrix)
            animEnd.postScale(scalePost, scalePost, mLastMovePoint.x, mLastMovePoint.y)
            animEnd.postTranslate(postX, postY)
            //清理當前可能正在執行的動畫
            cancelAllAnimator()
            //啟動矩陣動畫
            mScaleAnimator = ScaleAnimator(mOuterMatrix, animEnd)
            mScaleAnimator?.start()
            //清理臨時變量
            MathUtils.matrixGiven(animEnd)
        }
        //清理臨時變量
        MathUtils.rectFGiven(testBound)
        MathUtils.matrixGiven(testMatrix)
        MathUtils.matrixGiven(currentMatrix)
    }

    /**
     * 執行慣性動畫
     *
     * 動畫在遇到不能移動就停止.
     * 動畫速度衰減到很小就停止.
     *
     * 其中參數速度單位為像素/秒
     *
     * @param vx x方向速度
     * @param vy y方向速度
     */
    private fun fling(vx: Float, vy: Float) {
        if (!isReady) return

        //清理當前可能正在執行的動畫
        cancelAllAnimator()
        //創建慣性動畫
        // FlingAnimator單位為像素/幀,一秒60幀
        mFlingAnimator = FlingAnimator(vx / 60f, vy / 60f)
        mFlingAnimator?.start()
    }

    /**
     * 停止所有手勢動畫
     */
    private fun cancelAllAnimator() {
        mScaleAnimator?.cancel()
        mScaleAnimator = null
        mFlingAnimator?.cancel()
        mFlingAnimator = null
    }

    /**
     * 慣性動畫
     *
     * 速度逐漸衰減,每幀速度衰減為原來的FLING_DAMPING_FACTOR,當速度衰減到小於1時停止.
     * 當圖片不能移動時,動畫停止.
     */
    private inner class FlingAnimator
    /**
     * 創建慣性動畫
     *
     * 參數單位為像素/幀
     *
     * @param vectorX 速度向量
     * @param vectorY 速度向量
     */
    (vectorX: Float, vectorY: Float) : ValueAnimator(), ValueAnimator.AnimatorUpdateListener {

        /**
         * 速度向量
         */
        private val mVector: FloatArray

        init {
            setFloatValues(0f, 1f)
            duration = 1000000
            addUpdateListener(this)
            mVector = floatArrayOf(vectorX, vectorY)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            //移動圖像並給出結果
            val result = scrollBy(mVector[0], mVector[1])
            //衰減速度
            mVector[0] *= FLING_DAMPING_FACTOR
            mVector[1] *= FLING_DAMPING_FACTOR
            //速度太小或者不能移動了就結束
            if (!result || MathUtils.getDistance(0f, 0f, mVector[0], mVector[1]) < 1f) {
                animation.cancel()
            }
        }
    }

    /**
     * 縮放動畫
     *
     * 在給定時間內從一個矩陣的變化逐漸動畫到另一個矩陣的變化
     */
    private inner class ScaleAnimator
    /**
     * 構建一個縮放動畫
     *
     * 從一個矩陣變換到另外一個矩陣
     *
     * @param start 開始矩陣
     * @param end 結束矩陣
     * @param duration 動畫時間
     */
    @JvmOverloads
    constructor(start: Matrix, end: Matrix, duration: Long = SCALE_ANIMATOR_DURATION.toLong()) : ValueAnimator(), ValueAnimator.AnimatorUpdateListener {

        /**
         * 開始矩陣
         */
        private val mStart = FloatArray(9)

        /**
         * 結束矩陣
         */
        private val mEnd = FloatArray(9)

        /**
         * 中間結果矩陣
         */
        private val mResult = FloatArray(9)

        init {
            setFloatValues(0f, 1f)
            setDuration(duration)
            addUpdateListener(this)
            start.getValues(mStart)
            end.getValues(mEnd)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            //獲取動畫進度
            val value = animation.animatedValue as Float
            //根據動畫進度計算矩陣中間插值
            for (i in 0..8) {
                mResult[i] = mStart[i] + (mEnd[i] - mStart[i]) * value
            }
            //設置矩陣並重繪
            mOuterMatrix.setValues(mResult)
            dispatchOuterMatrixChanged()
            invalidate()
        }
    }

    // //////////////////////////////防止內存抖動復用對象//////////// ////////////////////

    /**
     * 對像池
     *
     * 防止頻繁new對象產生內存抖動.
     * 由於對像池最大長度限制,如果吞度量超過對像池容量,仍然會發生抖動.
     * 此時需要增大對像池容量,但是會佔用更多內存.
     *
     * @param <T>對像池容納的對像類型
    </T> */
    private abstract class ObjectsPool<T>
    /**
     * 创建一个对象池
     *
     * @param mSize 對像池的最大容量
     */
    (private val mSize: Int) {

        /**
         * 對像池隊列
         */
        private val mQueue: Queue<T>

        init {
            mQueue = LinkedList()
        }

        /**
         * 获取一个空闲的对象
         *
         * 如果对象池为空,则对象池自己会new一个返回.
         * 如果对象池内有对象,则取一个已存在的返回.
         * take出来的对象用完要记得调用given归还.
         * 如果不归还,让然会发生内存抖动,但不会引起泄漏.
         *
         * @return 可用的对象
         *
         * @see .given
         */
        fun take(): T {
            //如果池内为空就创建一个
            return if (mQueue.size == 0) {
                newInstance()
            } else {
                //对象池里有就从顶端拿出来一个返回
                resetInstance(mQueue.poll())
            }
        }

        /**
         * 归还对象池内申请的对象
         *
         * 如果归还的对象数量超过对象池容量,那么归还的对象就会被丢弃.
         *
         * @param obj 归还的对象
         *
         * @see .take
         */
        fun given(obj: T?) {
            //如果对象池还有空位子就归还对象
            if (obj != null && mQueue.size < mSize) {
                mQueue.offer(obj)
            }
        }

        /**
         * 實例化對象
         *
         * @return創建的對象
         */
        protected abstract fun newInstance(): T

        /**
         * 重置對象
         *
         * 把對像數據清空到就像剛創建的一樣.
         *
         * @param obj 需要被重置的對象
         * @return 被重置之後的對象
         */
        protected abstract fun resetInstance(obj: T): T
    }

    /**
     * 矩陣對像池
     */
    private class MatrixPool(size: Int) : ObjectsPool<Matrix>(size) {

        override fun newInstance(): Matrix {
            return Matrix()
        }

        override fun resetInstance(obj: Matrix): Matrix {
            obj.reset()
            return obj
        }
    }

    /**
     * 矩形對像池
     */
    private class RectFPool(size: Int) : ObjectsPool<RectF>(size) {

        override fun newInstance(): RectF {
            return RectF()
        }

        override fun resetInstance(obj: RectF): RectF {
            obj.setEmpty()
            return obj
        }
    }


    // //////////////////////////////數學計算工具類////////////// //////////////////

    /**
     * 數學計算工具類
     */
    object MathUtils {

        /**
         * 矩陣對像池
         */
        private val mMatrixPool = MatrixPool(16)

        /**
         * 矩形對像池
         */
        private val mRectFPool = RectFPool(16)

        /**
         * 獲取矩陣對象
         */
        fun matrixTake(): Matrix {
            return mMatrixPool.take()
        }

        /**
         * 獲取某個矩陣的copy
         */
        fun matrixTake(matrix: Matrix?): Matrix {
            val result = mMatrixPool.take()
            if (matrix != null) {
                result.set(matrix)
            }
            return result
        }

        /**
         * 歸還矩陣對象
         */
        fun matrixGiven(matrix: Matrix) {
            mMatrixPool.given(matrix)
        }

        /**
         * 獲取矩形對象
         */
        fun rectFTake(): RectF {
            return mRectFPool.take()
        }

        /**
         * 按照指定值獲取矩形對象
         */
        fun rectFTake(left: Float, top: Float, right: Float, bottom: Float): RectF {
            val result = mRectFPool.take()
            result.set(left, top, right, bottom)
            return result
        }

        /**
         * 獲取某個矩形的副本
         */
        fun rectFTake(rectF: RectF?): RectF {
            val result = mRectFPool.take()
            if (rectF != null) {
                result.set(rectF)
            }
            return result
        }

        /**
         * 歸還矩形對象
         */
        fun rectFGiven(rectF: RectF) {
            mRectFPool.given(rectF)
        }

        /**
         * 獲取兩點之間距離
         *
         * @param x1 點1
         * @param y1 點1
         * @param x2 點2
         * @param y2 點2
         * @return距離
         */
        fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val x = x1 - x2
            val y = y1 - y2
            return Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }

        /**
         * 獲取兩點的中點
         *
         * @param x1 點1
         * @param y1 點1
         * @param x2 點2
         * @param y2 點2
         * @return float[]{x, y}
         */
        fun getCenterPoint(x1: Float, y1: Float, x2: Float, y2: Float): FloatArray {
            return floatArrayOf((x1 + x2) / 2f, (y1 + y2) / 2f)
        }

        /**
         * 获取矩阵的缩放值
         *
         * @param matrix 要计算的矩阵
         * @return float[]{scaleX, scaleY}
         */
        fun getMatrixScale(matrix: Matrix?): FloatArray {
            return if (matrix != null) {
                val value = FloatArray(9)
                matrix.getValues(value)
                floatArrayOf(value[0], value[4])
            } else {
                FloatArray(2)
            }
        }

        /**
         * 計算點除以矩陣的值
         *
         * matrix.mapPoints(unknownPoint) -> point
         * 已知point和matrix,求unknownPoint的值.
         *
         * @param point
         * @param matrix
         * @return unknownPoint
         */
        fun inverseMatrixPoint(point: FloatArray?, matrix: Matrix?): FloatArray {
            return if (point != null && matrix != null) {
                val dst = FloatArray(2)
                //計算matrix的逆矩陣
                val inverse = matrixTake()
                matrix.invert(inverse)
                //用逆矩陣變換point到dst,dst就是結果
                inverse.mapPoints(dst, point)
                //清除臨時變量
                matrixGiven(inverse)
                dst
            } else {
                FloatArray(2)
            }
        }

        /**
         * 計算兩個矩形之間的變換矩陣
         *
         * unknownMatrix.mapRect(to, from)
         * 已知from矩形和to矩形,求unknownMatrix
         *
         * @param from
         * @param to
         * @param result unknownMatrix
         */
        fun calculateRectTranslateMatrix(from: RectF?, to: RectF?, result: Matrix?) {
            if (from == null || to == null || result == null) {
                return
            }
            if (from.width() == 0f || from.height() == 0f) {
                return
            }
            result.reset()
            result.postTranslate(-from.left, -from.top)
            result.postScale(to.width() / from.width(), to.height() / from.height())
            result.postTranslate(to.left, to.top)
        }

        /**
         * 計算圖片在某個ImageView中的顯示矩形
         *
         * @param container ImageView的Rect
         * @param srcWidth 圖片的寬度
         * @param srcHeight 圖片的高度
         * @param scaleType 圖片在ImageView中的ScaleType
         * @param result 圖片應該在ImageView中展示的矩形
         */
        fun calculateScaledRectInContainer(container: RectF?, srcWidth: Float, srcHeight: Float, scaleType: ScaleType?, result: RectF?) {
            val tempScaleType = scaleType ?: ScaleType.FIT_CENTER
            if (container == null || result == null) {
                return
            }
            if (srcWidth == 0f || srcHeight == 0f) {
                return
            }
            result.setEmpty()
            when (tempScaleType) {
                ScaleType.FIT_XY -> result.set(container)
                ScaleType.CENTER -> {
                    val matrix = matrixTake()
                    val rect = rectFTake(0f, 0f, srcWidth, srcHeight)
                    matrix.setTranslate((container.width() - srcWidth) * 0.5f, (container.height() - srcHeight) * 0.5f)
                    matrix.mapRect(result, rect)
                    rectFGiven(rect)
                    matrixGiven(matrix)
                    result.left += container.left
                    result.right += container.left
                    result.top += container.top
                    result.bottom += container.top
                }
                ScaleType.CENTER_CROP -> {
                    val matrix = matrixTake()
                    val rect = rectFTake(0f, 0f, srcWidth, srcHeight)
                    val scale: Float
                    var dx = 0f
                    var dy = 0f
                    if (srcWidth * container.height() > container.width() * srcHeight) {
                        scale = container.height() / srcHeight
                        dx = (container.width() - srcWidth * scale) * 0.5f
                    } else {
                        scale = container.width() / srcWidth
                        dy = (container.height() - srcHeight * scale) * 0.5f
                    }
                    matrix.setScale(scale, scale)
                    matrix.postTranslate(dx, dy)
                    matrix.mapRect(result, rect)
                    rectFGiven(rect)
                    matrixGiven(matrix)
                    result.left += container.left
                    result.right += container.left
                    result.top += container.top
                    result.bottom += container.top
                }
                ScaleType.CENTER_INSIDE -> {
                    val matrix = matrixTake()
                    val rect = rectFTake(0f, 0f, srcWidth, srcHeight)
                    val dx: Float
                    val dy: Float
                    val scale = if (srcWidth <= container.width() && srcHeight <= container.height()) {
                        1f
                    } else {
                        Math.min(container.width() / srcWidth, container.height() / srcHeight)
                    }
                    dx = (container.width() - srcWidth * scale) * 0.5f
                    dy = (container.height() - srcHeight * scale) * 0.5f
                    matrix.setScale(scale, scale)
                    matrix.postTranslate(dx, dy)
                    matrix.mapRect(result, rect)
                    rectFGiven(rect)
                    matrixGiven(matrix)
                    result.left += container.left
                    result.right += container.left
                    result.top += container.top
                    result.bottom += container.top
                }
                ScaleType.FIT_CENTER -> {
                    val matrix = matrixTake()
                    val rect = rectFTake(0f, 0f, srcWidth, srcHeight)
                    val tempSrc = rectFTake(0f, 0f, srcWidth, srcHeight)
                    val tempDst = rectFTake(0f, 0f, container.width(), container.height())
                    matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER)
                    matrix.mapRect(result, rect)
                    rectFGiven(tempDst)
                    rectFGiven(tempSrc)
                    rectFGiven(rect)
                    matrixGiven(matrix)
                    result.left += container.left
                    result.right += container.left
                    result.top += container.top
                    result.bottom += container.top
                }
                ScaleType.FIT_START -> {
                    val matrix = matrixTake()
                    val rect = rectFTake(0f, 0f, srcWidth, srcHeight)
                    val tempSrc = rectFTake(0f, 0f, srcWidth, srcHeight)
                    val tempDst = rectFTake(0f, 0f, container.width(), container.height())
                    matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.START)
                    matrix.mapRect(result, rect)
                    rectFGiven(tempDst)
                    rectFGiven(tempSrc)
                    rectFGiven(rect)
                    matrixGiven(matrix)
                    result.left += container.left
                    result.right += container.left
                    result.top += container.top
                    result.bottom += container.top
                }
                ScaleType.FIT_END -> {
                    val matrix = matrixTake()
                    val rect = rectFTake(0f, 0f, srcWidth, srcHeight)
                    val tempSrc = rectFTake(0f, 0f, srcWidth, srcHeight)
                    val tempDst = rectFTake(0f, 0f, container.width(), container.height())
                    matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.END)
                    matrix.mapRect(result, rect)
                    rectFGiven(tempDst)
                    rectFGiven(tempSrc)
                    rectFGiven(rect)
                    matrixGiven(matrix)
                    result.left += container.left
                    result.right += container.left
                    result.top += container.top
                    result.bottom += container.top
                }
                else -> result.set(container)
            }
        }
    }

    companion object {
        // //////////////////////////////配置參數//////////////// ////////////////

        /**
         * 圖片縮放動畫時間
         */
        const val SCALE_ANIMATOR_DURATION = 200

        /**
         * 慣性動畫衰減參數
         */
        const val FLING_DAMPING_FACTOR = 0.9f

        /**
         * 圖片最大放大比例
         */
        private const val MAX_SCALE = 4f


        // //////////////////////////////公共狀態獲取/////////////// /////////////////

        /**
         * 手勢狀態：自由狀態
         *
         * @see .getPinchMode
         */
        const val PINCH_MODE_FREE = 0

        /**
         * 手勢狀態：單指滾動狀態
         *
         * @see .getPinchMode
         */
        const val PINCH_MODE_SCROLL = 1

        /**
         * 手勢狀態：雙指縮放狀態
         *
         * @see .getPinchMode
         */
        const val PINCH_MODE_SCALE = 2
    }
}
