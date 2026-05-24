# Easy FTP

<p align="center">
  <strong>🚀 一款架构优雅、基于自研协议引擎的 Android FTP 客户端</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3.10-blueviolet?logo=kotlin" />
  <img src="https://img.shields.io/badge/Compose-Material%203-blue?logo=jetpackcompose" />
  <img src="https://img.shields.io/badge/Hilt-2.59.2-orange?logo=dagger" />
  <img src="https://img.shields.io/badge/Room-2.8.4-green?logo=android" />
  <img src="https://img.shields.io/badge/minSDK-24-brightgreen" />
  <img src="https://img.shields.io/badge/targetSDK-36-yellow" />
</p>

---

## ✨ 功能概览

| 模块 | 功能 |
|---|---|
| 🔐 账户管理 | 添加 / 编辑 / 删除 FTP 账户，支持头像，最近登录优先展示 |
| 📂 文件浏览 | Unix 风格文件列表、文件夹/文件区分、文件大小/修改时间 |
| ⬆️⬇️ 文件传输 | 上传（`STOR`）/ 下载（`RETR`），下载重名自动追加后缀 |
| 🔍 搜索过滤 | 当前目录客户端即时过滤 |
| 📊 排序 | 按名称 / 大小 / 时间排序，文件夹始终置顶 |
| 📝 远程操作 | 新建文件夹、重命名文件、删除文件 |
| 📋 下载历史 | 成功/失败状态追踪、点击打开文件 |

---

## 🧱 项目结构

```
FtpClient/
├── ftp-core/                 ← 纯 JVM FTP 协议引擎（自制，课程设计要求）
│   ├── FtpClient.kt          ← 基于 Ktor Socket 的 FTP 客户端
│   ├── FtpFile.kt            ← 文件实体
│   └── FtpResponse.kt        ← 响应实体
│
└── app/                      ← Android 应用层
    └── src/main/java/uestc/b3dman/ftpclient/
        ├── di/
        │   └── AppModule.kt           ← Hilt 依赖注入
        ├── data/
        │   ├── remote/
        │   │   ├── FtpManager.kt      ← FTP 操作接口
        │   │   ├── CustomFtpManager.kt ← 基于 ftp-core 实现
        │   │   └── ApacheFtpManager.kt ← 基于 Apache Commons Net 实现
        │   ├── repository/
        │   │   └── FtpRepository.kt   ← 单一数据源
        │   ├── local/
        │   │   ├── AppDatabase.kt     ← Room
        │   │   ├── FtpAccountDao.kt
        │   │   ├── DownloadHistoryDao.kt
        │   │   ├── StorageManager.kt  ← 本地文件操作之类的
        │   │   └── StorageManagerImpl.kt
        │   └── model/
        │       ├── FtpAccount.kt
        │       ├── FtpFileItem.kt
        │       └── DownloadHistoryEntry.kt
        └── ui/screens/
            ├── login/        ← 账户管理 / 一键登录
            ├── addaccount/   ← 添加 / 编辑账号
            ├── browser/      ← 文件浏览器
            └── history/      ← 下载历史
```

---

## 🏗 架构设计

```
┌─────────────────────────────────────────┐
│  View (Composable)                      │  纯 UI，观察 StateFlow
│  LoginScreen · BrowserScreen · ...      │
└──────────────┬──────────────────────────┘
               │ collectAsState()
┌──────────────▼──────────────────────────┐
│  ViewModel                              │  持有状态，启动协程
│  LoginViewModel · BrowserViewModel ...  │  不切换线程、不持有 Context
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Repository (FtpRepository)             │  单一数据源
│  协调 remote + local                     │
└──────┬──────────────────┬───────────────┘
       │                  │
┌──────▼──────┐   ┌──────▼──────┐
│  FtpManager │   │  Room DAO   │
│  (远程 FTP)  │   │ StorageMgr  │
│  (IO 线程)   │   │  (本地存储)  │
└──────┬──────┘   └─────────────┘
       │
┌──────▼──────┐
│  ftp-core   │
│  FtpClient  │   自制 Ktor Socket FTP 引擎
└─────────────┘
```

### 关键设计原则

- **MVVM**：View 只做渲染，ViewModel 只暴露 StateFlow，不持有 Context / Intent / 线程调度
- **单一数据源**：所有操作经由 `FtpRepository`，View 不直接调用数据层
- **层间依赖向下**：View → ViewModel → Repository → FtpManager/Room
- **自制引擎**：`ftp-core` 基于 Ktor 非阻塞 Socket，实现 FTP 协议栈的部分内容

---

## 🚀 快速开始

```bash
# 克隆项目
git clone https://github.com/azc-pkk/Ftp_Client.git
cd Ftp_Client

# 构建 Debug APK
./gradlew :app:assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

**环境要求**：Android Studio 2024+ · JDK 17+ · Android SDK 36

---

## 📦 技术栈

| 领域 | 技术 | 版本 |
|---|---|---|
| 语言 | Kotlin | 2.3.10 |
| UI | Jetpack Compose + Material 3 | BOM 2026.03.00 |
| 依赖注入 | Dagger Hilt | 2.59.2 |
| 数据库 | Room | 2.8.4 |
| 异步 | Kotlin Coroutines + StateFlow | 1.11.0 |
| 导航 | Navigation Compose | 2.9.7 |
| 图片加载 | Coil | 2.6.0 |
| 网络 | Ktor | 3.5.0 |
| 脱糖 | desugar_jdk_libs | 2.1.5 |
| 构建 | AGP + KSP | 9.1.0 / 2.3.4 |

---

## 📄 许可证

MIT License
