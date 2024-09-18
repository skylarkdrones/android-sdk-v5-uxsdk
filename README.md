# DJI Mobile SDK for Android V5 Latest Alpha Version 5.11.0-a1

[中文版](README_CN.md)

##  Alpha Version Announcement

1. To improve the problem-solving efficiency of developers' feedback, we will fix the serious problems first. And we will release the alpha version immediately after the regression test.
2. For letting developers experience and test the MSDK functions that have been developed but not officially released, we will also release the alpha version immediately after the product acceptance test and functional test.
3. The alpha version is not strictly tested before the release. There might exist some unstable problems. Please judge and choose whether to use this version according to the release note. If you have other problems, please contact us immediately.
4. All changes in the alpha version will be merged into the official version and will be strictly tested before the release.
5. It is not suggested that developers directly merge the MSDK alpha version and released it as an official version.

## Release Date
2024.09.18

## Release Notes
- Adapted to Android 14
- Support offline upgrade function for Matrice 350 RTK and Matrice 300 RTK
- Added error code for `IDeviceHealthManager`

### Bug fixes
- Occasional `IllegalStateException` crash when calling `ReceiveStreamListener`: Fixed
- Occasional `ConcurrentModificationException` crash when calling `sendVirtualStickAdvancedParam`: Fixed
- Calling `isVirtualStickEnable` to get status error after the aircraft reconnects: Fixed
- On Matrice 350 RTK aircraft, video transmission is stuck when using PSDK payload: Fixed
- On Matrice 350 RTK aircraft, calling `KeyExposureModeRange` with H30 camera returns empty: Fixed
- On Matrice 350 RTK aircraft, calling `KeyCameraZoomRatiosRange` with H30 camera gave incorrect range: fixed
- On atrice 300 RTK aircraft, failed to take a photo after switching from video mode to photo mode using the DJI smart controller: Fixed
- On the Mini 3 and Mini 3 Pro aircraft, firmware information cannot be obtained: Fixed

> **Note:**
> **Kotlin Android Extensions is deprecated, which means that using Kotlin synthetics for view binding is no longer supported. If your app uses Kotlin synthetics for view binding, follow this guide to migrate to Jetpack view binding:
<a href="https://developer.android.com/topic/libraries/view-binding/migration">Migrate from Kotlin synthetics to Jetpack view binding</a>**

## Offline Documentation

- /Docs/Android_API/en/index.html

## AAR Explanation

> **Notice:** sdkVersion = 5.11.0-a1

| SDK package  <div style="width: 150pt">  | Explanation  <div style="width: 200pt">   | How to use <div style="width: 300pt">|
| :---------------: | :-----------------:  | :---------------: |
|     dji-sdk-v5-aircraft-alpha      | Aircraft main package, which provides support for MSDK to control the aircraft. | implementation 'com.dji:dji-sdk-v5-aircraft-alpha:{sdkVersion}' |
| dji-sdk-v5-aircraft-provided-alpha | Aircraft compilation package, which provides interfaces related to the aircraft package. | compileOnly 'com.dji:dji-sdk-v5-aircraft-provided-alpha:{sdkVersion}' |
| dji-sdk-v5-networkImp-alpha | Network library package, which provides network connection ability for MSDK. Without this dependency, all network functions of MSDK will not work, but the interfaces of hardware control can be used normally. | runtimeOnly 'com.dji:dji-sdk-v5-networkImp-alpha:{sdkVersion}' |

- If only the aircraft product is in need to support, please use:
  ```groovy
  implementation "com.dji:dji-sdk-v5-aircraft-alpha:{sdkVersion}"
  compileOnly "com.dji:dji-sdk-v5-aircraft-provided-alpha:{sdkVersion}"
  ```

- If the MSDK have to use network(required by default), please use:
  ```groovy
  runtimeOnly "com.dji:dji-sdk-v5-networkImp-alpha:{sdkVersion}"
  ```