package com.letscooee.trigger.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.letscooee.BaseTestCase;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.trigger.InAppTriggerHelper;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;

public class CacheTriggerContentTest extends BaseTestCase {

    CacheTriggerContent cacheTriggerContent;
    @Mock
    InAppTriggerHelper inAppTriggerHelper;

    @Override
    public void setUp() {
        super.setUp();
        super.loadPayload();
        super.makeActiveTrigger();
        MockitoAnnotations.openMocks(this);
        CacheTriggerContent cacheTriggerContent = new CacheTriggerContent(context);
        this.cacheTriggerContent = spy(cacheTriggerContent);
        ReflectionHelpers.setField(this.cacheTriggerContent, "inAppTriggerHelper", inAppTriggerHelper);
    }

    private void commonTriggerLoading(TriggerData triggerData, int times) {
        doNothing().when(inAppTriggerHelper).loadLazyData(any(TriggerData.class), any());
        //cacheTriggerContent.loadAndSaveTriggerData(pendingTrigger, triggerData);
        if (times == 0) {
            verify(inAppTriggerHelper, never()).loadLazyData(any(TriggerData.class), any());
        } else {
            verify(inAppTriggerHelper, times(times)).loadLazyData(any(TriggerData.class), any());
        }
    }

    @Test
    public void load_ian_with_null_trigger_data() {
        commonTriggerLoading(null, 0);
    }

    @Test
    public void load_ian_with_empty_trigger_data() {
        commonTriggerLoading(TriggerData.fromJson("{}"), 0);
    }

    @Test
    public void load_ian_with_valid_trigger_data() {
        commonTriggerLoading(triggerData, 1);
    }

    private void commonContentDownloadDownload(TriggerData triggerData, int times) {
        doNothing().when(cacheTriggerContent).loadImage(anyString(), anyInt());
        cacheTriggerContent.loadAndCacheInAppContent(triggerData);
        if (times == 0) {
            verify(cacheTriggerContent, never()).loadImage(anyString(), anyInt());
        } else {
            verify(cacheTriggerContent, atLeast(times)).loadImage(anyString(), anyInt());
        }
    }

    @Test
    public void load_ian_content_with_null_trigger_content() {
        commonContentDownloadDownload(null, 0);
    }

    @Test
    public void load_ian_content_with_empty_trigger_content() {
        commonContentDownloadDownload(TriggerData.fromJson("{}"), 0);
    }

    @Test
    public void load_ian_content_with_valid_trigger_content() {
        commonContentDownloadDownload(triggerData, 1);
    }
}