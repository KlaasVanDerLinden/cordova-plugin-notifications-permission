---
title: Notifications Permission
description: Asks the user for permission to display your app's notifications on the lock screen.
---
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



This plugin provides the ability to have the Android user choose whether to display a notification on the lock screen from your app.  The user has to give a runtime permission (AKA a dangerous permission) for this since Android 13 (API level 33) and higher. See [the Android developer docs](https://developer.android.com/develop/ui/views/notifications/notification-permission) for an explanation.

Before Android 13 (API Level 33) apps running a Foreground service did not have to have runtime permission from the user. The FOREGROUND_SERVICE permission implied the use of a notification. But that has changed. Apps running on Android 13 (API Level 33) do need two things:
- These apps are required to have a notification (passed to the foreground service), so the user can see that the app is doing something in the foreground, while the app itself is in the background.
- To show the necessary notification, since Android 13 (API Level 33), an app using a foreground service notification has to ask runtime permission.

This plugin adds a system dialog ("Allow", "Deny") and a "rationale" dialog in case the user doesn't allow notifications in order to explain why the permission is needed.

This plugin defines the global `window.cordova.notifications_permission` and its method `maybeAskPermission` that does all the heavy lifting to get the permission handled.

<ul>

- 1. First call to `maybeAskPermission`: Shows System dialog. User chooses:

<ul>
		
- "Allow": You are allowed to show notifications. No further dialog for the user.
	
- "Don't Allow": continue to 2.

</ul>
	
- 2. Second call (and any further calls) to `maybeAskPermission`: Shows Rationale dialog explaining why permission is needed. User chooses:

<ul>

- "OK": System dialog is shown again - a the last time. User chooses:

<ul>

- "Allow": You are allowed to show notifications. No further dialog for the user.

- "Don't Allow": You are not allowed to show notifications. No further dialog for the user.

</ul>

- "Not now": continue to 2.

</ul>
	
</ul>

## Prerequisites

- Your app has to target Android SDK Level 33 or higher for this plugin to have any effect at all. In your app's `config.xml`, add or adjust the following preference tag:

```xml
<preference name="android-targetSdkVersion" value="33" />
```
- In order to see how the plugin works, you will need a device or emulator with Android 13 (API Level 33) or higher.

## Installation

```bash
cordova plugin add cordova-plugin-notifications-permission
```

## Supported Platforms

- All platforms are supported, but it only does something on Android.

## The global

```js
var permissionPlugin = window.cordova.notifications_permission;
```

### Methods


```javascript
permissionPlugin.maybeAskPermission({
		callback: function(status){
			/**
			 * status can be one of the following:
			 * - permissionPlugin.GRANTED ("Allow" has been clicked on the System Dialog)
			 * - permissionPlugin.DENIED_BY_SYSTEM_DIALOG ("Don't Allow" was clicked)
			 * - permissionPlugin.DENIED_BY_RATIONALE_DIALOG (Clicked on the rationale dialog's Cancel button.)
			 * - permissionPlugin.NOT_NEEDED (On device before Android 13 (API Level 33).)
			 * - permissionPlugin.NOT_ANDROID (Not on an Android device.)
			 */
		}, 
		rationaleDialog: {
			rationaleMsg, /* message on the rationale notification dialog */
			rationaleOkButton, /* text on the rationale OK button */
			rationaleCancelButton, /* text on the rationale Cancel button */
			theme /* theme to use to style the rationale dialog, see below */
		}
});
```

Asks for permission if not done already or declined. Permission is asked through the official - and only - Android system dialog. If permission is not granted by the user, a second time the app starts a "rationale" dialog is displayed explaining why permission needs to be given. You can customize the text, buttons, and theme of this dialog.

See below for an example of it's usage.

### Themes

The following native Android themes can be used to style your rationale dialog. Use them like this: `cordova.notifications_permission.themes.Theme_DeviceDefault_Dialog` (or as the value `16974126`), passing it as `theme` argument to the `maybeAskPermission` method.


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

You only need to add this bit of code and you are set. If you want to you can do something with the status that is being returned. Since a Foreground service also works even if it's notification is not allowed (at least in my experience), just calling `maybeAskPermission` is enough. If the user grants permission he/she will see the notification, else he/she will have to live without it.

```javascript
let permissionPlugin = window.cordova.notifications_permission;
let msg = "You really need to give permission!";
let okButton = "OK";
let cancelButton = "Not now";
let theme = permissionPlugin.themes.Theme_DeviceDefault_Dialog_Alert;
permissionPlugin.maybeAskPermission({
	callback: function(status) {
		/* Permission is either granted, denied, or not needed. */
		switch(status){
			case permissionPlugin.GRANTED:
			case permissionPlugin.NOT_NEEDED:
				/* Notification shows the same as it did before Android 13 (API Level 33). */
				break;
			case permissionPlugin.DENIED_BY_RATIONALE_DIALOG:
			case permissionPlugin.DENIED_BY_SYSTEM_DIALOG:
			case permissionPlugin.NOT_ANDROID:
				/* The notification does not show. */
				break;	
		}
	},
	rationaleDialog: {
		msg: msg,
		okButton: okButton,
		cancelButton: cancelButton,
		theme: theme
	}
);
```