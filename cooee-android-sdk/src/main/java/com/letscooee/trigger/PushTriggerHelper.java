package com.letscooee.trigger;

import android.content.Context;
import androidx.annotation.RestrictTo;
import androidx.core.app.NotificationManagerCompat;
import com.letscooee.CooeeFactory;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.cache.PendingTriggerService;
import com.letscooee.utils.Logger;
import java.util.Map;

/**
 * A helper class to render or clear the push notification based triggers.
 *
 * @author Shashank Agrawal
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PushTriggerHelper {

    private final Context context;
    private final TriggerData triggerData;
    private final PendingTriggerService pendingTriggerService;
    private final Logger logger;

    public PushTriggerHelper(Context context, TriggerData triggerData) {
        this.context = context;
        this.triggerData = triggerData;
        this.pendingTriggerService = CooeeFactory.getPendingTriggerService();
        this.logger = CooeeFactory.getLogger();
    }

    public void removePushFromTray() {
        PendingTrigger pendingTrigger = this.pendingTriggerService.findForTrigger(triggerData);
        if (pendingTrigger == null) {
            logger.debug("No pending trigger found for " + triggerData);
            return;
        }

        this.deleteNotification(pendingTrigger);
        this.pendingTriggerService.delete(pendingTrigger);
    }

    private void deleteNotification(PendingTrigger pendingTrigger) {
        Map<String, Object> triggerConfig = triggerData.getConfig();
        if (!shouldRemovePushFromTray(triggerConfig)) {
            return;
        }

        NotificationManagerCompat
                .from(context)
                .cancel((int) pendingTrigger.notificationId);
    }

    /**
     * Check for the {@code rmPN} key in given map to manage notification in the notification tray.
     * <br><br>
     * {@code rmPN} basically stands for <b>Remove Push Notification</b> from tray. It will be {@code true}
     * to remove PN from tray other wise {@code false}.
     * <b>By default if value is absent it will be {@code true}</b>.
     * <ul>
     * <li>If given <code>map</code> is <code>null</code> it will return <code>true</code>
     *     (As default value to close PN is true).</li>
     * <li>If given <code>map.get("rmPN")</code> is <code>null</code> it will return
     *     <code>true</code> (As default value to close PN is true).</li>
     * <li>If map.get("rmPN") is present it it will provide its value.</li>
     * </ul>
     *
     * @param triggerConfig Configuration to remove push notification from tray
     * @return Returns true to remove PN from tray other wise false
     */
    private boolean shouldRemovePushFromTray(Map<String, Object> triggerConfig) {
        //noinspection ConstantConditions
        return triggerConfig == null || triggerConfig.get("rmPN") == null || ((boolean) triggerConfig.get("rmPN"));
    }

}
