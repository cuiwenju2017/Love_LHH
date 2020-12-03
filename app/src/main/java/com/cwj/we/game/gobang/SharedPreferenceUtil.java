package com.cwj.we.game.gobang;

import android.content.Context;
import android.preference.PreferenceActivity;


/**
 * Created by yangjinxiao on 2016/7/4.
 */
public class SharedPreferenceUtil {

    public static String getSharedPreferences(String propertyName, Context context) {
        return context.getSharedPreferences(Constants.SharedPreferenceConstant.PREFERENCE_NAME, PreferenceActivity.MODE_MULTI_PROCESS).getString(propertyName, "");
    }

    public static String getSharedPreferences(String propertyName, String defaultValue, Context context) {
        return context.getSharedPreferences(Constants.SharedPreferenceConstant.PREFERENCE_NAME, PreferenceActivity.MODE_PRIVATE).getString(propertyName, defaultValue);
    }

    public static int getSharedPreferences(String propertyName, Context context, int defaultValue) {
        return context.getSharedPreferences(Constants.SharedPreferenceConstant.PREFERENCE_NAME, PreferenceActivity.MODE_PRIVATE).getInt(propertyName, defaultValue);
    }

    public static long getSharedPreferences(String propertyName, Context context, long defaultValue) {
        return context.getSharedPreferences(Constants.SharedPreferenceConstant.PREFERENCE_NAME, PreferenceActivity.MODE_PRIVATE).getLong(propertyName, defaultValue);
    }

    public static boolean setSharedPreferences(String propertyName, String propertyValue, Context context) {
        return context.getSharedPreferences(Constants.SharedPreferenceConstant.PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putString(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String propertyName, int propertyValue, Context context) {
        return context.getSharedPreferences(Constants.SharedPreferenceConstant.PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putInt(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String preferenceName, String propertyName, long propertyValue, Context context) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit()
                .putLong(propertyName, propertyValue).commit();
    }

}
