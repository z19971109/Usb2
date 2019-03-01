package com.dyx.voice.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dyx.voice.service.MainService;
import com.dyx.voice.service.ProtectService;
import com.dyx.voice.R;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class BaiDuActivity extends Activity {

    private final static String ACTION = "android.hardware.usb.action.USB_STATE";

    public static boolean checkFloatWindowPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //AppOpsManager添加于API 19
            return checkOps(context);
        } else {
            //4.4以下一般都可以直接添加悬浮窗
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static boolean checkOps(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = (Integer) method.invoke(object, arrayOfObject1);
            //4.4至6.0之间的非国产手机，例如samsung，sony一般都可以直接添加悬浮窗
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ignore) {
        }
        return false;
    }

    private RelativeLayout details_re;

    private TextView details_text;

    private RelativeLayout re;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        details_re = findViewById(R.id.details_re);

        details_text = findViewById(R.id.details_text);

        details_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BaiDuActivity.this,"details_text",Toast.LENGTH_SHORT).show();
            }
        });

        re = findViewById(R.id.re);

        System.out.println("11123333444");

        initPermission();

        if (!checkFloatWindowPermission(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            Toast.makeText(BaiDuActivity.this, "需要取得权限以使用悬浮窗", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(BaiDuActivity.this, MainService.class);
            startService(intent);
            Intent intent1 = new Intent(BaiDuActivity.this, ProtectService.class);
            startService(intent1);
//            finish();
        }

//        IntentFilter filter = new IntentFilter();
//
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//
//        registerReceiver(usBroadcastReceiver, filter);


        //注册usb权限广播
//        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_DEVICE_PERMISSION), 0);
//        IntentFilter permissionFilter = new IntentFilter(ACTION_DEVICE_PERMISSION);
//        registerReceiver(mUsbPermissionReceiver, permissionFilter);
//
//        searchUsb();

    }


    BroadcastReceiver usBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            String action = intent.getAction();
            UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            int vid = usbDevice.getVendorId();
            int pid = usbDevice.getProductId();

            if (vid == 4310 && pid == 45062) {
                if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    Toast.makeText(BaiDuActivity.this, "拔出USB", Toast.LENGTH_SHORT).show();
                } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    Toast.makeText(BaiDuActivity.this, "插入USB", Toast.LENGTH_SHORT).show();
                }
            }
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(usBroadcastReceiver);
    }

    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
