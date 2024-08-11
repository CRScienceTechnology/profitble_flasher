package crst.flasher.android

import android.content.Context.USB_SERVICE
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hoho.android.usbserial.driver.UsbSerialProber
import crst.flasher.android.ui.screen.EditScreen
import crst.flasher.android.ui.screen.FlashScreen
import crst.flasher.android.ui.theme.FlasherTheme
import crst.flasher.android.util.writeText


class MainActivity : ComponentActivity() {

    // 绝对参考对象是整块屏幕的物理区域划分，相对参考对象类比两个虚拟UI布局类，两者存在仿射变换
    // 创建Screen枚举类，两个成员意味着整个屏幕界面的布局存在两套，分别为Edit布局和Flash布局
    // 点击事件触发时首先得判定这属于哪一个界面布局，再去分析当前布局上被触发的UI函数
    // 每一个UI部分可能是两套界面共用的布局，也有可能这个区域在两个布局上显示的元素不一样
    enum class Screen(val route: String) {
        //        此类声明了应用中的所有页面，以及导航到这些页面需要用到的路由字符串
        Edit("edit"),   //代码修改页面
        Flash("flash")  //编译烧录页面
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainActivity = LocalContext.current as MainActivity
            val viewModel = viewModel<MainActivityViewModel>()
            val uiState by MainActivityViewModel.uiState.collectAsState()
            val navController = rememberNavController()
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    if (result.data?.data == null) {
                        Toast.makeText(mainActivity, "未选择文件", Toast.LENGTH_SHORT)
                            .show()
                        return@rememberLauncherForActivityResult
                    } else {                                     // 类似于网页开发一样，UI组件会具有一些属性.data就是访问result的uri
//                        注：此处代码与ui组件无关，result是由系统saf返回的结果信息
//                        result.data得到的是带有结果信息的intent。Intent是用于安卓系统中不同的四大组件之间交互或传输数据的类
//                        对此intent对象再次访问data属性得到的是一个Uri对象，在此处这个Uri表示系统saf返回的选中文档的uri。因为使用的是saf，所以不能使用绝对路径读写文件，而必须使用Uri
                        result.data?.data?.let { uri ->   // 代码作用 https://kimi.moonshot.cn/share/cqnpnucubms1eeb0k8h0
                            viewModel.addFile(uri)
                            /*
                                                val contentResolver =
                                                    mainActivity.contentResolver  // 获取内容解析器, 用于读取文件 原因 https://kimi.moonshot.cn/share/cqnq2g69e5jhdcr4hui0
                                                contentResolver.openInputStream(uri)?.bufferedReader()
                                                    ?.use { viewModel.setCode(it.readText()) }
                                                try {
                                                    // 打开输入流
                                                    val inputStream = contentResolver.openInputStream(uri)
                                                    // 创建对象暂存读取的文本
                                                    val reader = inputStream?.let { stream ->
                                                        BufferedReader(InputStreamReader(stream))
                                                    }
                                                    reader?.use { it ->
                                                        // 读取所有内容并转换为字符串
                                                        val content = it.readText()
                                                        // 打印内容到日志
                                                        Log.d("这是读取到的文本", content)
                                                        // 传出字符串变量给MainActivityViewModel的uploadSourceCode()函数作用域内
                                                        content.let {
                                                            viewModel.uploadSourceCode(content)
                                                        } // 这属于调用viewModel的uploadSourceCode()函数并且传参了
                                                    }
                                                } catch (e: IOException) {
                                                    // 处理可能发生的 IOException
                                                    e.printStackTrace()
                                                }
                            */
                        }
                    }
                }
            }
            // 新知识: UI函数层级结构以及判定触摸唤起的UI事件
            // 新知识：UI函数的组成以及UI事件响应函数的写法——UI函数大体由UI配置和事件响应函数组成，UI配置包括图标配置，位置配置，布局配置..

            // 过程分析:
            // --创建屏幕枚举类——划分整体屏幕布局为顶部中间底部三个部分
            // --扫描当前显示UI的点击位置
            // --分析当前点中位置是屏幕的top 还是bottom，在分析是edit的top还是flash的top或者是edit的bottom还是flash的bottom
            // --点击事件流转到相应的枚举成员的UI处理事件中取处理，如页面状态更新，执行相应UI响应事件
            // --单独更新顶部或者底部UI布局并执行被点击的子元素响应事件，这样避免全局更新UI导致的额外耗电


            // 创建一个导航控制器，拥有子元素Scaffold，子元素为全局元素，点击它时候要分析点击位置还有执行点击UI响应
            // 全局布局作用在edit和flash两个界面上，意味着两个界面都有顶部和底部导航栏属性
            // action事件中写入UI的话，UI此时就会具有交互属性，如果你知识在和centerAlignedTopAppBar()齐平
            // 的位置写入UI，它此时就会造成原来的上下布局变成新的布局
            // 先是父元素同样位置响应点击在跳到父元素相应子UI元素的点击响应事件中

            FlasherTheme {  // 为应用界面应用全局主题，例如提供动态取色和深色模式的支持
                Scaffold(   // Scaffold组件就是一个空容器，不过其中预先定义了顶栏、底栏、悬浮按钮和SnackBar的锚定位置，免去了自行对齐这些组件的位置的琐事
                    modifier = Modifier.fillMaxSize(),   //scaffold组件配置，配置为全屏铺满
                    // 顶部导航栏
                    topBar = {                           // 由UI配置和actions响应事件构成topbar类型
                        CenterAlignedTopAppBar(
                            title = { Text(text = stringResource(R.string.app_name)) },
//        actions参数中定义的是顶部导航栏右侧的那些操作按钮
                            actions = {                         // 因为顶部布局有两套所以被点击时得执行分析动作
//            不同页面中的额外操作不同，所以此处判断当前页面以展示不同的操作按钮
                                when (uiState.currentScreen) {  // 检测全局uiState.currentScreen 的值，若为Screen.edit
                                    Screen.Edit -> {            // 则意味着当前页面为Edit页面
//                    注：此处没有点击事件。Compose是声明式ui框架，在ui元素相关的状态发生改变时会自动进行重构（即@Composable可组合函数会被重新调用）
//                    此处的预期是：任意位置发生页面导航切换页面时应当同时更新uiState.currentScreen的值，此处的顶栏组件会跟随状态变化而重构
//                    顶栏会自行观察uiState中的状态变化来更新页面，而不是由点击事件主动更改页面（后者这种是命令式ui框架）
                                        if (uiState.selectedFileIndex != -1) // 检测是否有被选中的文件，有则显示保存文件按钮
                                        {
                                            IconButton(onClick = {           // 点击保存图标执行的功能函数
                                                uiState.files[uiState.selectedFileIndex].writeText(
                                                    uiState.code             // 将调用Flashscreen定义的writetext方法将uiState.code写入文件
                                                )
                                                Toast.makeText(              // 准备toast提示，准备后要用.show方法要不然不弹出的
                                                    BaseApplication.context, // 使用的上下文。Toast可以使用活动级上下文或应用级上下文（不能是其他应用的上下文），此处为后者
                                                    "保存成功",           // 填入提示信息
                                                    Toast.LENGTH_SHORT        // 弹出时间
                                                ).show()                      // 执行弹出动作
                                            }) {                             // 图标配置
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_save_24),
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        IconButton(onClick = {               //
                                            Toast.makeText(
                                                BaseApplication.context,
                                                "选择一个文件",
                                                Toast.LENGTH_SHORT
                                            ) // 弹出Toast提示
                                                .show()
                                            val intent =
                                                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {                   // 创建INTENT对象并设置action类型
                                                    type =
                                                        "*/*"                                                   // 设置MIME类型，这里是任意类型
                                                }

                                            launcher.launch(intent)                                             // 使用launcher启动此意图，回调逻辑在launcher中
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    // 若点中的是screen.flash，则响应事件处理逻辑流转到screen.flash的顶部ui函数
                                    Screen.Flash -> {
                                        IconButton(onClick = {
                                            viewModel.setExpandOptionMenu(true) // 连接两个kt文件的关键
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = null
                                            )
                                        }
                                        // 一级下拉菜单
                                        DropdownMenu(
                                            expanded = uiState.expandOptionMenu,
                                            onDismissRequest = {
                                                viewModel.setExpandOptionMenu(false)
                                            }
                                        ) {
                                            Text(text = "配置", modifier = Modifier.padding(8.dp))
                                            TextField(
                                                value = uiState.baudRate,
                                                onValueChange = { viewModel.setBaudRate(it) },
                                                label = {
                                                    Text(text = "波特率")
                                                }
                                            )
                                            //二级下拉菜单
                                            DropdownMenuItem(
                                                text = {
                                                    PortSelectRow(
                                                        uiState = uiState,
                                                        viewModel = viewModel
                                                    )
                                                },
                                                onClick = { }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    },
                    // 新知识：页面跳转以及页面画面更新
                    // 整体底部导航栏布局，没有when方法因为两个界面的底部UI一致,因此不需要判断点击发生是当前所处的页面
                    bottomBar = {
                        NavigationBar { // 导航栏父元素，因为底部元素就一套，所以分析所处的UI布局意义不大

                            NavigationBarItem(
                                selected = uiState.currentScreen == Screen.Edit,
                                onClick = {                                   // 实现页面跳转更新
                                    navController.navigate(Screen.Edit.route) {
                                        launchSingleTop = true
                                        popUpTo(Screen.Edit.route)
                                    }
                                    viewModel.setCurrentScreen(Screen.Edit)
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(text = "代码修改") }
                            )
                            NavigationBarItem(
                                selected = uiState.currentScreen == Screen.Flash,
                                onClick = {
                                    navController.navigate(Screen.Flash.route) {
                                        launchSingleTop = true
                                        popUpTo(Screen.Flash.route)
                                    }
                                    viewModel.setCurrentScreen(Screen.Flash)
                                },
                                icon = {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_developer_board_24),
                                        contentDescription = null
                                    )
                                },
                                label = { Text(text = "编译烧录") }
                            )
                        }
                    }
                ) { innerPadding -> // innerPadding对象指定的间距会根据scaffold中对顶栏底栏等的配置，自动填充间距。在内部组件中应用innerPadding可以避免内部组件被顶栏底栏等遮挡
                    NavHost(    // NavHost是导航容器，用于管理导航图，展示要显示的页面
                        navController = navController,  // 此NavHost的导航控制器
                        startDestination = Screen.Flash.route,  // NavHost的起始页面路由，此处配置为编译烧录页面
                        modifier = Modifier.padding(innerPadding)   // 应用Scaffold提供的间距偏移量，避免内容被顶栏底栏遮挡
                    ) { // 在lambda表达式内构建导航图，定义NavHost所有可能的目的页面
                        composable(Screen.Flash.route) { FlashScreen(uiState, viewModel) }
                        composable(Screen.Edit.route) { EditScreen(uiState, viewModel, launcher) }
                    }
                }
            }
        }
    }

}

@Composable
private fun PortSelectRow(uiState: MainActivityUIState, viewModel: MainActivityViewModel) {
    val usbManager =
        BaseApplication.context.getSystemService(USB_SERVICE) as UsbManager
    // 定义了一行布局，包含两个子元素
    Row(
        modifier = Modifier.fillMaxWidth(),                // 表示这个 Row 的宽度将填充其父容器的整个宽度
        horizontalArrangement = Arrangement.SpaceBetween,  // 设置了子元素在水平方向上的分布方式为两端对齐
        verticalAlignment = Alignment.CenterVertically     //确保子元素垂直居中。
    ) {
        Text(text = uiState.selectedDevice)                                       // text组件
        IconButton(onClick = { viewModel.setExpandPortSelectDropdownMenu(true) }) // IconButton组件，点击触发事件
        {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,                    // ICON图标设置
                contentDescription = null                                         // 无障碍描述
            )
            DropdownMenu(
                expanded = uiState.expandPortSelectDropdownMenu,                       // 是否展开下拉菜单，取决于uiState
                onDismissRequest = { viewModel.setExpandPortSelectDropdownMenu(false) } // 点击下拉菜单意外的地方触发回调
            ) {                                                                        // 组件子内容
                val availableDrivers =
                    UsbSerialProber.getDefaultProber()              // 获取所有可用USB驱动器
                        .findAllDrivers(usbManager)

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "选择端口"
                )
                availableDrivers.forEach { driver ->
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(text = driver.ports.toString()) },
                        onClick = {
                            // 请求选中的usb设备的权限
                            /*usbManager.requestPermission(
                                it.value,
                                PendingIntent.getBroadcast( mainActivity, 0,  Intent(), PendingIntent.FLAG_IMMUTABLE  )
                            )*/
                            viewModel.selectDevice(driver.ports.toString())
                        }
                    )
                }
            }
        }
    }
}