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



This plugin provides the ability to have the Android user choose whether to display notifications of your app. The user has to give a runtime permission (AKA a dangerous permission) for this.



This plugin defines the global `window.cordova.notifications_permission`.



## Installation

```bash
cordova plugin add cordova-plugin-notifications-permission
```

## Supported Platforms

- Android
- iOS - added for convenience sake
- Browser - added for convenience sake

## The global

```js
var permission = cordova.notifications_permission;
```

### Methods


```javascript
permission.maybeAskPermission(onSuccess, rationaleText, rationaleOkButton, rationaleCancelButton);
```

Asks for permission if not done already or declined. Permission is asked through the official - and only - Android dialog. If permission is not granted by the user, a second time the app starts a "rationale" dialog is displayed explaining why permission needs to be given. You can customize the text, buttons, and theme of this dialog.


Call it like this:

```javascript
permission.maybeAskPermission(
	/* Callback that receives whether the use does or doesn not allow notifications. */
	function(status){
		/**
		 * status can be one of the following:
		 * - window.cordova.notifications_permission.GRANTED ("Allow" has been clicked)
		 * - window.cordova.notifications_permission.DENIED ("Don't Allow" or the "Maybe later..." button is clicked)
		 */
		 if(status === window.cordova.notifications_permission.GRANTED){
		 	/* you can show notifications! */
		 }
		 else if(status === window.cordova.notifications_permission.DENIED){
		 	/* we cannot do anything */
		 }
	},
	/* text on the rationale notification dialog */
	"You need to give permission because it is important!", 
	/* text on the rationale OK button */
	"OK",
	/* text on the rationale Cancel button */
	"Maybe later...",
	/* theme to use for the rationale dialog, see below */
	style
	);
```

### Styles

The following Android themes can be used to style your rationale dialog. Use them like this: `cordova.notifications_permission.styles.Theme_DeviceDefault_Dialog` (or as the value `16974126`), passing it as `theme` argument to the `maybeAskPermission` method.


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

```javascript
let permissionPlugin = cordova.notifications_permission;
let message = "You really need to give permission!";
let ok = "OK";
let cancel = "Not now";
let style = permissionPlugin.styles.Theme_DeviceDefault_Dialog_Alert;
permissionPlugin.maybeAskPermission((status) => {
		/* Logs either "granted" or "denied" */
		console.log(status);  
	},
	message,
	ok,
	cancel,
	style
);
```