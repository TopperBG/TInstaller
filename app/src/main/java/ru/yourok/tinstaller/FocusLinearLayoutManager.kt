package ru.yourok.tinstaller

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager

class FocusLinearLayoutManager(context: Context, orientation: Int, defStyleAttr: Boolean) : LinearLayoutManager(context, orientation, defStyleAttr) {
    override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        val pos = getPosition(focused)
        val count = itemCount

        if (pos == 0 && direction == View.FOCUS_UP)
            return focused

        if (pos == count - 1 && direction == View.FOCUS_DOWN)
            return focused

        return super.onInterceptFocusSearch(focused, direction)
    }
}