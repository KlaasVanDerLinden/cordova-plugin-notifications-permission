<!--
title: Cordova Plugin Notifications Permission
description: Asks the user for permission to display your app's notifications on the lock screen.
-->
<!--
# license: Licensed to the Apache Software Foundation (ASF) under one
#         or more contributor license agreements.  See the NOTICE file
#         distributed with this work for additional information
#         regarding copyright ownership.  The ASF licenses this file
#         to you under the Apache License, Version 2.0 (the
#         "License"); you may not use this file except in compliance
#         with the License.  You may obtain a copy of the License at
#
#           http://www.apache.org/licenses/LICENSE-2.0
#
#         Unless required by applicable law or agreed to in writing,
#         software distributed under the License is distributed on an
#         "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#         KIND, either express or implied.  See the License for the
#         specific language governing permissions and limitations
#         under the License.
-->

# cordova-plugin-notifications-permission



This plugin adds runtime permission for POST_NOTIFICATIONS to your app.  The user has to give a runtime permission (AKA a dangerous permission) for this since Android 13 (API level 33). See [the Android developer docs](https://developer.android.com/develop/ui/views/notifications/notification-permission) for an explanation.

Before Android 13 (API Level 33) apps running a Foreground service did not have to have runtime permission to display notifications. The FOREGROUND_SERVICE permission implied the use of a notification. But that has changed. Apps running on Android 13 (API Level 33) do need two things:
* These apps are required to have a notification (passed to the foreground service), so the user can see that the app is doing something in the foreground, while the app itself is in the background.
* To show the necessary notification, since Android 13 (API Level 33), an app using a foreground service notification has to ask runtime permission.

This plugin adds a system dialog ("Allow", "Deny") and a "Rationale" dialog in case the user doesn't allow notifications in order to explain why the permission is needed. It also adds a "Last Resort" dialog: when the user has denied permission permanently, you can show a dialog explaining to the app user how to enable notifications from system settings. Both the Rationale Dialog and the Last resort Dialog can be skipped by setting their `show` properties to `false`.

This plugin defines the global `window.cordova.notifications_permission` and its method `maybeAskPermission` that does all the heavy lifting to get the permission handled.

1. First call to `maybeAskPermission`: Shows System dialog. User chooses:
	* "Allow": You are allowed to show notifications. No further dialog for the user.
	* "Don't Allow": continue to 2.
2. Second call (and any further calls) to `maybeAskPermission`: Shows Rationale dialog explaining why permission is needed. User chooses:
	* "OK": System dialog is shown again - a the last time. User chooses:
		* "Allow": You are allowed to show notifications. No further dialog for the user.
		* "Don't Allow": You are not allowed to show notifications. No further dialog for the user.
	* "Not now": continue to 2.
<figure>
	<img src="https://github.com/KlaasVanDerLinden/cordova-plugin-notifications-permission/blob/master/a-flowchart.svg?raw=true"/>
	<caption>A flowchart explaining the process and the different values for the status. The status ALREADY_DENIED_NOT_PERMANENTLY misses from this flow chart. It will happen when the user swipes away the System Dialog during the 2nd run. When the app user has already granted permission and afterwards revokes in Android system settings, the process starts over at the second run. (There is no way to distinguish this.)</caption>
</figure>

## Prerequisites

* Your app has to target Android SDK Level 33 or higher for this plugin to have any effect at all. In your app's `config.xml`, add or adjust the following preference tag:

```xml
<preference name="android-targetSdkVersion" value="33" />
```
* In order to see how the plugin works, you will need a device or emulator with Android 13 (API Level 33) or higher.

## Installation

```bash
cordova plugin add cordova-plugin-notifications-permission
```

## Supported Platforms

- This plugin was developed for Android. It has no effect on other platforms.

## The global

```js
var permissionPlugin = window.cordova.notifications_permission;
```

### Methods


```javascript
permissionPlugin.maybeAskPermission(
    /* Callback that returns the status. */
    function(status){
        /**
         * status can be one of the following:
         * - permissionPlugin.GRANTED_NEWLY_WITHOUT_RATIONALE ("Allow" has been clicked on the System Dialog)
         * - permissionPlugin.GRANTED_NEWLY_AFTER_RATIONALE ("Allow" has been clicked on the System Dialog after have confirmed the rationale dialog.)
         * - permissionPlugin.GRANTED_NEWLY_AFTER_SETTINGS (User was guided to system settings and switch on notifications.)
         * - permissionPlugin.GRANTED_ALREADY (User has already allowed the notification at some point earlier.)
         * - permissionPlugin.DENIED_NOT_PERMANENTLY_NEWLY  ("Don't allow" clicked or swiped away for the first time. OS will try again in the future.)
         * - permissionPlugin.DENIED_PERMANENTLY_NEWLY ("Don't allow" clicked. OS will never ask again.)
         * - permissionPlugin.DENIED_PERMANENTLY_ALREADY  (OS decided to stop asking at some point earlier.)
         * - permissionPlugin.DENIED_NOT_PERMANENTLY_ALREADY  (User denied again. But the OS will try again in the future.)
         * - permissionPlugin.ALREADY_DENIED_PERMANENTLY_AFTER_SETTINGS (User was guided to system settings but chose not to allow.)
         * - permissionPlugin.DENIED_THROUGH_RATIONALE_DIALOG (User clicked on the rationale dialog's Cancel button.)
         * - permissionPlugin.DENIED_THROUGH_LAST_RESORT_DIALOG (User clicked "cancel" instead of going to settings.)
         * - permissionPlugin.NOT_NEEDED (User is on device before Android 13 (API Level 33).)
         * - permissionPlugin.NOT_ANDROID (User is not on an Android device.)
         * - permissionPlugin.ERROR (A message was printed in the console indicating the cause of the error.)
         */
    }, 
    /* rationale dialog settings: an object with the options for texts and theme. */
    {
        show, /* whether to show te rationale dialog or skip it. Default true.
        title, /* title of the rationale notification dialog. Default "Notification Permission". */
        msg, /* message on the rationale notification dialog. Default "Permission is needed to show a notification on the lock screen." */
        okButton, /* text on the rationale OK button. Default "OK". */
        cancelButton, /* text on the rationale Cancel button. Default "Not now". */
        theme /* theme to use to style the rationale dialog, see below. Default window.cordova.notifications_permission.themes.Theme_DeviceDefault_Dialog_Alert. */
    }
    /* last resort dialog settings: an object with the options for texts and theme. */
    {
        /* see rationale dialog settings for properties */
    }
);
```

Asks for permission if not done already or declined. Permission is asked through the official - and only - Android System dialog. If permission is not granted by the user, a second time a "rationale" dialog is displayed explaining why permission needs to be given. You can customize the message, buttons, and theme of this rationale dialog.

See below for an example of it's usage.

### Themes

The following native Android themes can be used to style your rationale dialog. Use them like this: `cordova.notifications_permission.themes.Theme_DeviceDefault_Dialog` (or as the int value `16974126`), passing it as `theme` argument to the `maybeAskPermission` method.


```javascript
Theme_DeviceDefault_Dialog: 16974126
Theme_DeviceDefault_DialogWhenLarge: 16974134
Theme_DeviceDefault_DialogWhenLarge_NoActionBar: 16974135
Theme_DeviceDefault_Dialog_Alert: 16974545
Theme_DeviceDefault_Dialog_MinWidth: 16974127
Theme_DeviceDefault_Dialog_NoActionBar: 16974128
Theme_DeviceDefault_Dialog_NoActionBar_MinWidth: 16974129
Theme_DeviceDefault_Light_Dialog: 16974130
Theme_DeviceDefault_Light_DialogWhenLarge: 16974136
Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar: 16974137
Theme_DeviceDefault_Light_Dialog_Alert: 16974546
Theme_DeviceDefault_Light_Dialog_MinWidth: 16974131
Theme_DeviceDefault_Light_Dialog_NoActionBar: 16974132
Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth: 16974133
Theme_Material: 16974372
Theme_Material_Dialog: 16974373
Theme_Material_DialogWhenLarge: 16974379
Theme_Material_DialogWhenLarge_NoActionBar: 16974380
Theme_Material_Dialog_Alert: 16974374
Theme_Material_Dialog_MinWidth: 16974375
Theme_Material_Dialog_NoActionBar: 16974376
Theme_Material_Dialog_NoActionBar_MinWidth: 16974377
Theme_Material_Dialog_Presentation: 16974378
Theme_Material_Light: 16974391
Theme_Material_Light_DarkActionBar: 16974392
Theme_Material_Light_Dialog: 16974393
Theme_Material_Light_DialogWhenLarge: 16974399
Theme_Material_Light_DialogWhenLarge_DarkActionBar: 16974552
Theme_Material_Light_DialogWhenLarge_NoActionBar: 16974400
Theme_Material_Light_Dialog_Alert: 16974394
Theme_Material_Light_Dialog_MinWidth: 16974395
Theme_Material_Light_Dialog_NoActionBar: 16974396
Theme_Material_Light_Dialog_NoActionBar_MinWidth: 16974397
Theme_Material_Light_Dialog_Presentation: 16974398
```

### Full Example

You only need to add this bit of code and you are set. Place it somewhere within the Cordova device ready call. If you want to you can do something with the status that is being returned. Since a Foreground service also works even if it's notification is not allowed (at least in my experience), just calling `maybeAskPermission` is enough. If the user grants permission he/she will see the notification, else he/she will have to live without it. 

```javascript
/* Cordova code */
document.addEventListener('deviceready', onDeviceReady, false);
function onDeviceReady() {
/* Your code. (Make sure to not alert(status) until splashscreen
 * has been hidden with navigator.splashscreen.hide() 
 * else alert might not show and your app seems to stall.)
 */
let permissionPlugin = window.cordova.notifications_permission;
let rationaleTitle = "Notification Permission";
let rationaleMsg = "You really need to give permission!";
let rationaleOkButton = "OK";
let rationaleCancelButton = "Not now";
let rationaleTheme = permissionPlugin.themes.Theme_DeviceDefault_Dialog_Alert;
let lastResortTitle = "Notification Permission!";
let lastResortMsg = "You really need to give permission! Now the only way left is through system settings.";
let lastResortOkButton = "Settings";
let lastResortCancelButton = "No thanks";
let lastResortTheme = permissionPlugin.themes.Theme_DeviceDefault_Dialog_Alert;
permissionPlugin.maybeAskPermission(
    function(status) {
        /* Permission is either granted, denied, or not needed. */
        switch(status){
            case permissionPlugin.GRANTED_NEWLY_WITHOUT_RATIONALE:
            case permissionPlugin.GRANTED_NEWLY_AFTER_RATIONALE:
            case permissionPlugin.GRANTED_NEWLY_AFTER_SETTINGS:
            case permissionPlugin.GRANTED_ALREADY:
            case permissionPlugin.NOT_NEEDED:
                /* Notification shows the same as it did before Android 13 (API Level 33). */
                break;
            case permissionPlugin.DENIED_NOT_PERMANENTLY_NEWLY:
            case permissionPlugin.DENIED_PERMANENTLY_NEWLY:
            case permissionPlugin.DENIED_NOT_PERMANENTLY_ALREADY:
            case permissionPlugin.DENIED_PERMANENTLY_ALREADY:
            case permissionPlugin.DENIED_PERMANENTLY_ALREADY_AFTER_SETTINGS
            case permissionPlugin.DENIED_THROUGH_RATIONALE_DIALOG:
            case permissionPlugin.DENIED_THROUGH_LAST_RESORT_DIALOG:
            case permissionPlugin.NOT_ANDROID:
                /* The notification does not show. */
                break;    
            case permissionPlugin.ERROR:
                /* See console for error message */
                break;
        }
    },
    {
        show: true,
        title:rationaleTitle,
        msg: rationaleMsg,
        okButton: rationaleOkButton,
        cancelButton: rationaleCancelButton,
        theme: rationaleTheme
    },
    {
        show: true,
        title:lastResortTitle,
        msg: lastResortMsg,
        okButton: lastResortOkButton,
        cancelButton: lastResortCancelButton,
        theme: lastResortTheme
    }
);
/* END OF Your code */
}
/* END OF Cordova code */
```



