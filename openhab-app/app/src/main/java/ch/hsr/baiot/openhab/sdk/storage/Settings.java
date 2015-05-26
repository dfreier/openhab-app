package ch.hsr.baiot.openhab.sdk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by dominik on 23.05.15.
 */
public class Settings {

    private static final String SETTING_ENDPOINT = "endpoint";
    private static final String SETTING_SITEMAP = "sitemap";

    private Context mContext;
    private SharedPreferences mPreferences;

    public Settings(SharedPreferences preferences) {
        mPreferences = preferences;
    }


    public String getEndpoint() {
        return mPreferences.getString(SETTING_ENDPOINT, "");
    }

    public void setEndpoint(String endpoint) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SETTING_ENDPOINT, endpoint);
        editor.commit();
    }


    public String getSitemap() {
        return mPreferences.getString(SETTING_SITEMAP, "");
    }

    public void setSitemap(String sitemap) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SETTING_SITEMAP, sitemap);
        editor.commit();
    }


}
