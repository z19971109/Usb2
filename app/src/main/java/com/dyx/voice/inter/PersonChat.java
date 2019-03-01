package com.dyx.voice.inter;

public class PersonChat {
	//id
	private int id;
	//资源url
	private String url;
	//资源图片url
	private String pic_url;
	//资源名称
	private String url_name;
	//文字
	private String chatMessage;

    private boolean isMeSend;
    
    public String getUrl_name() {
		return url_name;
	}
	public void setUrl_name(String url_name) {
		this.url_name = url_name;
	}
	public String getPic_url() {
		return pic_url;
	}
	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getChatMessage() {
		return chatMessage;
	}
	public void setChatMessage(String chatMessage) {
		this.chatMessage = chatMessage;
	}
	public boolean isMeSend() {
		return isMeSend;
	}
	public void setMeSend(boolean isMeSend) {
		this.isMeSend = isMeSend;
	}
	public PersonChat(int id, String url, String pic_url, String url_name , String chatMessage, boolean isMeSend) {
		super();
		this.id = id;
		this.url = url;
		this.pic_url = pic_url;
		this.url_name = url_name;
		this.chatMessage = chatMessage;
		this.isMeSend = isMeSend;
	}
	public PersonChat() {
		super();
	}
	

}
