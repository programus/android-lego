package org.programus.book.mobilelego.motion_rc_vehicle.rc.activities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.ObstacleInforMessage;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotMoveCommand;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotReportMessage;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;
import org.programus.book.mobilelego.motion_rc_vehicle.rc.R;
import org.programus.book.mobilelego.motion_rc_vehicle.rc.net.RobotMoveCommandSender;
import org.programus.book.mobilelego.motion_rc_vehicle.rc.net.SppClient;
import org.programus.book.mobilelego.motion_rc_vehicle.rc.processors.ObstacleInforProcessor;
import org.programus.book.mobilelego.motion_rc_vehicle.rc.processors.RobotReportProcessor;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 5;
	private static final int MAX_ENGINE_SPEED = 800;

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
	
	private RobotReportProcessor mReportProcessor;
	private ObstacleInforProcessor mObstacleProcessor;

	private SensorManager mSensorManager;
	private Sensor mGravity;
	private SensorEventListener mSensorListener;
	private double mRotateAngle;

	private SurfaceView mRotateAngleView;
	private SurfaceHolder mRotateAngleHolder;

	private ProgressBar mRotationSpeedBar;
	private TextView mRotationSpeedText;
	private ProgressBar mSpeedBar;
	private TextView mSpeedText;
	private TextView mDistanceText;

	private ViewGroup mObstaclePart;
	private TextView mObstacleText;
	private ObstacleInforMessage mObstacleInfor;

	private RadioGroup mGears;
	private RadioButton mBackward;
	private VerticalSeekBar mEngineBar;
	private Button mBreak;
	private TextView mEngineSetSpeed;
	private boolean mMoveBackward;
	private int mEngineSpeed;
	private RobotMoveCommand.Command mCommand;
	private RobotMoveCommandSender mCmdSender;
	
	private ViewGroup mCover;
	private TextView mLog;
	
	public MainActivity() {
		initProcessors();
	}
	
	private void initProcessors() {
		this.mReportProcessor = new RobotReportProcessor();
		this.mReportProcessor.setReportCallback(new RobotReportProcessor.ReportCallback() {
			@Override
			public void displayReport(final RobotReportMessage msg) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateRobotReport(msg);
					}
				});
			}
		});
		
		this.mObstacleProcessor = new ObstacleInforProcessor();
		this.mObstacleProcessor.setCallback(new ObstacleInforProcessor.Callback() {
			@Override
			public void displayObstacleInfor(final ObstacleInforMessage msg) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateObstacleInfor(msg);
					}
				});
			}
		});
	}

	/**
	 * 初始化界面控件
	 */
	private void initComponents() {
		this.mRotateAngleView = (SurfaceView) this.findViewById(R.id.rotate_angle_view);
		this.mRotateAngleHolder = mRotateAngleView.getHolder();
		
		this.mRotationSpeedBar = (ProgressBar) this.findViewById(R.id.rotation_speed_progress);
		this.mRotationSpeedText = (TextView) this.findViewById(R.id.rotation_speed_text);
		this.mSpeedBar = (ProgressBar) this.findViewById(R.id.speed_progress);
		this.mSpeedText = (TextView) this.findViewById(R.id.speed_text);
		this.mDistanceText = (TextView) this.findViewById(R.id.distance_text);
		
		this.mObstaclePart = (ViewGroup) this.findViewById(R.id.obstacle_part);
		this.mObstacleText = (TextView) this.findViewById(R.id.obstacle_distance);
		
		this.mGears = (RadioGroup) this.findViewById(R.id.gears);
		this.mBackward = (RadioButton) this.findViewById(R.id.gear_backward);
		this.mEngineBar = (VerticalSeekBar) this.findViewById(R.id.engine_bar);
		this.mBreak = (Button) this.findViewById(R.id.engine_break);
		this.mEngineSetSpeed = (TextView) this.findViewById(R.id.set_speed);

		this.mCover = (ViewGroup) this.findViewById(R.id.cover);
		this.mLog = (TextView) this.findViewById(R.id.log);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_activity);

		this.initComponents();
		this.initSensor();
		this.setupComponents();
		this.initNetClient();
	}
	
	/**
	 * 初始化传感器
	 */
	private void initSensor() {
		mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // 取得界面旋转信息
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                float g = 0;
                switch (rotation) {
                case Surface.ROTATION_0:
                    // 界面无旋转，取x轴方向分量
                    // 右转为正，数据取反
                    g = -event.values[0];
                    break;
                case Surface.ROTATION_90:
                    // 界面逆时针90度旋转，取y轴分量
                    g = event.values[1];
                    break;
                case Surface.ROTATION_180:
                    // 界面旋转180度，取x轴方向分量
                    g = event.values[0];
                    break;
                case Surface.ROTATION_270:
                    // 界面逆时针旋转270度，取y轴分量
                    // 右转为正，数据取反
                    g = -event.values[1];
                    break;
                }
                mRotateAngle = Math.asin(g / SensorManager.GRAVITY_EARTH);
                setMoveCommand();
                sendCommand();
            }

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// 我们不关心精度的变化，不做任何事
			}
		};
	}
	
	private void initNetClient() {
		this.mClient = new SppClient();
		this.mCmdSender = new RobotMoveCommandSender();
		this.mClient.setOnConnectedListener(new SppClient.OnConnectedListener() {
			@Override
			public void onFailed(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBtConnectState(BtConnectState.Disconnected);
                    }
                });
				appendLog(e);
			}
			
			@Override
			public void onConnected(Communicator comm) {
				try {
                    appendLog("Connected");
                    mComm = comm;
                    
                    mComm.addProcessor(RobotReportMessage.class, mReportProcessor);
                    mComm.addProcessor(ObstacleInforMessage.class, mObstacleProcessor);
                    
                    mCmdSender.init(mComm);
                    
					appendLog("Communicator ready.");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
                            setBtConnectState(BtConnectState.Connected);
						}
					});
				} catch (Exception e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
                            setBtConnectState(BtConnectState.Disconnected);
						}
					});
					appendLog(e);
				}
			}
		});
	}
	
	private void updateRobotReport(RobotReportMessage msg) {
		final int RSPEED_RATE = 1000 / 6;
		final int SPEED_RATE = 1;
		final double RSPEED_CVT = 1. / 6;
		final double SPEED_CVT = 0.001;
		double distance = msg.getDistance();
		if (distance < 0) {
			this.mRotationSpeedBar.setMax(msg.getRotationSpeed() * RSPEED_RATE);
			this.mSpeedBar.setMax(msg.getSpeed() * SPEED_RATE);
		} else {
			short rspeed = msg.getRotationSpeed();
			short speed = msg.getSpeed();
			this.mRotationSpeedBar.setProgress(Math.abs(rspeed * RSPEED_RATE));
			this.mRotationSpeedBar.setBackgroundResource(rspeed >= 0 ? R.color.positive_progress_color : R.color.negative_progress_color);
			this.mRotationSpeedText.setText(this.getString(R.string.rotation_speed_format, rspeed * RSPEED_CVT));
			this.mSpeedBar.setProgress(Math.abs(speed * SPEED_RATE));
			this.mSpeedBar.setBackgroundResource(speed >= 0 ? R.color.positive_progress_color : R.color.negative_progress_color);
			this.mSpeedText.setText(this.getString(R.string.speed_format, speed * SPEED_CVT));
			this.mDistanceText.setText(this.getString(R.string.distance_format, distance * SPEED_CVT));
		}
	}
	
	private void updateObstacleInfor(ObstacleInforMessage msg) {
		this.mObstacleInfor = msg;
		float distance = msg.getFloatDistanceInMm();
		this.mObstacleText.setText(this.getString(R.string.obstacle_distance_format, distance));
		int colorId = R.color.obstacle_safe_color;
		switch (msg.getType()) {
		case Safe:
			colorId = R.color.obstacle_safe_color;
			break;
		case Warning:
			colorId = R.color.obstacle_warning_color;
			break;
		case Danger:
			colorId = R.color.obstacle_danger_color;
			break;
		case Unknown:
			colorId = R.color.obstacle_unknown_color;
			break;
		}
		this.mObstaclePart.setBackgroundResource(colorId);
	}
	
	private void setupComponents() {
		this.setupRotateAngleSurface();
		this.setupPowerControl();
		mCover.setVisibility(View.VISIBLE);
	}
	
	private void setupRotateAngleSurface() {
		mRotateAngleHolder.addCallback(new SurfaceHolder.Callback() {
			private Timer timer = new Timer("angle surface draw");
			private TimerTask task;

			private PointF center = new PointF();
			private float radius;
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				task.cancel();
				timer.purge();
			}
			
			@Override
			public void surfaceCreated(final SurfaceHolder holder) {
				task = new TimerTask() {
					@Override
					public void run() {
                        Canvas canvas = holder.lockCanvas();
                        if (canvas != null) {
                        	drawVehicleAngle(canvas, center, radius, mRotateAngle);
                            holder.unlockCanvasAndPost(canvas);
                        }
					}
				};
				timer.schedule(task, 0, 1000 / 25);
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
				center.x = width / 2.f;
				center.y = height /2.f;
				radius = Math.min(center.x, center.y);
			}
		});
	}
	
	private void drawVehicleAngle(Canvas canvas, PointF center, float radius, double angle) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        
        // 绘制单色背景
        canvas.drawARGB(0xff, 0xee, 0xee, 0xde);
        
        // 绘制前方基准线
        paint.setColor(0xff5588aa);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(center.x, center.y, center.x, center.y - radius + 3, paint);
        canvas.drawCircle(center.x, center.y, radius - 3, paint);
        int step = 30;
        for (int i = 0; i < 360; i+= step) {
            canvas.rotate(step, center.x, center.y);
            canvas.drawLine(center.x, center.y - radius + 8, center.x, center.y - radius + 3, paint);
        }

        canvas.save();
        canvas.rotate((float)Math.toDegrees(angle), center.x, center.y);
        // 绘制机器人
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xcccccccc);
        canvas.drawRect(center.x * 0.75f, center.y * 0.7f, center.x * 1.25f, center.y * 1.3f, paint);
        canvas.drawRect(center.x * 0.55f, center.y * 0.65f, center.x * 0.73f, center.y * 1.35f, paint);
        canvas.drawRect(center.x * 1.45f, center.y * 0.657f, center.x * 1.27f, center.y * 1.35f, paint);
        canvas.drawRect(center.x * 0.8f, center.y * 0.5f, center.x * 1.2f, center.y * 0.6f, paint);
        canvas.drawRect(center.x * 0.92f, center.y * 0.6f, center.x * 1.08f, center.y * 0.7f, paint);
        paint.setColor(0x77ffffff);
        canvas.drawRect(center.x * 0.82f, center.y * 0.77f, center.x * 1.18f, center.y * 0.95f, paint);
        // 绘制机器人方向指示
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xffff9900);
        canvas.drawLine(center.x, center.y, center.x, center.y - radius + 10, paint);
        canvas.drawLine(center.x - 2, center.y - radius + 20, center.x, center.y - radius + 10, paint);
        canvas.drawLine(center.x + 2, center.y - radius + 20, center.x, center.y - radius + 10, paint);
        canvas.restore();
	}
	
	private void setupPowerControl() {
		mEngineBar.setMax(MAX_ENGINE_SPEED);
		mEngineBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
                mEngineSpeed = Math.min(progress, seekBar.getMax());
                float rpm = mEngineSpeed / 6f;
                mEngineSetSpeed.setText(getString(R.string.rotation_speed_format, rpm));
                setMoveCommand();
                sendCommand();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				seekBar.setProgress(0);
				mEngineSetSpeed.setText(R.string.none);
				mEngineSpeed = 0;
				mCommand = RobotMoveCommand.Command.Float;
				sendCommand();
			}
		});

		mBreak.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mEngineSpeed = 0;
					mCommand = RobotMoveCommand.Command.Stop;
					sendCommand();
				}
				return false;
			}
		});
		
		mGears.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				mMoveBackward = mBackward.isChecked();
				setMoveCommand();
				sendCommand();
			}
		});
	}
	
	private void setMoveCommand() {
		if (!mBreak.isPressed()) {
			this.mCommand = mMoveBackward ? 
					RobotMoveCommand.Command.Backward : RobotMoveCommand.Command.Forward;
		}
	}
	
	private void sendCommand() {
		if (RobotMoveCommand.Command.Forward.equals(mCommand) && this.mObstacleInfor != null && this.mObstacleInfor.getType().equals(ObstacleInforMessage.Type.Danger)) {
			this.mCommand = RobotMoveCommand.Command.Stop;
		}
		if (this.mClient.isConnected() && mComm != null) {
            RobotMoveCommand cmd = new RobotMoveCommand();
            cmd.setCommand(this.mCommand);
            cmd.setRotation((short)Math.toDegrees(mRotateAngle));
            cmd.setSpeed((short)mEngineSpeed);
            mCmdSender.requestSend(cmd);
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
			mCmdSender.init(null);
			mClient.close();
			appendLog("Disconnected.");
		}
	}
	
	private void remoteFinish() {
		if (mClient != null && mClient.isConnected() && mComm != null) {
            mComm.send(ExitSignal.getInstance());
		}
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
		super.onResume();
		// 注册传感器事件监听器
		mSensorManager.registerListener(mSensorListener, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 解除传感器时间监听器
		mSensorManager.unregisterListener(mSensorListener);
		this.remoteFinish();
		this.disconnect();
	}
}
