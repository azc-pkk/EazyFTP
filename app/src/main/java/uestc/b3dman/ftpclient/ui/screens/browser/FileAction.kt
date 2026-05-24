package uestc.b3dman.ftpclient.ui.screens.browser

enum class FileAction {
    DOWNLOAD,
    RENAME,
    DELETE
}

enum class SortType(val displayName: String) {
    NAME("按名称"),
    SIZE("按大小"),
    TIME("按时间")
}
