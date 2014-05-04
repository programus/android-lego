package org.programus.book.mobilelego.research.sensor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Surface;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView mAngle;
	
	private SensorManager mSensorManager;
	private Sensor mGravity;
	private SensorEventListener mSensorListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_activity);
		this.initComponents();
		this.initSensor();
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
                double alpha = Math.asin(g / SensorManager.GRAVITY_EARTH);
                displayAngle(alpha);
            }

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// 我们不关心精度的变化，不做任何事
			}
		};
	}
	
	private void displayAngle(double angle) {
		double degree = Math.toDegrees(angle);
		mAngle.setText(String.format("%f / %f", angle, degree));
	}

	private void initComponents() {
		mAngle = (TextView) this.findViewById(R.id.angle);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 注册传感器事件监听器
		mSensorManager.registerListener(mSensorListener, mGravity, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 解除传感器时间监听器
		mSensorManager.unregisterListener(mSensorListener);
	}
}
