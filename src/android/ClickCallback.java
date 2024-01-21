package nl.klaasmaakt.cordova.notifications_permission;

/**
 * Callback interface for handling positive and negative actions in a dialog.
 */
public interface ClickCallback {

    /**
     * Enum representing the status of a click action.
     * It can be either positive ("yes" clicked) or negative ("cancel" clicked).
     */
    enum Status {
        POSITIVE,
        NEGATIVE
    }

    /**
     * Called when the user performs a click action in the dialog.
     *
     * @param status The status of the click action (POSITIVE or NEGATIVE).
     */
    void onClick(Status status);
}
