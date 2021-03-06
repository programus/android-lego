package org.programus.book.mobilelego.robopet.mobile;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.programus.book.mobilelego.robopet.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.robopet.comm.protocol.NetMessage;
import org.programus.book.mobilelego.robopet.comm.protocol.PetCommand;
import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.comm.util.Communicator.Processor;
import org.programus.book.mobilelego.robopet.mobile.net.SppClient;
import org.programus.book.mobilelego.robopet.net.OnConnectedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Processor<ExitSignal>{
	private static final int REQUEST_ENABLE_BT = 5;
	/** 语音识别对话框的请求代码 */
	private final static int REQUEST_VOICE_RECOGNITION = 1980;
	
	private final static String CMD_RE = "^([^0-9]+)([0-9]*)";
	
	private final static int DEFAULT_STEP = 3;
	private final static int DEFAULT_TURN_ANGLE = 90;
	
	private static enum BtConnectState {
		Disconnected,
		Connecting,
		Connected
	};

	private BtConnectState mBtConnectState = BtConnectState.Disconnected;

	private BluetoothAdapter mBtAdapter;
	private BluetoothDevice[] mDevices;

	private SppClient mClient;
	private Communicator mComm;
	/** 主要组件，用以显示命令和点击开始识别语音 */
	private TextView mMainView;
	/** 连接前的遮盖层 */
	private ViewGroup mCover;
	
	private Map<String, PetCommand.Command> mCmdTable = new HashMap<String, PetCommand.Command>();
	
	private Map<PetCommand.Command, String> mCmdFormats = new EnumMap<PetCommand.Command, String>(PetCommand.Command.class);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化控件
		this.initCtrls();
		// 初始化语音命令表及命令内容表
		this.initData();
		// 检查语音识别的可用性
		this.checkVoiceRecognitionAvailability();
		// 初始化网络客户端
		this.initNetClient();
	}
	
	private void initData() {
		Resources res = this.getResources();
		SparseArray<PetCommand.Command> resTable = new SparseArray<PetCommand.Command>();
		resTable.append(R.array.forward, PetCommand.Command.Forward);
		resTable.append(R.array.backward, PetCommand.Command.Backward);
		resTable.append(R.array.turn_left, PetCommand.Command.TurnLeft);
		resTable.append(R.array.turn_right, PetCommand.Command.TurnRight);
		resTable.append(R.array.calm, PetCommand.Command.Calm);
		resTable.append(R.array.stop, PetCommand.Command.Stop);
		resTable.append(R.array.exit, PetCommand.Command.Exit);
		resTable.append(R.array.shutdown, PetCommand.Command.Shutdown);
		for (int i = 0; i < resTable.size(); i++) {
			String[] candidates = res.getStringArray(resTable.keyAt(i));
			for (String text : candidates) {
				this.mCmdTable.put(text, resTable.valueAt(i));
			}
		}
		
		mCmdFormats.put(PetCommand.Command.Forward, res.getString(R.string.forward));
		mCmdFormats.put(PetCommand.Command.Backward, res.getString(R.string.backward));
		mCmdFormats.put(PetCommand.Command.TurnLeft, res.getString(R.string.turn_left));
		mCmdFormats.put(PetCommand.Command.TurnRight, res.getString(R.string.turn_right));
		mCmdFormats.put(PetCommand.Command.Calm, res.getString(R.string.calm));
		mCmdFormats.put(PetCommand.Command.Stop, res.getString(R.string.stop));
		mCmdFormats.put(PetCommand.Command.Exit, res.getString(R.string.exit));
		mCmdFormats.put(PetCommand.Command.Shutdown, res.getString(R.string.shutdown));
	}
	
	/**
	 * 检查是否支持语音支持
	 */
	private void checkVoiceRecognitionAvailability() {
		PackageManager pm = this.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() <= 0) {
			this.mMainView.setOnClickListener(null);
			this.mMainView.setText(this.getString(R.string.not_supported, this.getString(R.string.voice_recognition)));
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initCtrls() {
		this.mMainView = (TextView) this.findViewById(R.id.main_view);
		this.mCover = (ViewGroup) this.findViewById(R.id.cover);
		
		// 按下按钮则弹出语音识别窗口
		this.mMainView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});
	}
	
	/** 初始化网络客户端 */
	private void initNetClient() {
		this.mClient = new SppClient();
		this.mClient.setOnConnectedListener(new OnConnectedListener() {
			@Override
			public void onFailed(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBtConnectState(BtConnectState.Disconnected);
		                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
			}
			
			@Override
			public void onConnected(Communicator comm) {
				try {
                    mComm = comm;
                    mComm.addProcessor(ExitSignal.class, MainActivity.this);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
                            setBtConnectState(BtConnectState.Connected);
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
                            setBtConnectState(BtConnectState.Disconnected);
			                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		});
	}
	
	/**
	 * 启动语音识别对话框
	 */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.getString(R.string.app_name));
		// 使用请求代码启动Activity
		this.startActivityForResult(intent, REQUEST_VOICE_RECOGNITION);
	}
	
	/**
	 * 处理识别出的字符串
	 * @param results 识别出的所有字符串
	 */
	private void processRecognitionResults(List<String> results) {
		final int CMD_INDEX = 1;
		final int VALUE_INDEX = 2;
		
		// 编译正则表达式
		Pattern p = Pattern.compile(CMD_RE);
		String message = null;
		// 对所有语音识别的候选字符串做处理
		for (String result : results) {
			// 试图匹配正则表达式
			Matcher m = p.matcher(result);
			if (m.find()) {
				// 当查找到匹配部分时，分割命令部分和数值部分
				String cmdPart = m.group(CMD_INDEX);
				String valuePart = m.group(VALUE_INDEX);
				// 检查命令是否在我们的可识别命令表中
				PetCommand.Command cmd = this.mCmdTable.get(cmdPart);
				if (cmd != null) {
					// 如果命令为可识别命令，计算数值
					int value = 0;
					if (valuePart.length() <= 0) {
						// 数值不存在，使用默认值
						switch (cmd) {
						case Forward:
						case Backward:
							value = DEFAULT_STEP;
							break;
						case TurnLeft:
						case TurnRight:
							value = DEFAULT_TURN_ANGLE;
							break;
						default:
							break;
						}
					} else {
						value = Integer.parseInt(valuePart);
					}
					// 用识别出的信息生成宠物命令
					PetCommand msg = new PetCommand(cmd, value);
					// 发送命令
					this.sendMessage(msg);
					// 将命令与数值转化成标准格式
					message = String.format(this.mCmdFormats.get(cmd), value);
					break;
				}
			}
		}
		
		if (message == null) {
			// 若命令并非可识别命令，标记为未识别命令
			message = this.getString(R.string.unknown);
		}
		
		// 将识别出的命令显示在屏幕上
		this.mMainView.setText(message);
	}
	
	private void sendMessage(NetMessage msg) {
		if (this.mClient.isConnected() && this.mComm != null) {
			this.mComm.send(msg);
			Toast.makeText(this, "Command sended.", Toast.LENGTH_LONG).show();
		}
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
	
	/**
	 * 请求用户开启蓝牙
	 */
	private void requestEnableBluetooth() {
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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
						if (which < 0) {
							setBtConnectState(BtConnectState.Disconnected);
						} else {
							connect(devices[which]);
						}
					}
				});
			AlertDialog dialog = builder.create();
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					setBtConnectState(BtConnectState.Disconnected);
				}
			});
			dialog.show();
		} else {
			Toast.makeText(this, R.string.msg_bluetooth_pair_necessary, Toast.LENGTH_LONG).show();
		}
	}
	
	private void connect(BluetoothDevice device) {
		if (mClient.isConnected()) {
			mClient.close();
		}
		mClient.connect(device);
	}
	
	private void disconnect() {
		if (mClient.isConnected()) {
			mClient.close();
		}
	}
	
	private void remoteFinish() {
		if (mClient != null && mClient.isConnected() && mComm != null) {
            mComm.send(ExitSignal.getInstance());
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
		this.mMainView.setText(null);
		this.invalidateOptionsMenu();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_VOICE_RECOGNITION:
		{
			if (resultCode == RESULT_OK) {
				// 当请求代码是语音识别对话框的请求代码 并且 Activity返回结果为正常时
				// 取得识别到的文本列表
				List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				// 处理识别内容
				this.processRecognitionResults(results);
			}
		}
		break;
		case REQUEST_ENABLE_BT:
		{
			if (resultCode == Activity.RESULT_OK) {
				// 用户开启了蓝牙，提示用户选择设备进行连接
				this.selectDevice();
			} else {
				// 否则提示需要蓝牙并退出程序
				Toast.makeText(this, R.string.msg_bluetooth_is_necessary, Toast.LENGTH_LONG).show();
				this.setBtConnectState(BtConnectState.Disconnected);
			}
		}
		break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		for (int i = 0; i < menu.size(); i++) {
			menu.getItem(i).setVisible(i == mBtConnectState.ordinal());
		}
		return super.onPrepareOptionsMenu(menu);
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
	protected void onStop() {
		Log.d("MainActivity", "On Stop");
		super.onStop();
		this.remoteFinish();
		this.disconnect();
	}

	@Override
	public void process(ExitSignal msg, Communicator communicator) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setBtConnectState(BtConnectState.Disconnected);
			}
		});
	}
}
