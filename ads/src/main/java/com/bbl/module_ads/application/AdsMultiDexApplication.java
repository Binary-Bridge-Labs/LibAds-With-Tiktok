package com.bbl.module_ads.application;

import androidx.multidex.MultiDexApplication;

import com.bbl.module_ads.config.BBLAdConfig;
import com.bbl.module_ads.remote.ConfigManager;
import com.bbl.module_ads.util.AppUtil;
import com.bbl.module_ads.util.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AdsMultiDexApplication extends MultiDexApplication {

    protected BBLAdConfig mBBLAdConfig;
    protected List<String> listTestDevice;





    @Override
    public void onCreate() {
        super.onCreate();

        listTestDevice = new ArrayList<String>();
        mBBLAdConfig = new BBLAdConfig(this);
        if (SharePreferenceUtils.getInstallTime(this) == 0) {
            SharePreferenceUtils.setInstallTime(this);
        }
        AppUtil.currentTotalRevenue001Ad = SharePreferenceUtils.getCurrentTotalRevenue001Ad(this);
    }


}
