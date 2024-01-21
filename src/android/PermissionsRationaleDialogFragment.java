package nl.klaasmaakt.cordova.notifications_permission;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.app.Dialog;
import android.os.Bundle;

public class PermissionsRationaleDialogFragment extends DialogFragment{
    private static final String TAG = "PermissionsRationaleDialogFragment";

    private static final String KEY_RATIONALE_MESSAGE = "rationaleMsg";
    private static final String KEY_POSITIVE_BUTTON = "positiveButton";
    private static final String KEY_NEGATIVE_BUTTON = "negativeButton";
     private static final String KEY_THEME = "theme";
    private static final String KEY_REQUEST_CODE = "requestCode";
    private static ClickCallback mClickCallback;

    static PermissionsRationaleDialogFragment newInstance(
            String rationaleMsg,
            String positiveButton,
            String negativeButton,
            ClickCallback clickCallback,
            int theme,
            int requestCode) {
        mClickCallback = clickCallback;
        PermissionsRationaleDialogFragment f = new PermissionsRationaleDialogFragment();

        // Supply input as arguments.
        Bundle args = new Bundle();
        args.putString(KEY_RATIONALE_MESSAGE, rationaleMsg);
        args.putString(KEY_POSITIVE_BUTTON, positiveButton);
        args.putString(KEY_NEGATIVE_BUTTON, negativeButton);
        args.putInt(KEY_THEME, theme);
        args.putInt(KEY_REQUEST_CODE, requestCode);
        f.setArguments(args);

        return f;
    }
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        setCancelable(false);
        return new AlertDialog.Builder(requireContext(),
                args.getInt(KEY_THEME))
                .setMessage(args.getString(KEY_RATIONALE_MESSAGE))
                .setPositiveButton(args.getString(KEY_POSITIVE_BUTTON), (dialog, which) -> {
                    mClickCallback.onClick(ClickCallback.Status.POSITIVE);
                } )
                .setNegativeButton(args.getString(KEY_NEGATIVE_BUTTON), (dialog, which) -> {
                    mClickCallback.onClick(ClickCallback.Status.NEGATIVE);
                } )
                .create();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mClickCallback = null; // Ensure proper detachment
    }
}
