/*
   Licensed to the Apache Software Foundation (ASF) under one
   or more contributor license agreements.  See the NOTICE file
   distributed with this work for additional information
   regarding copyright ownership.  The ASF licenses this file
   to you under the Apache License, Version 2.0 (the
   "License"); you may not use this file except in compliance
   with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.
*/
package nl.klaasmaakt.cordova.notifications_permission;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
/**
 * CordovaPlugin for handling notification permissions.
 */
public class NotificationsPermission extends CordovaPlugin {
	// Tag for logging purposes
	private static final String TAG = "NotificationsPermission";
	// The permission we need
	private static final String PERMISSION = Manifest.permission.POST_NOTIFICATIONS;
	// Constants for permission status
	public static final String GRANTED_NEWLY_AFTER_RATIONALE = "granted_newly_after_rationale";
	public static final String GRANTED_NEWLY_WITHOUT_RATIONALE = "granted_newly_without_rationale";
	public static final String GRANTED_NEWLY_AFTER_SETTINGS = "granted_newly_after_settings";
	public static final String GRANTED_ALREADY = "granted_already";
	public static final String DENIED_PERMANENTLY_ALREADY = "denied_permanently_already";
	public static final String DENIED_PERMANENTLY_NEWLY = "denied_permanently_newly";
	public static final String DENIED_NOT_PERMANENTLY_ALREADY = "denied_not_permanently_already";
	public static final String DENIED_NOT_PERMANENTLY_NEWLY = "denied_not_permanently_newly";
	public static final String DENIED_PERMANENTLY_ALREADY_AFTER_SETTINGS = "denied_permanently_already_after_settings";
	public static final String DENIED_THROUGH_RATIONALE_DIALOG = "denied_through_rationale_dialog";
	public static final String DENIED_THROUGH_LAST_RESORT_DIALOG = "denied_through_last_resort_dialog";
	public static final String NOT_NEEDED = "not_needed";
	// Request code for permission request
	private static final int REQUEST_CODE_PERMISSION = 1;
	private static final int REQUEST_CODE_OPEN_SETTINGS = 1;
	// Stores the before state of shouldRequestPermissionRationale to differentiate between permanently and temporarily denied.
	private boolean mShowDialog;
	private String mTitle;
	private String mMsg;
	private String mPositiveButton;
	private String mNegativeButton;
	private Integer mTheme;
	private boolean mUserWentToSettings = false;
	private boolean beforeClickPermissionRat;
	// Dialog ID for managing multiple dialogs
	private static final String DIALOG_ID = "dialog";
	// Callback context for communicating with Cordova
	private CallbackContext mCallbackContext;
	// Instance of NotificationsPermission for referencing in callbacks
	private NotificationsPermission mInstance;
	private Utils mUtils;
	// Keeps track of whether the rationale has shown before making new requestPermission
	private boolean hasPassedRationale = false;
	// ClickCallback for handling positive and negative button clicks
	private ClickCallback mClickCallbackRationale = new ClickCallback() {
		@Override
		public void onClick(Status status) {
			if (status == ClickCallback.Status.POSITIVE) {
				beforeClickPermissionRat = mInstance.shouldShowRationale();
				hasPassedRationale = true;
				cordova.requestPermission(mInstance, REQUEST_CODE_PERMISSION, PERMISSION);
			}
			if (status == ClickCallback.Status.NEGATIVE) {
				PluginResult.Status resultStatus = PluginResult.Status.OK;
				String permissionStatus = mInstance.DENIED_THROUGH_RATIONALE_DIALOG;
				Log.v(TAG, permissionStatus);
				mCallbackContext.sendPluginResult(new PluginResult(resultStatus, permissionStatus));
			}
		}
	};
	private ClickCallback mCLickCallbackLastResort = new ClickCallback() {
		@Override
		public void onClick(Status status) {
			if (status == ClickCallback.Status.POSITIVE) {
				mUserWentToSettings = true;
				cordova.getActivity().startActivityForResult(new Intent(Settings.ACTION_ALL_APPS_NOTIFICATION_SETTINGS), REQUEST_CODE_OPEN_SETTINGS);
			}
			if (status == ClickCallback.Status.NEGATIVE) {
				PluginResult.Status resultStatus = PluginResult.Status.OK;
				String permissionStatus = mInstance.DENIED_THROUGH_LAST_RESORT_DIALOG;
				Log.v(TAG, permissionStatus);
				mCallbackContext.sendPluginResult(new PluginResult(resultStatus, permissionStatus));
			}
		}
	};

	/**
	 * Initialize the plugin.
	 */
	@Override
	public void pluginInitialize() {
		super.pluginInitialize();
		mInstance = this;
		mUtils = new Utils(cordova.getContext());
	}

	/**
	 * Handle the result of a permission request. Cordova has marked thsis as deprecated but hasn't implemented the alternative onRequestPermissionsResult()...
	 *
	 * @param requestCode  The request code associated with the permission request.
	 * @param permissions  The array of requested permissions.
	 * @param grantResults The array of grant results for each requested permission.
	 * @throws JSONException If there is an issue parsing JSON data.
	 */
	@Override
	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
		/* onRequestPermissionResult is deprecated, but the alternative onRequestPermissionsResult is not implemented yet/ */
		super.onRequestPermissionResult(requestCode,permissions,grantResults);
		if (requestCode == REQUEST_CODE_PERMISSION) {
			String result = "undefined";
			if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
				if(hasPassedRationale == true){
					result = GRANTED_NEWLY_AFTER_RATIONALE;
				}
				else {
					result = GRANTED_NEWLY_WITHOUT_RATIONALE;
				}
				mUtils.savePermissionHasBeenGrantedBefore();
			}
			else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
				/* We need to check whether we have been in the process or the user has just started.
				 * If the rationale dialog has been needed before we know this is not the first start
				 */
				boolean rationaleHasBeenNeededBefore = mUtils.getRationaleHasBeenNeededBefore();
				/* In the rare case that the user granted permission and then ungranted it via OS settings
				 * we will keep track of that grant.
				 */
				boolean permissionHasBeenGrantedBefore = mUtils.getPermissionHasBeenGrantedBefore();
				/* Also check whether we arrive her for a second time, so we can set already denied
				 * properly next time.
				 */
				boolean haveWeBeenHereBefore = mUtils.getHaveWeBeenHereBefore();
				mUtils.saveHaveWeBeenHereBefore();
				boolean afterClickPermissionRat = shouldShowRationale();
				// if true than we save it so we know this is not the first time
				if(afterClickPermissionRat == true){
					mUtils.saveRationaleHasBeenNeededBefore();
				}
				/* Since we know te state of shouldShowRationale before and after we requestPermission
				 * we know that there is still no need to show the rationale so was already denied
				 * or the whole process hasn't started yet since the first dialog has never finished.
				 */
				if(beforeClickPermissionRat == false && afterClickPermissionRat == false){
					/* We have had the first dialog a while ago, so this is the end */
					if(rationaleHasBeenNeededBefore == true || permissionHasBeenGrantedBefore == true){
						if(!mUtils.getLastResortHasShown()) {
							showExtraDialog(true);
							mUtils.saveLastResortHasShown();
						}
						result = DENIED_PERMANENTLY_ALREADY;
					}
					else if(haveWeBeenHereBefore == true){
						result = DENIED_NOT_PERMANENTLY_ALREADY;
					}
					else{
						result = DENIED_NOT_PERMANENTLY_NEWLY;
					}
				}
				else if(beforeClickPermissionRat == false && afterClickPermissionRat == true){
					if(haveWeBeenHereBefore == true) {
						result = DENIED_NOT_PERMANENTLY_ALREADY;
					}
					else{
						result = DENIED_NOT_PERMANENTLY_NEWLY;
					}
				}
				else if(beforeClickPermissionRat == true && afterClickPermissionRat == false){
					result = DENIED_PERMANENTLY_NEWLY;
				}
				else if(beforeClickPermissionRat == true && afterClickPermissionRat == true){
					result = DENIED_NOT_PERMANENTLY_ALREADY;
				}
			}
			Log.v(TAG, result);
			PluginResult.Status status = PluginResult.Status.OK;
			mCallbackContext.sendPluginResult(new PluginResult(status, result));
		}
	}

	/**
	 * Execute the plugin action and return a PluginResult.
	 *
	 * @param action          The action to execute.
	 * @param args            JSONArray of arguments for the plugin.
	 * @param callbackContext The callback context used when calling back into JavaScript.
	 * @return boolean Whether the action was valid.
	 */
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		mCallbackContext = callbackContext;
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
			PluginResult.Status status = PluginResult.Status.OK;
			String statusResult = NOT_NEEDED;
			Log.v(TAG, statusResult);
			mCallbackContext.sendPluginResult(new PluginResult(status, statusResult));
 			return true;
		}
		cordova.getThreadPool().execute(() -> {
			try {
				String showRationale = args.getString(0);
				boolean weWantRationale = showRationale.equals("true") ? true : false;

				if (cordova.hasPermission(PERMISSION)) {
					// Already have permission, return ALREADY_GRANTED
					String result = GRANTED_ALREADY;
					Log.v(TAG, result);
					PluginResult.Status status = PluginResult.Status.OK;
					callbackContext.sendPluginResult(new PluginResult(status, result));
				} else if (weWantRationale && shouldShowRationale()) {
					// Set text for extra dialog and its buttons

					String rationaleTitle = args.getString(1);
					String rationaleMsg = args.getString(2);
					String positiveButton = args.getString(3);
					String negativeButton = args.getString(4);
					int rationaleTheme = Integer.parseInt(args.getString(5));
					setExtraDialog(weWantRationale, rationaleTitle, rationaleMsg, positiveButton, negativeButton, rationaleTheme);
					showExtraDialog(false);
				} else {
					// Set the potential last resort dialog
					String showLastResort = args.getString(6);
					boolean weWantLastResort = showLastResort.equals("true") ? true : false;
					String lastResortTitle = args.getString(7);
					String lastResortMsg = args.getString(8);
					String positiveButton = args.getString(9);
					String negativeButton = args.getString(10);
					int lastResortTheme = Integer.parseInt(args.getString(5));
					setExtraDialog(weWantLastResort, lastResortTitle, lastResortMsg, positiveButton, negativeButton, lastResortTheme);
					// Save the status now in order to determine at return whether request is permanently denied.
					beforeClickPermissionRat = shouldShowRationale();
					cordova.requestPermission(mInstance, REQUEST_CODE_PERMISSION, PERMISSION);
				}

			} catch (JSONException e) {
				PluginResult.Status status = PluginResult.Status.ERROR;
				mCallbackContext.sendPluginResult(new PluginResult(status, e.toString()));
			}
		});
		return true;
	}

	/**
	 * Check if rationale for permission should be shown.
	 *
	 * @return True if rationale should be shown, false otherwise.
	 */
	private boolean shouldShowRationale() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(cordova.getActivity(), PERMISSION)) {
			return true;
		}
		return false;
	}
	/**
	 * Show the permission rationale dialog.
	 *
	 * @param title 		 Title of the dialog
	 * @param msg   		The message to be displayed in the dialog.
	 * @param positiveButton The text for the positive button.
	 * @param negativeButton The text for the negative button.
	 * @param theme           The theme resource ID for the dialog.
	 */
	private void setExtraDialog(@NonNull boolean showDialog,
								@NonNull String title,
								@NonNull String msg,
								@NonNull String positiveButton,
								@NonNull String negativeButton,
								@StyleRes int theme){
		mShowDialog = showDialog;
		mTitle = title;
		mMsg = msg;
		mPositiveButton = positiveButton;
		mNegativeButton = negativeButton;
		mTheme = theme;

	}
	/**
	 * @param doSettings     Whether we show a button to settings as OK button
	 */
	public void showExtraDialog(
			@NonNull boolean doSettings
			) {
		if(mShowDialog == false){
			return;
		}
		ClickCallback dialogClickCallback;
		if(doSettings == true){
			dialogClickCallback = mCLickCallbackLastResort;
		}
		else{
			dialogClickCallback = mClickCallbackRationale;
		}
		AppCompatActivity activity = cordova.getActivity();
		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
		Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(DIALOG_ID);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		// Create and show the dialog.
		DialogFragment newFragment = PermissionsDialogFragment.newInstance(
				mTitle, mMsg, mPositiveButton, mNegativeButton, mTheme, dialogClickCallback);
		newFragment.show(ft, DIALOG_ID);
	}
	@Override
	public void onResume(boolean multitasking){
		if(mUserWentToSettings == true){
			String result = "";
			if (cordova.hasPermission(PERMISSION)) {
				// We got permission, return NEWLY_GRANTED_AFTER_LAST_RESORT
				result = GRANTED_NEWLY_AFTER_SETTINGS;

			}
			else{
				result = DENIED_PERMANENTLY_ALREADY_AFTER_SETTINGS;
			}
			Log.v(TAG, result);
			PluginResult.Status status = PluginResult.Status.OK;
			mCallbackContext.sendPluginResult(new PluginResult(status, result));
			mUserWentToSettings = false;
		}

	}
}