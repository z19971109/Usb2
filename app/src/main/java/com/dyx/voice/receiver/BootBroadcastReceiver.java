package com.dyx.voice.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.dyx.voice.service.MainService;
import com.dyx.voice.service.ProtectService;

import java.util.ArrayList;
import java.util.Iterator;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("intent.getAction() ===== " + intent.getAction());
        if (intent.getAction().equals(ACTION)) {
//        	intent.getStringExtra("message");
        	//1.启动一个Activity
//            Intent mainActivityIntent = new Intent(context, MainActivity.class);// 要启动的Activity
//            System.out.println("开机自启动一个Activity");
//            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mainActivityIntent);
            
            //2.启动一个Service
//            Intent service = new Intent(context,MainService.class);// 要启动的Service
//            context.startService(service);
//            System.out.println("开机自启动一个Service");
            
            //3.启动一个app
//            Intent app = context.getPackageManager().getLaunchIntentForPackage("com.hfs.example");//包名
//            context.startActivity(app);

            Intent service = new Intent(context, MainService.class);
//            Toast.makeText(BaiDuActivity.this, "已开启Toucher", Toast.LENGTH_SHORT).show();
            context.startService(service);

            Intent service2 = new Intent(context, ProtectService.class);
//            Toast.makeText(BaiDuActivity.this, "已开启Toucher", Toast.LENGTH_SHORT).show();
            context.startService(service2);
            System.out.println("开机自启");

        }

        if (intent.getAction().equals("com.dyx.voice.service.MainService")){
            System.out.println("重启");

            Intent sevice = new Intent(context, MainService.class);
            context.startService(sevice);
        }

        if (intent.getAction().equals("android.intent.action.TIME_TICK")){

            if (isServiceRunning(context,"com.dyx.voice.service.MainService")){
                Intent sevice = new Intent(context, MainService.class);
                context.startService(sevice);
            }


        }


    }


    private boolean isServiceRunning(Context context, String serviceName) {
        if (!TextUtils.isEmpty(serviceName) && context != null) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ArrayList<ActivityManager.RunningServiceInfo> runningServiceInfoList
                    = (ArrayList<ActivityManager.RunningServiceInfo>) activityManager.getRunningServices(100);
            for (Iterator<ActivityManager.RunningServiceInfo> iterator = runningServiceInfoList.iterator(); iterator.hasNext(); ) {
                ActivityManager.RunningServiceInfo runningServiceInfo = iterator.next();
                if (serviceName.equals(runningServiceInfo.service.getClassName().toString()))
                    return true;
            }
        } else return false;
        return false;
    }
}