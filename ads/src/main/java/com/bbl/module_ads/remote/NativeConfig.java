package com.bbl.module_ads.remote;

import com.bbl.module_ads.R;

/**
 * Config cho Native Ads
 * Chứa các thông tin cấu hình cho một native ad
 */
public class NativeConfig {
    
    // ID của ads unit
    private String idAds;
    
    // Layout ID (string) - sẽ được map với layout thực tế sau
    private String layout;
    
    // Màu nền của ads
    private String backgroundColor;
    
    // Màu của CTA button
    private String ctaColor;
    
    // Có hiển thị ads hay không
    private boolean isShow;
    
    // Màu viền của ads
    private String stroke;
    
    // Constructor mặc định
    public NativeConfig() {
        this.idAds = "";
        this.layout = "";
        this.backgroundColor = "#FFFFFF";
        this.ctaColor = "#000000";
        this.isShow = true;
        this.stroke = "#CCCCCC";
    }
    
    // Constructor với tham số
    public NativeConfig(String idAds, String layout, String backgroundColor, 
                       String ctaColor, boolean isShow, String stroke) {
        this.idAds = idAds;
        this.layout = layout;
        this.backgroundColor = backgroundColor;
        this.ctaColor = ctaColor;
        this.isShow = isShow;
        this.stroke = stroke;
    }
    
    // Getters và Setters
    public String getIdAds() {
        return idAds;
    }
    
    public void setIdAds(String idAds) {
        this.idAds = idAds;
    }
    
    public String getLayout() {
        return layout;
    }
    
    public void setLayout(String layout) {
        this.layout = layout;
    }
    
    public String getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public String getCtaColor() {
        return ctaColor;
    }
    
    public void setCtaColor(String ctaColor) {
        this.ctaColor = ctaColor;
    }
    
    public boolean isShow() {
        return isShow;
    }
    
    public void setShow(boolean show) {
        isShow = show;
    }
    
    public String getStroke() {
        return stroke;
    }
    
    public void setStroke(String stroke) {
        this.stroke = stroke;
    }
    
    /**
     * Lấy resource ID của layout native ad
     * @return int resource ID của layout, mặc định là loading_native_small nếu không tìm thấy
     */
    public int getLayoutResourceId() {
        return getLayoutResourceId(this.layout);
    }
    
    /**
     * Lấy resource ID của layout shimmer
     * @return int resource ID của shimmer layout, mặc định là loading_native_small nếu không tìm thấy
     */
    public int getShimmerLayoutResourceId() {
        return getShimmerLayoutResourceId(this.layout);
    }
    
    /**
     * Map từ tên layout string sang resource ID của layout native ad
     * @param layoutName tên layout (ví dụ: "ads_native_1_button_2_info_3_media_layout")
     * @return int resource ID của layout
     */
    public static int getLayoutResourceId(String layoutName) {
        if (layoutName == null || layoutName.isEmpty()) {
            return R.layout.loading_native_small;
        }
        
        switch (layoutName) {
            case "ads_native_1_button_2_info_3_media_layout":
                return R.layout.ads_native_1_button_2_info_3_media_layout;
            case "ads_native_1_button_2_info_layout":
                return R.layout.ads_native_1_button_2_info_layout;
            case "ads_native_1_button_2_media_3_info_layout":
                return R.layout.ads_native_1_button_2_media_3_info_layout;
            case "ads_native_1_info_2_button_layout":
                return R.layout.ads_native_1_info_2_button_layout;
            case "ads_native_1_info_2_media_3_button_layout":
                return R.layout.ads_native_1_info_2_media_3_button_layout;
            case "ads_native_1_media_2_info_3_button_layout":
                return R.layout.ads_native_1_media_2_info_3_button_layout;
            case "ads_native_1_media_2xxxinfovsbutton_layout":
                return R.layout.ads_native_1_media_2xxxinfovsbutton_layout;
            case "ads_native_1_xxxinfovsbutton_layout":
                return R.layout.ads_native_1_xxxinfovsbutton_layout;
            case "ads_native_1_xxxinfovsmedia_2_button_layout":
                return R.layout.ads_native_1_xxxinfovsmedia_2_button_layout;
            case "ads_native_1_xxxmediavsyyyinfovsbutton_layout":
                return R.layout.ads_native_1_xxxmediavsyyyinfovsbutton_layout;
            case "ads_native_collap_1_button_2_info_3_media":
                return R.layout.ads_native_collap_1_button_2_info_3_media;
            case "ads_native_collap_1_media_2_info_3_button":
                return R.layout.ads_native_collap_1_media_2_info_3_button;
            case "ads_native_collap_1_xxxinfovsbuttonvsyyymedia":
                return R.layout.ads_native_collap_1_xxxinfovsbuttonvsyyymedia;
            case "ads_native_collap_1_xxxmediavsyyyinfovsbutton":
                return R.layout.ads_native_collap_1_xxxmediavsyyyinfovsbutton;
            case "ads_native_custom1_layout":
                return R.layout.ads_native_custom1_layout;
            case "ads_native_custom2_layout":
                return R.layout.ads_native_custom2_layout;
            case "ads_native_custom3_layout":
                return R.layout.ads_native_custom3_layout;
            case "ads_native_default_layout":
                return R.layout.ads_native_default_layout;
            case "ads_native_fullscreen1_layout":
                return R.layout.ads_native_fullscreen1_layout;
            case "ads_native_fullscreen2_layout":
                return R.layout.ads_native_fullscreen2_layout;
            case "ads_native_fullscreen3_layout":
                return R.layout.ads_native_fullscreen3_layout;
            default:
                return R.layout.loading_native_small;
        }
    }
    
    /**
     * Map từ tên layout string sang resource ID của layout shimmer
     * @param layoutName tên layout (ví dụ: "ads_native_1_button_2_info_3_media_layout")
     * @return int resource ID của shimmer layout
     */
    public static int getShimmerLayoutResourceId(String layoutName) {
        if (layoutName == null || layoutName.isEmpty()) {
            return R.layout.loading_native_small;
        }
        
        switch (layoutName) {
            case "ads_native_1_button_2_info_3_media_layout":
                return R.layout.ads_native_1_button_2_info_3_media_shimmer;
            case "ads_native_1_button_2_info_layout":
                return R.layout.ads_native_1_button_2_info_shimmer;
            case "ads_native_1_button_2_media_3_info_layout":
                return R.layout.ads_native_1_button_2_media_3_info_shimmer;
            case "ads_native_1_info_2_button_layout":
                return R.layout.ads_native_1_info_2_button_shimmer;
            case "ads_native_1_info_2_media_3_button_layout":
                return R.layout.ads_native_1_info_2_media_3_button_shimmer;
            case "ads_native_1_media_2_info_3_button_layout":
                return R.layout.ads_native_1_media_2_info_3_button_shimmer;
            case "ads_native_1_media_2xxxinfovsbutton_layout":
                return R.layout.ads_native_1_media_2xxxinfovsbutton_shimmer;
            case "ads_native_1_xxxinfovsbutton_layout":
                return R.layout.ads_native_1_xxxinfovsbutton_shimmer;
            case "ads_native_1_xxxinfovsmedia_2_button_layout":
                return R.layout.ads_native_1_xxxinfovsmedia_2_button_shimmer;
            case "ads_native_1_xxxmediavsyyyinfovsbutton_layout":
                return R.layout.ads_native_1_xxxmediavsyyyinfovsbutton_shimmer;
            case "ads_native_collap_1_button_2_info_3_media":
                return R.layout.ads_native_collap_1_button_2_info_3_media_shimmer;
            case "ads_native_collap_1_media_2_info_3_button":
                return R.layout.ads_native_collap_1_media_2_info_3_button_shimmer;
            case "ads_native_collap_1_xxxinfovsbuttonvsyyymedia":
                return R.layout.ads_native_collap_1_xxxinfovsbuttonvsyyymedia_shimmer;
            case "ads_native_collap_1_xxxmediavsyyyinfovsbutton":
                return R.layout.ads_native_collap_1_xxxmediavsyyyinfovsbutton_shimmer;
            case "ads_native_custom1_layout":
                return R.layout.ads_native_custom1_shimmer;
            case "ads_native_custom2_layout":
                return R.layout.ads_native_custom2_shimmer;
            case "ads_native_custom3_layout":
                return R.layout.ads_native_custom3_shimmer;
            case "ads_native_default_layout":
                return R.layout.ads_native_default_shimmer;
            case "ads_native_fullscreen1_layout":
                return R.layout.ads_native_fullscreen1_shimmer;
            case "ads_native_fullscreen2_layout":
                return R.layout.ads_native_fullscreen2_shimmer;
            case "ads_native_fullscreen3_layout":
                return R.layout.ads_native_fullscreen3_shimmer;
            default:
                return R.layout.loading_native_small;
        }
    }

    @Override
    public String toString() {
        return "NativeConfig{" +
                "idAds='" + idAds + '\'' +
                ", layout=" + layout +
                ", backgroundColor='" + backgroundColor + '\'' +
                ", ctaColor='" + ctaColor + '\'' +
                ", isShow=" + isShow +
                ", stroke='" + stroke + '\'' +
                '}';
    }
}
