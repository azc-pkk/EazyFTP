# app — Easy FTP

Android FTP 客户端应用，基于 MVVM + Clean Architecture。

## 技术栈

| 领域 | 技术 |
|---|---|
| UI | Jetpack Compose + Material 3 |
| DI | Dagger Hilt |
| 数据库 | Room |
| 导航 | Navigation Compose |
| 图片 | Coil |
| FTP 引擎 | `:ftp-core`（自研） |

## 模块结构

```
app/src/main/java/uestc/b3dman/ftpclient/
├── di/
│   └── AppModule.kt              ← Hilt 依赖注入配置
├── data/
│   ├── remote/
│   │   ├── FtpManager.kt         ← FTP 操作接口（策略模式）
│   │   ├── CustomFtpManager.kt   ← 基于 ftp-core 的实现
│   │   └── ApacheFtpManager.kt   ← 基于 Apache Commons Net 的实现
│   ├── repository/
│   │   └── FtpRepository.kt      ← 单一数据源，协调 remote + local
│   ├── local/
│   │   ├── AppDatabase.kt        ← Room 数据库
│   │   ├── FtpAccountDao.kt      ← 账号 DAO
│   │   ├── DownloadHistoryDao.kt ← 下载历史 DAO
│   │   ├── StorageManager.kt     ← 文件存储接口
│   │   └── StorageManagerImpl.kt ← 文件存储实现
│   └── model/
│       ├── FtpAccount.kt         ← 账号实体
│       ├── FtpFileItem.kt        ← 文件项实体
│       └── DownloadHistoryEntry.kt ← 下载历史实体
└── ui/
    └── screens/
        ├── login/                ← 账户管理 / 一键登录
        ├── addaccount/           ← 添加 / 编辑账号
        ├── browser/              ← 文件浏览、上传、下载、重命名、删除、搜索、新建文件夹
        └── history/              ← 下载历史、重试、打开文件
```

## 架构分层

```
View (Composable)  ──observes──►  ViewModel (StateFlow)
                                      │
                                      ▼
                                Repository (单数据源)
                                 │          │
                          FtpManager    Room DAO / StorageManager
                         (远程 FTP)      (本地持久化)
```

- **View**：纯 Compose，不包含业务逻辑
- **ViewModel**：仅持有 StateFlow 和启动协程，不切换线程、不持有 Context
- **Repository**：协调远程和本地数据源
- **FtpManager**：负责 `Dispatchers.IO` 调度，底层 `FtpClient` 执行协议

## 导航

| 路由 | 屏幕 | 说明 |
|---|---|---|
| `login` | LoginScreen | 起始页：账号列表、一键登录、切换/删除账号 |
| `add_account?accountId={id}` | AddAccountScreen | -1 新建，其他值编辑 |
| `browser?accountId={id}` | BrowserScreen | 文件浏览器：排序、搜索、上传、下载、新建文件夹、重命名、删除 |
| `history?accountId={id}` | HistoryScreen | 下载历史：状态图标、点击打开文件、重试下载 |

## 依赖注入

```
AppModule (SingletonComponent)
├── AppDatabase ──► FtpAccountDao     ──┐
│               ──► DownloadHistoryDao ─┤
│                                       ├──► FtpRepository
├── CustomFtpManager (FtpManager)   ────┤
└── StorageManagerImpl   ───────────────┘
```

## 权限

| 权限 | 用途 |
|---|---|
| `INTERNET` | FTP 网络通信 |
| FileProvider | 打开下载文件（`${applicationId}.fileprovider`） |

## 构建

```bash
./gradlew :app:assembleDebug
```

最低 SDK 24，目标 SDK 36。
