package com.bbl.module;

import com.bbl.module_ads.admob.Admob;
import com.bbl.module_ads.admob.AppOpenManager;
import com.bbl.module_ads.ads.BBLAd;
import com.bbl.module_ads.application.AdsMultiDexApplication;
import com.bbl.module_ads.billing.AppPurchase;
import com.bbl.module_ads.config.AdjustConfig;
import com.bbl.module_ads.config.BBLAdConfig;
import com.bbl.module.BuildConfig;
import com.bbl.module.R;

import java.util.ArrayList;
import java.util.List;

public class App extends AdsMultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        initAds();
        initBilling();
    }

    private void initAds() {
        String environment = BuildConfig.DEBUG ? BBLAdConfig.ENVIRONMENT_DEVELOP : BBLAdConfig.ENVIRONMENT_PRODUCTION;
        mBBLAdConfig = new BBLAdConfig(this, environment);

        AdjustConfig adjustConfig = new AdjustConfig(true, getString(R.string.adjust_token));
        mBBLAdConfig.setAdjustConfig(adjustConfig);
        mBBLAdConfig.setFacebookClientToken(getString(R.string.facebook_client_token));
        mBBLAdConfig.setAdjustTokenTiktok(getString(R.string.tiktok_token));
        ArrayList<String> listDevices = new ArrayList<>();
        listDevices.add("C20CB584D5F3884F6EEEC91B0FF540A2");
        mBBLAdConfig.setListDeviceTest(listDevices);
        mBBLAdConfig.setIdAdResume("");

        BBLAd.getInstance().init(this, mBBLAdConfig);
        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        Admob.getInstance().setOpenActivityAfterShowInterAds(true);
        AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity.class);
    }

    private void initBilling(){
        List<String> listIAP = new ArrayList<>();
        listIAP.add("android.test.purchased");
        List<String> listSub = new ArrayList<>();
        AppPurchase.getInstance().initBilling(this, listIAP, listSub);
    }
}
