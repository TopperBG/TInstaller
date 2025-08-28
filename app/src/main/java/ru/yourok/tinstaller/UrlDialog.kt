package ru.yourok.tinstaller

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog


class UrlDialog {
    fun show(context: Activity, url: String, onOk: (url: String) -> Unit, onCancel: () -> Unit) {
        val input = context.layoutInflater.inflate(R.layout.item_edittext, null) as EditText
        input.setText(url)
        input.requestFocus();
        val mgr = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)

        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(context.getString(R.string.enter_url))
        alertDialog.setCancelable(false)

        alertDialog.setView(input)

        alertDialog.setPositiveButton(context.getString(R.string.apply), DialogInterface.OnClickListener { dialogInterface, i ->
            onOk(input.text.toString())
            dialogInterface.dismiss()
        })

        alertDialog.setNegativeButton(context.getString(R.string.cancel), DialogInterface.OnClickListener { dialogInterface, i ->
            onCancel()
            dialogInterface.cancel()
        })

        alertDialog.show()
    }
}