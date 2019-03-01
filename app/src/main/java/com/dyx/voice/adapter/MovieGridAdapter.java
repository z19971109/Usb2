package com.dyx.voice.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dyx.voice.inter.MovieInterface;
import com.dyx.voice.R;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MovieGridAdapter extends BaseAdapter {

    private Context context;

    private List<MovieInterface> lists;

    public MovieGridAdapter(Context context, List<MovieInterface> lists) {
        this.context = context;
        this.lists = lists;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.movie_item, null);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int height = (dm.heightPixels) / 2 - 56;
        int width = (dm.widthPixels) / 6;

        AbsListView.LayoutParams param = new AbsListView.LayoutParams(
                width,
                height);//传入自己需要的宽高
        convertView.setLayoutParams(param);
        convertView.setLayoutParams(param);

        TextView mark_text = convertView.findViewById(R.id.mark_text);
        TextView title_text = convertView.findViewById(R.id.title_text);
        ImageView movie_image = convertView.findViewById(R.id.movie_image);

        MovieInterface movieInterface = lists.get(position);

        String mark = movieInterface.getMark();
        String title = movieInterface.getTitle();
        String image = movieInterface.getPic();

        if (mark.equals("") || mark.equals("0")){
            mark_text.setText("0.0");
        } else {
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(mark);
            if (isNum.matches()){
                mark_text.setText(mark+".0");
            } else {
                mark_text.setText(mark);
            }
        }

        title_text.setText(title);

        Glide.with(context).load(image).error(R.drawable.error_im).into(movie_image);

        return convertView;
    }
}
