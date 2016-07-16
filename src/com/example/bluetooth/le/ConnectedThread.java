package com.example.bluetooth.le;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/*
 �ѽ������Ӻ��������̣߳���Ҫ��������������
 socket������ȡ����������ȡԶ���������͹�������Ϣ
 handler�������յ����ʱ������Ϣ
 */
public class ConnectedThread extends Thread {

	private static final int RECEIVE_MSG = 7;
	private static final int SEND_MSG = 8;
	private boolean isStop;
	private BluetoothSocket socket;
	private Handler handler;
	private InputStream is;
	private OutputStream os;

	public ConnectedThread(BluetoothSocket s, Handler h) {
		socket = s;
		handler = h;
		isStop = false;
	}

	public void run() {
		System.out.println("connectedThread.run()");
		byte[] buf;
		int size;
		while (!isStop) {
			size = 0;
			buf = new byte[1024];
			try {
				is = socket.getInputStream();

				size = is.read(buf);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isStop = true;
			}
			if (size > 0) {

				sendMessageToHandler(buf, RECEIVE_MSG);
			}
		}
	}

	private void sendMessageToHandler(byte[] buf, int mode) {
		String data = BinaryToHexString(buf);

		String data1 = data.substring(2, 6);

		
		int data2 = Integer.parseInt(data1, 16);
		String data3 = String.valueOf(data2);
		String data4 = data3.substring(0, 2);
		char data5 = data3.charAt(2);
		String data6 = data4 + "." + data5;
		Bundle bundle = new Bundle();
		bundle.putString("str", data6);

		Message msg = new Message();
		msg.setData(bundle);
		msg.what = mode;
		handler.sendMessage(msg);
	}

	/**
	 * 
	 * @param bytes
	 * @return 将二进制转换为十六进制字符输出
	 */
	private static String hexStr = "0123456789ABCDEF";

	public static String BinaryToHexString(byte[] bytes) {

		String result = "";
		String hex = "";
		for (int i = 0; i < bytes.length; i++) {
			// 字节高4位
			hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
			// 字节低4位
			hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
			result += hex;
		}
		return result;
	}
}
