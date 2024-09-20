package dji.v5.ux.core.panel.topbar

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.core.view.setPadding
import dji.v5.ux.R
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.core.widget.battery.BatteryWidget

class BackButtonWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<BatteryWidget.ModelState>(context, attrs, defStyleAttr) {

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.uxsdk_widget_back_button, this)
        setPadding(0) // remove default padding

        val imageViewBack = findViewById<ImageView>(R.id.image_view_back)
        imageViewBack.setOnClickListener {
            simulateBackPress()
        }
    }

    private fun simulateBackPress() {
        (context as? Activity)?.onBackPressed()
    }

    override fun reactToModelChanges() {
        /* no-op */
    }

}