package com.letscooee.cooeetester;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.letscooee.services.CooeeFirebaseMessagingService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended class of FirebaseMessagingService to make sure multiple FirebaseMessagingService classes can work.
 *
 * @author Abhishek Taparia
 */
public class ParentFirebaseMessagingService extends FirebaseMessagingService {
    private List<FirebaseMessagingService> messagingServices = new ArrayList<>(2);

    public ParentFirebaseMessagingService() {
        messagingServices.add(new MyFirebaseMessagingService());
        messagingServices.add(new CooeeFirebaseMessagingService());
    }

    @Override
    public void onNewToken(String s) {
        delegate(service -> {
            injectContext(service);
            service.onNewToken(s);
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        delegate(service -> {
            injectContext(service);
            service.onMessageReceived(remoteMessage);
        });
    }

    private void delegate(Action<FirebaseMessagingService> action) {
        for (FirebaseMessagingService service : messagingServices) {
            action.run(service);
        }
    }

    private void injectContext(FirebaseMessagingService service) {
        setField(service, "mBase", this);
    }

    private boolean setField(Object targetObject, String fieldName, Object fieldValue) {
        Field field;
        try {
            field = targetObject.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = null;
        }
        Class superClass = targetObject.getClass().getSuperclass();
        while (field == null && superClass != null) {
            try {
                field = superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                superClass = superClass.getSuperclass();
            }
        }
        if (field == null) {
            return false;
        }
        field.setAccessible(true);
        try {
            field.set(targetObject, fieldValue);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    interface Action<T> {
        void run(T t);
    }
}
