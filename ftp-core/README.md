# ftp-core

纯 JVM FTP 协议引擎，基于 Ktor 非阻塞 Socket 实现。

## 技术栈

- **Kotlin 2.3** / JVM 1.8
- **Ktor 3.5**（ktor-network）

## 架构

```
FtpClient                       ← 自研 FTP 客户端
├── connect(host, port)         ← 建立控制连接
├── login(user, pass)           ← USER → PASS → TYPE I
├── listFiles(path)             ← PASV → LIST → Unix 解析
├── downloadFile(path, os)      ← PASV → RETR → 流拷贝
├── uploadFile(path, is)        ← PASV → STOR → 流拷贝
├── mkdir(path)                 ← MKD
├── rename(from, to)            ← RNFR → RNTO
├── deleteFile(path)            ← DELE
└── disconnect()                ← QUIT → 关闭 socket

FtpFile(name, isFolder, lastUpdateTime, size, fullPath)
FtpResponse(code, message)
```

## 设计特点

- **仅支持被动模式（PASV）**，不打开本地监听端口
- **仅支持 Unix/Linux 风格**文件列表解析（`ls -l` 格式）
- 所有操作为 `suspend` 函数，内部在 `Dispatchers.IO` 执行
- 控制连接和数据连接分离，数据连接用完即关（`use {}`）

## 依赖

| 库 | 用途 |
|---|---|
| `ktor-network` | 非阻塞 TCP Socket + ByteReadChannel/ByteWriteChannel |
| `kotlinx-coroutines` | 协程调度 |

