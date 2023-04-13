package com.jeeps.gamecollector.deprecated.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.deprecated.model.CurrentUser;

public class UserUtils {

    public static CurrentUser getCurrentUser(Context context, SharedPreferences sharedPreferences) {
        String username = sharedPreferences.getString(context.getString(R.string.current_user_username), "");
        String uid = sharedPreferences.getString(context.getString(R.string.current_user_uid), "");
        String token = sharedPreferences.getString(context.getString(R.string.current_user_token), "");
        return new CurrentUser(uid, token, username);
    }

    public static void saveCurrentUserData(
            Context context, SharedPreferences sharedPreferences,
            String username, String uid, String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.current_user_username), username);
        editor.putString(context.getString(R.string.current_user_uid), uid);
        editor.putString(context.getString(R.string.current_user_token), token);
        editor.apply();
    }

    public static void updateToken(
            Context context, SharedPreferences sharedPreferences,
            String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.current_user_token), token);
        editor.apply();
    }
}
