package com.dyx.voice.function;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import com.baidu.speech.EventManager;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.dyx.voice.inter.AsyncTaskCallBack;
import com.dyx.voice.inter.Volume;
import com.dyx.voice.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.broadlink.blnetworkdataparse.BLNetworkDataParse;
import cn.com.broadlink.blnetworkunit.SendDataResultInfo;

public class Functions {

    public static Volume parseVolumeJson(String jsonStr) {
        Volume vol = new Volume();
        vol.origalJson = jsonStr;
        try {
            JSONObject json = new JSONObject(jsonStr);
            vol.volumePercent = json.getInt("volume-percent");
            vol.volume = json.getInt("volume");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vol;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static String utterance(String talkText) {
        // 请求URL
        String talkUrl = "https://aip.baidubce.com/rpc/2.0/unit/bot/chat";
        try {
            // 请求参数
            String params = "{\"bot_session\":\"\",\"log_id\":\"7758521\",\"request\":{\"bernard_level\":1,\"client_session\":\"{\\\"client_results\\\":\\\"\\\", \\\"candidate_options\\\":[]}\",\"query\":\"" + talkText + "\",\"query_info\":{\"asr_candidates\":[],\"source\":\"KEYBOARD\",\"type\":\"TEXT\"},\"updates\":\"\",\"user_id\":\"88888\"},\"bot_id\":\"35205\",\"version\":\"2.0\"}";
            String accessToken = "24.2c5c99626c635cfbd44047b32d1f540d.2592000.1552640844.282335-14946762";
            String result = HttpUtil.post(talkUrl, accessToken, "application/json", params);

            System.out.println("result:" + result);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void ASR_START(EventManager asr) {
        JSONObject object = new JSONObject();
        try {
            object.put("accept-audio-volume", true);
            object.put("pid", 15361);
            object.put("grammar", "assets://baidu_speech_grammar.bsg");
            object.put("nlu", "enable");
            object.put("vad.endpoint-timeout", 0);
//                        object.put("outfile","/storage/emulated/0/baiduASR/outfile.pcm");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        asr.send(SpeechConstant.ASR_START, object.toString(), null, 0, 0);
    }

    public static void parseSwitchJson(JSONArray devicesArray , String result, String raw_text , BLNetworkDataParse mBroadLinkNetworkData , Handler switchHandler , boolean local) {
        try {
            JSONObject object = new JSONObject(result);
            int error_code = object.optInt("error_code");
            if (error_code == 0) {
                JSONObject resultObject = object.optJSONObject("result");
                if (resultObject != null) {
                    JSONObject responseObject = resultObject.optJSONObject("response");
                    if (responseObject != null) {
                        JSONObject schemaObject = responseObject.optJSONObject("schema");
                        if (schemaObject != null) {
                            JSONArray slotsArray = schemaObject.optJSONArray("slots");

                            if (slotsArray != null && slotsArray.length() > 0) {
                                String action = "";
                                String switch_name = "";
                                String switch_another = "";

                                for (int i = 0; i < slotsArray.length(); i++) {
                                    JSONObject slotsObject = slotsArray.optJSONObject(i);
                                    String name = slotsObject.optString("name");
                                    String original_word = slotsObject.optString("original_word");
                                    if (name.equals("user_open_action")) {
                                        action = "open";
                                    } else if (name.equals("user_close_action")) {
                                        action = "close";
                                    } else if (name.equals("user_switch_name")) {
                                        switch_name = original_word;
                                    } else if (name.equals("user_switch_another")) {
                                        switch_another = original_word;
                                    }
                                }

                                int status = 0;
                                if (action.equals("open")) {
                                    status = 1;
                                } else if (action.equals("close")) {
                                    status = 0;
                                }

                                String deviceMac = "";
                                String switchName = switch_another + switch_name;
                                if (devicesArray != null) {
                                    for (int i = 0; i < devicesArray.length(); i++) {
                                        JSONObject object1 = devicesArray.optJSONObject(i);
                                        String name = object1.optString("name");
                                        if (name.equals(switch_name)) {
                                            deviceMac = object1.optString("mac");
                                            break;
                                        } else if (name.equals(switch_another)) {
                                            deviceMac = object1.optString("mac");
                                            break;
                                        } else if (switchName.contains(name)) {
                                            deviceMac = object1.optString("mac");
                                            break;
                                        }
                                    }
                                }
                                Functions.controlSwitch(mBroadLinkNetworkData,status, deviceMac, raw_text,switchHandler);

                            } else {
                                System.out.println("local:" + local);
                                if (!local) {
                                    Message message = new Message();
                                    message.obj = raw_text;
                                    message.what = 1;
                                    switchHandler.sendMessage(message);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void controlSwitch(BLNetworkDataParse mBroadLinkNetworkData , final int status, String mac, final String bestText , final Handler switchHandler) {
        byte[] sendData = new byte[0];

        final Message message = new Message();

        message.obj = bestText;
        //1为开 0为关
        sendData = mBroadLinkNetworkData.BLSP2SwitchControlBytes(status);
        NewModuleNetUnit mNewModuleNetUnit = new NewModuleNetUnit();
        mNewModuleNetUnit.sendData(mac, sendData, new AsyncTaskCallBack() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void onPostExecute(SendDataResultInfo resultData) {
                try {
                    if (resultData != null) {
                        if (resultData.resultCode == 0){
                            message.what = 0;
                            switchHandler.sendMessage(message);
                        } else {
                            message.what = 1;
                            switchHandler.sendMessage(message);
                        }
                    } else {
                        message.what = 1;
                        switchHandler.sendMessage(message);
                    }
                } catch (Exception e) {
                    Log.i("mini home fragment", e.getMessage(), e);
                }
            }
        });
    }

    public static void initSpeech(SpeechSynthesizer mSpeechSynthesizer , Context context, final Handler handler , String appId , String appKey , String secretKey){
        mSpeechSynthesizer.setContext(context);
        mSpeechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
            @Override
            public void onSynthesizeStart(String s) {
                //准备开始合成
//                System.out.println("准备开始合成");
                handler.sendEmptyMessage(3);
            }
            @Override
            public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
                //合成过程回调
//                System.out.println("合成过程回调");

            }
            @Override
            public void onSynthesizeFinish(String s) {
                //合成结束回调
//                System.out.println("合成结束回调");
            }
            @Override
            public void onSpeechStart(String s) {
                //播放开始回调
//                System.out.println("播放开始回调");
            }
            @Override
            public void onSpeechProgressChanged(String s, int i) {
                //播放过程回调
//                System.out.println("播放过程回调");
            }
            @Override
            public void onSpeechFinish(String s) {
                //播放结束回调
//                System.out.println("播放结束回调");
            }
            @Override
            public void onError(String s, SpeechError speechError) {
                //错误回调
//                System.out.println("错误回调");
            }
        });
        mSpeechSynthesizer.setAppId(appId);
        mSpeechSynthesizer.setApiKey(appKey, secretKey);
        mSpeechSynthesizer.auth(TtsMode.ONLINE);

        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "6");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);

        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);
    }

    public static void initParams(WindowManager.LayoutParams params , int width , int height , int flags) {
        //Android8.0行为变更，对8.0进行适配https://developer.android.google.cn/about/versions/oreo/android-8.0-changes#o-apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        if (flags != 6899){
            params.flags = flags;

            System.out.println("设置");

        }
        //设置窗口初始停靠位置.
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        params.width = width;
        params.height = height;
    }

}
