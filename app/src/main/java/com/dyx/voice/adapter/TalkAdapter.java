package com.dyx.voice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dyx.voice.R;
import com.dyx.voice.service.MainService;

import java.util.List;

public class TalkAdapter extends BaseAdapter {

    private List<String> data;
    private Context context;

    public TalkAdapter(List<String> data , Context context){
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.item_listview, null);
            holder.title = convertView.findViewById(R.id.textView);
            holder.ic_image = convertView.findViewById(R.id.ic_image);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(data.get(position));
        if (data.get(position).equals("我要看刘德华的电影")) {
            holder.ic_image.setImageResource(R.drawable.ic_movie);
        } else if (data.get(position).equals("音量调大")) {
            holder.ic_image.setImageResource(R.drawable.ic_voice);
        }

        return convertView;
    }

    public class ViewHolder {
        public TextView title;
        public ImageView ic_image;
    }
}
