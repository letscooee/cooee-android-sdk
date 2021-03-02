package com.letscooee.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Abhishek Taparia
 */
public class Utility {

    public static ArrayList<HashMap<String, String>> getArrayListFromString(String stringList) {
        ArrayList<HashMap<String, String>> triggerHashMapList;

        Gson gson = new Gson();
        triggerHashMapList = gson.fromJson(stringList, new TypeToken<ArrayList<HashMap<String, String>>>() {
        }.getType());

        return triggerHashMapList;
    }

    public static ArrayList<HashMap<String, String>> getActiveArrayListFromString(String stringList) {
        if (stringList.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<HashMap<String, String>> allTriggerList = getArrayListFromString(stringList);
        ArrayList<HashMap<String, String>> activeTriggerList = new ArrayList<>();

        for (HashMap<String, String> map : allTriggerList) {
            long time = Long.parseLong(map.get("duration"));
            long currentTime = new Date().getTime();
            if (time > currentTime) {
                activeTriggerList.add(map);
            }
        }

        return activeTriggerList;
    }
}
