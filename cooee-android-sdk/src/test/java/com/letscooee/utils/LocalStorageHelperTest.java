package com.letscooee.utils;

import static com.google.common.truth.Truth.assertThat;
import com.letscooee.BaseTestCase;
import com.letscooee.models.trigger.EmbeddedTrigger;
import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalStorageHelperTest extends BaseTestCase {

    private final String KEY = "TEST_KEY";

    @Test
    public void put_string() {
        String testString = "Local storage Test";
        LocalStorageHelper.putString(context, KEY, testString);

        String storedString = LocalStorageHelper.getString(context, KEY, null);
        assertThat(storedString).isEqualTo(testString);
    }

    @Test
    public void put_string_immediately() {
        String testString = "Local storage Test";
        boolean result = LocalStorageHelper.putStringImmediately(context, KEY, testString);
        assertThat(result).isTrue();
    }

    @Test
    public void putInt() {
        int testInt = 26633;
        LocalStorageHelper.putInt(context, KEY, testInt);

        int storedInt = LocalStorageHelper.getInt(context, KEY, 0);
        assertThat(storedInt).isEqualTo(testInt);
    }

    @Test
    public void putIntImmediately() {
        int testInt = 26633;
        boolean result = LocalStorageHelper.putIntImmediately(context, KEY, testInt);
        assertThat(result).isTrue();
    }

    @Test
    public void putBoolean() {
        boolean testBool = true;
        LocalStorageHelper.putBoolean(context, KEY, testBool);

        boolean storedBoolean = LocalStorageHelper.getBoolean(context, KEY, false);
        assertThat(storedBoolean).isEqualTo(testBool);
    }

    @Test
    public void putBooleanImmediately() {
        boolean testBoolean = true;
        boolean result = LocalStorageHelper.putBooleanImmediately(context, KEY, testBoolean);
        assertThat(result).isTrue();
    }

    @Test
    public void putEmbeddedTriggersImmediately() {
        List<EmbeddedTrigger> triggerList = new ArrayList<>();
        EmbeddedTrigger trigger = new EmbeddedTrigger("TID-1", "EID-1", 789L);
        EmbeddedTrigger trigger1 = new EmbeddedTrigger("TID-2", "EID-2", 789L);
        triggerList.add(trigger);
        triggerList.add(trigger1);

        LocalStorageHelper.putEmbeddedTriggersImmediately(context, KEY, triggerList);

        List<EmbeddedTrigger> resultList = LocalStorageHelper.getEmbeddedTriggers(context, KEY);
        assertThat(resultList).isNotEmpty();
        assertThat(resultList.get(1).getTriggerID()).isEqualTo(trigger1.getTriggerID());
    }

    @Test
    public void putEmbeddedTriggerImmediately() {
        EmbeddedTrigger trigger = new EmbeddedTrigger("TID-1", "EID-1", 789L);
        LocalStorageHelper.putEmbeddedTriggerImmediately(context, KEY, trigger);

        EmbeddedTrigger resultTrigger = LocalStorageHelper.getLastActiveTrigger(context, KEY, null);
        assertThat(resultTrigger.getTriggerID()).isEqualTo(trigger.getTriggerID());
    }

    @Test
    public void remove() {
        EmbeddedTrigger trigger = new EmbeddedTrigger("TID-1", "EID-1", 789L);
        LocalStorageHelper.putEmbeddedTriggerImmediately(context, KEY, trigger);

        LocalStorageHelper.remove(context, KEY);
        EmbeddedTrigger resultTrigger = LocalStorageHelper.getLastActiveTrigger(context, KEY, null);
        assertThat(resultTrigger).isNull();
    }

    @Test
    public void putLong() {
        long testLong = 266330;
        LocalStorageHelper.putLong(context, KEY, testLong);

        long storedLong = LocalStorageHelper.getLong(context, KEY, 0);
        assertThat(storedLong).isEqualTo(testLong);
    }

    @Test
    public void put_map() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", true);
        LocalStorageHelper.putMap(context, KEY, testMap);

        Map<String, Object> resultMap = LocalStorageHelper.getMap(context, KEY, new HashMap<>());
        assertThat(resultMap).isNotEmpty();
        assertThat(resultMap.get("key1")).isEqualTo("value1");
        assertThat(resultMap.get("key2")).isEqualTo(true);
    }
}