

# MCar-Arudino-ESP01S
A Mecanum-wheel car based on ESP01S.

Demo video on Bilibili: https://www.bilibili.com/video/BV1oL4y1i7sb

Tutorial video: https://www.bilibili.com/video/BV1KR4y1b7LR

![](/5.Img/img_1.jpg)

## 0. About this Project

After seeing that the TMI8549 chip in the schematic by Bilibili creator @孟德尔の公主切 can control a wheel with just one pin, I got the idea to use an ESP01S, which happens to have exactly four GPIOs, to build a Mecanum wheel car. Thus, this project costing under 40 RMB came to life.



## 1. Hardware Prototyping Instructions

The `Hardware` folder contains the AD (Altium Designer) source files for the PCB, which can be sent directly for prototyping. The component BOM is listed on the top layer material list.

Since the ESP01S only has four pins, I directly connected the enable pin of the 8549 chip to 3.3V, and used PWM to control forward/reverse rotation and stopping. So far, I haven't found any issues other than it being quite noisy.

During soldering, you can refer to `ESP01S麦轮.html` to check the corresponding BOM table.



## 2. Firmware Compilation Instructions

Primarily developed based on Arduino, controlled according to the Mecanum wheel formulas shown below.

![](/5.Img/麦轮公式.jpg)



Mecanum wheels, often called Mecanums, are omnidirectional wheels. Because the rim features many small rollers arranged `diagonally`, the wheel can move laterally. By coordinating the four wheels, various maneuvers can be achieved. The image below shows the roller direction from a top-down view. For reference on the resultant force, you can imagine the direction of the rollers on the bottom side as “<>”.

![](/5.Img/麦轮控制.jpg)



## 3. Android Application Instructions

The app is in the `Software` folder. Currently, it's in a "just works" state.

The app essentially features two joysticks combined with UDP broadcasting on port 9999. The packet content consists of four numbers ranging from 0 to 32. For example, `16161616` represents the coordinates when both joysticks are centered. If both joysticks return to the center, `16161616` is sent once, and no further packets are sent until the joysticks move again.

Simply connect both your phone and the module to the same Wi-Fi network, and have the module listen on port 9999.

![](/5.Img/img_2.jpg)



## 4. Mecanum Wheel Manufacturing Instructions

The archive in the `Docs` folder contains the open-source 3D printing files and assembly instructions for the Mecanum wheels by Bilibili creator @孟德尔の公主切. Everyone is welcome to like, coin, and favorite their video!

https://www.bilibili.com/video/BV1wq4y1774g



>  `Docs`里面的`辊子100个.STL` was manually drawn in SolidWorks to meet the wall thickness requirements of 3D Monkey (a 3D printing service). It's extremely tedious to assemble manually, so use with caution. After snapping them off, one end is blocked, meaning you not only need to ream the holes but also drill them (tears). It took me a whole night to finish four wheels. If there's high demand, I'll redraw a new version.

![](/5.Img/img_3.jpg)



## 5. Issues with this Project

First, the remote control latency (Fixed)

> Thanks to @27岁的188 on Bilibili for pointing this out. The issue was caused by improper parameters passed to the `map()` function, which triggered a chip reset. Yes, it literally reset. Filtering out the erroneous values resolves the issue. The open-source code has been corrected.



Next, the drifting issue during lateral movement. Initially, it wouldn't move laterally at all. I took two measures to achieve the effect shown in the current video:

1. I used electrical tape between the motor casing and the contacts to prevent short circuits. This allowed me to tighten the screws on the motor mount, securing the motor more firmly.

2. I removed the rollers, sanded both ends with sandpaper, and reamed the holes again with a drill bit, allowing the rollers to spin much more smoothly.

Overall, this is a drawback of homemade Mecanum wheels. The only workaround is to increase friction. Whether by increasing the total vehicle weight (downward force) or increasing the friction coefficient of the wheel surface, there will be some improvement.


> ---

This project also has some issues that are beyond my current abilities or time constraints. I've listed them here hoping that experts in the relevant fields can provide some insights:

1. First, the significant motor noise when stopped.

This is because the ESP01S `has no spare pins to control the TMI8549 EN pin`, so I had to tie it high. This issue seems to also cause higher current draw, quickly draining the battery, so it's worth noting.

Normally, the EN pin should be pulled low when stopping. Without changing the main controller, the directions I've been overthinking are:

**Hardware:** I haven't formally studied digital or analog electronics, so I can only think of using a transistor circuit for control switching to multiplex pins. However, since there are no pins left to control the transistor itself, I currently have no hardware solutions.

**MCU:** The difference between ESP01S and ESP01 is that the ESP01S doesn't require the EN pin to be pulled high during startup. I'm wondering if the EN pin can be utilized under certain conditions, such as in sleep mode. Gaining an extra pin would directly solve the problem.

Anyway, I'm just overcomplicating it. I'd also like to learn what solutions exist for scenarios with limited pins.

2. Motor speed cannot be adjusted according to the joystick pull distance.

Due to the nature of Mecanum wheels, each wheel requires a different speed. There's a `localMax` value in the code used to amplify the speed.

Since a certain amount of torque is needed to get the motor running, pulling the joystick slightly without amplification might not move the car. The original author likely considered this and added amplification, scaling up the speeds proportionally so that one wheel reaches maximum speed.

There are two issues: one is that the joystick doesn't vary speed according to how far it's pulled; the other is that wheels with lower speed ratios might not generate enough torque to move, causing the actual movement direction to deviate from the desired direction.

The solution is to set a minimum torque threshold and map the speed between this minimum and maximum value. However, I currently don't have the need for these features, so I'm just leaving the solution idea here for now.

3. Insufficient friction on the Mecanum wheels. You can solve this by buying four commercial Mecanum wheels on Taobao for 8.5 RMB each. However, I designed a coupling based on the Mecanum manual for this motor, and it breaks very easily. It's not the printed part that breaks, but the motor itself. So, if using commercial Mecanum wheels, the motor should probably be replaced.

Here's the motor for commercial Mecanum wheels. I bought the 48mm version without an interface. The coupling I designed is in the `Docs` folder.

https://item.taobao.com/item.htm?spm=a1z09.2.0.0.1ef12e8dYhUjfD&id=597783667844&_u=g389riah1755


## More details will be added later. Everyone is welcome to like the video~
