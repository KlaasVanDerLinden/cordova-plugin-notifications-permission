package nl.klaasmaakt.cordova.notifications_permission;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.app.Dialog;
import android.os.Bundle;

/**
 * A DialogFragment to show a rationale dialog for permissions if needed.
 */
public class PermissionsRationaleDialogFragment extends DialogFragment {

    // Tag for logging purposes
    private static final String TAG = "PermissionsRationaleDialogFragment";

    // Keys for arguments to be supplied when creating an instance of the fragment
    private static final String KEY_RATIONALE_MESSAGE = "rationaleMsg";
    private static final String KEY_POSITIVE_BUTTON = "positiveButton";
    private static final String KEY_NEGATIVE_BUTTON = "negativeButton";
    private static final String KEY_THEME = "theme";
    private static final String KEY_REQUEST_CODE = "requestCode";

    // ClickCallback instance to handle positive and negative button clicks
    private static ClickCallback mClickCallback;

    /**
     * Create a new instance of PermissionsRationaleDialogFragment with the given arguments.
     *
     * @param rationaleMsg   The message to be displayed in the dialog.
     * @param positiveButton The text for the positive button.
     * @param negativeButton The text for the negative button.
     * @param clickCallback  The callback to handle button clicks.
     * @param theme           The theme resource ID for the dialog.
     * @param requestCode     The request code associated with the permission request.
     * @return A new instance of PermissionsRationaleDialogFragment.
     */
    static PermissionsRationaleDialogFragment newInstance(
            String rationaleMsg,
            String positiveButton,
            String negativeButton,
            ClickCallback clickCallback,
            int theme,
            int requestCode) {
        // Set the ClickCallback instance for handling button clicks
        mClickCallback = clickCallback;

        // Create a new instance of the fragment
        PermissionsRationaleDialogFragment f = new PermissionsRationaleDialogFragment();

        // Supply input as arguments
        Bundle args = new Bundle();
        args.putString(KEY_RATIONALE_MESSAGE, rationaleMsg);
        args.putString(KEY_POSITIVE_BUTTON, positiveButton);
        args.putString(KEY_NEGATIVE_BUTTON, negativeButton);
        args.putInt(KEY_THEME, theme);
        args.putInt(KEY_REQUEST_CODE, requestCode);
        f.setArguments(args);

        return f;
    }

    /**
     * Called to create the dialog, including setting its content and button actions.
     *
     * @param savedInstanceState A Bundle containing the saved state, or null if not applicable.
     * @return The created Dialog.
     */
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Retrieve arguments passed to the fragment
        Bundle args = getArguments();

        // Ensure the dialog is not cancelable by tapping outside it
        setCancelable(false);

        // Create an AlertDialog with the specified theme and message
        return new AlertDialog.Builder(requireContext(), args.getInt(KEY_THEME))
                .setMessage(args.getString(KEY_RATIONALE_MESSAGE))
                .setPositiveButton(args.getString(KEY_POSITIVE_BUTTON), (dialog, which) -> {
                    // Call the positive button click callback
                    mClickCallback.onClick(ClickCallback.Status.POSITIVE);
                })
                .setNegativeButton(args.getString(KEY_NEGATIVE_BUTTON), (dialog, which) -> {
                    // Call the negative button click callback
                    mClickCallback.onClick(ClickCallback.Status.NEGATIVE);
                })
                .create();
    }

    /**
     * Called when the fragment is being destroyed, ensuring proper detachment.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Ensure proper detachment by setting ClickCallback to null
        mClickCallback = null;
    }
}