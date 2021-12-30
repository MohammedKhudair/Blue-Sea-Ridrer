package com.barmej.bluesea.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper {
    private static final String RIDER_ID = "RIDER_ID";
    private static final String ASSIGN_TRIP = "ASSIGN_TRIP";


    public static void setRiderId(String id, Context content){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(content);
        sp.edit().putString(RIDER_ID, id).apply();
    }

    public static String getRiderId(Context content){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(content);
        return sp.getString(RIDER_ID,"");
    }

    public static void setAssignTrip(String id, Context content){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(content);
        sp.edit().putString(ASSIGN_TRIP, id).apply();
    }

    public static String getAssignTrip(Context content){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(content);
        return sp.getString(ASSIGN_TRIP,"");
    }
}
