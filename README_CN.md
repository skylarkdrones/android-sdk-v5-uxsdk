# DJI Mobile SDK for Android V5 最新Alpha版本 5.10.0-a3

[English Version](README.md)

## Alpha版本声明

1. 为了提高开发者反馈问题的解决效率，我们会优先修复一些严重的问题，在进行问题回归测试后，会第一时间对外发布我们的Alpha版本。
2. 为了能够让开发者尽快体验和测试到我们开发完成但暂未正式发布的功能，在进行产品验收和功能测试后，我们也会第一时间对外发布我们的Alpha版本。
3. Alpha版本未经过严格发布测试，可能存在一些不稳定问题。请开发者根据版本发布记录，自行判断和选择使用Alpha版本。如果有其他问题请第一时间反馈给我们。
4. Alpha版本的所有改动都会同步到正式版本，进行严格的发布测试后对外发布。
5. 不建议开发者直接集成MSDK Alpha版本作为正式版本进行发布。

## 发布日期
2024.07.29

## 发布记录
- 支持直播模块 `LiveStreamManager` 设置画面缩放类型：`setLiveStreamScaleType`

### Bug 修复
- 在 Matrice 350 RTK , Matrice 300 RTK 和 Mini 3 上，虚拟摇杆模式下设置 Ground 坐标系会漂移: 已修复

# DJI Mobile SDK for Android V5 Alpha版本 5.10.0-a2

## 发布日期
2024.07.11

## 发布记录
- 支持 MSDK 日志功能：`enableMSDKLog`

### Bug 修复
- 直播功能调用 `LiveStreamStatus` 获取的分辨率一直为-1: 已修复
- 在 Mini 3 和 Mini 3 Pro飞行器上，在欧盟地区偶现 MSDK 启动崩溃: 已修复
- 在 Mavic 3M 和  Mavic 3T 飞行器上，调用 `deleteMediaFiles` 删除照片会失败: 已修复
- 在 Mavic 3M 飞行器上，`BatterySettingWidget` 没有更新数据: 已修复
- 在 Mavic 3T 飞行器上，分屏模式中红外画面在录像模式会变小: 已修复
- 在 Mavic 3T 喊话器上，DJI Pilot 生成的 opus 音频文件无法在MSDK中播放: 已修复
- 在 Mavic 3T 飞行器上，`MediaFile.getResolution` 无法获取分辨率: 已修复
- 在 Mavic 3E 飞行器上，在默认演示页面按遥控器物理按键拍照没有生效: 已修复
- 在 Matrice 30T 飞行器上，`KeyPhotoSize` 无法通过设置镜头类型获取照片尺寸: 已修复
- 在 Matrice 30T 飞行器上，在默认演示页面 `LensControlWidget` 控件没有正常显示: 已修复
- 在 Matrice 30T 飞行器上，在默认演示页面 `CameraVisiblePanelWidget` 控件没有正常显示: 已修复
- 在 Matrice 30T 飞行器上，在默认演示页面 `CameraControlsWidget` 控件没有正常显示: 已修复
- 在 Matrice 30T 飞行器上，在默认演示页面 `HorizontalSituationIndicatorWidget` 控件没有正常显示: 已修复
- 在 Matrice 350 RTK 飞行器上，关闭避障开关后无法获取水平避障数据: 已修复
- 在 Matrice 350 RTK 飞行器上，`CameraControlsWidget` 控件快速连续点击拍照按键会失败: 已修复
- 在 Matrice 300 RTK 飞行器上，多次调用暂停接口暂停航线任务后，航线状态变为 `READY`: 已修复
- 在 Matrice 300 RTK 飞行器上，`CameraKey.KeyVideoResolutionFrameRate`获取分辨率错误: 已修复
- 在 DJI RC Plus 遥控器上，切换遥控器固件后会提示 `REMOTE_DISCONNECTION`: 已修复

## 离线文档

- /Docs/Android_API/cn/index.html

## AAR说明

> **注意：** sdkVersion = 5.10.0-a3

| SDK包  <div style="width: 150pt">  | 说明  <div style="width: 200pt">   | 使用方式 <div style="width: 300pt">|
| :---------------: | :-----------------:  | :---------------: |
|     dji-sdk-v5-aircraft-alpha     | 飞机主包，提供MSDK对飞机控制的支持。 | implementation 'com.dji:dji-sdk-v5-aircraft-alpha:{sdkVersion}' |
| dji-sdk-v5-aircraft-provided-alpha | 飞机编译包，提供飞机包相关接口。 | compileOnly 'com.dji:dji-sdk-v5-aircraft-provided-alpha:{sdkVersion}' |
| dji-sdk-v5-networkImp-alpha | 网络库包，为MSDK提供联网能力（如果不加此依赖，MSDK所有联网功能都会停用，但控制硬件的相关接口还可以正常使用）。 | runtimeOnly 'com.dji:dji-sdk-v5-networkImp-alpha:{sdkVersion}' |

- 如果仅需支持飞机产品，使用：
  ```groovy
  implementation "com.dji:dji-sdk-v5-aircraft-alpha:{sdkVersion}"
  compileOnly "com.dji:dji-sdk-v5-aircraft-provided-alpha:{sdkVersion}"
  ```


- 如果需要MSDK使用网络（默认都需要），使用：
  ```groovy
  runtimeOnly "com.dji:dji-sdk-v5-networkImp-alpha:{sdkVersion}"
  ```

