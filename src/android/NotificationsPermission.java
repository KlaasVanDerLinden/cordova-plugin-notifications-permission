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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
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
	public static final String NEWLY_GRANTED_AFTER_RATIONALE = "newly_granted_after_rationale";
	public static final String NEWLY_GRANTED_WITHOUT_RATIONALE = "newly_granted_without_rationale";
	public static final String ALREADY_GRANTED = "already_granted";
	public static final String ALREADY_DENIED_PERMANENTLY = "already_denied_permanently";
	public static final String NEWLY_DENIED_PERMANENTLY = "newly_denied_permanently";
	public static final String ALREADY_DENIED_NOT_PERMANENTLY = "already_denied_not_permanently";
	public static final String NEWLY_DENIED_NOT_PERMANENTLY = "newly_denied_not_permanently";
	public static final String DENIED_THROUGH_RATIONALE_DIALOG = "denied_through_rationale_dialog";
	public static final String NOT_NEEDED = "not_needed";
	// Request code for permission request
	private static final int REQUEST_CODE = 1;
	// Key to store in sharedpreference whether the notifcication has needed a rationale before
	private static final String SHARED_PREFERENCES_KEY = "shared_preferences_key";
	private static final String SP_RATIONALE_HAS_BEEN_NEEDED_BEFORE_KEY = "Rationale_has_been_needed_before";
	private static final String SP_WE_HAVE_BEEN_HERE_BEFORE_KEY = "we_have_been_here_before";
	// Stores the before state of shouldRequestPermissionRationale to differentiate between permanently and temporarily denied.
	private boolean beforeClickPermissionRat;
	// Dialog ID for managing multiple dialogs
	private static final String DIALOG_ID = "dialog";
	// Callback context for communicating with Cordova
	private CallbackContext mCallbackContext;
	// Instance of NotificationsPermission for referencing in callbacks
	private NotificationsPermission mInstance;
	// Keeps track of whether the rationale has shown before making new requestPermission
	private boolean hasPassedRationale = false;
	// ClickCallback for handling positive and negative button clicks
	private ClickCallback mClickCallback = new ClickCallback() {
		@Override
		public void onClick(Status status) {
			if (status == ClickCallback.Status.POSITIVE) {
				beforeClickPermissionRat = mInstance.shouldShowRationale();
				hasPassedRationale = true;
				cordova.requestPermission(mInstance, REQUEST_CODE, PERMISSION);
			}
			if (status == ClickCallback.Status.NEGATIVE) {
				PluginResult.Status resultStatus = PluginResult.Status.OK;
				String permissionStatus = mInstance.DENIED_THROUGH_RATIONALE_DIALOG;
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
		super.onRequestPermissionResult(requestCode,permissions,grantResults);
		if (requestCode == REQUEST_CODE) {
			String result = "undefined";
			if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
				if(hasPassedRationale == true){
					result = NEWLY_GRANTED_AFTER_RATIONALE;
				}
				else {
					result = NEWLY_GRANTED_WITHOUT_RATIONALE;
				}
			}
			else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
				/* We need to check whether we have been in the process or the user has just started
				 * if the rationale dialog has been needed before we know this is not the first start
				 */
				boolean rationaleHasBeenNeededBefore = getRationaleHasBeenNeededBefore();

				/* Also check whether we arrive her for a second time, so we can set already denied
				 * properly next time.
				 */
				boolean haveWeBeenHereBefore = getHaveWeBeenHereBefore();
				saveHaveWeBeenHereBefore();
				boolean afterClickPermissionRat = shouldShowRationale();
				// if true than we save it so we know this is not the first time
				if(afterClickPermissionRat == true){
					saveRationaleHasBeenNeededBefore();
				}
				/* Since we know te state of shouldShowRationale before and after we requestPermission
				 * we know that there is still no need to show the rationale so was already denied
				 * or the whole process hasn't started yet since the first dialog has never finished.
				 */
				if(beforeClickPermissionRat == false && afterClickPermissionRat == false){
					/* We have had the first dialog a while ago, so this is the end */
					if(rationaleHasBeenNeededBefore == true){
						result = ALREADY_DENIED_PERMANENTLY;
					}
					else if(haveWeBeenHereBefore == true){
						result = ALREADY_DENIED_NOT_PERMANENTLY;
					}
					else{
						result = NEWLY_DENIED_NOT_PERMANENTLY;
					}
				}
				else if(beforeClickPermissionRat == false && afterClickPermissionRat == true){
					if(haveWeBeenHereBefore == true) {
						result = ALREADY_DENIED_NOT_PERMANENTLY;
					}
					else{
						result = NEWLY_DENIED_NOT_PERMANENTLY;
					}
				}
				else if(beforeClickPermissionRat == true && afterClickPermissionRat == false){
					result = NEWLY_DENIED_PERMANENTLY;
				}
				else if(beforeClickPermissionRat == true && afterClickPermissionRat == true){
					result = ALREADY_DENIED_NOT_PERMANENTLY;
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
				if (cordova.hasPermission(PERMISSION)) {
					// Already have permission, return ALREADY_GRANTED
					String result = ALREADY_GRANTED;
					Log.v(TAG, result);
					PluginResult.Status status = PluginResult.Status.OK;
					callbackContext.sendPluginResult(new PluginResult(status, result));
				} else if (shouldShowRationale()) {
					// Set text for extra dialog and its buttons
					String rationaleMsg = args.getString(0);
					String positiveButton = args.getString(1);
					String negativeButton = args.getString(2);
					int theme = Integer.parseInt(args.getString(3));
					showRequestPermissionRationale(
							rationaleMsg, positiveButton, negativeButton, theme, REQUEST_CODE);
				} else {
					// Save the status now in order to determine at return whether request is permanently denied.
					beforeClickPermissionRat = shouldShowRationale();
					cordova.requestPermission(mInstance, REQUEST_CODE, PERMISSION);
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
	 * @param rationaleMsg   The message to be displayed in the dialog.
	 * @param positiveButton The text for the positive button.
	 * @param negativeButton The text for the negative button.
	 * @param theme           The theme resource ID for the dialog.
	 * @param requestCode     The request code associated with the permission request.
	 */
	public void showRequestPermissionRationale(
			@NonNull String rationaleMsg,
			@NonNull String positiveButton,
			@NonNull String negativeButton,
			@StyleRes int theme,
			int requestCode) {
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
		DialogFragment newFragment = PermissionsRationaleDialogFragment.newInstance(
				rationaleMsg, positiveButton, negativeButton, mClickCallback, theme, requestCode);
		newFragment.show(ft, DIALOG_ID);
	}
	public void saveRationaleHasBeenNeededBefore(){
		setSavedPref(SP_RATIONALE_HAS_BEEN_NEEDED_BEFORE_KEY);
	}
	public boolean getRationaleHasBeenNeededBefore(){
		return getSavedPref(SP_RATIONALE_HAS_BEEN_NEEDED_BEFORE_KEY);
	}
	public void saveHaveWeBeenHereBefore() {
		setSavedPref(SP_WE_HAVE_BEEN_HERE_BEFORE_KEY);
	}
	public boolean getHaveWeBeenHereBefore(){
		return getSavedPref(SP_WE_HAVE_BEEN_HERE_BEFORE_KEY);
	}
	public void setSavedPref(String pref){
		Context context = cordova.getContext();
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY,Context.MODE_PRIVATE);
		sharedPreferences.edit().putString(pref, "true").apply();
	}
	public boolean getSavedPref(String pref){
		Context context = cordova.getContext();
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY,Context.MODE_PRIVATE);
		String savedPref = sharedPreferences.getString(pref, "false");
		return savedPref.equals("true") ? true : false;
	}
}