# ProxyShare
Android application that shares proxy settings of Telegram messenger app ([Android](https://play.google.com/store/apps/details?id=org.telegram.messenger), [iOS](https://apps.apple.com/app/telegram-messenger/id686449807)) via NFC. Works with both Android and iOS

<a href="https://play.google.com/store/apps/details?id=com.luigivampa92.nfcshare"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" alt="Get it on Play Store" height="60"></a>
<a href="https://github.com/LuigiVampa92/ProxyShare/releases/download/v1.0/com.luigivampa92.nfcshare_1.0_release_16052020_0754.apk"><img src="assets/badge_download_direct_apk.png" alt="Direct APK download" height="60"></a>

## What does it do?
This app allows you to share MTProxy configurations of your Telegram messenger with a single touch.

The main purpose is to demonstrate how some specific information like URI can be transferred to another smartphone by NFC using Android ability of host card emulation (HCE). It emulates a tag that is completely valid to NDEF protocol and fits extra Android restrictions on HCE like full compatibility with ISO-7816-4 and CLA byte. 

### How can I send the proxy configuration data?

You will have to install this app on your device with embedded NFC module and Android OS version 5.0 or above. Enter MTProxy data manually or get it some other way. You can click on Telegram proxy URI in any app or web browser. This app will offer you to save proxy configuration

Once you have added the proxy configuration data to the list, click on the element and make sure it has a green "OK" icon. Once the icon is there your smartphone will act as an NDEF tag and will transfer the data to whoever reads it. Close the app. The share of proxy configuration will work __without__ app running.

On today (May 2020) iOS does not have public API for host card emulation. So it is not possible to make the similar app for iOS

### How can I receive the proxy configuration data?

You can receive the information on Android and iOS smartphones. You will need an Android device with embedded NFC module and system version 5.0 or above or an iOS device that is at least iPhone 7 and has system version 13.0 or above.

- To receive the data on another Android smartphone - just turn on the NFC in settings and bring your device close to the one that shares data. No extra special software required. 
- To receive the data on iPhone XS, XR, 11, 11-Pro and above - just bring your device close to the device that shares data. No extra special software required. Everything is the same as in Android.
- To receive the data on iPhone 7, 8, X - you will have to download a special NFC reader app from Apple AppStore because automatic background NFC scanning does not work on these iPhone models. Download any app and hit "scan"

Android devices without embedded NFC module and iPhones below 7 (SE and 6s and so on) can not read NFC NDEF tags and thus can not receive the data.

## Warning
This app may not work or may not work well on SOME devices because of hardware features of NFC chips and 13.56 MHz antennas that installed inside them. I tried to test it on as many device models as I could and it worked perfectly on some Huawei and Samsung models but worked very bad or didn’t work at all on some OnePlus devices. Also sometimes information cannot be received on a reading device because of its NFC module features. Please keep that in mind.

##### I have following observations about the devices:
- Pretty much all Android devices can SEND data properly
- Not many Android devices can RECEIVE data properly. It seems that some Android devices probably just don't want to recognize type-4 tags, even if they are completely valid, because technically another Android phone is a type-4 tag and most NDEF tags are type-2. I can suggest that there are troubles with antennas - perhaps another device cannot discover it or their near fields have interference. Not sure. I had the best result on Huawei
- Pretty much all iOS devices can RECEIVE data properly (what an irony)
- Sometimes you make things work when you disable "Android Beam" in your device settings
