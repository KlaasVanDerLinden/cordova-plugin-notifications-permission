let exec = require('cordova/exec');
let cordova = require('cordova');	
let NotificationsPermission = {

	DENIED: "denied",
	GRANTED: "granted",
	NOT_NEEDED: "not_needed"
	/**
	 * Show a notification to the user asking for permission to post notifications to the lock screen.
	 */
	maybeAskPermission: function(onSuccess, rationaleText, rationaleOkButton, rationaleCancelButton, theme){
		/* Only for Android. */
		if(cordova.platformId === 'android'){
			/* Make sure the arguments are all set */
			rationaleText = (typeof(rationaleText) !== "undefined") ? rationaleText : "";
			rationaleOkButton =  (typeof(rationaleOkButton) !== "undefined") ? rationaleOkButton : "";
			rationaleCancelButton =  (typeof(rationaleCancelButton) !== "undefined") ? rationaleCancelButton : "";
			theme = (typeof(theme) !== "undefined" && theme) ? theme : window.cordova.notifications_permission.themes.Theme_DeviceDefault_Dialog_Alert;
			/* Call Android. Get 'status':
			 *	- window.cordova.notifications_permission.GRANTED or 
			 *  - window.cordova.notifications_permission.DENIED
			 * Else error callback is called. Developers should console.log the error to see what happens.
			 */
			exec(function(status){
				onSuccess(status);
			}, function(error){
				console.log("error in plugin cordova-plugin-notifications-permission", error);
			}, "NotificationsPermission", "maybeAskPermission", 
			[
				rationaleText, 
				rationaleOkButton, 
				rationaleCancelButton,
				theme
			]);
		}
		else{
			onSuccess(this.GRANTED);
		}
	}
};

module.exports = NotificationsPermission;