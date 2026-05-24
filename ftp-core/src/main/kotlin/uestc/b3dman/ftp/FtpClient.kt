package uestc.b3dman.ftp
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.jvm.javaio.copyTo
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import io.ktor.utils.io.readLine
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.logging.Logger

/**
 * My custom FTP client
 *
 * Only support passive mode
 *
 * Only support Unix/Linux style response while listing files
 */
class FtpClient {
    private val logger = Logger.getLogger("FtpClient")

    private val selectorManager = ActorSelectorManager(Dispatchers.IO)

    private var controlSocket: Socket? = null
    private var controlReader: ByteReadChannel? = null
    private var controlWriter: ByteWriteChannel? = null

    var isConnected = false

    suspend fun connect(host: String, port: Int) = withContext(Dispatchers.IO) {
        controlSocket = aSocket(selectorManager)
            .tcp()
            .connect(host, port)
        isConnected = true
        controlReader = controlSocket!!.openReadChannel()
        controlWriter = controlSocket!!.openWriteChannel(autoFlush = true)
        logger.info(receiveResponse().message)
    }

    suspend fun login(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected) return@withContext false

        sendCmd("USER $username")
        var response = receiveResponse()
        logger.info(response.message)

        if (response.code == 331) {
            sendCmd("PASS $password")
            response = receiveResponse()
            logger.info(response.message)
        }
        if (response.code == 230) {
            sendCmd("TYPE I")
            logger.info(receiveResponse().message)
        }
        return@withContext response.code == 230
    }

    suspend fun listFiles(path: String): List<FtpFile> = withContext(Dispatchers.IO) {
        val dataSocket = openDataConnection()

        sendCmd("LIST $path")
        val response = receiveResponse()

        val result = mutableListOf<FtpFile>()
        val lines = mutableListOf<String>()
        if (response.code == 125 || response.code == 150) {
            dataSocket?.use { socket ->
                val dataReader = socket.openReadChannel()
                while (true) {
                    val line = dataReader.readLine() ?: break
                    lines.add(line)
                }
            }
        }

        logger.info(lines.joinToString("\n"))

        logger.info(receiveResponse().message)

        for (line in lines) {
            parseUnixFtpFileEntry(line, path)?.let { result.add(it) }
        }

        return@withContext result
    }

    suspend fun downloadFile(remotePath: String, outputStream: OutputStream): Boolean =
        withContext(Dispatchers.IO) {
            if (!isConnected) return@withContext false

            val dataSocket = openDataConnection() ?: return@withContext false

            try {
                sendCmd("RETR $remotePath")
                val response = receiveResponse()

                if (response.code != 125 && response.code != 150) {
                    return@withContext false
                }

                dataSocket.use { socket ->
                    val readChannel = socket.openReadChannel()
                    readChannel.copyTo(outputStream)
                }
                true
            } catch (e: Exception) {
                false
            } finally {
                outputStream.close()
                logger.info(receiveResponse().message)
            }
        }

    suspend fun uploadFile(remotePath: String, inputStream: InputStream): Boolean =
        withContext(Dispatchers.IO) {
            if (!isConnected) return@withContext false

            val dataSocket = openDataConnection() ?: return@withContext false

            try {
                sendCmd("STOR $remotePath")
                val response = receiveResponse()

                if (response.code != 125 && response.code != 150) {
                    return@withContext false
                }

                val writeChannel = dataSocket.openWriteChannel()
                val fileReadChannel = inputStream.toByteReadChannel(context = Dispatchers.IO)
                fileReadChannel.copyTo(writeChannel)
                writeChannel.flushAndClose()

                val finalResponse = receiveResponse()
                logger.info(finalResponse.message)
                return@withContext finalResponse.code == 226
            } catch (e: Exception) {
                false
            } finally {
                inputStream.close()
            }
        }

    suspend fun openDataConnection(): Socket? = withContext(Dispatchers.IO) {
        if (!isConnected) return@withContext null

        sendCmd("PASV")
        val response = receiveResponse()
        logger.info(response.message)
        if (response.code != 227) return@withContext null

        val regex = """(\d+),(\d+),(\d+),(\d+),(\d+),(\d+)""".toRegex()
        val match = regex.find(response.message) ?: return@withContext null
        val values = match.destructured.toList().map { it.toInt() }

        val ip = values.subList(0, 4).joinToString(".")
        val port = values[4] * 256 + values[5]

        logger.info("Opening data connection to $ip:$port")

        return@withContext aSocket(selectorManager).tcp().connect(ip, port)
    }

    suspend fun rename(fromPath: String, toPath: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected) return@withContext false

        sendCmd("RNFR $fromPath")
        var response = receiveResponse()
        logger.info(response.message)
        if (response.code != 350) return@withContext false

        sendCmd("RNTO $toPath")
        response = receiveResponse()
        logger.info(response.message)
        return@withContext response.code == 250
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        if (!isConnected) return@withContext

        sendCmd("QUIT")
        logger.info(receiveResponse().message)

        controlWriter?.flushAndClose()
        controlSocket?.close()

        controlSocket = null
        controlReader = null
        controlWriter = null
        isConnected = false
    }

    private suspend fun sendCmd(cmd: String) {
        logger.info("-> $cmd")
        controlWriter?.writeStringUtf8("$cmd\r\n")
    }

    private suspend fun receiveResponse(): FtpResponse {
        val lines = mutableListOf<String>()
        while (true) {
            val line = controlReader?.readLine() ?: break
            lines.add(line)
            logger.info(line)

            if (line.length >= 4 && line[3] == ' ') {
                break
            }
        }
        val code = lines.lastOrNull()?.substring(0, 3)?.toIntOrNull() ?: -1
        return FtpResponse(code, lines.joinToString("\n"))
    }

    private fun parseUnixFtpFileEntry(line: String, parentPath: String): FtpFile? {
        val regex = """^([bcdlp-][rwx-]{9})\s+\d+\s+\S+\s+\S+\s+(\d+)\s+(\w{3})\s+(\d{1,2})\s+([0-9:]+)\s+(.*)$""".toRegex()
        val match = regex.find(line)

        if (match == null) {
            logger.warning("Failed to parse line: $line")
            return null
        }

        val groups = match.groupValues

        val permissions = groups[1]
        val size = groups[2].toLong()
        val month = groups[3]
        val day = groups[4]
        val timeOrYear = groups[5]
        val name = groups[6]

        if (name == "." || name == "..") return null
        val isFolder = permissions.startsWith("d")

        return FtpFile(
            name = name,
            isFolder = isFolder,
            lastUpdateTime = parseUnixDate(month, day, timeOrYear),
            size = size,
            fullPath = if (parentPath.endsWith("/")) parentPath + name else parentPath + File.separator + name,
        )
    }

    private fun parseUnixDate(month: String, day: String, timeOrYear: String): Long {
        if (timeOrYear.contains(":")) {
            val now = java.time.LocalDateTime.now()
            val formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy MMM d HH:mm")
                .withLocale(java.util.Locale.ENGLISH)
            val dateTime = java.time.LocalDateTime.parse("${now.year} $month $day $timeOrYear", formatter)
            if (dateTime.isAfter(now)) {
                dateTime.minusYears(1)
            }
            return dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
            val formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy MMM d")
                .withLocale(java.util.Locale.ENGLISH)
            val dateTime = java.time.LocalDate.parse("$timeOrYear $month $day", formatter)
            return dateTime.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }
}