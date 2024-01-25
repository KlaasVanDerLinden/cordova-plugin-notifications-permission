let exec = require("cordova/exec");
let cordova = require("cordova");	
let NotificationsPermission = {
	/* Constants for the returned status of the permission. */
	NEWLY_GRANTED_AFTER_RATIONALE: "newly_granted_after_rationale",
	NEWLY_GRANTED_WITHOUT_RATIONALE: "newly_granted_without_rationale",
	ALREADY_GRANTED: "already_granted",
	ALREADY_DENIED_PERMANENTLY: "already_denied_permanently",
	NEWLY_DENIED_PERMANENTLY: "newly_denied_permanently",
	ALREADY_DENIED_NOT_PERMANENTLY: "already_denied_not_permanently",
	NEWLY_DENIED_NOT_PERMANENTLY: "newly_denied_not_permanently",
	DENIED_THROUGH_RATIONALE_DIALOG: "denied_through_rationale_dialog",
	NOT_NEEDED: "not_needed",
	NOT_ANDROID: "not_android",
	ERROR: "error",
	/**
	 * Show a notification to the user asking for permission to post notifications to the lock screen.
	 */
	maybeAskPermission: function(onResult, rationaleDialog){
		/* Only for Android. Else return window.cordova.notifications_permission.NOT_ANDROID */
		if(cordova.platformId === "android"){
			
			/* Make sure the arguments are all set for the rationaleDialog. 
			 * rationaleDialog should be an Object. All others default to defaults. 
			 */
			let rationale = (typeof(rationaleDialog) === "undefined" || !this.isObject(rationaleDialog)) ? {} : rationaleDialog;
			let rationaleMsg = (typeof(rationale.msg) === "string" && rationale.msg) ? rationale.msg : "Permission is needed to show a notification on the lock screen when this app is in the background.";
			let rationaleOkButton =  (typeof(rationale.okButton) === "string" && rationale.okButton) ? rationale.okButton : "OK";
			let rationaleCancelButton =  (typeof(rationale.cancelButton) === "string") && rationale.cancelButton ? rationale.cancelButton : "Not now";
			let theme = (typeof(rationale.theme) !== "undefined" && parseInt(rationale.theme)) ? parseInt(rationale.theme) : window.cordova.notifications_permission.themes.Theme_DeviceDefault_Dialog_Alert;
			/* Call Android. Get 'status':
			 *	- window.cordova.notifications_permission.NEWLY_GRANTED_AFTER_RATIONALE or 
			 *  - window.cordova.notifications_permission.NEWLY_GRANTED_WITHOUT_RATIONALE or 
			 *  - window.cordova.notifications_permission.ALREADY_GRANTED or 
			 *  - window.cordova.notifications_permission.ALREADY_DENIED_PERMANENTLY or
			 *  - window.cordova.notifications_permission.NEWLY_DENIED_PERMANENTLY or
			 *  - window.cordova.notifications_permission.ALREADY_DENIED_NOT_PERMANENTLY or
			 *  - window.cordova.notifications_permission.NEWLY_DENIED_NOT_PERMANENTLY or
			 * 	- window.cordova.notifications_permission.DENIED_BY_RATIONALE_DIALOG or
			 *  - window.cordova.notifications_permission.NOT_NEEDED( < Android 13 (API Level 33)) or
			 *  - window.cordova.notifications_permission.NOT_ANDROID
			 *  - window.cordova.notifications_permission.ERROR (see console);
			 * Else error callback is called. Developers should console.log the error to see what happens.
			 */
			exec(function(status){
				onResult(status);
			}, function(error){
				console.log("error in plugin cordova-plugin-notifications-permission", error);
			}, "NotificationsPermission", "maybeAskPermission", 
			[
				rationaleMsg, 
				rationaleOkButton, 
				rationaleCancelButton,
				theme
			]);
		}
		else{
			/* return window.cordova.notifications_permission.NOT_ANDROID */
			onSuccess(this.NOT_ANDROID);
		}
	},
	/* Private function to make sure we have an object as parameter to maybeAskPermission */
	isObject(obj) {
	    try {
	        // Test obj with in operator
	        0 in obj;// Throws TypeError if obj is not an object.
	        var isObject = true;// var works here due to hoisting
	    }
	    catch (e) {
	        // obj is not an object if e is a TypeError.
	        // But if e.constructor == Error or DOMException then obj is an object.
	        // If you don't care about security errors you can remove this:
	        isObject = e.constructor!=TypeError;
	    }
	    return isObject;
	}
};

module.exports = NotificationsPermission;

permissionPlugin.maybeAskPermission(
    /* Callback that returns the status. */
    function(status){
		/* Read the permission status. Can be granted or denied */
    }, 
    /* rationale dialog settings: an object with the options for texts and theme. */
    {
        rationaleMsg, /* message on the rationale notification dialog */
        rationaleOkButton, /* text on the rationale OK button */
        rationaleCancelButton, /* text on the rationale Cancel button */
        theme /* theme to use to style the rationale dialog, see below */
    }
);