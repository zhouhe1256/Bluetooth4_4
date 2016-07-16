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
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
@SuppressLint("NewApi")
public class DeviceScanActivity extends ListActivity {

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;
	private Map<BluetoothDevice,String> map ;
	private static final int REQUEST_ENABLE_BT = 1;
	// stop scan after 10 second
	private static final long SCAN_PERIOD = 10000;
	// ox100 advertisement data UUID prefix
	private static final String OXUUIDprefix = "ba11f08c5f140b0d1080"; // Ѫ��
	private static final String OXUUIDprefix1 = "ba11f08c5f140b0d10a0"; // Ѫѹ

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.title_devices);
		mHandler = new Handler();

		// ��鵱ǰ�ֻ��Ƿ�֧��ble ����,���֧���˳�����
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		// ��ʼ�� Bluetooth adapter,
		// ͨ�������������õ�һ���ο�����������(API����������android4.3�����ϺͰ汾)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// ����豸���Ƿ�֧������
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			mLeDeviceListAdapter.clear();
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		setListAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
		if (device == null){
			return;
		}else {
			
			String flag = map.get(device);
			final Intent intent = new Intent(this, DeviceControlActivity.class);
			intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME,
					device.getName());
			intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS,
					device.getAddress());
			intent.putExtra("FLAG", flag);
			intent.putExtra("device", device);
			
			if (mScanning) {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				mScanning = false;
			}
			startActivity(intent);
		}
			
		
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	// Adapter for holding devices found through scanning.
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;
		private String mflag;
		

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = DeviceScanActivity.this.getLayoutInflater();
			map = new HashMap<BluetoothDevice, String>();
		}

		public void addDevice(BluetoothDevice device,String flag) {
			if (!mLeDevices.contains(device)) {
				mflag = flag;
				mLeDevices.add(device);
				map.put(device, flag);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				viewHolder.deviceRSSI = (TextView) view
						.findViewById(R.id.device_RSSI);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			final String deviceRSSI = device.EXTRA_RSSI;
			if (deviceName != null && deviceName.length() > 0) {
				viewHolder.deviceName.setText(deviceName);
			}

			else if (deviceRSSI != null && deviceRSSI.length() > 0) {
				viewHolder.deviceRSSI.setText(deviceRSSI);
			}

			else
				viewHolder.deviceName.setText(R.string.unknown_device);
			   viewHolder.deviceAddress.setText(device.getAddress());
			   SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
			   String deviceAddress = sp.getString("DeviceAddress", "");
			   if(deviceAddress.equals(device.getAddress() +mflag)){
				   String flag = map.get(device);
					final Intent intent1 = new Intent(getApplicationContext(), DeviceControlActivity.class);
					intent1.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME,
							device.getName());
					intent1.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS,
							device.getAddress());
					intent1.putExtra("FLAG", flag);
					startActivity(intent1);
			   }

			return view;
		}
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				final byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					String struuid = bytes2HexString(scanRecord).replace("-",
							"").toLowerCase();

					if (struuid.contains(OXUUIDprefix)
						) {
						mLeDeviceListAdapter.addDevice(device,"1");
						mLeDeviceListAdapter.notifyDataSetChanged();
					}else if(struuid.contains(OXUUIDprefix1)){
						
						mLeDeviceListAdapter.addDevice(device,"2");
						mLeDeviceListAdapter.notifyDataSetChanged();
						
					}else  {
						mLeDeviceListAdapter.addDevice(device,"3");
						mLeDeviceListAdapter.notifyDataSetChanged();
					}
					

//					mLeDeviceListAdapter.addDevice(device,"1");
//					mLeDeviceListAdapter.notifyDataSetChanged();

				}
			});
		}
	};

	
	public static String bytes2HexString(byte[] a) {

		int len = a.length;
		byte[] b = new byte[len];
		for (int k = 0; k < len; k++) {
			b[k] = a[a.length - 1 - k];
		}

		String ret = "";
		for (int i = 0; i < len; i++) {
		
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
			
		}

		return ret;
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView deviceRSSI;
	}
}