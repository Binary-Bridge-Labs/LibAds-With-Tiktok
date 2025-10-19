package com.bbl.module_ads.ads.wrapper;

import android.view.View;

import com.bbl.module_ads.remote.NativeConfig;
import com.google.android.gms.ads.nativead.NativeAd;

public class ApNativeAd extends ApAdBase {
    private int layoutCustomNative;
    private View nativeView;
    private NativeAd admobNativeAd;

    private NativeConfig nativeConfig;

    public ApNativeAd(StatusAd status) {
        super(status);
    }

    public ApNativeAd(int layoutCustomNative, View nativeView) {
        this.layoutCustomNative = layoutCustomNative;
        this.nativeView = nativeView;
        status = StatusAd.AD_LOADED;
    }

    public ApNativeAd(int layoutCustomNative, NativeAd admobNativeAd) {
        this.layoutCustomNative = layoutCustomNative;
        this.admobNativeAd = admobNativeAd;
        status = StatusAd.AD_LOADED;
    }

    public ApNativeAd(int layoutCustomNative, NativeAd admobNativeAd, NativeConfig nativeConfig) {
        this.layoutCustomNative = layoutCustomNative;
        this.admobNativeAd = admobNativeAd;
        this.nativeConfig = nativeConfig;
        status = StatusAd.AD_LOADED;
    }

    public ApNativeAd(NativeAd admobNativeAd, NativeConfig nativeConfig) {
        this.admobNativeAd = admobNativeAd;
        this.nativeConfig = nativeConfig;
        status = StatusAd.AD_LOADED;
    }

    public NativeAd getAdmobNativeAd() {
        return admobNativeAd;
    }

    public void setAdmobNativeAd(NativeAd admobNativeAd) {
        this.admobNativeAd = admobNativeAd;
        if (admobNativeAd != null)
            status = StatusAd.AD_LOADED;
    }

    public ApNativeAd() {
    }


    @Override
    boolean isReady() {
        return nativeView != null || admobNativeAd != null;
    }


    public int getLayoutCustomNative() {
        return layoutCustomNative;
    }

    public void setLayoutCustomNative(int layoutCustomNative) {
        this.layoutCustomNative = layoutCustomNative;
    }


    public NativeConfig getNativeConfig() {
        return nativeConfig;
    }

    public View getNativeView() {
        return nativeView;
    }

    public void setNativeView(View nativeView) {
        this.nativeView = nativeView;
    }

    public String toString() {
        return "Status:" + status + " == nativeView:" + nativeView + " == admobNativeAd:" + admobNativeAd;
    }

}
