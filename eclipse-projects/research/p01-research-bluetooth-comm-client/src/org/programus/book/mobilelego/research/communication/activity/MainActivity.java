package org.programus.book.mobilelego.research.communication.activity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.programus.book.mobilelego.research.communication.R;
import org.programus.book.mobilelego.research.communication.net.SppClient;
import org.programus.book.mobilelego.research.communication.processor.MotorReportProcessor;
import org.programus.book.mobilelego.research.communication.protocol.ExitSignal;
import org.programus.book.mobilelego.research.communication.protocol.MotorMoveCommand;
import org.programus.book.mobilelego.research.communication.protocol.MotorReportCommand;
import org.programus.book.mobilelego.research.communication.protocol.MotorReportMessage;
import org.programus.book.mobilelego.research.communication.util.Communicator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 5;

	private Spinner mDevices;
	private ToggleButton mConnect;
	private TextView mLog;
	
	private SeekBar mSpeedBar;
	private Button mForward;
	private Button mBackword;
	private Button mFloat;
	private Button mStop;
	private ToggleButton mReport;
	private TextView mTachoCount;
	
	private BluetoothAdapter mBtAdapter;
	private List<BluetoothDevice> mDeviceList;
	
	private SppClient mClient;
	private Communicator mComm;

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
		this.mLog = (TextView) this.findViewById(R.id.log);
		this.mTachoCount = (TextView) this.findViewById(R.id.tacho_count);
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
                    MotorReportProcessor processor = new MotorReportProcessor();
                    processor.setReportCallback(new MotorReportProcessor.ReportCallback() {
                        @Override
                        public void displayReport(final MotorReportMessage msg) {
                            System.out.println("update report");
                            runOnUiThread(new Runnable () {
                                @Override
                                public void run() {
                                    mTachoCount.setText(String.format("TachoCount: %d\nSpeed: %d", msg.getTachoCount(), msg.getSpeed()));
                                }
                            });
                        }
                    });
                    
                    mComm.addProcessor(MotorReportMessage.class, processor);
					appendLog("Communicator ready.");
				} catch (Exception e) {
					appendLog(e);
				}
			}
		});
		
		this.mConnect = (ToggleButton) this.findViewById(R.id.connect);
		this.mConnect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					connect(mDeviceList.get(mDevices.getSelectedItemPosition()));
				} else {
					remoteFinish();
					disconnect();
				}
			}
		});
		
		this.mSpeedBar = (SeekBar) this.findViewById(R.id.speed);
		this.mForward = (Button) this.findViewById(R.id.forward);
		this.mBackword = (Button) this.findViewById(R.id.backward);
		this.mFloat = (Button) this.findViewById(R.id.flt);
		this.mStop = (Button) this.findViewById(R.id.stop);
		
		this.mForward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int speed = mSpeedBar.getProgress();
				sendMotorCommand(MotorMoveCommand.Command.Forward, speed);
			}
		});
		
		this.mBackword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int speed = mSpeedBar.getProgress();
				sendMotorCommand(MotorMoveCommand.Command.Backword, speed);
			}
		});
		
		this.mFloat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int speed = mSpeedBar.getProgress();
				sendMotorCommand(MotorMoveCommand.Command.Float, speed);
			}
		});
		
		this.mStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int speed = mSpeedBar.getProgress();
				sendMotorCommand(MotorMoveCommand.Command.Stop, speed);
			}
		});
		
		this.mReport = (ToggleButton) this.findViewById(R.id.toggle_report);
		
		this.mReport.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				MotorReportCommand cmd = new MotorReportCommand();
				cmd.setReportOn(isChecked);
				mComm.send(cmd);
				if (!isChecked) {
					mTachoCount.setText("");
				}
			}
		});
		
		
	}
	
	private void sendMotorCommand(MotorMoveCommand.Command command, float speed) {
		MotorMoveCommand cmd = new MotorMoveCommand();
		cmd.setCommand(command);
		cmd.setSpeed(speed);
        this.mComm.send(cmd);
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
