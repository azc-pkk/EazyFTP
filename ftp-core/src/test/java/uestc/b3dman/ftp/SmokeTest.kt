package uestc.b3dman.ftp

suspend fun main() {
    val client = FtpClient()
    client.connect("192.168.2.241", 21)
    client.login("ftpuser", "password")
    val files = client.listFiles("/upload")
    for (file in files) {
        println("${file.name} ${file.size} ${file.lastUpdateTime} ${file.fullPath} ${file.isFolder}")
    }
    client.disconnect()
}