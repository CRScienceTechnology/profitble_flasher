# Profitble_Flasher安卓嵌入式烧录工具
以往对嵌入式设备开发调试时一定得用上电脑，不便携且不灵活十分麻烦故提出此项目

工作流程：
前端（app）编辑源码，上传至后端  

后端解析前端HTTP发送请求，并且解析发送的源代码文件。源码云端交叉编译后将编译好的结果回传至前端  

前端接收到编译的hex或者bin文件保存至本地  

在点击烧录按钮后，将编译好的响应文件通过串口配合usb ttl下载器下载烧录至目标单片机的eeprom中

后端待实现的内容
--
// 实现接收解析单个main.c文件  X

// 实现交叉编译c文件  X

// 实现转录ihx文件到hex文件 X 

// 实现回传hex文件  X

参考项目以及教程
--
https://www.cnblogs.com/milton/p/14994533.html  

http://www.51hei.com/bbs/forum.php?mod=viewthread&tid=190291&extra=page%3D1&page=1&  

https://github.com/WallBreakerX/MCUFirmwareWriter
