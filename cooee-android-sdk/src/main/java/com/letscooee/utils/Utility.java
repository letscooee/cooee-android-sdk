package com.letscooee.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Abhishek Taparia
 */
public class Utility {

    public static ArrayList<HashMap<String, String>> getArrayListFromString(String stringList) {
        ArrayList<HashMap<String, String>> triggerHashMapList = new ArrayList<>();

        Gson gson = new Gson();
        HashMap<String, String>[] hashMaps = gson.fromJson(stringList, HashMap[].class);
        Collections.addAll(triggerHashMapList,hashMaps);

        return triggerHashMapList;
    }

    public static ArrayList<HashMap<String, String>> getActiveArrayListFromString(String stringList) {
        if (stringList.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<HashMap<String, String>> allTriggerList = getArrayListFromString(stringList);
        ArrayList<HashMap<String, String>> activeTriggerList = new ArrayList<>();

        for (HashMap<String, String> map : allTriggerList) {
            long time = (long) Double.parseDouble(String.valueOf(map.get("duration")));
            long currentTime = new Date().getTime();
            if (time > currentTime) {
                activeTriggerList.add(map);
            }
        }

        return activeTriggerList;
    }
}
