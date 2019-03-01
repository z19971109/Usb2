package com.dyx.voice.inter;

public class MovieInterface {

    private String act;

    private String mark;

    private String pic;

    private String title;

    private String uuid;

    public String getAct() {
        return act;
    }

    public String getMark() {
        return mark;
    }

    public String getPic() {
        return pic;
    }

    public String getTitle() {
        return title;
    }

    public String getUuid() {
        return uuid;
    }

    public MovieInterface(String act , String mark , String pic , String title , String uuid){
        this.act = act;
        this.mark = mark;
        this.pic = pic;
        this.title = title;
        this.uuid = uuid;
    }

}
