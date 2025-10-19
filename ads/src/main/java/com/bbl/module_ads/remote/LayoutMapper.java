package com.bbl.module_ads.remote;

import java.util.HashMap;
import java.util.Map;

/**
 * LayoutMapper - Helper class để map layout ID với layout resource thực tế
 * Giúp chuyển đổi từ layout ID (string) sang layout resource (int)
 */
public class LayoutMapper {
    
    // Map chứa mapping từ layout ID (string) sang layout resource (int)
    private static Map<String, Integer> layoutMap = new HashMap<>();
    
    // Khởi tạo mapping mặc định
    static {
        // Banner layouts
        layoutMap.put("ads_native_banner_layout", R.layout.ads_native_banner_layout);
        layoutMap.put("ads_native_banner_layout_2", R.layout.ads_native_banner_layout_2);
        layoutMap.put("ads_native_banner_small", R.layout.ads_native_banner_small);
        layoutMap.put("ads_native_banner_large", R.layout.ads_native_banner_large);
        
        // Interstitial layouts
        layoutMap.put("ads_native_interstitial_layout", R.layout.ads_native_interstitial_layout);
        layoutMap.put("ads_native_interstitial_fullscreen", R.layout.ads_native_interstitial_fullscreen);
        
        // Fullscreen layouts
        layoutMap.put("ads_native_fullscreen1_layout", R.layout.ads_native_fullscreen1_layout);
        layoutMap.put("ads_native_fullscreen2_layout", R.layout.ads_native_fullscreen2_layout);
        
        // Medium layouts
        layoutMap.put("ads_native_medium_layout", R.layout.ads_native_medium_layout);
        layoutMap.put("ads_native_medium_layout_2", R.layout.ads_native_medium_layout_2);
        
        // Video layouts
        layoutMap.put("ads_native_video_layout", R.layout.ads_native_video_layout);
        layoutMap.put("ads_native_video_small_layout", R.layout.ads_native_video_small_layout);
        layoutMap.put("ads_native_video_large_layout", R.layout.ads_native_video_large_layout);
        
        // Unify layouts
        layoutMap.put("ads_native_unify_layout", R.layout.ads_native_unify_layout);
        layoutMap.put("ads_native_unify_small_layout", R.layout.ads_native_unify_small_layout);
        layoutMap.put("ads_native_unify_medium_layout", R.layout.ads_native_unify_medium_layout);
        layoutMap.put("ads_native_unify_large_layout", R.layout.ads_native_unify_large_layout);
    }
    
    /**
     * Lấy layout resource từ layout ID
     * @param layoutId Layout ID (string)
     * @return Layout resource ID (int), trả về 0 nếu không tìm thấy
     */
    public static int getLayoutResource(String layoutId) {
        if (layoutId == null || layoutId.isEmpty()) {
            return 0;
        }
        return layoutMap.getOrDefault(layoutId, 0);
    }
    
    /**
     * Thêm mapping mới
     * @param layoutId Layout ID (string)
     * @param layoutResource Layout resource ID (int)
     */
    public static void addLayoutMapping(String layoutId, int layoutResource) {
        if (layoutId != null && !layoutId.isEmpty()) {
            layoutMap.put(layoutId, layoutResource);
        }
    }
    
    /**
     * Xóa mapping
     * @param layoutId Layout ID cần xóa
     * @return true nếu xóa thành công, false nếu không tồn tại
     */
    public static boolean removeLayoutMapping(String layoutId) {
        return layoutMap.remove(layoutId) != null;
    }
    
    /**
     * Kiểm tra xem layout ID có tồn tại không
     * @param layoutId Layout ID cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     */
    public static boolean hasLayoutMapping(String layoutId) {
        return layoutMap.containsKey(layoutId);
    }
    
    /**
     * Lấy tất cả layout ID đã được map
     * @return Array chứa tất cả layout ID
     */
    public static String[] getAllLayoutIds() {
        return layoutMap.keySet().toArray(new String[0]);
    }
    
    /**
     * Lấy số lượng mapping hiện có
     * @return Số lượng mapping
     */
    public static int getMappingCount() {
        return layoutMap.size();
    }
    
    /**
     * Xóa tất cả mapping
     */
    public static void clearAllMappings() {
        layoutMap.clear();
    }
}
