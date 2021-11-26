# MCar-Arudino-ESP01S
A Mecanum-wheel car based on ESP01S.

B站演示视频：https://www.bilibili.com/video/BV1oL4y1i7sb

![](/5.Img/img_1.jpg)

## 0. 关于本项目

因为看到up主@孟德尔の公主切的原理图中的TMI8549芯片只用一个引脚就可以控制一个轮，所以就突发奇想用正好只有四个GPIO的ESP01S来做一个麦轮小车，然后这个不到40元的项目就出来了



## 1.硬件打样说明

`Hardware`文件内有PCB板的AD源文件，可以直接发去打样，器件BOM有在表面的材料清单列出

因为ESP01S只有四个引脚所以8549这个芯片的使能脚我直接接的3.3V，然后通过PWM来控制正反转和停转，目前除了很吵之外没发现什么问题

焊接时可通过`ESP01S麦轮.html`来查看对应BOM表



## 2.固件编译说明

主要基于Arduino开发完成，根据下图的麦轮公式控制

![](/5.Img/麦轮公式.jpg)



麦克纳姆轮，简称麦轮，是一种全方位轮，因为轮缘上`斜向`分布着许多小滚子，所以可以横向移动，通过四个轮子的协同控制可以完成很多动作，下图是从顶部观看的辊子方向，所以合力参考可以想象底部辊子的方向是这样“<>”

![](/5.Img/麦轮控制.jpg)



## 3.Android端程序说明

程序在`Software`文件夹里，目前只是能用就行的状态

程序内容大概就是两个摇杆加UDP广播，广播到9999端口，数据包的内容是四个0-32的数字，如：16161616就是两个摇杆回到原点的坐标，如果两个摇杆回到了原点就发一次16161616之后不再发包直到下一次摇杆变化

只需要将手机和模块连接到同一WIFI下，然后模块监听9999端口即可

![](/5.Img/img_2.jpg)



## 4.麦轮制作说明

`Docs`文件夹里的压缩包里有B站up主@孟德尔の公主切开源的麦轮3D打印文件和装配说明，欢迎大家去给Ta的视频三连

https://www.bilibili.com/video/BV1wq4y1774g?spm_id_from=333.999.0.0



>  `Docs`里面的`辊子100个.STL`是为了达到三维猴的壁厚要求自己用Solidworks画的，究极废手，谨慎使用，因为掰下来另一头是堵的，不止扩孔还得钻孔（泪目）搞定四个轮子花了一个晚上，需要的人多的话我再重新画一个版本吧

![](/5.Img/img_3.jpg)



## 5.关于这个项目的问题

首先是遥控的延迟，估计是没使用多线程导致，后续研究下ESP8266的多线程



然后是麦轮横移时的漂移，开始的时候是完全横移不动的，然后我做了两个措施就有了现在视频里的效果

1、我用电工胶布粘在电机的外壳和触点之间防止短路，这样就可以把电机座的螺丝拧死了，电机得以更加固定

2、将辊子拆下来，两头用磨砂纸打磨，用钻头又扩了一次孔，辊子转的更加顺畅

后续再研究一下转速问题



## 其他的后续再补充，欢迎大家给视频点赞~

