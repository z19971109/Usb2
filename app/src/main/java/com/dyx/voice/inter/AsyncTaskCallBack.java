package com.dyx.voice.inter;

import cn.com.broadlink.blnetworkunit.SendDataResultInfo;

/***
 * 异步线程接口 
 * @author Administrator
 *
 */
public interface AsyncTaskCallBack {
	public void onPostExecute(SendDataResultInfo resultData);
	public void onPreExecute();
}
