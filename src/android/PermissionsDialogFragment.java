package nl.klaasmaakt.cordova.notifications_permission;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.app.Dialog;
import android.os.Bundle;

/**
 * A DialogFragment to show a rationale dialog for permissions if needed.
 */
public class PermissionsDialogFragment extends DialogFragment {

    // Tag for logging purposes
    private static final String TAG = "PermissionsRationaleDialogFragment";

    // Keys for arguments to be supplied when creating an instance of the fragment
    private static final String KEY_TITLE = "rationale_title";
    private static final String KEY_MSG = "rationale_msg";
    private static final String KEY_POSITIVE_BUTTON = "positive_button";
    private static final String KEY_NEGATIVE_BUTTON = "negative_button";
    private static final String KEY_THEME = "theme";

    // ClickCallback instance to handle positive and negative button clicks
    private static ClickCallback mClickCallback;

    /**
     * Create a new instance of PermissionsDialogFragment with the given arguments.
     *
     * @param title   The title to be displayed in the dialog.
     * @param msg   The message to be displayed in the dialog.
     * @param positiveButton The text for the positive button.
     * @param negativeButton The text for the negative button.
     * @param clickCallback  The callback to handle button clicks.
     * @param theme           The theme resource ID for the dialog.
     * @return A new instance of PermissionsDialogFragment.
     */
    static PermissionsDialogFragment newInstance(
            String title,
            String msg,
            String positiveButton,
            String negativeButton,
            int theme,
            ClickCallback clickCallback) {
        // Set the ClickCallback instance for handling button clicks
        mClickCallback = clickCallback;

        // Create a new instance of the fragment
        PermissionsDialogFragment f = new PermissionsDialogFragment();

        // Supply input as arguments
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_MSG, msg);
        args.putString(KEY_POSITIVE_BUTTON, positiveButton);
        args.putString(KEY_NEGATIVE_BUTTON, negativeButton);
        args.putInt(KEY_THEME, theme);
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
                .setMessage(args.getString(KEY_MSG))
                .setPositiveButton(args.getString(KEY_POSITIVE_BUTTON), (dialog, which) -> {
                    // Call the positive button click callback
                    mClickCallback.onClick(ClickCallback.Status.POSITIVE);
                })
                .setNegativeButton(args.getString(KEY_NEGATIVE_BUTTON), (dialog, which) -> {
                    // Call the negative button click callback
                    mClickCallback.onClick(ClickCallback.Status.NEGATIVE);
                })
                .setTitle(args.getString(KEY_TITLE))
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