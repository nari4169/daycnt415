package com.billcoreatech.daycnt415.billing;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.billcoreatech.daycnt415.R;
import com.billcoreatech.daycnt415.util.KakaoToast;
import com.billcoreatech.daycnt415.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BillingManager<mConsumResListnere> implements PurchasesUpdatedListener {
    String TAG = "BillingManager" ;
    BillingClient mBillingClient ;
    Activity mActivity ;
    public List<SkuDetails> mSkuDetails ;
    public enum connectStatusTypes { waiting, connected, fail, disconnected }
    public connectStatusTypes connectStatus = connectStatusTypes.waiting ;
    private ConsumeResponseListener mConsumResListnere ;
    /**
     * 구글에 설정한 구독 상품 아이디와 일치 하지 않으면 오류를 발생 시킴.
     */
    String punchName = "210414_monthly_bill_999";
    SharedPreferences option ;
    SharedPreferences.Editor editor ;

    public BillingManager (Activity _activity) {
        mActivity = _activity ;
        option = mActivity.getSharedPreferences("option", mActivity.MODE_PRIVATE);
        editor = option.edit();
        mBillingClient = BillingClient.newBuilder(mActivity)
                .setListener(this)
                .enablePendingPurchases()
                .build() ;
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.i(TAG, "respCode=" + billingResult.getResponseCode() ) ;
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    connectStatus = connectStatusTypes.connected ;
                    Log.i(TAG, "connected...") ;

                    Log.i(TAG, "resp=" + mBillingClient.queryPurchases(punchName).getBillingResult()
                             + "=" + mBillingClient.queryPurchases(punchName).getResponseCode());

                    getSkuDetailList() ;

                } else {
                    connectStatus = connectStatusTypes.fail ;
                    Log.i(TAG, "connected... fail ") ;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectStatus = connectStatusTypes.disconnected ;
                Log.i(TAG, "disconnected ") ;
            }
        });

        mConsumResListnere = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "사용끝 + " + purchaseToken) ;
                    return ;
                } else {
                    Log.i(TAG, "소모에 실패 " + billingResult.getResponseCode() + " 대상 상품 " + purchaseToken) ;
                    return ;
                }
            }
        };
    }

    public int purchase(SkuDetails skuDetails) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        return mBillingClient.launchBillingFlow(mActivity, flowParams).getResponseCode();
    }


    public void getSkuDetailList() {
        List<String> skuIdList = new ArrayList<>() ;
        skuIdList.add(punchName);

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuIdList).setType(BillingClient.SkuType.SUBS);
        mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> skuDetailsList) {
                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "detail respCode=" + billingResult.getResponseCode()) ;
                    return ;
                }
                if (skuDetailsList == null) {
                    KakaoToast.makeToast(mActivity, mActivity.getString(R.string.msgNotInfo), Toast.LENGTH_LONG).show();
                    return ;
                }
                Log.i(TAG, "listCount=" + skuDetailsList.size());
                for(SkuDetails skuDetails : skuDetailsList) {
                    Log.i(TAG, "\n" + skuDetails.getSku()
                            + "\n" + skuDetails.getTitle()
                            + "\n" + skuDetails.getPrice()
                            + "\n" + skuDetails.getDescription()
                            + "\n" + skuDetails.getFreeTrialPeriod()
                            + "\n" + skuDetails.getIconUrl()
                            + "\n" + skuDetails.getIntroductoryPrice()
                            + "\n" + skuDetails.getIntroductoryPriceAmountMicros()
                            + "\n" + skuDetails.getOriginalPrice()
                            + "\n" + skuDetails.getPriceCurrencyCode()) ;
                }
                mSkuDetails = skuDetailsList ;

            }
        });
    }

    /**
     *
     * {"orderId":"GPA.3309-4529-7908-05842",
     * "packageName":"com.billcoreatech.dream314",
     * "productId":"210318_dream314_monthly",
     * "purchaseTime":1616593760061,
     * "purchaseState":0,
     * "purchaseToken":"nneihaohkgnjojiifcbfdmpg.AO-J1Ow9pKVS60AxVJqKYq_UckNpJ-znNO2ObQLJ0EBKyrAQaatnxxW2DbRWE5vD3cpnS3aPMsKLoCQhwJK8hIOI9ywwGvraN-o3af4njLdq0419SY0TKlg",
     * "autoRenewing":true,
     * "acknowledged":false}
     *
     * @param billingResult
     * @param purchases
     */
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            Log.i(TAG, "구매 성공>>>" + billingResult.getDebugMessage());
            JSONObject object = null ;
            String pID = "" ;
            String pDate = "" ;
            editor = option.edit();
            for(Purchase purchase : purchases) {
                Log.i(TAG, "성공값=" + purchase.getPurchaseToken()) ;
                Log.i(TAG, "성공값=" + purchase.getOriginalJson());
                try {
                    object = new JSONObject(purchase.getOriginalJson());
                    pID = object.getString("purchaseToken");
                    pDate = StringUtil.getDate(object.getLong("purchaseTime"));
                    editor.putLong("billTimeStamp", System.currentTimeMillis());
                    editor.putBoolean("isBill", true);
                    editor.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "token=" + pID + "" + pDate) ;
                ConsumeParams params = ConsumeParams.newBuilder()
                        .setPurchaseToken(pID)
                        .build() ;
                mBillingClient.consumeAsync(params, mConsumResListnere);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "결제 취소");
            editor = option.edit();
            editor.putBoolean("isBill", false);
            editor.commit();
        } else {
            Log.i(TAG, "오류 코드=" + billingResult.getResponseCode()) ;
            editor = option.edit();
            editor.putBoolean("isBill", false);
            editor.commit();
        }
    }
}
