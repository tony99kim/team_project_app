package com.example.team_project.Environment.Store.Payment;

import android.app.Application;
import kr.co.bootpay.android.BootpayAnalytics;

public class BootpayApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BootpayAnalytics.init(this, "6641c2390a4877c5eb277af5");
    }
}