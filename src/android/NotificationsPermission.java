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
	// Constants for permission status
	public static final String GRANTED = "granted";
	public static final String DENIED = "denied";
	public static final String NOT_NEEDED = "not_needed";
	// Request code for permission request
	private static final int REQUEST_CODE = 1;
	// Dialog ID for managing multiple dialogs
	private static final String DIALOG_ID = "dialog";
	// Callback context for communicating with Cordova
	private CallbackContext mCallbackContext;
	// Instance of NotificationsPermission for referencing in callbacks
	private NotificationsPermission mInstance;
	// ClickCallback for handling positive and negative button clicks
	private ClickCallback mClickCallback = new ClickCallback() {
		@Override
		public void onClick(Status status) {
			if (status == ClickCallback.Status.POSITIVE) {
				cordova.requestPermission(mInstance, REQUEST_CODE, Manifest.permission.POST_NOTIFICATIONS);
			}
			if (status == ClickCallback.Status.NEGATIVE) {
				PluginResult.Status resultStatus = PluginResult.Status.OK;
				mCallbackContext.sendPluginResult(new PluginResult(resultStatus, mInstance.DENIED));
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
				result = GRANTED;
			}
			else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
				result = DENIED;
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
			mCallbackContext.sendPluginResult(new PluginResult(status, NOT_NEEDED));
 			return true;
		}
		cordova.getThreadPool().execute(() -> {
			try {
				AppCompatActivity activity = cordova.getActivity();
				String result;
				String[] perms = {Manifest.permission.POST_NOTIFICATIONS};
				if (cordova.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
					// Already have permission, return GRANTED
					result = GRANTED;
					PluginResult.Status status = PluginResult.Status.OK;
					callbackContext.sendPluginResult(new PluginResult(status, result));
				} else {
					// Set text for extra dialog and its buttons
					String rationaleMsg = args.getString(0);
					String positiveButton = args.getString(1);
					String negativeButton = args.getString(2);
					int theme = Integer.parseInt(args.getString(3));
					if (shouldShowRationale(perms)) {
						showRequestPermissionRationale(
								rationaleMsg, positiveButton, negativeButton, theme, REQUEST_CODE, perms);
					} else {
						cordova.requestPermission(mInstance, REQUEST_CODE, Manifest.permission.POST_NOTIFICATIONS);
					}
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
	 * @param perms The array of requested permissions.
	 * @return True if rationale should be shown, false otherwise.
	 */
	private boolean shouldShowRationale(@NonNull String... perms) {
		for (String perm : perms) {
			if (shouldShowRequestPermissionRationale(perm)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if rationale for a specific permission should be shown.
	 *
	 * @param perm The requested permission.
	 * @return True if rationale should be shown, false otherwise.
	 */
	public boolean shouldShowRequestPermissionRationale(@NonNull String perm) {
		return ActivityCompat.shouldShowRequestPermissionRationale(cordova.getActivity(), perm);
	}

	/**
	 * Show the permission rationale dialog.
	 *
	 * @param rationaleMsg   The message to be displayed in the dialog.
	 * @param positiveButton The text for the positive button.
	 * @param negativeButton The text for the negative button.
	 * @param theme           The theme resource ID for the dialog.
	 * @param requestCode     The request code associated with the permission request.
	 * @param perms           The array of requested permissions.
	 */
	public void showRequestPermissionRationale(
			@NonNull String rationaleMsg,
			@NonNull String positiveButton,
			@NonNull String negativeButton,
			@StyleRes int theme,
			int requestCode,
			@NonNull String... perms) {
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
}