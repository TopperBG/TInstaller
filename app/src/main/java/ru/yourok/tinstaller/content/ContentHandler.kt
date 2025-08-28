package ru.yourok.tinstaller.content

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import com.google.gson.Gson
import ru.yourok.tinstaller.R
import ru.yourok.tinstaller.app.App
import ru.yourok.tinstaller.utils.Client
import ru.yourok.tinstaller.utils.Prefs
import kotlin.concurrent.thread

object ContentHandler {
    private var content: Content? = null
    private var isLoad = false
    private val lock = Any()
    private val load = Any()

    fun clearContent() {
        synchronized(load) {
            content = null
        }
    }

    fun Activity.loadContent(onEnd: (content: Content) -> Unit) {
        if (content == null && Prefs.getURL().isNotBlank()) {
            synchronized(lock) {
                if (isLoad)
                    return
                isLoad = true
            }
            findViewById<FrameLayout>(R.id.flProgress)?.visibility = View.VISIBLE
            thread {
                synchronized(load) {
                    try {
                        val jsonStr = Client()
                            .host(Prefs.getURL())
                            .connect()
                            .getBody()

                        content = Gson().fromJson(jsonStr, Content::class.java)
                        content?.apps?.forEach {
                            it.progress = -1
                        }
                    } catch (e: Exception) {
                        App.Toast(R.string.error_retrieve_data)
                    }
                    isLoad = false
                    content?.let {
                        Handler(Looper.getMainLooper()).post { onEnd(it) }
                    }
                    Handler(Looper.getMainLooper()).post {
                        findViewById<FrameLayout>(R.id.flProgress)?.visibility = View.GONE
                    }
                }
            }
        } else
            content?.let {
                Handler(Looper.getMainLooper()).post { onEnd(it) }
            }
    }
}