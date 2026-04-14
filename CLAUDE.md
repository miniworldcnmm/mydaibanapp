# 我的待办 - 手绘风格待办APP
## 项目介绍
这是一个简单易用的安卓待办APP，采用手绘风格的界面设计，支持任务的添加、编辑、删除、标记完成、筛选查看等功能，专为安卓开发小白打造，代码结构清晰易懂。

## 技术栈
- 语言：纯Java，无Kotlin依赖，兼容性更好
- 架构：MVVM (Model-View-ViewModel) 分层架构，代码清晰易维护
- 本地存储：Room数据库，数据持久化不会丢失
- 异步处理：ExecutorService后台线程执行数据库操作，不阻塞UI
- UI框架：Material Design 3 + View Binding，避免findViewById
- 构建工具：Gradle KTS

## 项目架构
```
com.example.mydaibanapp
├── data/          # 数据层
│   ├── Task.java       # 任务实体类
│   ├── TaskDao.java    # 数据库操作接口
│   └── AppDatabase.java # Room数据库配置
├── repository/    # 数据仓库层，统一管理数据源
│   └── TaskRepository.java
├── viewmodel/     # ViewModel层，处理业务逻辑，持有UI数据
│   └── TaskViewModel.java
├── adapter/       # 列表适配器
│   └── TaskAdapter.java
└── MainActivity.java # 主界面
```

## 功能列表
✅ 添加新任务（支持标题和可选描述，空标题有错误提示）
✅ 编辑现有任务
✅ 删除任务（带确认对话框，防止误删）
✅ 标记任务为完成/未完成（完成后显示删除线效果）
✅ 筛选查看任务：全部/进行中/已完成，右上角菜单切换
✅ 数据本地持久化，重启APP不会丢失任何任务
✅ 手绘风格界面设计：浅黄背景、无蓝色横条的透明Toolbar、虚线边框列表项、蓝色主色调标题文字，美观清爽

## 项目配置
- 最低SDK版本：29（Android 10）
- 目标SDK版本：36（Android 14）
- 编译SDK版本：36
- Java版本：11

## 已解决的问题记录
1. Kotlin版本冲突：重构为纯Java版本，移除所有Kotlin相关依赖和代码，避免版本不兼容问题
2. 资源链接错误：移除布局中不存在的自定义样式引用
3. 启动闪退：修改主题为NoActionBar，避免和自定义Toolbar冲突
4. 列表复用问题：修改CheckBox绑定逻辑，先清空监听再设置状态，避免复用时回调错误
5. 对话框关闭问题：手动接管对话框确认按钮点击事件，输入校验失败时不关闭对话框，显示错误提示
6. 数据不一致问题：新增getAllTasks统一数据流，筛选直接从全量数据过滤，避免多数据流合并时的瞬时不一致
7. 状态栏重叠问题：根布局添加fitsSystemWindows="true"，让系统自动为状态栏留出空间
8. FAB按钮白色圆圈：系统自带ic_menu_add图标有白色圆圈背景，替换为自定义纯净矢量图标ic_add.xml，并设置borderWidth="0dp"
9. Toolbar蓝色横条：Toolbar和AppBarLayout背景改为页面背景色，elevation设为0去掉阴影，视觉上融入页面
10. Stream#toList()兼容性：toList()需要API 34，minSDK 29的项目必须用collect(Collectors.toList())替代
11. Task缺少equals实现：DiffUtil的areContentsTheSame依赖equals()，实体类必须实现equals()和hashCode()

## 开发规范
- 遵循MVVM架构分层，各层职责明确，低耦合高内聚
- 使用View Binding替代findViewById，类型安全空安全
- 数据库操作全部在后台线程执行，不阻塞UI主线程
- 界面交互友好，操作有反馈，危险操作有二次确认
- 自定义图标使用矢量drawable（res/drawable/），不依赖系统图标避免样式不可控
- Toolbar如需隐藏视觉效果，用背景色融入+elevation=0dp的方式，保留功能（菜单等）
- 注意API兼容性：minSDK 29，避免使用高版本API（如Stream#toList()需API 34），可用`JAVA_HOME="D:/android/jbr" ./gradlew lint`检查
- Room实体类必须实现equals()和hashCode()，否则DiffUtil无法正确比较内容变化
