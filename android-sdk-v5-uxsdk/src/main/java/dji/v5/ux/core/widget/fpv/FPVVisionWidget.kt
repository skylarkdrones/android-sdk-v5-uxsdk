package dji.v5.ux.core.widget.fpv

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.isVisible
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.sdk.keyvalue.value.flightassistant.VisionAssistDirection
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.manager.datacenter.MediaDataCenter
import dji.v5.manager.interfaces.ICameraStreamManager.AvailableCameraUpdatedListener
import dji.v5.manager.interfaces.ICameraStreamManager.VisionAssistStatusListener
import dji.v5.ux.R
import kotlin.math.roundToInt

class FPVVisionWidget @JvmOverloads constructor(
    context: Context, val attrs: AttributeSet? = null, val defStyleAttr: Int = 0
) : FPVWidget(context, attrs, defStyleAttr) {

    private val availableCameraUpdatedListener = object : AvailableCameraUpdatedListener {
        override fun onAvailableCameraUpdated(availableCameraList: List<ComponentIndexType>) {
            updateFPVWidgetSource(availableCameraList)
        }

        override fun onCameraStreamEnableUpdate(
            cameraStreamEnableMap: Map<ComponentIndexType, Boolean>
        ) {
            // No-op
        }
    }

    private val cameraStreamManager by lazy {
        MediaDataCenter.getInstance().cameraStreamManager
    }

    private val arrowFront by lazy {
        createArrow(
            View.generateViewId(),
            R.drawable.uxsdk_arrow_up,
        )
    }

    private val arrowBack by lazy {
        createArrow(
            View.generateViewId(),
            R.drawable.uxsdk_arrow_down,
        )
    }

    private val arrowLeft by lazy {
        createArrow(
            View.generateViewId(),
            R.drawable.uxsdk_arrow_left,
        )
    }

    private val arrowRight by lazy {
        createArrow(
            View.generateViewId(),
            R.drawable.uxsdk_arrow_right,
        )
    }

    private val arrowDown by lazy {
        createArrow(
            View.generateViewId(),
            R.drawable.uxsdk_double_down_chevron,
        ).apply {
            visibility = GONE
        }
    }

    private val visionAssistStatusListener = object : VisionAssistStatusListener {
        override fun onVisionAssistEnabled(isEnable: Boolean) {
            /* no-op */
        }

        override fun onVisionAssistViewDirectionRangeUpdated(modes: MutableList<VisionAssistDirection>) {
            post {
                updateDownwardArrowVisibility(modes.contains(VisionAssistDirection.DOWN))
            }
        }

        override fun onVisionAssistViewDirectionUpdated(mode: VisionAssistDirection) {
            post {
                resetArrowColors()
                when (mode) {
                    VisionAssistDirection.FRONT -> {
                        setArrowSelected(
                            arrowFront,
                            selected = true,
                        )
                    }

                    VisionAssistDirection.BACK -> {
                        setArrowSelected(
                            arrowBack,
                            selected = true,
                        )
                    }

                    VisionAssistDirection.LEFT -> {
                        setArrowSelected(
                            arrowLeft,
                            selected = true,
                        )
                    }

                    VisionAssistDirection.RIGHT -> {
                        setArrowSelected(
                            arrowRight,
                            selected = true,
                        )
                    }

                    VisionAssistDirection.DOWN -> {
                        if (arrowDown.isVisible) {
                            setArrowSelected(
                                arrowDown,
                                selected = true,
                            )
                        }
                    }

                    VisionAssistDirection.AUTO -> {
                        setFPVDirection(VisionAssistDirection.FRONT)
                    }

                    VisionAssistDirection.UP,
                    VisionAssistDirection.OFF,
                    VisionAssistDirection.UNKNOWN -> {
                        /* no-op */
                    }
                }
            }
        }
    }

    private val topGuide by lazy {
        createGuideline(
            View.generateViewId(),
            LayoutParams.HORIZONTAL,
            TOP_GUIDE_PERCENTAGE,
        )
    }

    private val bottomGuide by lazy {
        createGuideline(
            View.generateViewId(),
            LayoutParams.HORIZONTAL,
            BOTTOM_GUIDE_PERCENTAGE,
        )
    }

    private val startGuide by lazy {
        createGuideline(
            View.generateViewId(),
            LayoutParams.VERTICAL,
            START_GUIDE_PERCENTAGE,
        )
    }

    private val endGuide by lazy {
        createGuideline(
            View.generateViewId(),
            LayoutParams.VERTICAL,
            END_GUIDE_PERCENTAGE,
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupArrows()
        cameraStreamManager.addAvailableCameraUpdatedListener(availableCameraUpdatedListener)
    }

    private fun setupArrows() {
        this.post {
            addGuidelines()
            addArrows()
            when (getLayoutSizeClass()) {
                LayoutSizeClass.SMALL -> {
                    arrangeArrows()
                }

                LayoutSizeClass.MEDIUM -> {
                    arrangeArrowsByGuidelines()
                }

                LayoutSizeClass.LARGE -> {
                    arrangeArrowsByGuidelines()
                }
            }
            setupArrowClickListeners()
            setupVisionAssistListener()
        }
    }

    private fun updateFPVWidgetSource(availableCameraList: List<ComponentIndexType?>?) {
        if (availableCameraList.isNullOrEmpty()) {
            visibility = GONE
            return
        }

        if (availableCameraList.contains(ComponentIndexType.FPV)) {
            updateVideoSource(ComponentIndexType.FPV)
            visibility = VISIBLE
            return
        }

        if (availableCameraList.contains(ComponentIndexType.VISION_ASSIST)) {
            updateVideoSource(ComponentIndexType.VISION_ASSIST)
            visibility = VISIBLE
            return
        }

        visibility = GONE
    }

    private fun createArrow(id: Int, drawableRes: Int): ImageView {
        return ImageView(context).apply {
            this.id = id
            setImageResource(drawableRes)
            layoutParams = LayoutParams(ARROW_SIZE, ARROW_SIZE)
            isClickable = true
            isFocusable = true
        }
    }

    private fun createGuideline(id: Int, orientation: Int, percent: Float): Guideline {
        return Guideline(context).apply {
            this.id = id
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                this.orientation = orientation
                this.guidePercent = percent
            }
        }
    }

    private fun addGuidelines() {
        this.addView(topGuide)
        this.addView(bottomGuide)
        this.addView(startGuide)
        this.addView(endGuide)
    }

    private fun removeGuidelines() {
        this.removeView(topGuide)
        this.removeView(bottomGuide)
        this.removeView(startGuide)
        this.removeView(endGuide)
    }

    private fun addArrows() {
        this.addView(arrowFront)
        this.addView(arrowBack)
        this.addView(arrowLeft)
        this.addView(arrowRight)
        this.addView(arrowDown)
    }

    private fun arrangeArrowsByGuidelines() {
        arrangeArrows(
            topGuideId = topGuide.id,
            bottomGuideId = bottomGuide.id,
            startGuideId = startGuide.id,
            endGuideId = endGuide.id,
        )
    }

    private fun arrangeArrows(
        topGuideId: Int = ConstraintSet.PARENT_ID,
        bottomGuideId: Int = ConstraintSet.PARENT_ID,
        startGuideId: Int = ConstraintSet.PARENT_ID,
        endGuideId: Int = ConstraintSet.PARENT_ID,
    ) {
        val set = ConstraintSet()
        set.clone(this)

        set.connect(arrowFront.id, ConstraintSet.TOP, topGuideId, ConstraintSet.TOP)
        set.connect(arrowFront.id, ConstraintSet.START, startGuideId, ConstraintSet.START)
        set.connect(arrowFront.id, ConstraintSet.END, endGuideId, ConstraintSet.END)

        set.connect(arrowBack.id, ConstraintSet.BOTTOM, bottomGuideId, ConstraintSet.BOTTOM)
        set.connect(arrowBack.id, ConstraintSet.START, startGuideId, ConstraintSet.START)
        set.connect(arrowBack.id, ConstraintSet.END, endGuideId, ConstraintSet.END)

        set.connect(arrowLeft.id, ConstraintSet.START, startGuideId, ConstraintSet.START)
        set.connect(arrowLeft.id, ConstraintSet.TOP, topGuideId, ConstraintSet.TOP)
        set.connect(arrowLeft.id, ConstraintSet.BOTTOM, bottomGuideId, ConstraintSet.BOTTOM)

        set.connect(arrowRight.id, ConstraintSet.END, endGuideId, ConstraintSet.END)
        set.connect(arrowRight.id, ConstraintSet.TOP, topGuideId, ConstraintSet.TOP)
        set.connect(arrowRight.id, ConstraintSet.BOTTOM, bottomGuideId, ConstraintSet.BOTTOM)

        set.clear(arrowDown.id, ConstraintSet.BOTTOM)
        set.clear(arrowDown.id, ConstraintSet.START)
        set.connect(arrowDown.id, ConstraintSet.TOP, topGuideId, ConstraintSet.TOP)
        set.connect(arrowDown.id, ConstraintSet.END, endGuideId, ConstraintSet.END)
        val topMarginPx = (DOWNWARD_ARROW_MARGIN_TOP_DP * resources.displayMetrics.density).roundToInt()
        val endMarginPx = (DOWNWARD_ARROW_MARGIN_END_DP * resources.displayMetrics.density).roundToInt()
        set.setMargin(arrowDown.id, ConstraintSet.TOP, topMarginPx)
        set.setMargin(arrowDown.id, ConstraintSet.END, endMarginPx)

        set.applyTo(this)
    }

    private fun setupArrowClickListeners() {
        arrowFront.setOnClickListener {
            setFPVDirection(VisionAssistDirection.FRONT)
        }
        arrowBack.setOnClickListener {
            setFPVDirection(VisionAssistDirection.BACK)
        }
        arrowLeft.setOnClickListener {
            setFPVDirection(VisionAssistDirection.LEFT)
        }
        arrowRight.setOnClickListener {
            setFPVDirection(VisionAssistDirection.RIGHT)
        }
        arrowDown.setOnClickListener {
            setFPVDirection(VisionAssistDirection.DOWN)
        }
    }

    private fun setupVisionAssistListener() {
        cameraStreamManager.addVisionAssistStatusListener(visionAssistStatusListener)
    }

    private fun removeArrows() {
        this.removeView(arrowFront)
        this.removeView(arrowBack)
        this.removeView(arrowLeft)
        this.removeView(arrowRight)
        this.removeView(arrowDown)
    }

    private fun resetArrowColors() {
        setArrowSelected(arrowFront)
        setArrowSelected(arrowBack)
        setArrowSelected(arrowLeft)
        setArrowSelected(arrowRight)
        setArrowSelected(arrowDown)
    }

    private fun updateDownwardArrowVisibility(isVisible: Boolean) {
        arrowDown.visibility = if (isVisible) VISIBLE else GONE
        if (!isVisible) {
            setArrowSelected(arrowDown)
        }
    }

    private fun setArrowSelected(view: ImageView, selected: Boolean = false) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FPVVisionWidget,
            0, 0
        ).apply {
            view.setColorFilter(
                getColor(
                    if (selected) {
                        R.styleable.FPVVisionWidget_uxsdk_arrowSelectedColor
                    } else {
                        R.styleable.FPVVisionWidget_uxsdk_arrowUnSelectedColor
                    },
                    if (selected) DEFAULT_SELECTED_ARROW_COLOR else DEFAULT_ARROW_COLOR,
                )
            )
        }
    }

    private fun setFPVDirection(direction: VisionAssistDirection) {
        cameraStreamManager.setVisionAssistViewDirection(direction, object :
            CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                Log.d(TAG, "onSuccess: Direction set successfully to = $direction")
            }

            override fun onFailure(error: IDJIError) {
                Log.d(
                    TAG, "onFailure: Failed to set the direction to = $direction, " +
                            "error = $error"
                )
            }
        })
    }

    private fun getLayoutSizeClass(): LayoutSizeClass {
        val widthPx = this.width
        val density = this.resources.displayMetrics.density
        val widthDp = widthPx / density

        return when {
            widthDp < 600 -> {
                LayoutSizeClass.SMALL
            }

            widthDp in 600f..839f -> {
                LayoutSizeClass.MEDIUM
            }

            else -> {
                LayoutSizeClass.LARGE
            }
        }
    }

    override fun onDetachedFromWindow() {
        cameraStreamManager.removeVisionAssistStatusListener(visionAssistStatusListener)
        cameraStreamManager.removeAvailableCameraUpdatedListener(
            availableCameraUpdatedListener
        )
        removeArrows()
        removeGuidelines()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val TAG = "FPVVisionWidget"
        private const val ARROW_SIZE = 60
        private const val DOWNWARD_ARROW_MARGIN_TOP_DP = 8
        private const val DOWNWARD_ARROW_MARGIN_END_DP = 20
        private const val DEFAULT_ARROW_COLOR = Color.WHITE
        private const val DEFAULT_SELECTED_ARROW_COLOR = Color.YELLOW
        private const val TOP_GUIDE_PERCENTAGE = .2f
        private const val BOTTOM_GUIDE_PERCENTAGE = .7f
        private const val START_GUIDE_PERCENTAGE = .15f
        private const val END_GUIDE_PERCENTAGE = .85f
    }

    enum class LayoutSizeClass {
        SMALL,
        MEDIUM,
        LARGE,
    }
}
