package com.bbl.module_ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.bbl.module_ads.R;

public class PrepareLoadingAdsDialog extends Dialog {


    public PrepareLoadingAdsDialog(Context context) {
        super(context, R.style.AppTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_prepair_loading_ads);
    }

}
