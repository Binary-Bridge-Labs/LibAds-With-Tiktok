package com.bbl.module_ads.funtion;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bbl.module_ads.ads.wrapper.ApInterstitialAd;
import com.bbl.module_ads.ads.wrapper.ApNativeAd;
import com.bbl.module_ads.ads.wrapper.ApRewardItem;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class AdCallback {

    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onAdFailedToLoad(@Nullable LoadAdError i) {
    }

    public void onAdFailedToShow(@Nullable AdError adError) {
    }

    public void onAdFailedToShowHigh(@Nullable AdError adError) {
    }

    public void onAdFailedToShowMedium(@Nullable AdError adError) {
    }

    public void onAdFailedToShowAll(@Nullable AdError adError) {
    }

    public void onAdLoaded() {
    }

    public void onAdLoadedHigh() {
    }

    public void onAdLoadedAll() {
    }

    public void onAdSplashReady() {
    }

    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {

    }


    public void onApInterstitialLoad(@Nullable ApInterstitialAd apInterstitialAd) {

    }


    public void onAdClicked() {
    }

    public void onAdClickedHigh() {
    }

    public void onAdClickedMedium() {
    }

    public void onAdClickedAll() {
    }


    public void onAdImpression() {
    }

    public void onRewardAdLoaded(RewardedAd rewardedAd) {
    }

    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
    }


    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {

    }

    public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {

    }

    public void onNativeAdLoaded( ApNativeAd nativeAd,  ApNativeAd nativeHighAd) {

    }

    public void onInterstitialShow() {

    }

    public void onAdSplashHigh1Ready() {

    }

    public void onAdSplashHigh2Ready() {

    }

    public void onAdSplashHigh3Ready() {

    }

    public void onAdSplashNormalReady() {

    }

    public void onAdHighFailedToLoad() {

    }

    public void onAdPriorityFailedToLoad(@Nullable AdError adError) {

    }

    public void onAdPriorityFailedToShow(@Nullable AdError adError) {

    }

    public void onUserEarnedReward(@NonNull ApRewardItem rewardItem) {

    }

    // Log Rev
    public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
    }

    public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
    }

    public void onTrackingClickAds(Context context, AdValue adValue){

    }

    public void onTimeOutInterSplash() {
    }
}
