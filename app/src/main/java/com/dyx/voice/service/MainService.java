package com.dyx.voice.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.dyx.voice.adapter.TalkAdapter;
import com.dyx.voice.function.Functions;
import com.dyx.voice.inter.AsyncTaskCallBack;
import com.dyx.voice.adapter.ChatAdapter;
import com.dyx.voice.inter.Hdiy;
import com.dyx.voice.function.NewModuleNetUnit;
import com.dyx.voice.inter.PersonChat;
import com.dyx.voice.R;
import com.dyx.voice.function.RecogResult;
import com.dyx.voice.UsbApplication;
import com.dyx.voice.activity.BaiDuActivity;
import com.dyx.voice.activity.MovieActivity;
import com.dyx.voice.bridge.EffectNoDrawBridge;
import com.dyx.voice.inter.Volume;
import com.dyx.voice.view.ListViewTV;
import com.dyx.voice.view.MainUpView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.broadlink.blnetworkdataparse.BLNetworkDataParse;
import cn.com.broadlink.blnetworkunit.BLNetworkUnit;
import cn.com.broadlink.blnetworkunit.ScanDevice;
import cn.com.broadlink.blnetworkunit.ScanDeviceListener;
import cn.com.broadlink.blnetworkunit.SendDataResultInfo;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.conn.scheme.PlainSocketFactory;
import cz.msebera.android.httpclient.conn.scheme.Scheme;
import cz.msebera.android.httpclient.conn.scheme.SchemeRegistry;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.params.HttpProtocolParams;
import cz.msebera.android.httpclient.protocol.HTTP;

public class MainService extends Service {

    private EventManager asr;
    private EventListener listener;
    private AudioManager am;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    private ChatAdapter chatAdapter;
    private Intent channelIntent;
    private SpeechSynthesizer mSpeechSynthesizer;
    private TtsMode ttsMode = TtsMode.ONLINE;

    private ImageView image1, image2, image3, image4, image5, image6, image7, imageView;
    private ListView lv_chat_dialog;
    private LinearLayout image_lin, tips_lin, sessionLinearLayout;
    private RelativeLayout my_re;

    private boolean talking = false;
    private int DB = 0;
    private static final String TAG = "MainService";
    private static final String ERROR_ANSWER = "啊哦~~这个问题太难了，换个问题吧！";
    private int voiceMax;
    private boolean local = false;
    private boolean isReading = false;
    private String channelS;
    private List<String> data;
    private List<PersonChat> personChats = new ArrayList<PersonChat>();
    private String appId = "14946762";
    private String appKey = "ccIyYAENa6kTA1AKcG7iOh2G";
    private String secretKey = "CveL4xdBB894eFBXD7Dl4TP5kISIxGap";

    private AsyncHttpClient asyncHttpClient;

    //不与Activity进行绑定.
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "MainService Created");
        createToucher();
    }

    private Hdiy hdiy;
    ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hdiy = Hdiy.Stub.asInterface(service);
            try {
                channelS = hdiy.getAllChannelInfo();
                System.out.println("json:" + channelS);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private LayoutInflater mInflater;


    private MainUpView mainUpView1;
    private View mOldView;
    private ListViewTV talk_list;


    @SuppressLint("ClickableViewAccessibility")
    private void createToucher() {
        searchDevices();
        initView();
        initListener();
        init();

        DisplayMetrics dm2 = getResources().getDisplayMetrics();
        Functions.initParams(params, dm2.widthPixels / 8 * 2, 440, 6899);

        listener = new EventListener() {
            @Override
            public void onEvent(final String name, String params, final byte[] data, int offset, int length) {
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_VOLUME)) {
                    Volume vol = Functions.parseVolumeJson(params);
                    DB = vol.volumePercent;
                    voiceAnimation();
                    DB = 0;
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                    try {
                        JSONObject object = new JSONObject(params);
                        int error = object.optInt("error");
                        int sub_error = object.optInt("sub_error");
                        String desc = object.optString("desc");
                        System.out.println(TAG + "结束:" + error + "|" + sub_error + "|" + desc);
                        if (error != 0) {
                            if (asr != null) {
                                asr.send(SpeechConstant.ASR_CANCEL, null, null, 0, 0);
                                Functions.ASR_START(asr);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                    System.out.println("params:" + params);
                    talking = true;
                    RecogResult recogResult = RecogResult.parseJson(params);
                    if (recogResult.isFinalResult()) {
                        System.out.println(TAG + "本地:" + recogResult.getOrigalJson());
                        String json = recogResult.getOrigalJson();
                        try {
                            JSONObject object = new JSONObject(json);
                            final String best_result = object.optString("best_result");
                            String results_nlu = object.optString("results_nlu");
                            if (!results_nlu.equals("")) {
                                JSONObject localObject = new JSONObject(results_nlu);
                                if (localObject != null) {
                                    JSONArray localResultsArray = localObject.optJSONArray("results");
                                    String local_raw_text = localObject.optString("raw_text");
                                    if (localResultsArray.length() > 0) {
                                        System.out.println("本地大于0");
                                        JSONObject localResultObject = localResultsArray.optJSONObject(0);
                                        String domain = localResultObject.optString("domain");
                                        String intent = localResultObject.optString("intent");
                                        if (domain.equals("change") && intent.equals("channel")) {
                                            if (channelS != null) {
                                                JSONArray array = new JSONArray(channelS);
                                                Random rand = new Random();
                                                if (array != null && array.length() > 0) {
                                                    int random = rand.nextInt(array.length() - 1) + 0;
                                                    System.out.println("random:" + random);
                                                    JSONObject channelObject = array.optJSONObject(random);
                                                    int channelNum = channelObject.optInt("channelNum");
                                                    changeChannel(local_raw_text, "随机频道", channelNum);
                                                }
                                            }
                                        } else {
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (recogResult.isNluResult()) {
                        System.out.println(TAG + "在线:" + new String(data, offset, length));
                        try {
                            JSONObject object = new JSONObject(new String(data, offset, length));
                            System.out.println("object:" + object);
                            JSONObject merged_res = object.optJSONObject("merged_res");
                            JSONObject semantic_form = merged_res.optJSONObject("semantic_form");
                            int err_no = semantic_form.optInt("err_no", -1);
                            if (err_no == 0) {
                                //分词
                                String parsed_text = semantic_form.optString("parsed_text");
                                String[] parsedS = parsed_text.split(" ");
                                //本义
                                final String raw_text = semantic_form.optString("raw_text");
                                JSONArray resultsArray = semantic_form.optJSONArray("results");
                                if (resultsArray != null && resultsArray.length() > 0) {
                                    JSONObject resultsObject = resultsArray.optJSONObject(0);
                                    //领域，范围
                                    String domain = resultsObject.optString("domain");
                                    //意图
                                    String intent = resultsObject.optString("intent");
                                    //内容
                                    JSONObject object1 = resultsObject.optJSONObject("object");
                                    String answer_text = ERROR_ANSWER;
                                    switch (domain) {
                                        case "tv_instruction":
                                            //电视指令
                                            switch (intent) {
                                                case "change_channel":
                                                    if (object1 != null) {
                                                        //频道数
                                                        String channel = object1.optString("channel");
                                                        if (Functions.isNumeric(channel)) {
                                                            int channelI = Integer.valueOf(channel);
                                                            changeChannel(raw_text, channelI + "", channelI);
                                                        } else {
                                                            speakEnAndDisplay(raw_text, ERROR_ANSWER);
                                                        }
                                                    }
                                                    break;
                                                case "change_station":
                                                    if (object1 != null) {
                                                        //节目名称
                                                        String station = object1.optString("station");
                                                        String _station = object1.optString("_station");
                                                        System.out.println("节目：" + station + "|" + _station);
                                                        String other_name = object1.optString("other_name");
                                                        String[] otherNameArray = other_name.split("#");
                                                        String channelNames = "";
                                                        int channelNums = 0;
                                                        if (station.contains("新闻") || _station.contains("新闻")) {
                                                            channelNames = "新闻频道";
                                                            channelNums = 13;
                                                        } else if (station.contains("综合") || _station.contains("综合")) {
                                                            channelNames = "综合频道";
                                                            channelNums = 1;
                                                        } else {
                                                            if (channelS != null) {
                                                                JSONArray array = new JSONArray(channelS);
                                                                if (array != null && array.length() > 0) {
                                                                    outterLoop:
                                                                    for (int j = 0; j < array.length(); j++) {
                                                                        JSONObject channelObject = array.optJSONObject(j);
                                                                        int channelNum = channelObject.optInt("channelNum");
                                                                        String channelName = channelObject.optString("channelName").replaceAll(" ", "");
                                                                        String reg = "[\\u4e00-\\u9fa5]+";
                                                                        if (!station.matches(reg)) {
                                                                            station = station.replaceAll("[^a-z^A-Z^0-9]", "");
                                                                        } else {
                                                                            station = station;
                                                                        }
                                                                        for (int k = 0; k < otherNameArray.length; k++) {
                                                                            if (otherNameArray[k].toUpperCase().equals(channelName.toUpperCase())) {
                                                                                channelNames = channelName;
                                                                                channelNums = channelNum;
                                                                                break outterLoop;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        changeChannel(raw_text, channelNames, channelNums);
                                                    }
                                                    break;
                                                case "set":
                                                    //音量
                                                    JSONObject insObject = resultsObject.optJSONObject("object");
                                                    String settingtype = insObject.optString("settingtype");
                                                    if (settingtype.equals("volume_setting")) {
                                                        String value = insObject.optString("value");
                                                        String regEx = "[^0-9]";
                                                        if (!value.equals("")) {
                                                            Pattern p = Pattern.compile(regEx);
                                                            Matcher m = p.matcher(value);
                                                            int valueI = Integer.valueOf(m.replaceAll("").trim());
                                                            am.setStreamVolume(AudioManager.STREAM_MUSIC, valueI, AudioManager.FLAG_SHOW_UI);
                                                        }
                                                    } else {
                                                        speakEnAndDisplay(raw_text, ERROR_ANSWER);
                                                    }
                                                    break;
                                            }
                                            break;
                                        case "tv_show":
                                            //电视节目
                                            if (intent.equals("play") || intent.equals("search")) {
                                                if (object1 != null) {
                                                    //节目名称
                                                    final String tvName = object1.optString("name");
                                                    String getS = "/type_综艺/movie_" + tvName + "/input_" + raw_text + ".dat";
                                                    System.out.println("getS:" + getS);
                                                    searcTVShow(getS, raw_text, tvName);
                                                }
                                            } else {
                                                speakEnAndDisplay(raw_text, ERROR_ANSWER);
                                            }
                                            break;

                                        case "novel":
                                            final String novelName = object1.optString("name");
                                            String getNovelS = "";
                                            if (!novelName.equals("")) {
                                                getNovelS = "/actor_" + novelName;
                                            }
                                            getNovelS = getNovelS + "/input_" + novelName + ".dat";
                                            System.out.println("getNovelS:" + getNovelS);
                                            searchMovies(getNovelS, raw_text, novelName);
                                            break;
                                        case "person":
                                            final String person = object1.optString("person");
                                            String getS = "";
                                            if (!person.equals("")) {
                                                getS = "/actor_" + person;
                                            }
                                            getS = getS + "/input_" + person + ".dat";
                                            System.out.println("getS:" + getS);
                                            searchMovies(getS, raw_text, person);
                                            break;
                                        case "video":
                                            if (intent.equals("play") || intent.equals("search")) {
                                                String getSVideo = "";
                                                //节目类型
                                                String _sub_type = object1.optString("_sub_type");//category
                                                if (!_sub_type.equals("")) {
                                                    getSVideo = "/category_" + _sub_type;
                                                }
                                                //节目名称
                                                final String tvName = object1.optString("name");//movie
                                                if (!tvName.equals("")) {
                                                    getSVideo = getSVideo + "/movie_" + tvName;
                                                }
                                                //节目类型（国产，韩国，恐怖之类的）Array
                                                JSONArray typeArray = object1.optJSONArray("type");
                                                //国家
                                                String country = object1.optString("country");//area
                                                if (!country.equals("")) {
                                                    getSVideo = getSVideo + "/area_" + country;
                                                }
                                                //演员Array
                                                JSONArray actor = object1.optJSONArray("actor");
                                                //导演
                                                JSONArray director = object1.optJSONArray("director");
                                                //时间
                                                String _datepublish = object1.optString("_datepublish");//year
                                                if (!_datepublish.equals("")) {
                                                    getSVideo = getSVideo + "/year_" + _datepublish.replaceAll("[^a-z^A-Z^0-9]", "");
                                                }
                                                if (actor != null && actor.length() > 0) {
                                                    String actor0 = actor.optString(0);//actor
                                                    if (!actor0.equals("")) {
                                                        getSVideo = getSVideo + "/actor_" + actor0;
                                                    }
                                                }
                                                if (director != null && director.length() > 0) {
                                                    String director0 = director.optString(0);//director
                                                    if (!director0.equals("")) {
                                                        getSVideo = getSVideo + "/actor_" + director0;
                                                    }
                                                }
                                                if (typeArray != null && typeArray.length() > 0) {
                                                    String type0 = typeArray.getString(0);//type
                                                    if (!type0.equals("")) {
                                                        if (type0.equals("游戏")) {
                                                            getSVideo = "/category_" + type0;
                                                            if (!tvName.equals("")) {
                                                                getSVideo = getSVideo + "/game_" + tvName;
                                                            }
                                                        } else {
                                                            getSVideo = getSVideo + "/type_" + type0;
                                                        }
                                                    }
                                                }
                                                getSVideo = getSVideo + "/input_" + raw_text + ".dat";
                                                System.out.println("getSVideo:" + getSVideo);
                                                if (actor !=null && actor.length() > 0){
                                                    searchVideo(getSVideo, raw_text, tvName , actor.optString(0));
                                                } else {
                                                    searchVideo(getSVideo, raw_text, tvName , "");
                                                }
                                            } else {
                                                speakEnAndDisplay(raw_text, ERROR_ANSWER);
                                            }
                                            break;
                                        case "music":
                                            String getSMusic = "";
                                            if (intent.equals("search")) {
                                                JSONArray byartist = object1.optJSONArray("byartist");
                                                if (byartist != null) {
                                                    String byarName = byartist.optString(0);
                                                    getSMusic = getSMusic + "/actor_" + byarName;
                                                }
                                                String movieName = object1.optString("name");
                                                getSMusic = getSMusic + "/movie_" + movieName;
                                                getSMusic = getSMusic + "/input_" + raw_text + ".dat";
                                                System.out.println("getSMusic:" + getSMusic);
                                                searchMovies(getSMusic, raw_text, movieName);
                                            } else {
                                                speakEnAndDisplay(raw_text, ERROR_ANSWER);
                                            }
                                            break;
                                        case "player":
                                            if (object1 != null) {
                                                String action_type = object1.optString("action_type");
                                                JSONObject object2 = new JSONObject();
                                                switch (intent) {
                                                    case "locate":
                                                        object2.put("operate", "seek");
                                                        if (!object1.optString("minute").equals("")) {
                                                            int minute = Integer.valueOf(object1.optString("minute"));
                                                            minute = minute * 60;
                                                            object2.put("seek_absolute_pos", minute);
                                                            answer_text = "已为您跳转到" + object1.optString("minute") + "分钟";
                                                        }
                                                        break;
                                                    case "set":
                                                        switch (action_type) {
                                                            case "previous_episode":
                                                                object2.put("operate", "prev_episode");
                                                                object2.put("seek_relative_pos", 1);
                                                                answer_text = "已为您" + raw_text;
                                                                break;
                                                            case "next_episode":
                                                                object2.put("operate", "next_episode");
                                                                object2.put("seek_relative_pos", 1);
                                                                answer_text = "已为您" + raw_text;
                                                                break;
                                                            case "play":
                                                                object2.put("operate", "resume");
                                                                object2.put("cause", "user");
                                                                answer_text = "已为您继续播放";
                                                                break;
                                                            case "pause":
                                                                object2.put("operate", "pause");
                                                                object2.put("cause", "user");
                                                                answer_text = "已为您暂停播放";
                                                                break;
                                                            case "fast_forward":
                                                                object2.put("operate", "fast_forward");
                                                                if (!object1.optString("minute").equals("")) {
                                                                    int minute = Integer.valueOf(object1.optString("minute"));
                                                                    minute = minute * 60;
                                                                    object2.put("seek_relative_pos", minute);
                                                                    answer_text = "已为您快进" + object1.optString("minute") + "分钟";
                                                                } else {
                                                                    object2.put("seek_relative_pos", 15);
                                                                    answer_text = "已为您快进15秒";
                                                                }
                                                                break;
                                                            case "fast_backward":
                                                                object2.put("operate", "rewind");
                                                                if (!object1.optString("minute").equals("")) {
                                                                    int minute = Integer.valueOf(object1.optString("minute"));
                                                                    minute = minute * 60;
                                                                    object2.put("seek_relative_pos", minute);
                                                                    answer_text = "已为您快退" + object1.optString("minute") + "分钟";
                                                                } else {
                                                                    object2.put("seek_relative_pos", 15);
                                                                    answer_text = "已为您快退15秒";
                                                                }
                                                                break;

                                                        }
                                                        break;
                                                }
                                                object2.put("input", raw_text);
                                                System.out.println("设置:" + object2.toString());
                                                Intent controlIntent = new Intent("com.cocheer.remoter.vst.vod.control");
                                                controlIntent.putExtra("data", object2.toString());
                                                sendBroadcast(controlIntent);
                                                speakEnAndDisplay(raw_text, answer_text);
                                            }
                                            break;

                                        case "instruction":
                                            JSONObject insObject = resultsObject.optJSONObject("object");
                                            String regEx = "[^0-9]";
                                            String value = insObject.optString("value");
                                            switch (intent) {
                                                case "volume_down":
                                                    if (!value.equals("")) {
                                                        Pattern p = Pattern.compile(regEx);
                                                        Matcher m = p.matcher(value);
                                                        int valueI = Integer.valueOf(m.replaceAll("").trim());
                                                        int valueNow = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                                                        int controlVoice = 0;
                                                        System.out.println("valueI:" + valueI);
                                                        System.out.println("valueNow:" + valueNow);
                                                        if ((valueNow - valueI) <= 0) {
                                                            controlVoice = 0;
                                                        } else {
                                                            controlVoice = valueNow - valueI;
                                                        }
                                                        System.out.println("controlVoice:" + controlVoice);
                                                        am.setStreamVolume(AudioManager.STREAM_MUSIC, controlVoice, AudioManager.FLAG_SHOW_UI);
                                                        answer_text = "已为您调小音量";
                                                    } else {
                                                        System.out.println("音量调小");
                                                        for (int j = 0; j < 6; j++) {
                                                            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                                                        }
                                                        answer_text = "已为您调小音量";
                                                    }
                                                    break;
                                                case "volume_up":
                                                    if (!value.equals("")) {
                                                        Pattern p = Pattern.compile(regEx);
                                                        Matcher m = p.matcher(value);
                                                        int valueI = Integer.valueOf(m.replaceAll("").trim());
                                                        int valueNow = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                                                        int controlVoice = 0;
                                                        System.out.println("valueI:" + valueI);
                                                        System.out.println("valueNow:" + valueNow);
                                                        System.out.println("voiceMax:" + voiceMax);
                                                        if ((valueI + valueNow) >= voiceMax) {
                                                            controlVoice = voiceMax;
                                                        } else {
                                                            controlVoice = valueI + valueNow;
                                                        }
                                                        System.out.println("controlVoice:" + controlVoice);
                                                        am.setStreamVolume(AudioManager.STREAM_MUSIC, controlVoice, AudioManager.FLAG_SHOW_UI);
                                                        answer_text = "已为您调大音量";
                                                    } else {
                                                        System.out.println("音量调大");
                                                        for (int j = 0; j < 6; j++) {
                                                            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                                                        }
                                                        answer_text = "已为您调大音量";
                                                    }
                                                    break;
                                                case "set":
                                                    if (!value.equals("")) {
                                                        Pattern p = Pattern.compile(regEx);
                                                        Matcher m = p.matcher(value);
                                                        int valueI = Integer.valueOf(m.replaceAll("").trim());
                                                        am.setStreamVolume(AudioManager.STREAM_MUSIC, valueI, AudioManager.FLAG_SHOW_UI);
                                                        answer_text = "已为您设置音量";
                                                    }
                                                    break;
                                                case "volume_up_max":
                                                    int maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                                    am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVoice, AudioManager.FLAG_SHOW_UI);
                                                    answer_text = "音量已调节到最大";
                                                    break;
                                                case "volume_down_min":
                                                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
                                                    answer_text = "音量已调节到最小";
                                                    break;
                                                case "quit":
                                                    JSONObject object2 = new JSONObject();
                                                    object2.put("operate", "finish");
                                                    object2.put("cause", "user");
                                                    object2.put("input", "退出");
                                                    System.out.println("设置:" + object2.toString());
                                                    Intent controlIntent = new Intent("com.cocheer.remoter.vst.vod.control");
                                                    controlIntent.putExtra("data", object2.toString());
                                                    sendBroadcast(controlIntent);
                                                    answer_text = "已为您退出播放";
                                                    break;
                                            }
                                            speakEnAndDisplay(raw_text, answer_text);
                                            break;

                                        case "setting":
                                            if (intent.equals("set")) {
                                                String settingtype = object1.optString("settingtype");
                                                if (settingtype.equals("ring_silent")) {
                                                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
                                                    answer_text = "设置静音";
                                                } else {
                                                    answer_text = ERROR_ANSWER;
                                                }
                                                speakEnAndDisplay(raw_text, answer_text);
                                            }
                                            break;

                                        default:
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String result = Functions.utterance(raw_text);
                                                    if (result != null) {
                                                        Functions.parseSwitchJson(devicesArray, result, raw_text, mBroadLinkNetworkData, switchHandler, local);
                                                    } else {
                                                        if (!local) {
                                                            speakEnAndDisplay(raw_text, ERROR_ANSWER);
                                                        }
                                                    }
                                                }
                                            }).start();
                                            break;
                                    }

                                } else {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String result = Functions.utterance(raw_text);
                                            if (result != null) {
                                                Functions.parseSwitchJson(devicesArray, result, raw_text, mBroadLinkNetworkData, switchHandler, local);
                                            } else {
                                                if (!local) {
                                                    speakEnAndDisplay(raw_text, ERROR_ANSWER);
                                                }
                                            }
                                        }
                                    }).start();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        asr.registerListener(listener);

        mSpeechSynthesizer.initTts(ttsMode);

        initIntent();
        searchUsb();
        Functions.ASR_START(asr);
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        sessionLinearLayout = (LinearLayout) inflater.inflate(R.layout.smart_interactive, null);
        //主动计算出当前View的宽高信息.
        sessionLinearLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        imageView = sessionLinearLayout.findViewById(R.id.image);
        mainUpView1 = sessionLinearLayout.findViewById(R.id.mainUpView1);
        talk_list = sessionLinearLayout.findViewById(R.id.talk_list);
        lv_chat_dialog = sessionLinearLayout.findViewById(R.id.lv_chat_dialog);
        my_re = sessionLinearLayout.findViewById(R.id.my_re);
        image1 = sessionLinearLayout.findViewById(R.id.image1);
        image2 = sessionLinearLayout.findViewById(R.id.image2);
        image3 = sessionLinearLayout.findViewById(R.id.image3);
        image4 = sessionLinearLayout.findViewById(R.id.image4);
        image5 = sessionLinearLayout.findViewById(R.id.image5);
        image6 = sessionLinearLayout.findViewById(R.id.image6);
        image7 = sessionLinearLayout.findViewById(R.id.image7);
        tips_lin = sessionLinearLayout.findViewById(R.id.tips_lin);
        image_lin = sessionLinearLayout.findViewById(R.id.image_lin);

        mainUpView1.setEffectBridge(new EffectNoDrawBridge());

        EffectNoDrawBridge bridget = (EffectNoDrawBridge) mainUpView1.getEffectBridge();
        bridget.setTranDurAnimTime(200);

        mainUpView1.setUpRectResource(R.drawable.white_light_10); // 设置移动边框的图片.
        mainUpView1.setDrawUpRectPadding(new Rect(0, 0, 10, 0)); // 边框图片设置间距.


        imageView.setClickable(false);
        lv_chat_dialog.setClickable(false);
        lv_chat_dialog.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        data = new ArrayList<String>();
        String text = "我要看刘德华的电影";
        data.add(text);
        String text2 = "音量调大";
        data.add(text2);

        TalkAdapter talkAdapter = new TalkAdapter(data,this);
        talk_list.setAdapter(talkAdapter);

        chatAdapter = new ChatAdapter(getApplicationContext(), personChats);
        lv_chat_dialog.setAdapter(chatAdapter);
    }

    private void initListener() {
        talk_list.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (sessionLinearLayout.getParent() != null) {

                        controlHandler.removeCallbacks(stopRunnable);

                        isShowing = false;

                        windowManager.removeView(sessionLinearLayout);
                    }
                    return true;
                }
                return false;
            }
        });

        talk_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    view.bringToFront();
                    mainUpView1.setFocusView(view, mOldView, 1f);
                    mOldView = view;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        talk_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    //刘德华的电影
                    if (!talking) {
                        talking = true;
                        final String raw_text = "我要看刘德华的电影";
                        String getS = "/category_电影/actor_刘德华/input_刘德华的电影.dat";
                        searcTVShow(getS, raw_text, "刘德华");
                    }
                } else if (position == 1) {
                    //音量调大
                    for (int j = 0; j < 6; j++) {
                        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    }
                    speakEnAndDisplay("音量调大", "已为您调大音量~");
                }
            }
        });
    }

    private void init() {

        LoggerProxy.printable(true);

        channelIntent = new Intent();

        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        Functions.initSpeech(mSpeechSynthesizer, this, handler, appId, appKey, secretKey);

        asyncHttpClient = new AsyncHttpClient(getSchemeRegistry());

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        voiceMax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        this.mInflater = LayoutInflater.from(getApplicationContext());

        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);

        asr = EventManagerFactory.create(getApplicationContext(), "asr");
    }

    private void initIntent() {
        Intent intent = new Intent();
        intent.setAction("hdpfans.com.aidl.Hdiy");
        intent.setPackage("hdpfans.com");
        bindService(intent, connection,
                Context.BIND_AUTO_CREATE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_DEVICE_PERMISSION), 0);
        IntentFilter permissionFilter = new IntentFilter();
        permissionFilter.addAction(ACTION_DEVICE_PERMISSION);
        permissionFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        permissionFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbPermissionReceiver, permissionFilter);
    }

    private void searchMovies(String getS, final String raw_text, final String person) {

        final String[] answer_text = {""};
        asyncHttpClient.get("https://api.cp33.ott.cibntv.net/cibnvst-api/voicesearch" + getS, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                System.out.println("搜索:" + new String(bytes));
                try {
                    JSONObject searchObject = new JSONObject(new String(bytes));
                    int code = searchObject.optInt("code");

                    System.out.println("code:" + code);

                    if (code == 100) {
                        JSONArray dataArray = searchObject.optJSONArray("data");
                        if (dataArray != null && dataArray.length() > 0) {
                            System.out.println("dataArray:" + dataArray.toString());
                            answer_text[0] = "点播" + person + "相关";
                            Intent intent1 = new Intent(getApplicationContext(), MovieActivity.class);
                            intent1.putExtra("dataLists", dataArray.toString());
                            intent1.putExtra("title_name", person);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent1);
                        } else {
                            JSONObject dataObject = searchObject.optJSONObject("data");
                            if (dataObject != null) {
                                System.out.println("dataObject:" + dataObject.toString());
                                JSONObject classify = dataObject.optJSONObject("classify");
                                if (classify != null) {
                                    String action = classify.optString("action");
                                    String key = classify.optString("key");
                                    String value = classify.optString("value");
                                    System.out.println("action:" + action + "|key:" + key + "|value:" + value);
                                    Intent intent = new Intent(action);
                                    intent.putExtra(key, value);
                                    intent.putExtra("checkbackhome", false);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    answer_text[0] = "";
                                } else {
                                    answer_text[0] = ERROR_ANSWER;
                                }
                            } else {
                                answer_text[0] = ERROR_ANSWER;
                            }
                        }
                    } else {
                        answer_text[0] = ERROR_ANSWER;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    answer_text[0] = ERROR_ANSWER;
                    handler2.sendEmptyMessage(2);
                }
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                System.out.println("接口不通或网络异常:" + i + "|" + throwable.getMessage());
                answer_text[0] = ERROR_ANSWER;
                handler2.sendEmptyMessage(2);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                speakEnAndDisplay(raw_text, answer_text[0]);
            }
        });
    }

    private void searcTVShow(String getS, final String raw_text, final String tvName) {
        final String[] answer_text = {""};
        asyncHttpClient.get("https://api.cp33.ott.cibntv.net/cibnvst-api/voicesearch" + getS, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                System.out.println("搜索:" + new String(bytes));
                try {
                    JSONObject searchObject = new JSONObject(new String(bytes));
                    int code = searchObject.optInt("code");

                    System.out.println("code:" + code);

                    if (code == 100) {
                        JSONArray dataArray = searchObject.optJSONArray("data");
                        if (dataArray != null && dataArray.length() > 0) {
                            System.out.println("dataArray:" + dataArray.toString());
                            answer_text[0] = "点播" + tvName + "相关";
                            Intent intent1 = new Intent(getApplicationContext(), MovieActivity.class);
                            intent1.putExtra("dataLists", dataArray.toString());
                            intent1.putExtra("title_name", tvName);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent1);
                        }
                    } else {
                        answer_text[0] = ERROR_ANSWER;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    answer_text[0] = ERROR_ANSWER;
                    handler2.sendEmptyMessage(2);
                }
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                System.out.println("接口不通或网络异常:" + i + "|" + throwable.getMessage());
                answer_text[0] = ERROR_ANSWER;
                handler2.sendEmptyMessage(2);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                speakEnAndDisplay(raw_text, answer_text[0]);
            }
        });
    }

    private void searchVideo(String getS, final String raw_text, final String tvName , final String actor) {
        final String[] answer_text = {""};
        asyncHttpClient.get("https://api.cp33.ott.cibntv.net/cibnvst-api/voicesearch" + getS, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                System.out.println("搜索:" + new String(bytes));
                try {
                    JSONObject searchObject = new JSONObject(new String(bytes));
                    int code = searchObject.optInt("code");
                    System.out.println("code:" + code);
                    if (code == 100) {
                        JSONArray dataArray = searchObject.optJSONArray("data");
                        if (dataArray != null && dataArray.length() > 0) {
                            System.out.println("dataArray:" + dataArray.toString());
                            if (!tvName.equals("")) {
                                answer_text[0] = "点播" + tvName;
                            } else if (!actor.equals("")) {
                                answer_text[0] = "点播" + actor+ "相关";
                            } else {
                                answer_text[0] = "点播" + raw_text;
                            }
                            Intent intent1 = new Intent(getApplicationContext(), MovieActivity.class);
                            intent1.putExtra("dataLists", dataArray.toString());
                            if (!tvName.equals("")) {
                                intent1.putExtra("title_name", tvName);
                            } else if (!actor.equals("")){
                                intent1.putExtra("title_name", actor);
                            } else {
                                intent1.putExtra("title_name", raw_text);
                            }
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent1);
                        } else {
                            JSONObject dataObject = searchObject.optJSONObject("data");
                            if (dataObject != null) {
                                System.out.println("dataObject:" + dataObject.toString());
                                JSONObject classify = dataObject.optJSONObject("classify");
                                if (classify != null) {
                                    String action = classify.optString("action");
                                    String key = classify.optString("key");
                                    String value = classify.optString("value");
                                    System.out.println("action:" + action + "|key:" + key + "|value:" + value);
                                    Intent intent = new Intent(action);
                                    intent.putExtra(key, value);
                                    intent.putExtra("checkbackhome", false);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                    answer_text[0] = "点播" + tvName;
                                } else {
                                    answer_text[0] = ERROR_ANSWER;
                                }
                            } else {
                                answer_text[0] = ERROR_ANSWER;
                            }
                        }
                    } else {
                        answer_text[0] = ERROR_ANSWER;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    answer_text[0] = ERROR_ANSWER;
                    handler2.sendEmptyMessage(2);
                }
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                System.out.println("接口不通或网络异常:" + i + "|" + throwable.getMessage());
                answer_text[0] = ERROR_ANSWER;
                handler2.sendEmptyMessage(2);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                speakEnAndDisplay(raw_text, answer_text[0]);
            }
        });
    }

    private void changeChannel(String raw_text, String channelName, int channelNum) {
        try {
            System.out.println("节目名称equals：" + channelName);
            speakEnAndDisplay(raw_text, "切换到" + channelName);
            Intent channelIntent = new Intent();
            channelIntent.setAction("com.hdpfans.live.start");
            channelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            channelIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            channelIntent.putExtra("ChannelNum", channelNum);
            getApplicationContext().startActivity(channelIntent);
        } catch (Exception e) {
            speakEnAndDisplay(raw_text, "您尚未安装HDP直播软件!");
            Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_DEVICE_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            initDevice(device);
                        }
                    }
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                int vid = usbDevice.getVendorId();
                int pid = usbDevice.getProductId();
                if (vid == 4310 && pid == 45062) {
                    isReading = false;
                    Toast.makeText(context, "拔出USB", Toast.LENGTH_SHORT).show();
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                int vid = usbDevice.getVendorId();
                int pid = usbDevice.getProductId();
                if (vid == 4310 && pid == 45062) {
                    isReading = true;
                    searchUsb();
                    Toast.makeText(context, "插入USB", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    //搜索usb设备
    private void searchUsb() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devices = mUsbManager.getDeviceList();
        Iterator<UsbDevice> iterator = devices.values().iterator();
        while (iterator.hasNext()) {
            UsbDevice device = iterator.next();
            System.out.println(device.getVendorId() + "|" + device.getProductId());
            if (device.getVendorId() == 4310 && device.getProductId() == 45062) {
                if (mUsbManager.hasPermission(device)) {
                    initDevice(device);
                } else {
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }
            }
        }
    }
    //初始化设备
    private void initDevice(UsbDevice device) {
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            System.out.println("class:" + device.getInterface(i).getInterfaceClass());
        }

        UsbInterface usbInterface = device.getInterface(0);

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            System.out.println("end:" + usbInterface.getEndpoint(i).getType() + "|" + usbInterface.getEndpoint(i).getDirection());
        }

        UsbEndpoint ep = usbInterface.getEndpoint(0);
        mUsbEndpointIn = ep;
        if ((null == mUsbEndpointIn)) {
            mUsbEndpointIn = null;
            mUsbInterface = null;
        } else {
            mUsbInterface = usbInterface;
            mUsbDeviceConnection = mUsbManager.openDevice(device);

            startReading();

        }
    }
    //开线程读取数据
    private void startReading() {
        System.out.println("starting");
        mUsbDeviceConnection.claimInterface(mUsbInterface, true);
        isReading = true;
        final StringBuffer qr = new StringBuffer();
        mReadingthread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isReading) {
                    synchronized (this) {
                        byte[] bytes = new byte[1];
                        int requestType = UsbConstants.USB_DIR_IN | UsbConstants.USB_TYPE_VENDOR | 0x01;
                        int ret = mUsbDeviceConnection.controlTransfer(requestType, 0, 0, mUsbInterface.getId(), bytes, bytes.length, 1000);
                        if (ret > -1) {
                            StringBuilder stringbuilder = new StringBuilder(bytes.length);
                            for (byte b : bytes) {
                                if (b != 0) {
                                    if (b == 2) {
                                        stringbuilder.append("da");
                                    }
                                    stringbuilder.append(Integer.toHexString(b));
                                }
                            }
                            int getInt = (bytes[0] & 0xff);
                            if (getInt == 170) {
                                controlHandler.removeCallbacks(stopRunnable);
                                System.out.println("长按");
                                talking = false;
                                local = false;
                                handler2.sendEmptyMessage(1);
                            } else if (getInt == 85) {
                                System.out.println("松开");
                                handler2.sendEmptyMessage(2);
                            }
                        }
                    }
                }
                mUsbDeviceConnection.close();
            }
        });
        mReadingthread.start();
    }

    private UsbManager mUsbManager = null;
    private static final String ACTION_DEVICE_PERMISSION = "com.linc.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private UsbEndpoint mUsbEndpointIn;
    private UsbInterface mUsbInterface;
    private UsbDeviceConnection mUsbDeviceConnection;
    private Thread mReadingthread = null;

    public static SchemeRegistry getSchemeRegistry() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 10000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));
            return registry;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, BaiDuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification noti = new Notification.Builder(this)
                .setContentTitle("Title")
                .setContentText("Message")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(12346, noti);
        service = this;
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    @Override
    public void onDestroy() {
        if (asr != null) {
            asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
            asr.send(SpeechConstant.ASR_CANCEL, null, null, 0, 0);
        }
        if (sessionLinearLayout != null) {
            windowManager.removeView(sessionLinearLayout);
            isShowing = false;
        }
        Intent intent = new Intent("com.dyx.voice.service.MainService");
        sendBroadcast(intent);
        unregisterReceiver(mUsbPermissionReceiver);
        isReading = false;
        super.onDestroy();
    }

    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
                    my_re.setVisibility(View.INVISIBLE);
                    tips_lin.setVisibility(View.INVISIBLE);
                    image_lin.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    personChats.clear();
                    handler.sendEmptyMessage(1);

                    if (!isShowing) {
                        if (sessionLinearLayout != null) {
                            System.out.println("addView1");
                            windowManager.addView(sessionLinearLayout, params);
                            isShowing = true;
                        }
                    } else {
                        if (sessionLinearLayout != null) {
                            System.out.println("updateView1");
                            windowManager.updateViewLayout(sessionLinearLayout, params);
                            isShowing = true;
                        }
                    }
                    break;
                case 2:
                    System.out.println("handler2 - 2:" + talking);
                    if (talking) {
                        tips_lin.setVisibility(View.INVISIBLE);
                    } else {
                        tips_lin.setVisibility(View.VISIBLE);
                        my_re.setVisibility(View.GONE);
                        controlHandler.removeCallbacks(stopRunnable);
                    }
                    image_lin.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private void voiceAnimation() {
        if (DB > 0 && DB < 10) {
            Random rand = new Random();
            int random = rand.nextInt(2);
            int random2 = rand.nextInt(2);
            int random3 = rand.nextInt(2);
            int random4 = rand.nextInt(2);
            int random5 = rand.nextInt(2);
            int random6 = rand.nextInt(2);
            int random7 = rand.nextInt(2);
            image1.setImageLevel(random);
            image2.setImageLevel(random2);
            image3.setImageLevel(random3);
            image4.setImageLevel(random4);
            image5.setImageLevel(random5);
            image6.setImageLevel(random6);
            image7.setImageLevel(random7);
        } else if (DB >= 10 && DB < 20) {
            Random rand = new Random();
            int random = rand.nextInt(3);
            int random2 = rand.nextInt(3);
            int random3 = rand.nextInt(3);
            int random4 = rand.nextInt(3);
            int random5 = rand.nextInt(3);
            int random6 = rand.nextInt(3);
            int random7 = rand.nextInt(3);
            image1.setImageLevel(random);
            image2.setImageLevel(random2);
            image3.setImageLevel(random3);
            image4.setImageLevel(random4);
            image5.setImageLevel(random5);
            image6.setImageLevel(random6);
            image7.setImageLevel(random7);
        } else if (DB >= 60) {
            Random rand = new Random();
            int random = rand.nextInt(3);
            int random2 = rand.nextInt(3);
            int random3 = rand.nextInt(3);
            int random4 = rand.nextInt(3);
            int random5 = rand.nextInt(3);
            int random6 = rand.nextInt(3);
            int random7 = rand.nextInt(3);
            image1.setImageLevel(random);
            image2.setImageLevel(random2);
            image3.setImageLevel(random3);
            image4.setImageLevel(random4);
            image5.setImageLevel(random5);
            image6.setImageLevel(random6);
            image7.setImageLevel(random7);
        } else if (DB <= 0) {
            image1.setImageLevel(0);
            image2.setImageLevel(0);
            image3.setImageLevel(0);
            image4.setImageLevel(0);
            image5.setImageLevel(0);
            image6.setImageLevel(0);
            image7.setImageLevel(0);
        }
    }

    Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            if (isShowing) {
                if (sessionLinearLayout != null) {
                    System.out.println("remove2");
                    windowManager.removeView(sessionLinearLayout);
                    personChats.clear();
                    isShowing = false;
                }
            }
        }
    };
    private volatile static MainService service;

    public static boolean isServiceRunning() {
        return service != null;
    }

    private boolean isShowing = false;

    private void speakEnAndDisplay(String my_text, String answer_text) {
        answerChat = null;
        personChats.clear();
        System.out.println("添加" + my_text + answer_text);
        PersonChat personChat = new PersonChat();
        //代表自己发送
        personChat.setMeSend(true);
        //得到发送内容
        personChat.setChatMessage("" + my_text);
        answerChat = new PersonChat();
        //代表自己发送
        answerChat.setMeSend(false);
        //得到发送内容
        answerChat.setChatMessage("" + answer_text);
        //加入集合
        personChats.add(personChat);
        handler.sendEmptyMessage(1);
        handler.sendEmptyMessage(2);

        int speakResult = mSpeechSynthesizer.speak(answer_text);

        System.out.println("speak:" + speakResult);

    }

    private PersonChat answerChat;
    private BLNetworkDataParse mBroadLinkNetworkData;
    private BLNetworkUnit mBlNetworkUnit;
    private JSONArray devicesArray = new JSONArray();

    private void searchDevices() {
        if (mBlNetworkUnit == null) {
            mBlNetworkUnit = BLNetworkUnit.getInstanceBlNetworkUnit(this, "jiajutx1.6899.com", 16384, "jiajutx2.6899.com", 1812, "jiajutx2.6899.com", 80);
            UsbApplication.mBlNetworkUnit = mBlNetworkUnit;
        }
        if (mBlNetworkUnit != null) {
            SharedPreferences sp = getSharedPreferences(
                    "array", Activity.MODE_PRIVATE);
            String ar = sp.getString("ar", "");
            if (!ar.equals("")) {
                try {
                    JSONArray array = new JSONArray(ar);
                    System.out.println("array:" + array.toString());
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.optJSONObject(i);
                        mBlNetworkUnit.removeDevice(object.getString("mac"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mBroadLinkNetworkData = BLNetworkDataParse.getInstance();
            mBlNetworkUnit.setScanDeviceListener(new ScanDeviceListener() {
                @Override
                public void deviceInfoCallBack(ScanDevice scanDevice) {
                    Log.e("---------->scanDevice",
                            scanDevice.deviceName + " : " + scanDevice.mac
                                    + "\ntype:" + scanDevice.deviceType
                                    + "\nid: " + scanDevice.id
                                    + "\npassword:" + scanDevice.password);
                    System.out.println("找到设备");
                    if (scanDevice.deviceType == 30025) {
                        //插座
                        JSONObject deviceObject = new JSONObject();
                        try {
                            deviceObject.put("name", scanDevice.deviceName);
                            deviceObject.put("mac", scanDevice.mac);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        devicesArray.put(deviceObject);
                        mBlNetworkUnit.addDevice(scanDevice);
                    }
                    System.out.println("array2:" + devicesArray.toString());
                    SharedPreferences sp = getSharedPreferences(
                            "array", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("ar", devicesArray.toString());
                    editor.commit();
                }
            });
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case 3:
                    personChats.add(answerChat);
                    lv_chat_dialog.requestLayout();
                    chatAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    lv_chat_dialog.requestLayout();
                    chatAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                    DisplayMetrics dm2 = getResources().getDisplayMetrics();
                    Functions.initParams(params, dm2.widthPixels / 8 * 2, 440, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    windowManager.updateViewLayout(sessionLinearLayout, params);

                    System.out.println("update");
                    tips_lin.setVisibility(View.INVISIBLE);
                    my_re.setVisibility(View.VISIBLE);
                    controlHandler.postDelayed(stopRunnable, 6000);
                    break;
            }
        }
    };
    private Handler controlHandler = new Handler();

    private Handler switchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    speakEnAndDisplay(msg.obj.toString(), "控制成功~");
                    break;
                case 1:
                    speakEnAndDisplay(msg.obj.toString(), ERROR_ANSWER);
                    break;
            }
        }
    };

}
