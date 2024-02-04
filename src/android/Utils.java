package nl.klaasmaakt.cordova.notifications_permission;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {

    private static final String SHARED_PREFERENCES_KEY = "shared_preferences_key";
    private static final String SP_RATIONALE_HAS_BEEN_NEEDED_BEFORE_KEY = "rationale_has_been_needed_before";
    private static final String SP_WE_HAVE_BEEN_HERE_BEFORE_KEY = "we_have_been_here_before";
    private static final String SP_PERMISSION_HAS_BEEN_GRANTED_BEFORE_KEY = "permission_has_been_granted_before";
    private static final String SP_LAST_RESORT_HAS_SHOWN_KEY = "last_resort_has_shown";

    private final SharedPreferences sharedPreferences;

    public Utils(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public void saveLastResortHasShown() {
        setSavedPref(SP_LAST_RESORT_HAS_SHOWN_KEY);
    }
    public boolean getLastResortHasShown() {
        return getSavedPref(SP_LAST_RESORT_HAS_SHOWN_KEY);
    }
    public void saveRationaleHasBeenNeededBefore() {
        setSavedPref(SP_RATIONALE_HAS_BEEN_NEEDED_BEFORE_KEY);
    }
    public boolean getRationaleHasBeenNeededBefore() {
        return getSavedPref(SP_RATIONALE_HAS_BEEN_NEEDED_BEFORE_KEY);
    }
    public void saveHaveWeBeenHereBefore() {
        setSavedPref(SP_WE_HAVE_BEEN_HERE_BEFORE_KEY);
    }
    public boolean getHaveWeBeenHereBefore() {
        return getSavedPref(SP_WE_HAVE_BEEN_HERE_BEFORE_KEY);
    }
    public void savePermissionHasBeenGrantedBefore() {
        setSavedPref(SP_PERMISSION_HAS_BEEN_GRANTED_BEFORE_KEY);
    }
    public boolean getPermissionHasBeenGrantedBefore() {
        return getSavedPref(SP_PERMISSION_HAS_BEEN_GRANTED_BEFORE_KEY);
    }
    private void setSavedPref(String pref) {
        sharedPreferences.edit().putBoolean(pref, true).apply();
    }
    private boolean getSavedPref(String pref) {
        return sharedPreferences.getBoolean(pref, false);
    }
}
