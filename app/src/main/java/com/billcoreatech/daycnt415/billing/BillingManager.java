package com.billcoreatech.daycnt415.billing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.billcoreatech.daycnt415.R;
import com.billcoreatech.daycnt415.util.KakaoToast;
import com.billcoreatech.daycnt415.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BillingManager implements PurchasesUpdatedListener, ConsumeResponseListener {
    String TAG = "BillingManager" ;
    BillingClient mBillingClient ;
    Activity mActivity ;
    public List<SkuDetails> mSkuDetails ;

    public enum connectStatusTypes { waiting, connected, fail, disconnected }
    public connectStatusTypes connectStatus = connectStatusTypes.waiting ;
    private ConsumeResponseListener mConsumResListnere ;
    /**
     * 구글에 설정한 구독 상품 아이디와 일치 하지 않으면 오류를 발생 시킴.
     * 21.04.20 이번에는 1회성 구매로 변경   210414_monthly_bill_999, 210420_monthly_bill
     */
    String punchName = "210414_monthly_bill_999";
    String punchNameInapp = "210420_monthly_bill";
    String payType = BillingClient.SkuType.SUBS ;

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
                Log.e(TAG, "respCode=" + billingResult.getResponseCode() ) ;
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    connectStatus = connectStatusTypes.connected ;
                    Log.e(TAG, "connected...") ;
                    purchaseAsync();
                    getSkuDetailList();

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

    }

    /**
     * 정기 결재 소모 여부를 수신 : 21.04.20 1회성 구매의 경우는 결재하면 끝임.
     * @param billingResult
     * @param purchaseToken
     */
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

    public int purchase(SkuDetails skuDetails) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        return mBillingClient.launchBillingFlow(mActivity, flowParams).getResponseCode();
    }

    public void purchaseAsync() {
        Log.e(TAG, "--------------------------------------------------------------");

        mBillingClient.queryPurchasesAsync(payType, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                Log.e(TAG, "onQueryPurchasesResponse=" + billingResult.getResponseCode()) ;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (list.size() < 1) {
                    editor = option.edit();
                    editor.putBoolean("isBill", false);
                    editor.commit();
                } else {
                    for (Purchase purchase : list) {
                        Log.e(TAG, "getPurchaseToken=" + purchase.getPurchaseToken());
                        for (String str : purchase.getSkus()) {
                            Log.e(TAG, "getSkus=" + str);
                        }
                        Date now = new Date();
                        now.setTime(purchase.getPurchaseTime());
                        Log.e(TAG, "getPurchaseTime=" + sdf.format(now));
                        Log.e(TAG, "getQuantity=" + purchase.getQuantity());
                        Log.e(TAG, "getSignature=" + purchase.getSignature());
                        Log.e(TAG, "isAutoRenewing=" + purchase.isAutoRenewing());

                        editor = option.edit();
                        editor.putBoolean("isBill", purchase.isAutoRenewing());
                        editor.commit();
                    }
                }
            }
        });

        mBillingClient.queryPurchaseHistoryAsync(payType, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
                if (billingResult.getResponseCode() == 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    for(PurchaseHistoryRecord purchase : list) {
                        Log.e(TAG, "getPurchaseToken=" + purchase.getPurchaseToken());
                        for (String str : purchase.getSkus()) {
                            Log.e(TAG, "getSkus=" + str);
                        }
                        Date now = new Date();
                        now.setTime(purchase.getPurchaseTime());
                        Log.e(TAG, "getPurchaseTime=" + sdf.format(now));
                        Log.e(TAG, "getQuantity=" + purchase.getQuantity());
                        Log.e(TAG, "getSignature=" + purchase.getSignature());

                        if (payType.equals(BillingClient.SkuType.SUBS)) {
                            ConsumeParams params = ConsumeParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                            mBillingClient.consumeAsync(params, BillingManager.this);
                        }

                    }
                }
            }
        });
    }

    public void getSkuDetailList() {
        List<String> skuIdList = new ArrayList<>() ;
        skuIdList.add(punchName);

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuIdList).setType(payType);
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

            for(Purchase purchase : purchases) {
                //handlePurchase(purchase);
                Log.i(TAG, "성공값=" + purchase.getPurchaseToken()) ;
                try {
                    Log.e(TAG, "getOriginalJson=" + purchase.getOriginalJson());
                    object = new JSONObject(purchase.getOriginalJson());
                    String sku = "";
                    for (String str : purchase.getSkus()) {
                        sku = str ;
                        Log.e(TAG, "SKU=" + sku);
                    }
                    pID = object.getString("purchaseToken");
                    pDate = StringUtil.getDate(object.getLong("purchaseTime"));
                    if (!sku.equals(punchName)) {
                        // 자동 구매가 아닌 때 1회성 일때 소모 처리 ?
                        ConsumeParams params = ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                        mBillingClient.consumeAsync(params, BillingManager.this);
                        editor.putLong("billTimeStamp", object.getLong("purchaseTime"));
                        editor.putBoolean("isBill", true);
                        editor.putString("token", purchase.getPurchaseToken());
                    } else {
                        editor.putLong("billTimeStamp", object.getLong("purchaseTime"));
                        editor.putBoolean("isBill", object.getBoolean("autoRenewing"));
                        editor.putString("token", purchase.getPurchaseToken());
                    }
                    editor.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        Log.e(TAG, "getResponseCode=" + billingResult.getResponseCode());
                    }
                });
            }
        }
    }

}
