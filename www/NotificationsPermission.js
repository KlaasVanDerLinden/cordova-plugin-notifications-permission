let exec = require("cordova/exec");
let cordova = require("cordova");	
let NotificationsPermission = {
	/* Constants for the returned status of the permission. */
	GRANTED_NEWLY_AFTER_RATIONALE: "granted_newly_after_rationale",
	GRANTED_NEWLY_WITHOUT_RATIONALE: "granted_newly_without_rationale",
	GRANTED_NEWLY_AFTER_SETTINGS: "granted_newly_after_settings",
	GRANTED_ALREADY: "granted_already",
	DENIED_PERMANENTLY_ALREADY: "denied_permanently_already",
	DENIED_PERMANENTLY_NEWLY: "denied_permanently_newly",
	DENIED_NOT_PERMANENTLY_ALREADY: "denied_not_permanently_already",
	DENIED_NOT_PERMANENTLY_NEWLY: "denied_not_permanently_newly",
	DENIED_PERMANENTLY_ALREADY_AFTER_SETTINGS: "denied_permanently_already_after_settings",
	DENIED_THROUGH_RATIONALE_DIALOG: "denied_through_rationale_dialog",
	DENIED_THROUGH_LAST_RESORT_DIALOG: "denied_trough_last_resort_dialog",
	NOT_NEEDED: "not_needed",
	NOT_ANDROID: "not_android",
	ERROR: "error",
	/**
	 * Show a notification to the user asking for permission to post notifications to the lock screen.
	 */
	maybeAskPermission: function(onResult, rationaleDialog, lastResortDialog){
		/* Only for Android. Else return window.cordova.notifications_permission.NOT_ANDROID */
		if(cordova.platformId === "android"){
			
			/* Make sure the arguments are all set for the rationaleDialog. 
			 * rationaleDialog should be an Object. All others default to defaults. 
			 */
			let rationale = (typeof(rationaleDialog) === "undefined" || !this.isObject(rationaleDialog)) ? {} : rationaleDialog;
			let rationaleShow = this.getBoolAsString(rationale, "show", "true");
			let rationaleTitle = this.getString(rationale, "title", "Notification Permission");
			let rationaleMsg = this.getString(rationale, "msg", "Permission is needed to show a notification on the lock screen.");
			let rationaleOkButton =  this.getString(rationale, "okButton", "OK");
			let rationaleCancelButton =  this.getString(rationale, "cancelButton", "Not now");
			let rationaleTheme = this.getInt(rationale, "theme", window.cordova.notifications_permission.themes.Theme_DeviceDefault_Dialog_Alert);
			
			let lastResort = (typeof(lastResortDialog) === "undefined" || !this.isObject(lastResortDialog)) ? {} : lastResortDialog;
			let lastResortShow = this.getBoolAsString(lastResort, "show", "true");
			let lastResortTitle = this.getString(lastResort, "title", "Notification Permission");
			let lastResortMsg = this.getString(lastResort, "msg", "Notification permission has been set not to ask again! Please provide them from settings.");
			let lastResortOkButton =  this.getString(lastResort, "okButton", "Settings");
			let lastResortCancelButton =  this.getString(lastResort, "cancelButton", "Cancel");
			let lastResortTheme = this.getInt(lastResort, "theme", window.cordova.notifications_permission.themes.Theme_DeviceDefault_Dialog_Alert);
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
				console.log("error in cordova-plugin-notifications-permission", error);
			}, "NotificationsPermission", "maybeAskPermission", 
			[
				rationaleShow,
				rationaleTitle,
				rationaleMsg, 
				rationaleOkButton, 
				rationaleCancelButton,
				rationaleTheme,
				lastResortShow,
				lastResortTitle,
				lastResortMsg, 
				lastResortOkButton, 
				lastResortCancelButton,
				lastResortTheme
			]);
		}
		else{
			/* return window.cordova.notifications_permission.NOT_ANDROID */
			onResult(this.NOT_ANDROID);
		}
	},
	/* Private functions to do typechecks and set defaults. */
	getString(obj, key, defaultString){
		return typeof(obj[key]) === "string" ? obj[key] : defaultString;
	},
	getInt(obj, key, defaultInt){
		return typeof(obj[key]) !== "undefined" && parseInt(obj[key]) ? parseInt(obj[key]) : defaultInt;
	},
	getBoolAsString(obj, key, defaultBool){
	    let output = "";
	    if(typeof(obj[key]) === "undefined"){
	        obj[key] = defaultBool.toString();
	    }
        if(obj[key] === true || obj[key] === "true"){
            output = "true";
        }
        else{
            output = "false";
        }
        return output;
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