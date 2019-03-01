/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/apple/Downloads/shiyang_prd_svn_newwork_csy_15_06_07/HDP/src/hdp/http/Hdiy.aidl
 */
package com.dyx.voice.inter;

public interface Hdiy extends android.os.IInterface {
	/** Local-side IPC implementation stub class. */
	public static abstract class Stub extends android.os.Binder implements
			Hdiy {
		private static final String DESCRIPTOR = "hdp.http.Hdiy";

		/** Construct the stub at attach it to the interface. */
		public Stub() {
			this.attachInterface(this, DESCRIPTOR);
		}

		/**
		 * Cast an IBinder object into an hdp.http.Hdiy interface, generating a
		 * proxy if needed.
		 */
		public static Hdiy asInterface(android.os.IBinder obj) {
			if ((obj == null)) {
				return null;
			}
			android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
			if (((iin != null) && (iin instanceof Hdiy))) {
				return ((Hdiy) iin);
			}
			return new Hdiy.Stub.Proxy(obj);
		}

		@Override
		public android.os.IBinder asBinder() {
			return this;
		}

		@Override
		public boolean onTransact(int code, android.os.Parcel data,
				android.os.Parcel reply, int flags)
				throws android.os.RemoteException {
			switch (code) {
			case INTERFACE_TRANSACTION: {
				reply.writeString(DESCRIPTOR);
				return true;
			}
			case TRANSACTION_InsertDiyList: {
				data.enforceInterface(DESCRIPTOR);
				String _arg0;
				_arg0 = data.readString();
				String _arg1;
				_arg1 = data.readString();
				int _result = this.InsertDiyList(_arg0, _arg1);
				reply.writeNoException();
				reply.writeInt(_result);
				return true;
			}
			case TRANSACTION_GetNamebuNum: {
				data.enforceInterface(DESCRIPTOR);
				int _arg0;
				_arg0 = data.readInt();
				String _result = this.GetNamebuNum(_arg0);
				reply.writeNoException();
				reply.writeString(_result);
				return true;
			}
			case TRANSACTION_ChangeNum: {
				data.enforceInterface(DESCRIPTOR);
				int _arg0;
				_arg0 = data.readInt();
				this.ChangeNum(_arg0);
				reply.writeNoException();
				return true;
			}
			case TRANSACTION_getAllChannelInfo: {
				data.enforceInterface(DESCRIPTOR);
				String _result = this.getAllChannelInfo();
				reply.writeNoException();
				reply.writeString(_result);
				return true;
			}
			}
			return super.onTransact(code, data, reply, flags);
		}

		private static class Proxy implements Hdiy {
			private android.os.IBinder mRemote;

			Proxy(android.os.IBinder remote) {
				mRemote = remote;
			}

			@Override
			public android.os.IBinder asBinder() {
				return mRemote;
			}

			public String getInterfaceDescriptor() {
				return DESCRIPTOR;
			}

			@Override
			public int InsertDiyList(String path,
					String type) throws android.os.RemoteException {
				android.os.Parcel _data = android.os.Parcel.obtain();
				android.os.Parcel _reply = android.os.Parcel.obtain();
				int _result;
				try {
					_data.writeInterfaceToken(DESCRIPTOR);
					_data.writeString(path);
					_data.writeString(type);
					mRemote.transact(Stub.TRANSACTION_InsertDiyList, _data,
							_reply, 0);
					_reply.readException();
					_result = _reply.readInt();
				} finally {
					_reply.recycle();
					_data.recycle();
				}
				return _result;
			}

			@Override
			public String GetNamebuNum(int num)
					throws android.os.RemoteException {
				android.os.Parcel _data = android.os.Parcel.obtain();
				android.os.Parcel _reply = android.os.Parcel.obtain();
				String _result;
				try {
					_data.writeInterfaceToken(DESCRIPTOR);
					_data.writeInt(num);
					mRemote.transact(Stub.TRANSACTION_GetNamebuNum, _data,
							_reply, 0);
					_reply.readException();
					_result = _reply.readString();
				} finally {
					_reply.recycle();
					_data.recycle();
				}
				return _result;
			}

			@Override
			public void ChangeNum(int num) throws android.os.RemoteException {
				android.os.Parcel _data = android.os.Parcel.obtain();
				android.os.Parcel _reply = android.os.Parcel.obtain();
				try {
					_data.writeInterfaceToken(DESCRIPTOR);
					_data.writeInt(num);
					mRemote.transact(Stub.TRANSACTION_ChangeNum, _data, _reply,
							0);
					_reply.readException();
				} finally {
					_reply.recycle();
					_data.recycle();
				}
			}

			@Override
			public String getAllChannelInfo()
					throws android.os.RemoteException {
				android.os.Parcel _data = android.os.Parcel.obtain();
				android.os.Parcel _reply = android.os.Parcel.obtain();
				String _result;
				try {
					_data.writeInterfaceToken(DESCRIPTOR);
					mRemote.transact(Stub.TRANSACTION_getAllChannelInfo, _data,
							_reply, 0);
					_reply.readException();
					_result = _reply.readString();
				} finally {
					_reply.recycle();
					_data.recycle();
				}
				return _result;
			}
		}

		static final int TRANSACTION_InsertDiyList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
		static final int TRANSACTION_GetNamebuNum = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
		static final int TRANSACTION_ChangeNum = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
		static final int TRANSACTION_getAllChannelInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
	}

	public int InsertDiyList(String path, String type)
			throws android.os.RemoteException;

	public String GetNamebuNum(int num)
			throws android.os.RemoteException;

	public void ChangeNum(int num) throws android.os.RemoteException;

	public String getAllChannelInfo()
			throws android.os.RemoteException;
}
