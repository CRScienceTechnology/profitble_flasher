# Profitble_Flasher安卓嵌入式烧录工具
以往对嵌入式设备开发调试时一定得用上电脑，不便携且不灵活十分麻烦故提出此项目

工作流程：
前端（app）编辑源码，上传至后端  

后端解析前端HTTP发送请求，并且解析发送的源代码文件。源码云端交叉编译后将编译好的结果回传至前端  

前端接收到编译的hex或者bin文件保存至本地  

在点击烧录按钮后，将编译好的响应文件通过串口配合usb ttl下载器下载烧录至目标单片机的eeprom中  

前端待实现的内容（按时间顺序）
--
- [x] 实现打开文件到文本编辑框/波特率选择/文件保存

- [x] 实现HTTP发送读取到文件（JSON格式）

- [ ] app点击编译下载-->保存接收到的hex文件，同时弹出toast提示保存路径

- [ ] app能记录上次加载的c程序标签页

- [ ] 实现串口监视

后端待实现的内容（按时间顺序）
--
- [x] 前端发送HTTP请求-->解析文件

- [x] 实现交叉编译c文件

- [x] 实现转录ihx文件到hex文件

- [x] 实现回传hex文件

参考项目以及教程
--
- https://www.cnblogs.com/milton/p/14994533.html  

- http://www.51hei.com/bbs/forum.php?mod=viewthread&tid=190291&extra=page%3D1&page=1&  

- https://github.com/WallBreakerX/MCUFirmwareWriter

使用的开源库
--
- [usb-serial-for-android](https://github.com/mik3y/usb-serial-for-android)
- [okhttp](https://github.com/square/okhttp)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [Android Jetpack](https://github.com/androidx/androidx)
