package com.dyx.voice;

import android.app.Application;

import cn.com.broadlink.blnetworkunit.BLNetworkUnit;

public class UsbApplication extends Application {

    public static BLNetworkUnit mBlNetworkUnit;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
