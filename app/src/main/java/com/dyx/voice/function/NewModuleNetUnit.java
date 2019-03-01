package com.dyx.voice.function;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import com.dyx.voice.UsbApplication;
import com.dyx.voice.inter.AsyncTaskCallBack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.broadlink.blnetworkunit.SendDataResultInfo;

@SuppressLint("NewApi")
public class NewModuleNetUnit {
	private int sec1 = 2;
	private int sec2 = 3;
	private int repeatCount = 2;
	
	private static ExecutorService FULL_TASK_EXECUTOR;
	
	public NewModuleNetUnit(){
		 FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
	}
	
	/*********************************************************
	 * sp2 rm2发送数据
	 * @param device 设备
	 * @param sendData 发送的数据
	 * @param onAuthLisnter 发送监听
	 * ********************************************************* 
	 */
	public void sendData(String devicemac, byte[] sendData, AsyncTaskCallBack callBackLisnter) {
		NewModleSendDataTask task = new NewModleSendDataTask(callBackLisnter, sendData);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(FULL_TASK_EXECUTOR, devicemac);
		else
			task.execute(devicemac);
	}

	public void setTimeOut(int localTimeOut, int serTimeOut){
		sec1 = localTimeOut;
		sec2 = serTimeOut;
	}

	/***************************** sp2 rm2发送数据线程*************************************/
	class NewModleSendDataTask extends AsyncTask<String, Void, SendDataResultInfo> {
		private AsyncTaskCallBack mOnAuthLisnter;
		private byte[] data;

		//构造方法
		public NewModleSendDataTask(AsyncTaskCallBack onAuthLisnter, byte[] data){
			this.mOnAuthLisnter = onAuthLisnter;
			this.data = data;
		}

		@Override
		protected void onPostExecute(SendDataResultInfo result) {
			super.onPostExecute(result);
			if(!isCancelled() && mOnAuthLisnter != null){
				mOnAuthLisnter.onPostExecute(result);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(!isCancelled() && mOnAuthLisnter != null){
				mOnAuthLisnter.onPreExecute();
			}
		}

		@Override
		protected SendDataResultInfo doInBackground(String... params) {
			SendDataResultInfo resultInfo = null;
			for(int i = 0; i < 3; i++){
				if(isCancelled() || UsbApplication.mBlNetworkUnit == null)
					return null;

				resultInfo = UsbApplication.mBlNetworkUnit.sendData(params[0],
						data, sec1, sec2, repeatCount);

				if(resultInfo == null)
					return resultInfo;

				if(resultInfo != null
						&& resultInfo.resultCode != -7
						&& resultInfo.resultCode != -103){
					return resultInfo;
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return resultInfo;
		}
	}

}
