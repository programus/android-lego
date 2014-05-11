package org.programus.book.mobilelego.motion_rc_vehicle.rc.activities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;
import org.programus.book.mobilelego.motion_rc_vehicle.rc.R;
import org.programus.book.mobilelego.motion_rc_vehicle.rc.net.SppClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 5;

	private BluetoothAdapter mBtAdapter;
	private BluetoothDevice[] mDevices;
	
	private static enum BtConnectState {
		Disconnected,
		Connecting,
		Connected
	};
	private BtConnectState mBtConnectState = BtConnectState.Disconnected;
	
	private SppClient mClient;
	private Communicator mComm;

	private ViewGroup mCover;
	private TextView mLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_activity);
		this.initComponents();
	}

	/**
	 * 初始化界面控件
	 */
	private void initComponents() {
		this.mCover = (ViewGroup) this.findViewById(R.id.all);
		this.mLog = (TextView) this.findViewById(R.id.log);
		
		mCover.setVisibility(View.VISIBLE);
		this.mClient = new SppClient();
		this.mClient.setOnConnectedListener(new SppClient.OnConnectedListener() {
			@Override
			public void onFailed(Exception e) {
				appendLog(e);
			}
			
			@Override
			public void onConnected(Communicator comm) {
				try {
                    appendLog("Connected");
                    mComm = comm;
                    
					appendLog("Communicator ready.");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
                            setBtConnectState(BtConnectState.Connected);
						}
					});
				} catch (Exception e) {
					appendLog(e);
				}
			}
		});
	}
	
    /**
     * 追加文本到日志文本框中
     * @param log 需要追加的文本
     */
    private void appendLog(final String log) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLog.append(log);
                mLog.append("\n");
            }
        });
    }

    /**
     * 追加例外信息到日志文本框中
     * @param e 需要追加的例外
     */
    private void appendLog(final Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        String stackTrace = sw.toString();
        pw.close();
        this.appendLog(stackTrace);
    }

    /**
     * 清除日志
     */
    private void clearLog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLog.setText("");
            }
        });
    }
	
	private void connect(BluetoothDevice device) {
		this.clearLog();
		if (mClient.isConnected()) {
			mClient.close();
		}
		appendLog("Connecting...");
		mClient.connect(device);
	}
	
	private void disconnect() {
		if (mClient.isConnected()) {
			mClient.close();
			appendLog("Disconnected.");
		}
	}
	
	private void remoteFinish() {
		mComm.send(ExitSignal.getInstance());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.action_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.bt_connect:
			this.setBtConnectState(BtConnectState.Connecting);
			break;
		case R.id.bt_connecting:
			Toast.makeText(this, "Connecting... please wait.", Toast.LENGTH_LONG).show();
			break;
		case R.id.bt_disconnect:
			remoteFinish();
			disconnect();
			this.setBtConnectState(BtConnectState.Disconnected);
		}
		this.invalidateOptionsMenu();
		return result;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		for (int i = 0; i < menu.size(); i++) {
			menu.getItem(i).setVisible(i == mBtConnectState.ordinal());
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * 取得蓝牙信息，并在未开启蓝牙时提示开启蓝牙。
	 */
	private void enableBluetooth() {
		// 取得蓝牙适配器
		this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (this.mBtAdapter == null) {
			// 无法取得蓝牙适配器，说明设备不支持蓝牙
			Toast.makeText(this, R.string.msg_bluetooth_not_supported, Toast.LENGTH_LONG).show();
			this.finish();
		}
		// 检查蓝牙是否已经开启
		if (!this.mBtAdapter.isEnabled()) {
			// 如果没有开启，请求开启
			this.requestEnableBluetooth();
		} else {
			// 如果已经开启，弹出对话框，让用户选择可连接设备。
			this.selectDevice();
		}
	}
	
	/**
	 * 请求用户开启蓝牙
	 */
	private void requestEnableBluetooth() {
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	}
	
	/**
	 * 弹出对话框，让用户选择可连接设备
	 */
	private void selectDevice() {
		if (this.mBtAdapter != null) {
			// 取出已配对设备
			Set<BluetoothDevice> deviceSet = this.mBtAdapter.getBondedDevices();
			mDevices = deviceSet.toArray(new BluetoothDevice[deviceSet.size()]);
			this.askForDeviceSel(mDevices);
		}
	}
	
	private void askForDeviceSel(final BluetoothDevice[] devices) {
		if (devices.length > 0) {
			CharSequence[] deviceDescriptions = new CharSequence[devices.length];
			for (int i = 0; i < devices.length; i++) {
				deviceDescriptions[i] = Html.fromHtml(String.format("<b>%s</b> [%s]", devices[i].getName(), devices[i].getAddress()));
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_select_device)
				.setItems(deviceDescriptions, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which <= 0) {
							setBtConnectState(BtConnectState.Disconnected);
						} else {
							connect(devices[which - 1]);
						}
					}
				});
			AlertDialog dialog = builder.create();
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					setBtConnectState(BtConnectState.Disconnected);
				}
			});
			dialog.show();
		} else {
			Toast.makeText(this, R.string.msg_bluetooth_pair_necessary, Toast.LENGTH_LONG).show();
		}
	}
	
	private void setBtConnectState(BtConnectState state) {
		this.mBtConnectState = state;
		switch(state) {
		case Connected:
			this.mCover.setVisibility(View.GONE);
			break;
		case Disconnected:
			this.mCover.setVisibility(View.VISIBLE);
			break;
		case Connecting:
			enableBluetooth();
			break;
		}
		this.invalidateOptionsMenu();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				// 用户开启了蓝牙，提示用户选择设备进行连接
				this.selectDevice();
			} else {
				// 否则提示需要蓝牙并退出程序
				Toast.makeText(this, R.string.msg_bluetooth_is_necessary, Toast.LENGTH_LONG).show();
				this.setBtConnectState(BtConnectState.Disconnected);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}
