package com.mihir.portfoliomonitoring.application;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by Mihir on 06-08-2017.
 */

public class AppController  extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //mas codigo
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
