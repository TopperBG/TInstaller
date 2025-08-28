package ru.yourok.tinstaller.utils

import ru.yourok.tinstaller.content.App
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

object Download {

    fun download(res: App, file: File, onProgress: ((prc: Int) -> Unit)?) {
        if (file.exists())
            file.delete()

        var istr: InputStream? = null
        var conn: Http? = null
        try {
            conn = Http(res.url)
            conn.connect()
            conn.setTimeout(5000)
            istr = conn.getInputStream()
        } catch (e: Exception) {
            if (res.mirror == null)
                throw e
            conn = Http(res.mirror)
            conn.connect()
            conn.setTimeout(5000)
            istr = conn.getInputStream()
        }

        istr.use { input ->
            FileOutputStream(file).use { fileOut ->
                val contentLength = conn?.getSize() ?: return@use
                if (onProgress == null)
                    input?.copyTo(fileOut)
                else {
                    val buffer = ByteArray(65535)
                    val length = contentLength + 1
                    var offset: Long = 0
                    while (true) {
                        val readed = input?.read(buffer) ?: 0
                        offset += readed
                        val prc = (offset * 100 / length).toInt()
                        onProgress(prc)
                        if (readed <= 0)
                            break
                        fileOut.write(buffer, 0, readed)
                    }
                    fileOut.flush()
                }
                fileOut.flush()
                fileOut.close()
            }
        }
        conn?.close()
    }

    fun getFileNameFromURL(url: String): String {
        try {
            val resource = URL(url)
            val host: String = resource.host
            if (host.isNotEmpty() && url.endsWith(host)) {
                return ""
            }
        } catch (e: MalformedURLException) {
            return ""
        }
        val startIndex = url.lastIndexOf('/') + 1
        val length = url.length

        // find end index for ?
        var lastQMPos = url.lastIndexOf('?')
        if (lastQMPos == -1) {
            lastQMPos = length
        }

        // find end index for #
        var lastHashPos = url.lastIndexOf('#')
        if (lastHashPos == -1) {
            lastHashPos = length
        }

        // calculate the end index
        val endIndex = Math.min(lastQMPos, lastHashPos)
        return url.substring(startIndex, endIndex)
    }
}