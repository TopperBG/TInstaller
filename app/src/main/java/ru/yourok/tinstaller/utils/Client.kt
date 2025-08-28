package ru.yourok.tinstaller.utils

import java.nio.charset.Charset

class Client {
    private var url: String = ""
    private var mirror: String = ""
    private var http: Http? = null

    fun host(url: String): Client {
        this.url = url
        if (!url.startsWith("http")) {
            this.url = "http://$url"
            this.mirror = "https://$url"
        }
        return this
    }

    fun connect(): Client {
        try {
            http = Http(url)
            http?.connect()
        } catch (e: Exception) {
            if (mirror.isNotEmpty()) {
                http = Http(mirror)
                http?.connect()
            } else
                throw e
        }
        return this
    }

    fun getBody(): String {
        val body = http?.getInputStream()?.bufferedReader(Charset.defaultCharset())?.readText() ?: ""
        http?.close()
        return body
    }
}