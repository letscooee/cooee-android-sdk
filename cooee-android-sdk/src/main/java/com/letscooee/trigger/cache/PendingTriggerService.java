package com.letscooee.trigger.cache;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.google.gson.Gson;
import com.letscooee.BuildConfig;
import com.letscooee.ContextAware;
import com.letscooee.enums.trigger.PendingTriggerAction;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.LazyTriggerLoader;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static com.letscooee.utils.Constants.TAG;

/**
 * Store the payload and cache all the content of InApp to local cache.
 *
 * @author Ashish Gaikwad 25/05/22
 * @since 1.3.12
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PendingTriggerService extends ContextAware {

    private final Gson gson = new Gson();
    private final CooeeDatabase cooeeDatabase;

    public PendingTriggerService(Context context) {
        super(context);
        cooeeDatabase = CooeeDatabase.getInstance(context);
    }

    /**
     * Loads lazy data for the {@code triggerData} and update it in the local storage.
     *
     * @param pendingTrigger {@link PendingTrigger} object whose data need to be updated.
     * @param triggerData    the trigger data to be loaded and cached.
     */
    // TODO Update test cases and make it private
    public void lazyLoadAndUpdate(PendingTrigger pendingTrigger, TriggerData triggerData) {
        try {
            new LazyTriggerLoader(triggerData).load();
        } catch (ExecutionException | InterruptedException e) {
            return;
        }

        pendingTrigger.data = triggerData;

        this.cooeeDatabase.pendingTriggerDAO().update(pendingTrigger);
        Log.v(TAG, "Updated " + pendingTrigger);
    }

    public PendingTrigger findForTrigger(TriggerData triggerData) {
        return cooeeDatabase.pendingTriggerDAO().getByID(triggerData.getId());
    }

    /**
     * Add new {@link PendingTrigger} to the database.
     *
     * @param triggerData {@link TriggerData} to be added.
     * @return {@link PendingTrigger} added. null if failed.
     */
    public PendingTrigger newTrigger(TriggerData triggerData) {
        if (triggerData == null || TextUtils.isEmpty(triggerData.getId())) {
            return null;
        }

        if (triggerData.shouldLazyLoad()) {
            return null;
        }

        PendingTrigger pendingTrigger = new PendingTrigger();
        pendingTrigger.dateCreated = new Date().getTime();
        pendingTrigger.triggerId = triggerData.getId();
        pendingTrigger.data = triggerData;
        pendingTrigger.scheduleAt = 0;
        pendingTrigger.notificationId = triggerData.getNotificationID();
        pendingTrigger.sdkCode = BuildConfig.VERSION_CODE;
        pendingTrigger.id = this.cooeeDatabase.pendingTriggerDAO().insert(pendingTrigger);
        Log.v(TAG, "Created " + pendingTrigger);

        // After saving the main trigger, pull the lazy data from backend and update it
        // So that even OS allowed time to render push is finished, at least base trigger is stored
        this.lazyLoadAndUpdate(pendingTrigger, triggerData);

        return pendingTrigger;
    }

    /**
     * Pull the latest pending trigger from the DB but do not delete it.
     *
     * @return The last stored PendingTrigger.
     */
    public PendingTrigger peep() {
        return this.cooeeDatabase.pendingTriggerDAO().getFirst();
    }

    /**
     * Remove the {@link PendingTrigger} from the database. If the trigger is not found, it will do nothing.
     *
     * @param action    {@link PendingTriggerAction} to be perform.
     * @param triggerID the trigger id to be removed.
     */
    public void delete(PendingTriggerAction action, String triggerID) {
        if (action == null) {
            return;
        }

        if (action == PendingTriggerAction.DELETE_ALL) {
            // delete all trigger
            this.cooeeDatabase.pendingTriggerDAO().deleteAll();
        } else if (action == PendingTriggerAction.DELETE_ID && !TextUtils.isEmpty(triggerID)) {
            // delete all loaded trigger
            this.cooeeDatabase.pendingTriggerDAO().deleteByID(triggerID);
        }
    }

    /**
     * Removes {@link PendingTrigger} related {@link TriggerData}
     *
     * @param triggerData {@link TriggerData} whose pending trigger need to be removed
     */
    public void delete(TriggerData triggerData) {
        this.delete(PendingTriggerAction.DELETE_ID, triggerData.getId());
    }

    /**
     * Removes {@link PendingTrigger}.
     */
    public void delete(PendingTrigger pendingTrigger) {
        Log.v(TAG, "Deleting PendingTrigger");
        this.cooeeDatabase.pendingTriggerDAO().delete(pendingTrigger);
    }

}
