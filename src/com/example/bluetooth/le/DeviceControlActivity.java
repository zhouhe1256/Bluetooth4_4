/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth.le;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.tsz.afinal.FinalDb;

import com.example.bluetooth.bean.HistoryTiWen;
import com.example.bluetooth.bean.HistoryXueYa;
import com.example.bluetooth.bean.HistoryXueYang;
import com.example.bluetooth.history.MailvHistoryActivity;
import com.example.bluetooth.history.TiwenHistoryActivity;
import com.example.bluetooth.history.XueYaHistoryActivity;
import com.example.bluetooth.history.XueYangHistoryActivity;
import com.example.bluetooth.le.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
@SuppressLint("NewApi")
public class DeviceControlActivity extends Activity implements OnClickListener {
	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mConnectionState;
	private TextView mConnectionState_1;
	private TextView mConnectionState_2;
	private TextView mConnectionState_3;
	private TextView xueyang; // 血氧
	private TextView mailv; // 脉率
	private TextView hxueya; // 高血压
	private TextView lxueya; // 低血压
	private TextView tiwen; // 体温
	private TextView shezhi; // 设置
	private TextView search; // 搜索
	private String mDeviceName;
	private String mDeviceAddress;
	// private ExpandableListView mGattServicesList;
	public static BluetoothGatt mBluetoothGatt;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

	private BluetoothGattCharacteristic mCharacteristicCD01;
	private BluetoothGattCharacteristic mCharacteristicCD02;
	private BluetoothGattCharacteristic mCharacteristicCD03;
	private BluetoothGattCharacteristic mCharacteristicCD04;
	private BluetoothGattCharacteristic mCharacteristicWrite;

	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	private LinearLayout xueyang_history;

	private LinearLayout mailv_history;

	private LinearLayout xueya_history;

	private LinearLayout tiwen_history;
	BluetoothAdapter bluetoothAdapter = null;
	BluetoothSocket socket;
	private String flag = "0";
	private WebView webView1; // 快操手册

	private WebView webView2; // 公司简介

	// 消息处理器使用的常量
	private static final int FOUND_DEVICE = 1; // 发现设备
	private static final int START_DISCOVERY = 2; // 开始查找设备
	private static final int FINISH_DISCOVERY = 3; // 结束查找设备
	private static final int CONNECT_FAIL = 4; // 连接失败
	private static final int CONNECT_SUCCEED_P = 5; // 主动连接成功
	private static final int CONNECT_SUCCEED_N = 6; // 收到连接成功
	private static final int RECEIVE_MSG = 7; // 收到消息
	private static final int SEND_MSG = 8; // 发送消息

	ConnectedThread connectedThread; // 与远程蓝牙连接成功时启动
	ConnectThread connectThread; // 用户点击列表中某一项，要与远程蓝牙连接时启动

	// 连接设备对话框相关控件
	private Dialog dialog;
	private ProgressBar discoveryPro;
	private ListView foundList;
	List<BluetoothDevice> foundDevices;
	// 广播接收器，主要是接收蓝牙状态改变时发出的广播
	private BroadcastReceiver mReceiver3;
	private String[] reset = new String[] { "重置血氧仪", "重置血压计", "重置体温计" };
	private PopupWindow pw;
	private ListView lv;
	private NumberAdapter adapter;

	private Intent intent;

	private SharedPreferences sp;
	// 消息处理器.
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case FOUND_DEVICE:
				foundList.setAdapter(new MyAdapter(DeviceControlActivity.this,
						foundDevices));
				break;
			case START_DISCOVERY:
				discoveryPro.setVisibility(View.VISIBLE);
				break;
			case FINISH_DISCOVERY:
				discoveryPro.setVisibility(View.GONE);
				break;
			case CONNECT_FAIL:
				Toast.makeText(DeviceControlActivity.this, "连接失败",
						Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_SUCCEED_P:
				socket = connectThread.getSocket();
				connectedThread = new ConnectedThread(socket, mHandler);
				connectedThread.start();
				Toast.makeText(DeviceControlActivity.this, "连接成功",Toast.LENGTH_SHORT).show();

			case CONNECT_SUCCEED_N:
				System.out.println("连接成功-----");
				dialog.cancel();
				mConnectionState_3.setText("已连接");
				if (msg.what == CONNECT_SUCCEED_P) {

					socket = connectThread.getSocket();
					connectedThread = new ConnectedThread(socket, mHandler);
					connectedThread.start();
				} else {
					if (connectThread != null) {
						connectThread.interrupt();
					}

					connectedThread = new ConnectedThread(socket, mHandler);
					connectedThread.start();
				}

				String stateStr = msg.getData().getString("name");
				stateStr = "串口工具：" + "与" + stateStr + "连接中";
				Toast.makeText(DeviceControlActivity.this, "连接成功",Toast.LENGTH_SHORT).show();
				break;
			case RECEIVE_MSG:
			case SEND_MSG:
				String chatStr = msg.getData().getString("str");

				FinalDb db = FinalDb.create(DeviceControlActivity.this);
				HistoryTiWen historyTiWen = new HistoryTiWen();
				historyTiWen.setTime(getCurrentTime());
				historyTiWen.setTiwen(Double.parseDouble(chatStr));
				db.save(historyTiWen);
				tiwen.setText(chatStr);

				break;
			}
		}

	};

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			System.out.println("action = " + action);
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;

				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				mConnected = false;

				invalidateOptionsMenu();
				// clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent
						.getStringExtra(BluetoothLeService.EXTRA_DATA));
			} else if (BluetoothLeService.ACTION_CD01NOTIDIED.equals(action)) {
				mBluetoothLeService.setCharacteristicNotification(
						mCharacteristicCD02, true);
			} else if (BluetoothLeService.ACTION_CD02NOTIDIED.equals(action)) {
				mBluetoothLeService.setCharacteristicNotification(
						mCharacteristicCD03, true);
			} else if (BluetoothLeService.ACTION_CD03NOTIDIED.equals(action)) {
				mBluetoothLeService.setCharacteristicNotification(
						mCharacteristicCD04, true);
			} else if (BluetoothLeService.ACTION_CD04NOTIDIED.equals(action)) {
				// 要求显示密码
				// mCharacteristicWrite.setValue(getHexBytes("AA5502B0B2"));
				// 密码配对
				// mCharacteristicWrite.setValue(getHexBytes("AA5504B10000B5"));
				// // 血氧
				mCharacteristicWrite.setValue(getHexBytes("AA5504B10000B5")); // 血压
				mBluetoothLeService.wirteCharacteristic(mCharacteristicWrite);
			}
		}
	};

	// If a given GATT characteristic is selected, check for supported features.
	// This sample
	// demonstrates 'Read' and 'Notify' features. See
	// http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for
	// the complete
	// list of supported characteristic features.
	private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

			if (mGattCharacteristics != null) {
				final BluetoothGattCharacteristic characteristic = mGattCharacteristics
						.get(groupPosition).get(childPosition);
				final int charaProp = characteristic.getProperties();
				System.out.println("charaProp = " + charaProp + ",UUID = "
						+ characteristic.getUuid().toString());
				// Random r = new Random();
				// CD01
				if (characteristic
						.getUuid()
						.equals(UUID
								.fromString("0000cd01-0000-1000-8000-00805f9b34fb"))) {
					System.out.println("--------------CD01------------------");
					// mBluetoothLeService.setCharacteristicNotification(characteristic,
					// true);
				}

				// CD02
				if (characteristic
						.getUuid()
						.equals(UUID
								.fromString("0000cd02-0000-1000-8000-00805f9b34fb"))) {
					System.out.println("--------------CD02------------------");
					// mBluetoothLeService.setCharacteristicNotification(characteristic,
					// true);
				}

				// CD03
				if (characteristic
						.getUuid()
						.equals(UUID
								.fromString("0000cd03-0000-1000-8000-00805f9b34fb"))) {
					System.out.println("--------------CD03------------------");
					// mBluetoothLeService.setCharacteristicNotification(characteristic,
					// true);
				}

				// CD04
				if (characteristic
						.getUuid()
						.toString()
						.equals(UUID
								.fromString("0000cd04-0000-1000-8000-00805f9b34fb"))) {
					System.out.println("--------------CD04------------------");
					// mBluetoothLeService.setCharacteristicNotification(characteristic,
					// true);
				}

				// CD20 Write data send to device
				if (characteristic
						.getUuid()
						.equals(UUID
								.fromString("0000cd20-0000-1000-8000-00805f9b34fb"))) {
					System.out.println("--------------CD20------------------");
					// characteristic.setValue(getHexBytes("AA5502B0B2"));
					// System.out.println("---------------------------->"+String.valueOf(getHexBytes("AA5502B0B2")));
					// mBluetoothLeService.wirteCharacteristic(characteristic);
				}



				return true;
			}
			return false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_services_characteristics);
		initView();
		sp = this.getSharedPreferences("config", MODE_PRIVATE); //  存地址

		// 判断本机是否有蓝牙和是否处于可用状态
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(this, "本机没有蓝牙设备！", Toast.LENGTH_SHORT).show();
			finish();
		}

		//DialogShow();

		// 注册广播接收器
		mReceiver3 = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				String actionStr = arg1.getAction();
				if (actionStr.equals(BluetoothDevice.ACTION_FOUND)) {
					BluetoothDevice device = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					foundDevices.add(device);
					Toast.makeText(DeviceControlActivity.this,"找到蓝牙设备：" + device.getName(), Toast.LENGTH_SHORT).show();
					//体温计自动连接
					String tiwenjiAddress = sp.getString("TIWENJI", "");
					if(tiwenjiAddress.equals(device.getAddress())){
						connect(device);
					}
					String xueyangAddress = sp.getString("XUEYANG", "");
					String xueyaAddress = sp.getString("XUEYA", "");
					if(xueyangAddress.equals(device.getAddress())){
						mDeviceAddress = device.getAddress();
						Intent gattServiceIntent = new Intent(getApplicationContext(),BluetoothLeService.class);
						bindService(gattServiceIntent, mServiceConnection,BIND_AUTO_CREATE);
						//connect(device);
						if(dialog  != null){
							dialog.cancel();
						}
					}else if (xueyaAddress.equals(device.getAddress())){
						mDeviceAddress = device.getAddress();
						//connect(device);
						Intent gattServiceIntent = new Intent(getApplicationContext(),BluetoothLeService.class);
						bindService(gattServiceIntent, mServiceConnection,BIND_AUTO_CREATE);
						if(dialog  != null){
							dialog.cancel();
						}
					}
					mHandler.sendEmptyMessage(FOUND_DEVICE);
				} else if (actionStr.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
					mHandler.sendEmptyMessage(START_DISCOVERY);
				} else if (actionStr.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
					mHandler.sendEmptyMessage(FINISH_DISCOVERY);
				}
			}

		};
		IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		registerReceiver(mReceiver3, filter1);
		registerReceiver(mReceiver3, filter2);
		registerReceiver(mReceiver3, filter3);

	}

	/**
	 * 连接体温计
	 * 
	 * @param device
	 */
	public void connect(BluetoothDevice device) {

		Toast.makeText(this, "正在与 " + device.getName() + " 连接 .... ",Toast.LENGTH_LONG).show();

		connectThread = new ConnectThread(device, mHandler);
		connectThread.start();

	}

	private void initView() {
		mConnectionState = (TextView) findViewById(R.id.connection_state);
		mConnectionState_1 = (TextView) findViewById(R.id.connection_state_1);
		mConnectionState_2 = (TextView) findViewById(R.id.connection_state_2);
		mConnectionState_3 = (TextView) findViewById(R.id.connection_state_3);
		xueyang = (TextView) findViewById(R.id.data_xueyang);
		mailv = (TextView) findViewById(R.id.data_mailv);
		hxueya = (TextView) findViewById(R.id.data_hxueya);
		lxueya = (TextView) findViewById(R.id.data_lxueya);
		tiwen = (TextView) findViewById(R.id.data_tiwen);
		shezhi = (TextView) findViewById(R.id.tv_shezhi);
		search = (TextView) findViewById(R.id.tv_search);

		xueyang_history = (LinearLayout) findViewById(R.id.ll_xueyang_history);
		mailv_history = (LinearLayout) findViewById(R.id.ll_mailv_history);
		xueya_history = (LinearLayout) findViewById(R.id.ll_xueya_history);
		tiwen_history = (LinearLayout) findViewById(R.id.ll_tiwen_history);
		webView1 = (WebView) findViewById(R.id.wv_webview1);
		webView2 = (WebView) findViewById(R.id.wv_webview2);
		xueyang_history.setOnClickListener(this);
		mailv_history.setOnClickListener(this);
		xueya_history.setOnClickListener(this);
		tiwen_history.setOnClickListener(this);
		shezhi.setOnClickListener(this);
		search.setOnClickListener(this);
		webView1.loadUrl("file:///android_asset/qq.htm");
		webView2.loadUrl("file:///android_asset/qq.htm");
		// getActionBar().setTitle(mDeviceName);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_xueyang_history:
			intent = new Intent(DeviceControlActivity.this,
					XueYangHistoryActivity.class);
			startActivity(intent);
			break;
		case R.id.ll_mailv_history:
			intent = new Intent(DeviceControlActivity.this,
					MailvHistoryActivity.class);
			startActivity(intent);
			break;
		case R.id.ll_xueya_history:
			intent = new Intent(DeviceControlActivity.this,
					XueYaHistoryActivity.class);

			startActivity(intent);
			break;
		case R.id.ll_tiwen_history:
			intent = new Intent(DeviceControlActivity.this,TiwenHistoryActivity.class);

			startActivity(intent);
			break;

		case R.id.tv_shezhi:

			showSelectNumberPopupWindow();
			break;
		case R.id.tv_search:
			if (mServiceConnection != null && mBluetoothLeService != null) {                  
				unbindService(mServiceConnection);
				mBluetoothLeService = null;

			}

			updateConnectionState("未连接");
			updateConnectionState1("未连接");
			mConnectionState_3.setText("未连接");
			DialogShow();
			break;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}

	}



	/**
	 * 弹出设置对话框
	 */
	private void showSelectNumberPopupWindow() {
		initListView();

		pw = new PopupWindow(lv, shezhi.getWidth() + 130, 300);
		// 点击外面可以被关闭
		pw.setOutsideTouchable(true);
		pw.setBackgroundDrawable(new BitmapDrawable());

		pw.setFocusable(true); // 使popupwindow可以获得焦点
		// 显示在编辑框的左下角
		pw.showAsDropDown(shezhi, 30, 15);
	}

	/**
	 * 创建ListView对象
	 */
	private void initListView() {
		lv = new ListView(this);
		lv.setDivider(null);
		lv.setDividerHeight(0); // 消除分割线
		lv.setVerticalFadingEdgeEnabled(false); // 消除滚动条

		adapter = new NumberAdapter();
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				pw.dismiss();

			}
		});
	}

	public class NumberAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return reset.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View v = null;
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(getApplicationContext(),
						R.layout.listview_background, null);
				holder.tv_listview_background_number = (TextView) convertView
						.findViewById(R.id.tv_listview_background_number);

				convertView.setTag(holder);
			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			holder.tv_listview_background_number.setText(reset[position]);

			return convertView;
		}

	}

	public class ViewHolder {

		TextView tv_listview_background_number;

	}

	/**
	 * 弹出搜索蓝牙对话框
	 */
	private void DialogShow() {
		bluetoothAdapter.cancelDiscovery();
		bluetoothAdapter.startDiscovery();

		/*
		 * 通过LayoutInflater得到对话框中的三个控件 第一个ListView为局部变量，因为它显示的是已配对的蓝牙设备，不需随时改变
		 * 第二个ListView和ProgressBar为全局变量
		 */
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog, null);
		discoveryPro = (ProgressBar) view.findViewById(R.id.discoveryPro);
		ListView bondedList = (ListView) view.findViewById(R.id.bondedList);
		foundList = (ListView) view.findViewById(R.id.foundList);

		// 将已配对的蓝牙设备显示到第一个ListView中
		Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
		final List<BluetoothDevice> bondedDevices = new ArrayList<BluetoothDevice>();
		if (deviceSet.size() > 0) {
			for (Iterator<BluetoothDevice> it = deviceSet.iterator(); it
					.hasNext();) {
				BluetoothDevice device = (BluetoothDevice) it.next();
				bondedDevices.add(device);
			}
		}
		bondedList.setAdapter(new MyAdapter(DeviceControlActivity.this,
				bondedDevices));

		// 将找到的蓝牙设备显示到第二个ListView中
		foundDevices = new ArrayList<BluetoothDevice>();
		foundList.setAdapter(new MyAdapter(DeviceControlActivity.this,foundDevices));

		// 两个ListView绑定监听器
		/**
		 * 绑定的蓝牙
		 */
		bondedList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				BluetoothDevice device = bondedDevices.get(arg2);
				connect(device);
				sp.edit().putString("TIWENJI",device.getAddress()).commit();
				dialog.cancel();
			}
		});

		/**
		 * 搜到 的蓝牙
		 */
		foundList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				BluetoothDevice device = foundDevices.get(arg2);
				//connect(device);
				mDeviceAddress = device.getAddress();

				System.out.println(mDeviceAddress+"我在这里");
				Toast.makeText(getApplicationContext(), mDeviceAddress, 1).show();
				Intent gattServiceIntent = new Intent(getApplicationContext(),BluetoothLeService.class);
				bindService(gattServiceIntent, mServiceConnection,BIND_AUTO_CREATE);
				dialog.cancel();

			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(
				DeviceControlActivity.this);
		builder.setMessage("请选择要连接的蓝牙设备").setPositiveButton("取消",
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				bluetoothAdapter.cancelDiscovery();
			}
		});
		builder.setView(view);
		builder.create();
		dialog = builder.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mGattUpdateReceiver != null) {
			unregisterReceiver(mGattUpdateReceiver);
		}

	}

	@Override
	protected void onDestroy() {
		if (mServiceConnection != null && mBluetoothLeService != null) {
			unbindService(mServiceConnection);
			mBluetoothLeService = null;
		}
		if (mReceiver3 != null) {
			unregisterReceiver(mReceiver3);
		}

		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (connectThread != null) {
			connectThread.interrupt();
		}
		if (connectedThread != null) {
			connectedThread.interrupt();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final String resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}

	private void updateConnectionState1(final String resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState_1.setText(resourceId);
				mConnectionState_2.setText(resourceId);
			}
		});
	}

	private void displayData(String data) {

		if (data != null) {

			if (data.length() == 49) {
				String ml = data.substring(40, 42);
				String ly = data.substring(34, 36);
				String hy = data.substring(28, 30);

				int c = Integer.parseInt(ml, 16);
				int d = Integer.parseInt(ly, 16);
				int e = Integer.parseInt(hy, 16);

				FinalDb db = FinalDb.create(DeviceControlActivity.this);
				HistoryXueYa historyXueYa = new HistoryXueYa();
				historyXueYa.setTime(getCurrentTime());
				historyXueYa.setHxueya((double) e);
				historyXueYa.setLxueya((double) d);
				historyXueYa.setMailv((double) c);

				db.save(historyXueYa);
				mailv.setText(c + "");
				hxueya.setText(e + "");
				lxueya.setText(d + "");

			} else if (data.length() == 25) {

				String dataOne = data.substring(16, 21);
				String[] strArr = dataOne.split(" ");
				int a = Integer.parseInt(strArr[0], 16);
				int b = Integer.parseInt(strArr[1], 16);

				if (a == 177 || b == 0) {
					xueyang.setText("0");

				} else {

					FinalDb db = FinalDb.create(DeviceControlActivity.this);
					HistoryXueYang historyXueYang = new HistoryXueYang();
					historyXueYang.setTime(getCurrentTime());
					historyXueYang.setXueyang((double) a);
					db.save(historyXueYang);
					xueyang.setText(a + "");

				}

			}

		}
	}

	// Demonstrates how to iterate through the supported GATT
	// Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the
	// ExpandableListView
	// on the UI.
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = getResources().getString(
				R.string.unknown_service);
		String unknownCharaString = getResources().getString(
				R.string.unknown_characteristic);
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			if (uuid.contains("ba11f08c-5f14-0b0d-1080")) {  //xueyangyi
				updateConnectionState("已连接");

				sp.edit().putString("XUEYANG", mDeviceAddress).commit();

				Toast.makeText(getApplicationContext(), mDeviceAddress+"第二次", 1).show();
				System.out.println("this gattService UUID is:"
						+ gattService.getUuid().toString());
				currentServiceData
				.put(LIST_NAME, SampleGattAttributes.lookup(uuid,
						unknownServiceString));
				currentServiceData.put(LIST_UUID, uuid);
				gattServiceData.add(currentServiceData);
				ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService
						.getCharacteristics();
				ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

				// Loops through available Characteristics.
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					charas.add(gattCharacteristic);
					HashMap<String, String> currentCharaData = new HashMap<String, String>();
					uuid = gattCharacteristic.getUuid().toString();
					currentCharaData.put(LIST_NAME, SampleGattAttributes
							.lookup(uuid, unknownCharaString));
					currentCharaData.put(LIST_UUID, uuid);
					gattCharacteristicGroupData.add(currentCharaData);
				}

				mGattCharacteristics.add(charas);

				gattCharacteristicData.add(gattCharacteristicGroupData);

				mCharacteristicCD01 = gattService.getCharacteristic(UUID
						.fromString("0000cd01-0000-1000-8000-00805f9b34fb"));
				mCharacteristicCD02 = gattService.getCharacteristic(UUID
						.fromString("0000cd02-0000-1000-8000-00805f9b34fb"));
				mCharacteristicCD03 = gattService.getCharacteristic(UUID
						.fromString("0000cd03-0000-1000-8000-00805f9b34fb"));
				mCharacteristicCD04 = gattService.getCharacteristic(UUID
						.fromString("0000cd04-0000-1000-8000-00805f9b34fb"));
				mCharacteristicWrite = gattService.getCharacteristic(UUID
						.fromString("0000cd20-0000-1000-8000-00805f9b34fb"));

				// ==========================================================
				/*
				 * import connected code is here
				 */
				System.out
				.println("=======================Set Notification==========================");
				// CD01
				mBluetoothLeService.setCharacteristicNotification(
						mCharacteristicCD01, true);

				// Sleep time, make sure the previous Notification connected
				// StepSleep(500);

				// CD02
				// mBluetoothLeService.setCharacteristicNotification(mCharacteristicCD02,
				// true);

				// StepSleep(500);

				// CD03
				// mBluetoothLeService.setCharacteristicNotification(mCharacteristicCD03,
				// true);

				// StepSleep(500);

				// CD04
				// mBluetoothLeService.setCharacteristicNotification(mCharacteristicCD04,
				// true);

				// StepSleep(500);

				// Make sure CD01~CD04 Notification is connected,send the cmd to
				// CD20
				// CD20 Write data send to device
				// cd20.setValue(getHexBytes("AA5502B0B2"));
				// mBluetoothLeService.wirteCharacteristic(mCharacteristicWrite);
				// Now,will be see the device is display 4 Number
				//
				// So,connected successfully,will call back
				// BluetoothLeService.java in onCharacteristicChanged method
			}
			if (uuid.contains("ba11f08c-5f14-0b0d-10a0")) {  //xueya
				updateConnectionState1("已连接");
				sp.edit().putString("XUEYA", mDeviceAddress).commit();
				System.out.println("this gattService UUID is:"
						+ gattService.getUuid().toString());
				currentServiceData
				.put(LIST_NAME, SampleGattAttributes.lookup(uuid,
						unknownServiceString));
				currentServiceData.put(LIST_UUID, uuid);
				gattServiceData.add(currentServiceData);
				ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService
						.getCharacteristics();
				ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

				// Loops through available Characteristics.
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					charas.add(gattCharacteristic);
					HashMap<String, String> currentCharaData = new HashMap<String, String>();
					uuid = gattCharacteristic.getUuid().toString();
					currentCharaData.put(LIST_NAME, SampleGattAttributes
							.lookup(uuid, unknownCharaString));
					currentCharaData.put(LIST_UUID, uuid);
					gattCharacteristicGroupData.add(currentCharaData);
				}

				mGattCharacteristics.add(charas);

				gattCharacteristicData.add(gattCharacteristicGroupData);

				mCharacteristicCD01 = gattService.getCharacteristic(UUID
						.fromString("0000cd01-0000-1000-8000-00805f9b34fb"));
				mCharacteristicCD02 = gattService.getCharacteristic(UUID
						.fromString("0000cd02-0000-1000-8000-00805f9b34fb"));
				mCharacteristicCD03 = gattService.getCharacteristic(UUID
						.fromString("0000cd03-0000-1000-8000-00805f9b34fb"));
				mCharacteristicCD04 = gattService.getCharacteristic(UUID
						.fromString("0000cd04-0000-1000-8000-00805f9b34fb"));
				mCharacteristicWrite = gattService.getCharacteristic(UUID
						.fromString("0000cd20-0000-1000-8000-00805f9b34fb"));

				// ==========================================================
				/*
				 * import connected code is here
				 */
				System.out
				.println("=======================Set Notification==========================");
				// CD01
				mBluetoothLeService.setCharacteristicNotification(
						mCharacteristicCD01, true);

				// Sleep time, make sure the previous Notification connected
				// StepSleep(500);

				// CD02
				// mBluetoothLeService.setCharacteristicNotification(mCharacteristicCD02,
				// true);

				// StepSleep(500);

				// CD03
				// mBluetoothLeService.setCharacteristicNotification(mCharacteristicCD03,
				// true);

				// StepSleep(500);

				// CD04
				// mBluetoothLeService.setCharacteristicNotification(mCharacteristicCD04,
				// true);

				// StepSleep(500);

				// Make sure CD01~CD04 Notification is connected,send the cmd to
				// CD20
				// CD20 Write data send to device
				// cd20.setValue(getHexBytes("AA5502B0B2"));
				// mBluetoothLeService.wirteCharacteristic(mCharacteristicWrite);
				// Now,will be see the device is display 4 Number
				//
				// So,connected successfully,will call back
				// BluetoothLeService.java in onCharacteristicChanged method
			}
		}

		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
				this, gattServiceData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 }, gattCharacteristicData,
						android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		// mGattServicesList.setAdapter(gattServiceAdapter);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
		.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);

		intentFilter.addAction(BluetoothLeService.ACTION_CD01NOTIDIED);
		intentFilter.addAction(BluetoothLeService.ACTION_CD02NOTIDIED);
		intentFilter.addAction(BluetoothLeService.ACTION_CD03NOTIDIED);
		intentFilter.addAction(BluetoothLeService.ACTION_CD04NOTIDIED);
		return intentFilter;
	}

	private byte[] getHexBytes(String message) {
		int len = message.length() / 2;
		char[] chars = message.toCharArray();
		String[] hexStr = new String[len];
		byte[] bytes = new byte[len];
		for (int i = 0, j = 0; j < len; i += 2, j++) {
			hexStr[j] = "" + chars[i] + chars[i + 1];
			bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
		}
		return bytes;
	}

	public static String getCurrentTime() {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String currentTime = format.format(curDate);

		return currentTime;
	}

}
