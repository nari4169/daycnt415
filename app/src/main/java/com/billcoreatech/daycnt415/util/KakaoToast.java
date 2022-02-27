package com.billcoreatech.daycnt415.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.billcoreatech.daycnt415.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.ResponseInfo;


/**
 *
 */
public class KakaoToast {

    static private AdView mAdView;
    static String TAG = "KakaoToast";
    static SharedPreferences option ;

    public static Toast makeToast(Context context, String body, int duration){
        LayoutInflater inflater;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.view_toast, null);
        TextView text = v.findViewById(R.id.message);
        text.setText(body);
        mAdView = v.findViewById(R.id.adView);
        option = context.getSharedPreferences("option", context.MODE_PRIVATE);
        MobileAds.initialize(context);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if (option.getBoolean("isBill", false)) {
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
        }

        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.e(TAG, "onAdLoaded");
            }
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.e(TAG, "onAdClosed");
            }

            @Override
            public void onAdOpened() {
                Log.e(TAG, "onAdOpened");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.e(TAG, "onAdClicked");
            }
            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.e(TAG, "onAdImpression");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                super.onAdFailedToLoad(error);
                String errorDomain = error.getDomain();
                int errorCode = error.getCode();
                String errorMessage = error.getMessage();
                ResponseInfo responseInfo = error.getResponseInfo();
                AdError cause = error.getCause();
                Log.i(TAG,"------------------------------------");
                Log.i(TAG, "error=" + error.toString());
                Log.i(TAG, "errorDomain=" + errorDomain);
                Log.i(TAG, "errorCode=" + errorCode);
                Log.i(TAG, "errorMessage=" + errorMessage);
                Log.i(TAG, "responseInfo=" + responseInfo.getResponseId());
                Log.i(TAG, "responseInfo=" + responseInfo.getMediationAdapterClassName());
                Log.i(TAG,"------------------------------------");
            }

        });

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setView(v);
        toast.setDuration(duration);
        return toast;
    }
}