package ru.yourok.tinstaller.content

data class Content(
    var apps: List<App>?,
    var links: List<Link>?,
//    var update: Update
)

data class App(
    val description: String?,
    val title: String?,
    val url: String,
    val mirror: String?,
    val category: String,
    val app_review: String?,

    //not in json
    var select: Boolean,
    var progress: Int,
)

data class Link(
    val description: String?,
    val title: String?,
    val url: String,
    val app_review: String?,
)
/*
data class Update(
    val description: String,
    val title: String,
    val url: String,
    val version: Int,
    val app_review: String?,
)

 */