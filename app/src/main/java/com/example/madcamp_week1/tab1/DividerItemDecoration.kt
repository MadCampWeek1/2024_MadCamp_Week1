package com.example.madcamp_week1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(context: Context, resId: Int) : RecyclerView.ItemDecoration() {
    private val divider: Drawable? = ContextCompat.getDrawable(context, resId)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        divider?.let {
            val padding = 30
            val left = parent.paddingLeft + padding
            val right = parent.width - parent.paddingRight - padding

            val childCount = parent.childCount
            for (i in 0 until childCount - 1) {
                val child = parent.getChildAt(i)

                val params = child.layoutParams as RecyclerView.LayoutParams
                val top = child.bottom + params.bottomMargin
                val bottom = top + it.intrinsicHeight

                it.setBounds(left, top, right, bottom)
                it.draw(c)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        divider?.let {
            outRect.bottom = it.intrinsicHeight
        }
    }
}
