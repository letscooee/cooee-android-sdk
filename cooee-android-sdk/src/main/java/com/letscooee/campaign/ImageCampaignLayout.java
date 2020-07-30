package com.letscooee.campaign;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.letscooee.R;
import com.letscooee.models.Campaign;


import static com.letscooee.utils.CooeeSDKConstants.LOG_PREFIX;

/**
 * @author Abhishek Taparia
 * ImageCampaignLayout create layout for image campaign
 */
public class ImageCampaignLayout {

    private Context context;

    public ImageCampaignLayout(Context context) {
        this.context = context;
    }

    public void createLayout(final Campaign campaign) {
        Activity activity = (Activity) context;
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (activity.findViewById(android.R.id.content))).getChildAt(0);
        View popupView = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.image_campaign, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ImageView imageView = popupView.findViewById(R.id.imageView);
        Glide.with(context.getApplicationContext()).load(campaign.getMediaURL()).into(imageView);
        Log.i(LOG_PREFIX + " campaign", campaign.getMediaURL());

        TextView textView = popupView.findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        Runnable runnable =new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        };
        if (campaign.getAutoClose()==null){
            textView.setVisibility(View.VISIBLE);
        }
        else{
            Integer autoClose =Integer.valueOf(campaign.getAutoClose());
            textView.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(runnable,autoClose*1000);
        }
        popupWindow.showAtLocation(viewGroup, Gravity.CENTER, 0, 0);
    }
}
