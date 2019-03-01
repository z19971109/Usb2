package com.dyx.voice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class ProtectService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Timer tImer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tImer = null;
        new Thread(sendable).start();
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    Runnable sendable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            tImer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
//                    boolean b = isServiceExisted(MainService.class.getName());
//                    if (!b) {
//                        System.out.println("服务已死");
//                        Intent service = new Intent(ProtectService.this, MainService.class);
//                        startService(service);
//                    } else {
//                        System.out.println("持续运行");
//                    }

                    if (MainService.isServiceRunning()) {
//                        System.out.println("运行");
                    } else {
//                        System.out.println("死亡");
                        Intent service = new Intent(ProtectService.this, MainService.class);
                        startService(service);
                    }

                }
            };
            tImer.schedule(task, 0, 1000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
