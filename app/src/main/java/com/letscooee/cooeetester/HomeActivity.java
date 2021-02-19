package com.letscooee.cooeetester;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.letscooee.CooeeSDK;
import com.letscooee.cooeetester.databinding.ActivityHomeBinding;
import com.letscooee.utils.InAppNotificationClickListener;
import com.letscooee.utils.PropertyNameException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements InAppNotificationClickListener {
    private CooeeSDK cooeeSDK;
    private final String TAG = "HomeActivity";
    private ActivityHomeBinding binding;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        cooeeSDK = CooeeSDK.getDefaultInstance(this);
        cooeeSDK.setInAppNotificationButtonListener(this);

        new CountDownTimer(1000, 3000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {

                binding.ivSplash.setVisibility(View.GONE);
            }
        }.start();

        /**
         * blur image code
         * */
        /*Bitmap mIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.homepage);
        //noinspection deprecation
        binding.parent.setBackground(new BitmapDrawable(BlurBuilder.blur(this, mIcon)));*/


       /* Map<String, String> userData = new HashMap<>();
        userData.put("name", "Ashish Gaikwad");
        userData.put("mobile", "9999999999");
        userData.put("email", "ashish@wizpanda.com");*/
        /*try {
            cooeeSDK.setCurrentScreen(TAG);
            cooeeSDK.sendEvent("onCreate", new HashMap<>());
            cooeeSDK.updateUserData(userData);
            Log.d(TAG, "UUID : "+ cooeeSDK.getUUID());
        } catch (Exception ignored) {
        }*/
        try {
            cooeeSDK.setCurrentScreen(TAG);

        } catch (Exception ignored) {
        }
        try {
            cooeeSDK.sendEvent("onCreate", new HashMap<>());
        } catch (Exception ignored) {
        }
        try {
           /* cooeeSDK.updateUserData(userData);
            cooeeSDK.updateUserProfile(userData, new HashMap<>());
            cooeeSDK.updateUserProperties(new HashMap<>());*/
        } catch (Exception ignored) {
        }
        try {
            Log.d(TAG, "************************************UUID : " + cooeeSDK.getUUID());
        } catch (Exception ignored) {
        }
        binding.btnSendImageEvent.setOnClickListener(view -> {
            try {
                cooeeSDK.sendEvent("image", new HashMap<>());
                Log.d(TAG, "****************************Image Event Sent");
            } catch (PropertyNameException e) {
                Log.e(TAG, "******************************Failed Image Event", e);
                e.printStackTrace();
            }
        });
        binding.btnSendVideoEvent.setOnClickListener(view -> {
            try {
                cooeeSDK.sendEvent("video", new HashMap<>());
                Log.d(TAG, "****************************Video Event Sent");
            } catch (PropertyNameException e) {
                Log.e(TAG, "******************************Failed Video Event", e);
                e.printStackTrace();
            }
        });
        /*Dexter.withContext(this).withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                Map<String, String> map = new HashMap<>();

                List<PermissionDeniedResponse> deniedList = multiplePermissionsReport.getDeniedPermissionResponses();
                for (PermissionDeniedResponse permission : deniedList) {
                    if (ContextCompat.checkSelfPermission(context, permission.getPermissionName()) == PackageManager.PERMISSION_GRANTED) {
                        map.put(permission.getPermissionName(), "DENIED");
                    }
                }
                List<PermissionGrantedResponse> grantedList = multiplePermissionsReport.getGrantedPermissionResponses();
                for (PermissionGrantedResponse permission : grantedList) {
                    if (ContextCompat.checkSelfPermission(context, permission.getPermissionName()) == PackageManager.PERMISSION_GRANTED) {
                        map.put(permission.getPermissionName(), "GRANTED");
                    }
                }
                try {
                    cooeeSDK.sendEvent("permissions", map);
                } catch (PropertyNameException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                Map<String, String> map = new HashMap<>();
                for (PermissionRequest permission : list) {
                    if (ContextCompat.checkSelfPermission(context, permission.getName()) == PackageManager.PERMISSION_GRANTED) {
                        map.put(permission.getName(), "GRANTED");
                    } else {
                        map.put(permission.getName(), "DENIED");
                    }
                }
                try {
                    cooeeSDK.sendEvent("permissions", map);
                } catch (PropertyNameException e) {
                    e.printStackTrace();
                }
                permissionToken.cancelPermissionRequest();
            }
        }).onSameThread().check();*/
        binding.btnProfile.setOnClickListener(view -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        try {
            cooeeSDK.sendEvent("onDestroy", new HashMap<>());
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @Override
    public void onInAppButtonClick(HashMap<String, String> hashMap) {
        Log.d(TAG, "onInAppButtonClick: ************************************" + hashMap.toString());
    }
}