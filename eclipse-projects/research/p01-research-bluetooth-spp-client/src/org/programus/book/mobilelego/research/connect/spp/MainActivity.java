package org.programus.book.mobilelego.research.connect.spp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 5;
	private final static String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

	private Spinner mDevices;
	private Button mBeep;
	private TextView mLog;
	
	private BluetoothAdapter mBtAdapter;
	private List<BluetoothDevice> mDeviceList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_activity);
		this.initComponents();
		this.enableBluetooth();
	}

	/**
	 * 初始化界面控件
	 */
	private void initComponents() {
		this.mDevices = (Spinner) this.findViewById(R.id.paired_devices);
		this.mBeep = (Button) this.findViewById(R.id.beep);
		this.mLog = (TextView) this.findViewById(R.id.log);		

		this.mBeep.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 为防止界面线程阻塞，在新线程执行网络相关代码。
				Thread t = new Thread("net-thread") {
					@Override
					public void run() {
						connectAndSendData();
					}
				};
				t.start();
			}
		});
	}
	
	/**
	 * 连接并发送数据到EV3
	 */
    private void connectAndSendData() {
    	this.clearLog();
    	if (this.mDeviceList != null && this.mDeviceList.size() > 0) {
            // 从列表中取得用户选择的设备
            BluetoothDevice device = this.mDeviceList.get(this.mDevices.getSelectedItemPosition());
            BluetoothSocket socket = null;
            OutputStream out = null;
            try {
                // 与设备建立SPP连接
                this.appendLog(String.format("正在与%s[%s]建立SPP连接...", device.getName(), device.getAddress()));
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                socket.connect();
                this.appendLog(String.format("连接%s[%s]成功！", device.getName(), device.getAddress()));
                // 取得输出流
                out = socket.getOutputStream();
                this.appendLog("成功取得输出流。");
                // 输出数据
                out.write(1);
                this.appendLog(String.format("输出数据：%d。", 1));
                // 清除本地缓存，确保数据发送出去
                out.flush();
            } catch (IOException e) {
                this.appendLog(e);
            } finally {
                // 确保输出流和连接关闭
                if (out != null) {
                    try {
                        this.appendLog("关闭输出流。");
                        out.close();
                    } catch (IOException e) {}
                }
                try {
                    this.appendLog("关闭socket。");
                    socket.close();
                } catch (IOException e) {}
            }
    	}
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
			// 如果已经开启，将已配对设备列表填入下拉列表框
			this.fillBtDevicesToSpinner();
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
	 * 将已配对设配填入下拉列表
	 */
	private void fillBtDevicesToSpinner() {
		if (this.mBtAdapter != null) {
			// 取出已配对设备
			Set<BluetoothDevice> deviceSet = this.mBtAdapter.getBondedDevices();
			if (this.mDeviceList == null) {
				// 如果存储设备信息的列表未初始化，则初始化之
				this.mDeviceList = new ArrayList<BluetoothDevice>(deviceSet.size());
			}
			
			// 新建下拉列表用的Adapter
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 循环将设备信息写入列表和下拉列表用的Adapter
			for (BluetoothDevice device : deviceSet) {
				this.mDeviceList.add(device);
				adapter.add(device.getName());
			}
			// 将Adapter与下拉列表关联
			this.mDevices.setAdapter(adapter);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				// 用户开启了蓝牙，填充已配对设备列表
				this.fillBtDevicesToSpinner();
			} else {
				// 否则提示需要蓝牙并退出程序
				Toast.makeText(this, R.string.msg_bluetooth_is_necessary, Toast.LENGTH_LONG).show();
				this.finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
