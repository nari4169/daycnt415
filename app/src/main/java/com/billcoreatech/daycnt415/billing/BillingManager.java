package com.billcoreatech.daycnt415.billing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

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
    String punchName = "220302_bill_1month_999";
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
                    Log.e(TAG, "getData=" + list.size());
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
                        Log.e(TAG, "getPurchaseState=" + purchase.getPurchaseState());

                        editor = option.edit();
                        editor.putBoolean("isBill", purchase.isAutoRenewing());
                        editor.commit();
                    }
                }
                Log.e(TAG, "--------------------------------------------------------------");
            }
        });

//        mBillingClient.queryPurchaseHistoryAsync(payType, new PurchaseHistoryResponseListener() {
//            @Override
//            public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
//                if (billingResult.getResponseCode() == 0) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    for(PurchaseHistoryRecord purchase : list) {
//                        Log.e(TAG, "getPurchaseToken=" + purchase.getPurchaseToken());
//                        for (String str : purchase.getSkus()) {
//                            Log.e(TAG, "getSkus=" + str);
//                        }
//                        Date now = new Date();
//                        now.setTime(purchase.getPurchaseTime());
//                        Log.e(TAG, "getPurchaseTime=" + sdf.format(now));
//                        Log.e(TAG, "getQuantity=" + purchase.getQuantity());
//                        Log.e(TAG, "getSignature=" + purchase.getSignature());
//
//                        ConsumeParams params = ConsumeParams.newBuilder()
//                                .setPurchaseToken(purchase.getPurchaseToken())
//                                .build();
//                        mBillingClient.consumeAsync(params, BillingManager.this);
//
//                    }
//                }
//            }
//        });
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

                purchase(skuDetailsList.get(0));
            }
        });
    }

    /**
     * @param billingResult
     * @param purchases
     */
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult == null) {
            Log.wtf(TAG, "onPurchasesUpdated: null BillingResult");
            return;
        }
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        Log.d(TAG, "onPurchasesUpdated: $responseCode $debugMessage");
        switch (responseCode) {
            case BillingClient.BillingResponseCode.OK:
                if (purchases == null) {
                    Log.d(TAG, "onPurchasesUpdated: null purchase list");
                    processPurchases(null);
                } else {
                    processPurchases(purchases);
                }
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                Log.i(TAG, "onPurchasesUpdated: User canceled the purchase");
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                Log.i(TAG, "onPurchasesUpdated: The user already owns this item");
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                Log.e(TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys."
                );
                break;
        }
    }

    private void processPurchases(List<Purchase> purchasesList) {
        if (purchasesList != null) {
            Log.d(TAG, "processPurchases: " + purchasesList.size() + " purchase(s)");
        } else {
            Log.d(TAG, "processPurchases: with no purchases");
        }
        if (isUnchangedPurchaseList(purchasesList)) {
            Log.d(TAG, "processPurchases: Purchase list has not changed");
            return;
        }
    }

    /**
     * subs 의 경우는 아래와 같이 구매확인을 해 주어야 됨.
     * @param purchase
     */
    public void confirmPerchase(Purchase purchase) {
        //PURCHASED
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        Log.e(TAG, "getResponseCode=" + billingResult.getResponseCode());
                        editor.putBoolean("isBill", true);
                        editor.commit();
                    }
                });
            }
        }
        //PENDING
        else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            //구매 유예
            Log.e(TAG, "//구매 유예");
        }
        else {
            //구매확정 취소됨(기타 다양한 사유...)
            Log.e(TAG, "//구매확정 취소됨(기타 다양한 사유...)");
        }
    }

    private boolean isUnchangedPurchaseList(List<Purchase> purchasesList) {
        for (Purchase purchase : purchasesList) {
            confirmPerchase(purchase);
        }
        return false;
    }

}
