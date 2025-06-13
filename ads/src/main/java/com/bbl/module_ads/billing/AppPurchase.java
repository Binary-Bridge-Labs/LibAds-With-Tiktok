package com.bbl.module_ads.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.bbl.module_ads.event.BBLLogEventManager;
import com.bbl.module_ads.funtion.BillingListener;
import com.bbl.module_ads.funtion.PurchaseListener;
import com.bbl.module_ads.funtion.UpdatePurchaseListener;
import com.bbl.module_ads.util.AppUtil;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppPurchase {
    private static final String LICENSE_KEY = null;
    private static final String MERCHANT_ID = null;
    private static final String TAG = "PurchaseEG";

    public static final String PRODUCT_ID_TEST = "android.test.purchased";
    @SuppressLint("StaticFieldLeak")
    private static AppPurchase instance;

    @SuppressLint("StaticFieldLeak")
    private String price = "1.49$";
    private String oldPrice = "2.99$";

    @Deprecated
    private String productId;
    private ArrayList<QueryProductDetailsParams.Product> listSubscriptionId;
    private ArrayList<QueryProductDetailsParams.Product> listINAPId;
    private PurchaseListener purchaseListener;
    private UpdatePurchaseListener updatePurchaseListener;
    private BillingListener billingListener;
    private Boolean isInitBillingFinish = false;
    private BillingClient billingClient;
    private List<ProductDetails> skuListINAPFromStore;
    private List<ProductDetails> skuListSubsFromStore;
    final private Map<String, ProductDetails> skuDetailsINAPMap = new HashMap<>();
    final private Map<String, ProductDetails> skuDetailsSubsMap = new HashMap<>();
    private boolean isAvailable;
    private boolean isListGot;
    private boolean isConsumePurchase = false;
    private String idPurchaseCurrent = "";
    private int typeIap;
    private boolean verifyFinish = false;

    private boolean isVerifyINAP = false;
    private boolean isVerifySUBS = false;
    private boolean isUpdateInapps = false;
    private boolean isUpdateSubs = false;

    private boolean isPurchase = false;//state purchase on app
    private String idPurchased = "";//id purchased
    private List<PurchaseResult> ownerIdSubs = new ArrayList<>();//id sub
    private List<String> ownerIdInapps = new ArrayList<>();//id inapp

    private Handler handlerTimeout;
    private Runnable rdTimeout;

    public void setPurchaseListener(PurchaseListener purchaseListener) {
        this.purchaseListener = purchaseListener;
    }

    public void setUpdatePurchaseListener(UpdatePurchaseListener listener) {
        this.updatePurchaseListener = listener;
    }

    /**
     * Listener init billing app
     * When init available auto call onInitBillingFinish with resultCode = 0
     *
     * @param billingListener
     */
    public void setBillingListener(BillingListener billingListener) {
        this.billingListener = billingListener;
        if (isAvailable) {
            billingListener.onInitBillingFinished(0);
            isInitBillingFinish = true;
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public Boolean getInitBillingFinish() {
        return isInitBillingFinish;
    }

    public void setEventConsumePurchaseTest(View view) {
        view.setOnClickListener(view1 -> {
            if (AppUtil.VARIANT_DEV) {
                Log.d(TAG, "setEventConsumePurchaseTest: success");
                AppPurchase.getInstance().consumePurchase(PRODUCT_ID_TEST);
            }
        });
    }

    /**
     * Listener init billing app with timeout
     * When init available auto call onInitBillingFinish with resultCode = 0
     *
     * @param billingListener
     * @param timeout
     */
    public void setBillingListener(BillingListener billingListener, int timeout) {
        Log.d(TAG, "setBillingListener: timeout " + timeout);
        this.billingListener = billingListener;
        if (isAvailable) {
            Log.d(TAG, "setBillingListener: finish");
            billingListener.onInitBillingFinished(0);
            isInitBillingFinish = true;
            return;
        }
        handlerTimeout = new Handler();
        rdTimeout = () -> {
            Log.d(TAG, "setBillingListener: timeout run ");
            isInitBillingFinish = true;
            billingListener.onInitBillingFinished(BillingClient.BillingResponseCode.ERROR);
        };
        handlerTimeout.postDelayed(rdTimeout, timeout);
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setConsumePurchase(boolean consumePurchase) {
        isConsumePurchase = consumePurchase;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

    PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> list) {
            Log.e(TAG, "onPurchasesUpdated code: " + billingResult.getResponseCode());
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (Purchase purchase : list) {

                    List<String> sku = purchase.getSkus();
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                if (purchaseListener != null)
                    purchaseListener.onUserCancelBilling();
                Log.d(TAG, "onPurchasesUpdated:USER_CANCELED ");
            } else {
                Log.d(TAG, "onPurchasesUpdated:... ");
            }
        }
    };

    BillingClientStateListener purchaseClientStateListener = new BillingClientStateListener() {
        @Override
        public void onBillingServiceDisconnected() {
            isAvailable = false;
        }

        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
            Log.d(TAG, "onBillingSetupFinished:  " + billingResult.getResponseCode());

            if (!isInitBillingFinish) {
                verifyPurchased(true);
            }

            isInitBillingFinish = true;
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                isAvailable = true;
                // check product detail INAP
                if (listINAPId.size() > 0) {
                    QueryProductDetailsParams paramsINAP = QueryProductDetailsParams.newBuilder()
                            .setProductList(listINAPId)
                            .build();

                    billingClient.queryProductDetailsAsync(
                            paramsINAP,
                            new ProductDetailsResponseListener() {
                                public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                                    if (productDetailsList != null) {
                                        Log.d(TAG, "onSkuINAPDetailsResponse: " + productDetailsList.size());
                                        skuListINAPFromStore = productDetailsList;
                                        isListGot = true;
                                        addSkuINAPToMap(productDetailsList);
                                    }
                                }
                            });
                }
                // check product detail SUBS
                if (listSubscriptionId.size() > 0) {
                    QueryProductDetailsParams paramsSUBS = QueryProductDetailsParams.newBuilder()
                            .setProductList(listSubscriptionId)
                            .build();

                    billingClient.queryProductDetailsAsync(
                            paramsSUBS,
                            new ProductDetailsResponseListener() {
                                public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                                    if (productDetailsList != null) {
                                        Log.d(TAG, "onSkuSubsDetailsResponse: " + productDetailsList.size());
                                        skuListSubsFromStore = productDetailsList;
                                        isListGot = true;
                                        addSkuSubsToMap(productDetailsList);
                                    }
                                }
                            });
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE || billingResult.getResponseCode() == BillingClient.BillingResponseCode.ERROR) {
                Log.e(TAG, "onBillingSetupFinished:ERROR ");

            }
        }
    };

    public static AppPurchase getInstance() {
        if (instance == null) {
            instance = new AppPurchase();
        }
        return instance;
    }

    public List<PurchaseResult> getOwnerIdSubs() {
        return ownerIdSubs;
    }

    public List<String> getOwnerIdInapps() {
        return ownerIdInapps;
    }

    private AppPurchase() {

    }


    public void initBilling(final Application application, List<
            String> listINAPId, List<String> listSubsId) {

        if (AppUtil.VARIANT_DEV) {
            listINAPId.add(PRODUCT_ID_TEST);
        }
        this.listSubscriptionId = listIdToListProduct(listSubsId, BillingClient.ProductType.SUBS);
        this.listINAPId = listIdToListProduct(listINAPId, BillingClient.ProductType.INAPP);

        billingClient = BillingClient.newBuilder(application)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(purchaseClientStateListener);
    }


    private void addSkuSubsToMap(List<ProductDetails> skuList) {
        for (ProductDetails skuDetails : skuList) {
            skuDetailsSubsMap.put(skuDetails.getProductId(), skuDetails);
        }
    }

    private void addSkuINAPToMap(List<ProductDetails> skuList) {
        for (ProductDetails skuDetails : skuList) {
            skuDetailsINAPMap.put(skuDetails.getProductId(), skuDetails);
        }
    }

    public void setPurchase(boolean purchase) {
        isPurchase = purchase;
    }

    public boolean isPurchased() {
        return isPurchase;
    }

    public boolean isPurchased(Context context) {
        return isPurchase;
    }

    public String getIdPurchased() {
        return idPurchased;
    }

    private void addOrUpdateOwnerIdSub(PurchaseResult purchaseResult, String id) {
        boolean isExistId = false;
        for (PurchaseResult p : ownerIdSubs) {
            if (p.getProductId().contains(id)) {
                isExistId = true;
                ownerIdSubs.remove(p);
                ownerIdSubs.add(purchaseResult);
                break;
            }
        }
        if (!isExistId) {
            ownerIdSubs.add(purchaseResult);
        }
    }

    public void verifyPurchased(boolean isCallback) {
        if (billingClient == null || !billingClient.isReady()) {
            Log.e(TAG, "BillingClient is not ready");
            return;
        }

        verifyFinish = false;
        ArrayList<String> productIdsINAP = getProductIds(listINAPId);
        ArrayList<String> productIdsSUBS = getProductIds(listSubscriptionId);

        if (!productIdsINAP.isEmpty()) {
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                    (billingResult, list) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (Purchase purchase : list) {
                                for (String productId : productIdsINAP) {
                                    if (purchase.getProducts().contains(productId)) {
                                        ownerIdInapps.add(productId);
                                        isPurchase = true;
                                    }
                                }
                            }
                        }
                        isVerifyINAP = true;
                        checkAndFinishCallback(isCallback, billingResult);
                    }
            );
        } else {
            isVerifyINAP = true;
        }

        if (!productIdsSUBS.isEmpty()) {
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                    (billingResult, list) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (Purchase purchase : list) {
                                for (String productId : productIdsSUBS) {
                                    if (purchase.getProducts().contains(productId)) {
                                        PurchaseResult purchaseResult = new PurchaseResult(
                                                purchase.getPackageName(),
                                                purchase.getProducts(),
                                                purchase.getPurchaseState(),
                                                purchase.isAutoRenewing()
                                        );
                                        addOrUpdateOwnerIdSub(purchaseResult, productId);
                                        isPurchase = true;
                                    }
                                }
                            }
                        }
                        isVerifySUBS = true;
                        checkAndFinishCallback(isCallback, billingResult);
                    }
            );
        } else {
            isVerifySUBS = true;
        }
    }


    private ArrayList<String> getProductIds(ArrayList<QueryProductDetailsParams.Product> productList) {
        ArrayList<String> productIds = new ArrayList<>();
        if (productList != null) {
            for (QueryProductDetailsParams.Product product : productList) {
                productIds.add(product.zza());
            }
        }
        return productIds;
    }

    private void checkAndFinishCallback(boolean isCallback, BillingResult billingResult) {
        if (isVerifyINAP && isVerifySUBS && billingListener != null && isCallback) {
            billingListener.onInitBillingFinished(billingResult.getResponseCode());
            if (handlerTimeout != null && rdTimeout != null) {
                handlerTimeout.removeCallbacks(rdTimeout);
            }
            verifyFinish = true;
        }
    }


    public void updatePurchaseStatus() {
        if (listINAPId != null) {
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                    (billingResult, list) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (Purchase purchase : list) {
                                for (QueryProductDetailsParams.Product id : listINAPId) {
                                    if (purchase.getProducts().contains(id.zza())) {
                                        if (!ownerIdInapps.contains(id.zza())) {
                                            ownerIdInapps.add(id.zza());
                                        }
                                    }
                                }
                            }
                        }
                        isUpdateInapps = true;
                        if (isUpdateSubs) {
                            if (updatePurchaseListener != null) {
                                updatePurchaseListener.onUpdateFinished();
                            }
                        }
                    }
            );
        }

        if (listSubscriptionId != null) {
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                    (billingResult, list) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (Purchase purchase : list) {
                                for (QueryProductDetailsParams.Product id : listSubscriptionId) {
                                    if (purchase.getProducts().contains(id.zza())) {
                                        PurchaseResult purchaseResult = new PurchaseResult(
                                                purchase.getPackageName(),
                                                purchase.getProducts(),
                                                purchase.getPurchaseState(),
                                                purchase.isAutoRenewing()
                                        );
                                        addOrUpdateOwnerIdSub(purchaseResult, id.zza());
                                    }
                                }
                            }
                        }
                        isUpdateSubs = true;
                        if (isUpdateInapps) {
                            if (updatePurchaseListener != null) {
                                updatePurchaseListener.onUpdateFinished();
                            }
                        }
                    }
            );
        }
    }

    @Deprecated
    public void purchase(Activity activity) {
        if (productId == null) {
            Toast.makeText(activity, "Product id must not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        purchase(activity, productId);
    }


    public String purchase(Activity activity, String productId) {
        if (skuListINAPFromStore == null) {
            if (purchaseListener != null)
                purchaseListener.displayErrorMessage("Billing error init");
            return "";
        }
        ProductDetails productDetails = skuDetailsINAPMap.get(productId);
        if (AppUtil.VARIANT_DEV) {
            productId = PRODUCT_ID_TEST;
            PurchaseDevBottomSheet purchaseDevBottomSheet = new PurchaseDevBottomSheet(TYPE_IAP.PURCHASE, productDetails, activity, purchaseListener);
            purchaseDevBottomSheet.show();
            return "";
        }

        if (productDetails == null) {
            return "Product ID invalid";
        }

        idPurchaseCurrent = productId;
        typeIap = TYPE_IAP.PURCHASE;


        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);

        switch (billingResult.getResponseCode()) {

            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                if (purchaseListener != null)
                    purchaseListener.displayErrorMessage("Billing not supported for type of request");
                return "Billing not supported for type of request";

            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                return "";

            case BillingClient.BillingResponseCode.ERROR:
                if (purchaseListener != null)
                    purchaseListener.displayErrorMessage("Error completing request");
                return "Error completing request";

            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                return "Error processing request.";

            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                return "Selected item is already owned";

            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                return "Item not available";

            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                return "Play Store service is not connected now";

            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                return "Timeout";

            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                if (purchaseListener != null)
                    purchaseListener.displayErrorMessage("Network error.");
                return "Network Connection down";

            case BillingClient.BillingResponseCode.USER_CANCELED:
                if (purchaseListener != null)
                    purchaseListener.displayErrorMessage("Request Canceled");
                return "Request Canceled";

            case BillingClient.BillingResponseCode.OK:
                return "Subscribed Successfully";
        }
        return "";
    }

    public String subscribe(Activity activity, String SubsId) {
        if (AppUtil.VARIANT_DEV) {
            purchase(activity, PRODUCT_ID_TEST);
            return "Billing test";
        } else {
            if (skuListSubsFromStore == null) {
                if (purchaseListener != null)
                    purchaseListener.displayErrorMessage("Billing error init");
                return "";
            }
        }
        ProductDetails productDetails = skuDetailsSubsMap.get(SubsId);
        if (productDetails == null) {
            return "Product ID invalid";
        }
        List<ProductDetails.SubscriptionOfferDetails> subsDetail = productDetails.getSubscriptionOfferDetails();
        if (subsDetail == null || subsDetail.isEmpty()) {
            return "No available offers for this subscription";
        }

        String offerToken = getOfferToken(subsDetail);

        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);

        switch (billingResult.getResponseCode()) {

            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                if (purchaseListener != null)
                    purchaseListener.displayErrorMessage("Billing not supported for type of request");
                return "Billing not supported for type of request";

            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                return "";

            case BillingClient.BillingResponseCode.ERROR:
                if (purchaseListener != null)
                    purchaseListener.displayErrorMessage("Error completing request");
                return "Error completing request";

            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                return "Error processing request.";

            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                return "Selected item is already owned";

            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                return "Item not available";

            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                return "Play Store service is not connected now";

            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                return "Timeout";

            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                if (purchaseListener != null)
                    purchaseListener.displayErrorMessage("Network error.");
                return "Network Connection down";

            case BillingClient.BillingResponseCode.USER_CANCELED:
                if (purchaseListener != null)
                    purchaseListener.displayErrorMessage("Request Canceled");
                return "Request Canceled";

            case BillingClient.BillingResponseCode.OK:
                return "Subscribed Successfully";
        }
        return "";
    }

    private String getOfferToken(List<ProductDetails.SubscriptionOfferDetails> subsDetail) {
        String offerToken = null;
        for (ProductDetails.SubscriptionOfferDetails offer : subsDetail) {
            List<ProductDetails.PricingPhase> pricingPhases = offer.getPricingPhases().getPricingPhaseList();
            for (ProductDetails.PricingPhase phase : pricingPhases) {
                if (phase.getPriceAmountMicros() == 0L) { // Free trial
                    offerToken = offer.getOfferToken();
                    break;
                }
            }
            if (offerToken != null) break;
        }

        if (offerToken == null) {
            offerToken = subsDetail.get(0).getOfferToken();
        }
        return offerToken;
    }

    public void consumePurchase() {
        if (productId == null) {
            Log.e(TAG, "Consume Purchase false:productId null ");
            return;
        }
        consumePurchase(productId);
    }

    public void consumePurchase(String productId) {
        if (billingClient == null || !billingClient.isReady()) {
            Log.e(TAG, "BillingClient is not ready");
            return;
        }

        billingClient.queryPurchasesAsync(BillingClient.ProductType.INAPP, (billingResult, list) -> {
            Purchase pc = null;
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (Purchase purchase : list) {
                    if (purchase.getProducts().contains(productId)) {
                        pc = purchase;
                    }
                }
            }

            if (pc == null) {
                Log.e(TAG, "No purchases found to consume.");
                return;
            }

            try {
                ConsumeParams consumeParams =
                        ConsumeParams.newBuilder()
                                .setPurchaseToken(pc.getPurchaseToken())
                                .build();

                billingClient.consumeAsync(consumeParams, (billingResult1, purchaseToken) -> {
                    if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "onConsumeResponse: OK");
                        verifyPurchased(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private List<String> getListInappId() {
        List<String> list = new ArrayList<>();
        for (QueryProductDetailsParams.Product product : listINAPId) {
            list.add(product.zza());
        }
        return list;
    }

    private List<String> getListSubId() {
        List<String> list = new ArrayList<>();
        for (QueryProductDetailsParams.Product product : listSubscriptionId) {
            list.add(product.zza());
        }
        return list;
    }

    private void handlePurchase(Purchase purchase) {
        double price = getPriceWithoutCurrency(idPurchaseCurrent, typeIap);
        String currency = getCurrency(idPurchaseCurrent, typeIap);
        BBLLogEventManager.onTrackRevenuePurchase((float) price, currency, idPurchaseCurrent, typeIap);

        if (purchaseListener != null) {
            isPurchase = true;
            purchaseListener.onProductPurchased(purchase.getOrderId(), purchase.getOriginalJson());
        }
        if (isConsumePurchase) {
            ConsumeParams consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

            ConsumeResponseListener listener = new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                    Log.d(TAG, "onConsumeResponse: " + billingResult.getDebugMessage());
                }
            };

            billingClient.consumeAsync(consumeParams, listener);
        } else {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                if (!purchase.isAcknowledged()) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                        @Override
                        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                            Log.d(TAG, "onAcknowledgePurchaseResponse: " + billingResult.getDebugMessage());
                        }
                    });
                }
            }
        }
    }


    @Deprecated
    public String getPrice() {
        return getPrice(productId);
    }

    public String getPrice(String productId) {

        ProductDetails skuDetails = skuDetailsINAPMap.get(productId);
        if (skuDetails == null)
            return "";

        Log.e(TAG, "getPrice: " + skuDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());

        return skuDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
    }

    public String getPriceSub(String productId) {
        ProductDetails skuDetails = skuDetailsSubsMap.get(productId);
        if (skuDetails == null)
            return "";


        List<ProductDetails.SubscriptionOfferDetails> subsDetail = skuDetails.getSubscriptionOfferDetails();
        List<ProductDetails.PricingPhase> pricingPhaseList = subsDetail.get(subsDetail.size() - 1).getPricingPhases().getPricingPhaseList();
        Log.e(TAG, "getPriceSub: " + pricingPhaseList.get(pricingPhaseList.size() - 1).getFormattedPrice());
        return pricingPhaseList.get(pricingPhaseList.size() - 1).getFormattedPrice();
    }

    /**
     * Get Price Pricing Phase List Subs
     *
     * @param productId
     * @return
     */
    public List<ProductDetails.PricingPhase> getPricePricingPhaseList(String productId) {
        ProductDetails skuDetails = skuDetailsSubsMap.get(productId);
        if (skuDetails == null)
            return null;

        List<ProductDetails.SubscriptionOfferDetails> subsDetail = skuDetails.getSubscriptionOfferDetails();
        List<ProductDetails.PricingPhase> pricingPhaseList = subsDetail.get(subsDetail.size() - 1).getPricingPhases().getPricingPhaseList();
        return pricingPhaseList;
    }

    /**
     * Get Formatted Price by country
     * Get final price with id
     *
     * @param productId
     * @return
     */
    public String getIntroductorySubPrice(String productId) {
        ProductDetails skuDetails = skuDetailsSubsMap.get(productId);
        if (skuDetails == null) {
            return "";
        }
        if (skuDetails.getOneTimePurchaseOfferDetails() != null)
            return skuDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
        else if (skuDetails.getSubscriptionOfferDetails() != null) {
            List<ProductDetails.SubscriptionOfferDetails> subsDetail = skuDetails.getSubscriptionOfferDetails();
            List<ProductDetails.PricingPhase> pricingPhaseList = subsDetail.get(subsDetail.size() - 1).getPricingPhases().getPricingPhaseList();
            return pricingPhaseList.get(pricingPhaseList.size() - 1).getFormattedPrice();
        } else {
            return "";
        }

    }

    /**
     * Get Currency subs or IAP by country
     *
     * @param productId
     * @param typeIAP
     * @return
     */
    public String getCurrency(String productId, int typeIAP) {
        ProductDetails skuDetails = typeIAP == TYPE_IAP.PURCHASE ? skuDetailsINAPMap.get(productId) : skuDetailsSubsMap.get(productId);
        if (skuDetails == null) {
            return "";
        }
        if (typeIAP == TYPE_IAP.PURCHASE)
            return skuDetails.getOneTimePurchaseOfferDetails().getPriceCurrencyCode();
        else {
            List<ProductDetails.SubscriptionOfferDetails> subsDetail = skuDetails.getSubscriptionOfferDetails();
            List<ProductDetails.PricingPhase> pricingPhaseList = subsDetail.get(subsDetail.size() - 1).getPricingPhases().getPricingPhaseList();
            return pricingPhaseList.get(pricingPhaseList.size() - 1).getPriceCurrencyCode();
        }
    }

    /**
     * Get Price Amount Micros subs or IAP
     * Get final price with id
     *
     * @param productId
     * @param typeIAP
     * @return
     */
    public double getPriceWithoutCurrency(String productId, int typeIAP) {
        ProductDetails skuDetails = typeIAP == TYPE_IAP.PURCHASE ? skuDetailsINAPMap.get(productId) : skuDetailsSubsMap.get(productId);
        if (skuDetails == null) {
            return 0;
        }
        if (typeIAP == TYPE_IAP.PURCHASE)
            return skuDetails.getOneTimePurchaseOfferDetails().getPriceAmountMicros();
        else {
            List<ProductDetails.SubscriptionOfferDetails> subsDetail = skuDetails.getSubscriptionOfferDetails();
            List<ProductDetails.PricingPhase> pricingPhaseList = subsDetail.get(subsDetail.size() - 1).getPricingPhases().getPricingPhaseList();
            return pricingPhaseList.get(pricingPhaseList.size() - 1).getPriceAmountMicros();
        }
    }

    /**
     * Format currency and price by country
     *
     * @param price
     * @param currency
     * @return
     */
    private String formatCurrency(double price, String currency) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setMaximumFractionDigits(0);
        format.setCurrency(Currency.getInstance(currency));
        return format.format(price);
    }

    private double discount = 1;

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getDiscount() {
        return discount;
    }

    private ArrayList<QueryProductDetailsParams.Product> listIdToListProduct(List<String> listId, String styleBilling) {
        ArrayList<QueryProductDetailsParams.Product> listProduct = new ArrayList<QueryProductDetailsParams.Product>();
        for (String id : listId) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(styleBilling)
                    .build();
            listProduct.add(product);
        }
        return listProduct;
    }

    @IntDef({TYPE_IAP.PURCHASE, TYPE_IAP.SUBSCRIPTION})
    public @interface TYPE_IAP {
        int PURCHASE = 1;
        int SUBSCRIPTION = 2;
    }
}
