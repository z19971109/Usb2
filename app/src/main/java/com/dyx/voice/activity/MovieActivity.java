package com.dyx.voice.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.dyx.voice.view.MainUpView;
import com.dyx.voice.adapter.MovieGridAdapter;
import com.dyx.voice.inter.MovieInterface;
import com.dyx.voice.R;
import com.dyx.voice.bridge.EffectNoDrawBridge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovieActivity extends Activity{


    private GridView movie_grid;

    private MainUpView mainUpView1;

    private View mOldView;


    // 更新数据后还原焦点框.
    Handler mFindhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            if (mSavePos != -1) {
            movie_grid.requestFocusFromTouch();
//                movie_grid.setSelection(mSavePos);
//            }
        }
    };

    private List<MovieInterface> lists;

    private String dataLists;

    private String title_name;

    private TextView title_name_text , movie_size_text;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_layout);

        title_name_text = findViewById(R.id.title_name_text);
        movie_grid = findViewById(R.id.movie_grid);
        movie_size_text = findViewById(R.id.movie_size_text);
        mainUpView1 = findViewById(R.id.mainUpView1);

        // 建议使用 NoDraw.
        mainUpView1.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge bridget = (EffectNoDrawBridge) mainUpView1.getEffectBridge();
        bridget.setTranDurAnimTime(200);
        // 设置移动边框的图片.
        mainUpView1.setUpRectResource(R.drawable.white_light_10);
        // 移动方框缩小的距离.
        mainUpView1.setDrawUpRectPadding(new Rect(-11, -5, -11, -57));

        movie_grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
        //
        movie_grid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * 这里注意要加判断是否为NULL.
                 * 因为在重新加载数据以后会出问题.
                 */
                if (view != null) {
                    mainUpView1.setFocusView(view, mOldView, 1.1f);
                }
                mOldView = view;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        movie_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFindhandler.removeCallbacksAndMessages(null);
                mFindhandler.sendMessageDelayed(mFindhandler.obtainMessage(), 111);

                MovieInterface movieInterface = lists.get(position);
                String uuid = movieInterface.getUuid();

                try {
                    Intent intent = new Intent("myvst.intent.action.VodPlayer");
                    intent.putExtra("uuid", uuid);
                    intent.putExtra("checkbackhome", false);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "您尚未安装CIBN视频软件!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        lists = new ArrayList<>();

        dataLists = getIntent().getStringExtra("dataLists");
        title_name = getIntent().getStringExtra("title_name");

        title_name_text.setText(title_name);

        if (dataLists != null && !dataLists.equals("")){
            try {
                JSONArray dataArray = new JSONArray(dataLists);
                if (dataArray != null && dataArray.length() > 0){


                    movie_size_text.setText("共"+dataArray.length()+"部");

                    for (int i = 0 ; i < dataArray.length() ; i++){
                        JSONObject dataObject = dataArray.optJSONObject(i);
                        String act = dataObject.optString("act");
                        String mark = dataObject.optString("mark");
                        String pic = dataObject.optString("pic");
                        String title = dataObject.optString("title");
                        String uuid = dataObject.optString("uuid");
                        MovieInterface movieInterface = new MovieInterface(act,mark,pic,title,uuid);

                        lists.add(movieInterface);

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        MovieGridAdapter movieGridAdapter = new MovieGridAdapter(MovieActivity.this,lists);

        movie_grid.setAdapter(movieGridAdapter);
    }
}
