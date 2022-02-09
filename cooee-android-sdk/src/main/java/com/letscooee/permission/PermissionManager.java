package com.letscooee.permission;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.letscooee.init.DefaultUserPropertiesCollector;
import com.letscooee.utils.PermissionType;

import java.util.HashMap;
import java.util.Map;

/**
 * Performs permission related work like check for granted/denied permissions and provide related data in a map
 *
 * @author Ashish Gaikwad 25/11/21
 * @since 1.1.0
 */
public class PermissionManager {

    private final PermissionType[] permissions;
    private final Context context;
    private final DefaultUserPropertiesCollector devicePropsCollector;

    public PermissionManager(Context context) {
        this.context = context;
        this.permissions = PermissionType.values();
        this.devicePropsCollector = new DefaultUserPropertiesCollector(context);
    }

    /**
     * Check for all permissions and fetch permission related data and add to <code>deviceProps</code>
     *
     * @return {@link Map} of permission info
     */
    @NonNull
    public Map<String, Object> getPermissionInformation() {
        Map<String, Object> permissionMap = new HashMap<>();
        Map<String, Object> deviceProps = new HashMap<>();

        for (PermissionType permissionType : permissions) {

            boolean permissionStatus = ContextCompat.checkSelfPermission(context,
                    permissionType.toString()) == PackageManager.PERMISSION_GRANTED;

            permissionMap.put(permissionType.name(), permissionStatus ? "GRANTED" : "DENIED");

            if (permissionType == PermissionType.LOCATION && permissionStatus) {
                double[] location = devicePropsCollector.getLocation();
                deviceProps.put("coords", location);
            }

            deviceProps.put("perm", permissionMap);
        }

        String[] networkData = devicePropsCollector.getNetworkData();
        Map<String, Object> network = new HashMap<>();
        network.put("opr", networkData[0]);
        network.put("type", networkData[1]);
        deviceProps.put("net", network);

        return deviceProps;
    }
}
