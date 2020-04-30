package com.todomap

import android.content.res.Resources
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * @author WeiYi Yu
 * @date 2020-04-28
 */

//region BottomSheet

/**
 * Example:
 * If screen height = 1000 unit
 * ratio = 0.2 -> BottomSheet has a 200(1000 * 0.2) top offset when expanded
 * ratio = 0.5 -> BottomSheet has a 500(1000 * 0.5) top offset when expanded
 */
@BindingAdapter("expandedTopOffsetRatio")
fun setBottomSheetExpandedOffset(view: View, ratio: Double) {
    val behavior = BottomSheetBehavior.from(view)
    behavior.expandedOffset = (Resources.getSystem().displayMetrics.heightPixels * ratio).toInt()
}

@BindingAdapter("bottomSheetState")
fun setBottomSheetState(view: View, bottomSheetState: Int) {
    val behavior = BottomSheetBehavior.from(view)
    behavior.state = bottomSheetState
}

@BindingAdapter("onBottomSheetClosed")
fun addBottomSheetHiddenCallback(view: View, callback: () -> Unit) {
    val behavior = BottomSheetBehavior.from(view)
    behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                callback.invoke()
            }
        }
    })
}
//endregion

@BindingAdapter("todoListVisible")
fun setTodoListVisibility(recyclerView: RecyclerView, todoListVisible: Boolean) {
    val height = Resources.getSystem().displayMetrics.heightPixels / 2f
    val translationY = if (todoListVisible) 0f else -height
    recyclerView.animate()
        .translationY(translationY)
        .start()
}

@BindingAdapter("fabVisible")
fun setVisibility(fab: FloatingActionButton, fabVisible: Boolean) {
    if (fabVisible) fab.show() else fab.hide()
}