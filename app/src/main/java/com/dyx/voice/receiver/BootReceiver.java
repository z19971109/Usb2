package com.dyx.voice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dyx.voice.service.MainService;
import com.dyx.voice.service.ProtectService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.dbjtech.waiqin.destroy")) {
            //TODO
            //在这里写重新启动service的相关操作

            Intent service = new Intent(context, MainService.class);
//            Toast.makeText(BaiDuActivity.this, "已开启Toucher", Toast.LENGTH_SHORT).show();
            context.startService(service);

            Intent intent1 = new Intent(context,ProtectService.class);
            context.startService(intent1);

        }

    }

}
