package org.programus.book.mobilelego.trafficsign.mobile;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.programus.book.mobilelego.trafficsign.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.trafficsign.comm.protocol.NetMessage;
import org.programus.book.mobilelego.trafficsign.comm.util.Communicator;
import org.programus.book.mobilelego.trafficsign.comm.util.Communicator.Processor;
import org.programus.book.mobilelego.trafficsign.mobile.imagerecognition.CameraPreviewer;
import org.programus.book.mobilelego.trafficsign.mobile.imagerecognition.TrafficSign;
import org.programus.book.mobilelego.trafficsign.mobile.imagerecognition.TrafficSignDetector;
import org.programus.book.mobilelego.trafficsign.mobile.net.SppClient;
import org.programus.book.mobilelego.trafficsign.net.OnConnectedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements Processor<ExitSignal>{
	private static final int REQUEST_ENABLE_BT = 5;
	
	private static final int MIN_RES = 720 * 720;
	
	private static final int MIN_UNIT = 5;
	
	private static final String SEL_SIZE_KEY = "SelectedSize";
	
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
	
	/** 连接前的遮盖层 */
	private ViewGroup mCover;
	
	
	/** 选中的分辨率在支持分辨率中的所在位置 */
	private int mSelectedSizeIndex = -1;
	/** 供相机预览用的SurfaceView */
	private SurfaceView mCameraPreviewView;
	
	/** 相机预览器 */
	private CameraPreviewer mPreviewer;
	
	/** 显示处理过程图像的SurfaceView对应的Holder */
	private SurfaceHolder mImageHolder;
	
	/** 显示检测出的路标的SurfaceView对应的Holder */
	private SurfaceHolder mSignHolder;
	
	/** 路标检测器 */
	private TrafficSignDetector mDetector;
	
	/** 绘制文字用的Paint */
	private Paint mPaint;
	/** 处理帧率 */
	private float mFps;
	
	/** 已知路标和对应文字的对照表 */
	private Map<TrafficSign, String> mSignMap = new HashMap<TrafficSign, String>();
	
	/** 处理相机预览时每帧数据的回调接口 */
	private Camera.PreviewCallback mCamPrevCallback = new Camera.PreviewCallback() {
		private long time;
		/* (non-Javadoc)
		 * @see android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[], android.hardware.Camera)
		 */
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// 取得当前系统时间（单位：毫秒）
			long now = System.currentTimeMillis();
			// 计算帧率: 帧率 = 1000 / 本帧与上一帧之间的时间差
			mFps = 1000f / (now - time);
			// 更新时间
			time = now;
			if (mDetector != null) {
				// 将本帧图像数据传给检测器
				mDetector.updateRawBuffer(data);
				// 检测路标
				mDetector.detectTrafficSign();
				// 将存储图像数据的数组传给相机重用
				camera.addCallbackBuffer(data);
				// 绘制检测过程图像
				drawInfoImage(mDetector);
				// 绘制检测出的路标信息
				drawDetectedSign(mDetector);
			} else {
				// 将存储图像数据的数组传给相机重用
				camera.addCallbackBuffer(data);
			}
		}
	};
	
	/**
	 * 绘制检测信息
	 * @param detector 检测器
	 */
	private void drawInfoImage(TrafficSignDetector detector) {
		Canvas canvas = this.mImageHolder.lockCanvas();
		if (canvas != null) {
			try {
				detector.drawInfoImage(canvas);
				// 绘制帧率信息
				canvas.drawText(String.format("FPS: %.2f", mFps), canvas.getWidth() >> 1, canvas.getHeight(), mPaint);
			} finally {
				this.mImageHolder.unlockCanvasAndPost(canvas);
			}	
		}
	}
	
	/**
	 * 绘制检测出的路标，对已知路标，显示路标名称
	 * @param detector 检测器
	 */
	private void drawDetectedSign(TrafficSignDetector detector) {
		Canvas canvas = this.mSignHolder.lockCanvas();
		if (canvas != null) {
			try {
				TrafficSign sign = detector.getDetectedSign();
				// 绘制路标图形
				sign.draw(canvas, !detector.isSignDetected());
				// 绘制已知路标名称
				this.drawKnownSign(canvas, sign);
			} finally {
				this.mSignHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	/**
	 * 绘制已知图标名称
	 * @param canvas
	 * @param sign
	 */
	private void drawKnownSign(Canvas canvas, TrafficSign sign) {
		String signName = this.mSignMap.get(sign);
		if (signName == null) {
			// 当路标并非已知时，显示“未知”
			signName = "未知";
		}
		
		canvas.drawText(signName, canvas.getWidth() >> 1, canvas.getHeight() >> 1, mPaint);
	}

	/**
	 * 初始化
	 */
	private void initComponents() {
		this.mCameraPreviewView = (SurfaceView) this.findViewById(R.id.camera_preview);
		this.mPreviewer = new CameraPreviewer();
		// 设置预览用的SurfaceView
		this.mPreviewer.setPreviewView(this.mCameraPreviewView);
		// 设置处理预览图片的回调接口
		this.mPreviewer.setPreviewCallback(this.mCamPrevCallback);
		
		SurfaceView imageView = (SurfaceView) this.findViewById(R.id.information_image);
		this.mImageHolder = imageView.getHolder();
		SurfaceView signView = (SurfaceView) this.findViewById(R.id.sign);
		this.mSignHolder = signView.getHolder();
		// 设置路标显示区大小
		this.mSignHolder.setFixedSize(TrafficSign.SIGN_EDGE_LEN << 3, TrafficSign.SIGN_EDGE_LEN << 3);
		
		this.mPaint = new Paint();
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(32);
		mPaint.setColor(0x7f00ff00);
		
		this.mDetector = new TrafficSignDetector();
		TrafficSign sign = new TrafficSign();
		this.mDetector.setSign(sign);
		// 设置最小检测宽度
		this.mDetector.setMinUnit(MIN_UNIT);
		
		this.initKnownSign();
		this.initCameraSize();
	}
	
	/**
	 * 初始化已知路标与名称对照表
	 */
	private void initKnownSign() {
		this.mSignMap.put(new TrafficSign(
				(short)0x000a, (short)0x0109, (short)0x010a, (short)0x010b, (short)0x0208, (short)0x0209, (short)0x020a, (short)0x020b, 
				(short)0x020c, (short)0x0307, (short)0x0308, (short)0x0309, (short)0x030a, (short)0x030b, (short)0x030c, (short)0x030d, 
				(short)0x0406, (short)0x0407, (short)0x0408, (short)0x0409, (short)0x040a, (short)0x040b, (short)0x040c, (short)0x040d, 
				(short)0x040e, (short)0x0508, (short)0x0509, (short)0x050a, (short)0x050b, (short)0x050c, (short)0x0608, (short)0x0609, 
				(short)0x060a, (short)0x060b, (short)0x060c, (short)0x0708, (short)0x0709, (short)0x070a, (short)0x070b, (short)0x070c, 
				(short)0x0808, (short)0x0809, (short)0x080a, (short)0x080b, (short)0x080c, (short)0x0908, (short)0x0909, (short)0x090a, 
				(short)0x090b, (short)0x090c, (short)0x0a08, (short)0x0a09, (short)0x0a0a, (short)0x0a0b, (short)0x0a0c, (short)0x0b08, 
				(short)0x0b09, (short)0x0b0a, (short)0x0b0b, (short)0x0b0c, (short)0x0c08, (short)0x0c09, (short)0x0c0a, (short)0x0c0b, 
				(short)0x0c0c, (short)0x0d08, (short)0x0d09, (short)0x0d0a, (short)0x0d0b, (short)0x0d0c, (short)0x0e08, (short)0x0e09, 
				(short)0x0e0a, (short)0x0e0b, (short)0x0e0c, (short)0x0f08, (short)0x0f09, (short)0x0f0a, (short)0x0f0b, (short)0x0f0c, 
				(short)0x1008, (short)0x1009, (short)0x100a, (short)0x100b, (short)0x100c, (short)0x1108, (short)0x1109, (short)0x110a, 
				(short)0x110b, (short)0x110c, (short)0x1208, (short)0x1209, (short)0x120a, (short)0x120b, (short)0x120c, (short)0x1308, 
				(short)0x1309, (short)0x130a, (short)0x130b, (short)0x130c
			), "前进");
		this.mSignMap.put(new TrafficSign(
				(short)0x0004, (short)0x0103, (short)0x0104, (short)0x0202, (short)0x0203, (short)0x0204, (short)0x0205, (short)0x0206, 
				(short)0x0207, (short)0x0208, (short)0x0209, (short)0x020a, (short)0x020b, (short)0x020c, (short)0x020d, (short)0x020e, 
				(short)0x020f, (short)0x0210, (short)0x0211, (short)0x0301, (short)0x0302, (short)0x0303, (short)0x0304, (short)0x0305, 
				(short)0x0306, (short)0x0307, (short)0x0308, (short)0x0309, (short)0x030a, (short)0x030b, (short)0x030c, (short)0x030d, 
				(short)0x030e, (short)0x030f, (short)0x0310, (short)0x0311, (short)0x0312, (short)0x0400, (short)0x0401, (short)0x0402, 
				(short)0x0403, (short)0x0404, (short)0x0405, (short)0x0406, (short)0x0407, (short)0x0408, (short)0x0409, (short)0x040a, 
				(short)0x040b, (short)0x040c, (short)0x040d, (short)0x040e, (short)0x040f, (short)0x0410, (short)0x0411, (short)0x0412, 
				(short)0x0413, (short)0x0501, (short)0x0502, (short)0x0503, (short)0x0504, (short)0x0505, (short)0x0506, (short)0x0507, 
				(short)0x0508, (short)0x0509, (short)0x050a, (short)0x050b, (short)0x050c, (short)0x050d, (short)0x050e, (short)0x050f, 
				(short)0x0510, (short)0x0511, (short)0x0512, (short)0x0513, (short)0x0602, (short)0x0603, (short)0x0604, (short)0x0605, 
				(short)0x0606, (short)0x0607, (short)0x0608, (short)0x0609, (short)0x060a, (short)0x060b, (short)0x060c, (short)0x060d, 
				(short)0x060e, (short)0x060f, (short)0x0610, (short)0x0611, (short)0x0612, (short)0x0613, (short)0x0703, (short)0x0704, 
				(short)0x070f, (short)0x0710, (short)0x0711, (short)0x0712, (short)0x0713, (short)0x0804, (short)0x080f, (short)0x0810, 
				(short)0x0811, (short)0x0812, (short)0x0813, (short)0x090f, (short)0x0910, (short)0x0911, (short)0x0912, (short)0x0913, 
				(short)0x0a0f, (short)0x0a10, (short)0x0a11, (short)0x0a12, (short)0x0a13, (short)0x0b0f, (short)0x0b10, (short)0x0b11, 
				(short)0x0b12, (short)0x0b13, (short)0x0c0f, (short)0x0c10, (short)0x0c11, (short)0x0c12, (short)0x0c13, (short)0x0d0f, 
				(short)0x0d10, (short)0x0d11, (short)0x0d12, (short)0x0d13, (short)0x0e0f, (short)0x0e10, (short)0x0e11, (short)0x0e12, 
				(short)0x0e13, (short)0x0f0f, (short)0x0f10, (short)0x0f11, (short)0x0f12, (short)0x0f13, (short)0x100f, (short)0x1010, 
				(short)0x1011, (short)0x1012, (short)0x1013, (short)0x110f, (short)0x1110, (short)0x1111, (short)0x1112, (short)0x1113, 
				(short)0x120f, (short)0x1210, (short)0x1211, (short)0x1212, (short)0x1213, (short)0x130f, (short)0x1310, (short)0x1311, 
				(short)0x1312, (short)0x1313
			), "左转");
		this.mSignMap.put(new TrafficSign(
				(short)0x000f, (short)0x010f, (short)0x0110, (short)0x0202, (short)0x0203, (short)0x0204, (short)0x0205, (short)0x0206, 
				(short)0x0207, (short)0x0208, (short)0x0209, (short)0x020a, (short)0x020b, (short)0x020c, (short)0x020d, (short)0x020e, 
				(short)0x020f, (short)0x0210, (short)0x0211, (short)0x0301, (short)0x0302, (short)0x0303, (short)0x0304, (short)0x0305, 
				(short)0x0306, (short)0x0307, (short)0x0308, (short)0x0309, (short)0x030a, (short)0x030b, (short)0x030c, (short)0x030d, 
				(short)0x030e, (short)0x030f, (short)0x0310, (short)0x0311, (short)0x0312, (short)0x0400, (short)0x0401, (short)0x0402, 
				(short)0x0403, (short)0x0404, (short)0x0405, (short)0x0406, (short)0x0407, (short)0x0408, (short)0x0409, (short)0x040a, 
				(short)0x040b, (short)0x040c, (short)0x040d, (short)0x040e, (short)0x040f, (short)0x0410, (short)0x0411, (short)0x0412, 
				(short)0x0413, (short)0x0500, (short)0x0501, (short)0x0502, (short)0x0503, (short)0x0504, (short)0x0505, (short)0x0506, 
				(short)0x0507, (short)0x0508, (short)0x0509, (short)0x050a, (short)0x050b, (short)0x050c, (short)0x050d, (short)0x050e, 
				(short)0x050f, (short)0x0510, (short)0x0511, (short)0x0512, (short)0x0600, (short)0x0601, (short)0x0602, (short)0x0603, 
				(short)0x0604, (short)0x0605, (short)0x0606, (short)0x0607, (short)0x0608, (short)0x0609, (short)0x060a, (short)0x060b, 
				(short)0x060c, (short)0x060d, (short)0x060e, (short)0x060f, (short)0x0610, (short)0x0611, (short)0x0700, (short)0x0701, 
				(short)0x0702, (short)0x0703, (short)0x0704, (short)0x070f, (short)0x0710, (short)0x0800, (short)0x0801, (short)0x0802, 
				(short)0x0803, (short)0x0804, (short)0x080f, (short)0x0900, (short)0x0901, (short)0x0902, (short)0x0903, (short)0x0904, 
				(short)0x0a00, (short)0x0a01, (short)0x0a02, (short)0x0a03, (short)0x0a04, (short)0x0b00, (short)0x0b01, (short)0x0b02, 
				(short)0x0b03, (short)0x0b04, (short)0x0c00, (short)0x0c01, (short)0x0c02, (short)0x0c03, (short)0x0c04, (short)0x0d00, 
				(short)0x0d01, (short)0x0d02, (short)0x0d03, (short)0x0d04, (short)0x0e00, (short)0x0e01, (short)0x0e02, (short)0x0e03, 
				(short)0x0e04, (short)0x0f00, (short)0x0f01, (short)0x0f02, (short)0x0f03, (short)0x0f04, (short)0x1000, (short)0x1001, 
				(short)0x1002, (short)0x1003, (short)0x1004, (short)0x1100, (short)0x1101, (short)0x1102, (short)0x1103, (short)0x1104, 
				(short)0x1200, (short)0x1201, (short)0x1202, (short)0x1203, (short)0x1204, (short)0x1300, (short)0x1301, (short)0x1302, 
				(short)0x1303, (short)0x1304
			), "右转");
		this.mSignMap.put(new TrafficSign(
				(short)0x0007, (short)0x0008, (short)0x0009, (short)0x000a, (short)0x000b, (short)0x000c, (short)0x000d, (short)0x000e, 
				(short)0x0106, (short)0x0107, (short)0x0108, (short)0x0109, (short)0x010a, (short)0x010b, (short)0x010c, (short)0x010d, 
				(short)0x010e, (short)0x010f, (short)0x0205, (short)0x0206, (short)0x0207, (short)0x0208, (short)0x0209, (short)0x020a, 
				(short)0x020b, (short)0x020c, (short)0x020d, (short)0x020e, (short)0x020f, (short)0x0210, (short)0x0304, (short)0x0305, 
				(short)0x0306, (short)0x0307, (short)0x0308, (short)0x0309, (short)0x030a, (short)0x030b, (short)0x030c, (short)0x030d, 
				(short)0x030e, (short)0x030f, (short)0x0310, (short)0x0311, (short)0x0404, (short)0x0405, (short)0x0406, (short)0x0407, 
				(short)0x0408, (short)0x0409, (short)0x040c, (short)0x040d, (short)0x040e, (short)0x040f, (short)0x0410, (short)0x0411, 
				(short)0x0504, (short)0x0505, (short)0x0506, (short)0x0507, (short)0x0508, (short)0x050d, (short)0x050e, (short)0x050f, 
				(short)0x0510, (short)0x0511, (short)0x0604, (short)0x0605, (short)0x0606, (short)0x0607, (short)0x0608, (short)0x060d, 
				(short)0x060e, (short)0x060f, (short)0x0610, (short)0x0611, (short)0x0704, (short)0x0705, (short)0x0706, (short)0x0707, 
				(short)0x0708, (short)0x070d, (short)0x070e, (short)0x070f, (short)0x0710, (short)0x0711, (short)0x0804, (short)0x0805, 
				(short)0x0806, (short)0x0807, (short)0x0808, (short)0x080d, (short)0x080e, (short)0x080f, (short)0x0810, (short)0x0811, 
				(short)0x0904, (short)0x0905, (short)0x0906, (short)0x0907, (short)0x0908, (short)0x090d, (short)0x090e, (short)0x090f, 
				(short)0x0910, (short)0x0911, (short)0x0a04, (short)0x0a05, (short)0x0a06, (short)0x0a07, (short)0x0a08, (short)0x0a0d, 
				(short)0x0a0e, (short)0x0a0f, (short)0x0a10, (short)0x0a11, (short)0x0b04, (short)0x0b05, (short)0x0b06, (short)0x0b07, 
				(short)0x0b08, (short)0x0b0d, (short)0x0b0e, (short)0x0b0f, (short)0x0b10, (short)0x0b11, (short)0x0c04, (short)0x0c05, 
				(short)0x0c06, (short)0x0c07, (short)0x0c08, (short)0x0c0d, (short)0x0c0e, (short)0x0c0f, (short)0x0c10, (short)0x0c11, 
				(short)0x0d04, (short)0x0d05, (short)0x0d06, (short)0x0d07, (short)0x0d08, (short)0x0d0d, (short)0x0d0e, (short)0x0d0f, 
				(short)0x0d10, (short)0x0d11, (short)0x0e04, (short)0x0e05, (short)0x0e06, (short)0x0e07, (short)0x0e08, (short)0x0e0d, 
				(short)0x0e0e, (short)0x0e0f, (short)0x0e10, (short)0x0e11, (short)0x0f04, (short)0x0f05, (short)0x0f06, (short)0x0f07, 
				(short)0x0f08, (short)0x0f0b, (short)0x0f0c, (short)0x0f0d, (short)0x0f0e, (short)0x0f0f, (short)0x0f10, (short)0x0f11, 
				(short)0x0f12, (short)0x0f13, (short)0x1004, (short)0x1005, (short)0x1006, (short)0x1007, (short)0x1008, (short)0x100c, 
				(short)0x100d, (short)0x100e, (short)0x100f, (short)0x1010, (short)0x1011, (short)0x1012, (short)0x1104, (short)0x1105, 
				(short)0x1106, (short)0x1107, (short)0x1108, (short)0x110d, (short)0x110e, (short)0x110f, (short)0x1110, (short)0x1111, 
				(short)0x1204, (short)0x1205, (short)0x1206, (short)0x1207, (short)0x1208, (short)0x120e, (short)0x120f, (short)0x1210, 
				(short)0x1304, (short)0x1305, (short)0x1306, (short)0x1307, (short)0x1308, (short)0x130f
			), "掉头");
		this.mSignMap.put(new TrafficSign(
				(short)0x0008, (short)0x0009, (short)0x0107, (short)0x010a, (short)0x0205, (short)0x0206, (short)0x0207, (short)0x020a, 
				(short)0x020b, (short)0x020c, (short)0x0304, (short)0x0307, (short)0x030a, (short)0x030d, (short)0x0404, (short)0x0407, 
				(short)0x040a, (short)0x040d, (short)0x040e, (short)0x040f, (short)0x0504, (short)0x0507, (short)0x050a, (short)0x050d, 
				(short)0x0510, (short)0x0604, (short)0x0607, (short)0x060a, (short)0x060d, (short)0x0610, (short)0x0700, (short)0x0701, 
				(short)0x0702, (short)0x0704, (short)0x0707, (short)0x070a, (short)0x070d, (short)0x0710, (short)0x0800, (short)0x0803, 
				(short)0x0804, (short)0x0807, (short)0x080a, (short)0x080d, (short)0x0810, (short)0x0900, (short)0x0903, (short)0x0904, 
				(short)0x0907, (short)0x090a, (short)0x090d, (short)0x0910, (short)0x0a00, (short)0x0a03, (short)0x0a04, (short)0x0a10, 
				(short)0x0b00, (short)0x0b03, (short)0x0b10, (short)0x0c00, (short)0x0c03, (short)0x0c10, (short)0x0d00, (short)0x0d10, 
				(short)0x0e00, (short)0x0e10, (short)0x0f01, (short)0x0f10, (short)0x1002, (short)0x1010, (short)0x1103, (short)0x110f, 
				(short)0x1204, (short)0x120e, (short)0x1305, (short)0x1306, (short)0x1307, (short)0x1308, (short)0x1309, (short)0x130a, 
				(short)0x130b, (short)0x130c, (short)0x130d
			), "停止");
		this.mSignMap.put(new TrafficSign(
				(short)0x0004, (short)0x000f, (short)0x0103, (short)0x0104, (short)0x0105, (short)0x010e, (short)0x010f, (short)0x0110, 
				(short)0x0202, (short)0x0203, (short)0x0204, (short)0x0205, (short)0x0206, (short)0x020d, (short)0x020e, (short)0x020f, 
				(short)0x0210, (short)0x0211, (short)0x0301, (short)0x0302, (short)0x0303, (short)0x0304, (short)0x0305, (short)0x0306, 
				(short)0x0307, (short)0x030c, (short)0x030d, (short)0x030e, (short)0x030f, (short)0x0310, (short)0x0311, (short)0x0312, 
				(short)0x0400, (short)0x0401, (short)0x0402, (short)0x0403, (short)0x0404, (short)0x0405, (short)0x0406, (short)0x0407, 
				(short)0x0408, (short)0x040b, (short)0x040c, (short)0x040d, (short)0x040e, (short)0x040f, (short)0x0410, (short)0x0411, 
				(short)0x0412, (short)0x0413, (short)0x0501, (short)0x0502, (short)0x0503, (short)0x0504, (short)0x0505, (short)0x0506, 
				(short)0x0507, (short)0x0508, (short)0x0509, (short)0x050a, (short)0x050b, (short)0x050c, (short)0x050d, (short)0x050e, 
				(short)0x050f, (short)0x0510, (short)0x0511, (short)0x0512, (short)0x0602, (short)0x0603, (short)0x0604, (short)0x0605, 
				(short)0x0606, (short)0x0607, (short)0x0608, (short)0x0609, (short)0x060a, (short)0x060b, (short)0x060c, (short)0x060d, 
				(short)0x060e, (short)0x060f, (short)0x0610, (short)0x0611, (short)0x0703, (short)0x0704, (short)0x0705, (short)0x0706, 
				(short)0x0707, (short)0x0708, (short)0x0709, (short)0x070a, (short)0x070b, (short)0x070c, (short)0x070d, (short)0x070e, 
				(short)0x070f, (short)0x0710, (short)0x0804, (short)0x0805, (short)0x0806, (short)0x0807, (short)0x0808, (short)0x0809, 
				(short)0x080a, (short)0x080b, (short)0x080c, (short)0x080d, (short)0x080e, (short)0x080f, (short)0x0905, (short)0x0906, 
				(short)0x0907, (short)0x0908, (short)0x0909, (short)0x090a, (short)0x090b, (short)0x090c, (short)0x090d, (short)0x090e, 
				(short)0x0a05, (short)0x0a06, (short)0x0a07, (short)0x0a08, (short)0x0a09, (short)0x0a0a, (short)0x0a0b, (short)0x0a0c, 
				(short)0x0a0d, (short)0x0a0e, (short)0x0b04, (short)0x0b05, (short)0x0b06, (short)0x0b07, (short)0x0b08, (short)0x0b09, 
				(short)0x0b0a, (short)0x0b0b, (short)0x0b0c, (short)0x0b0d, (short)0x0b0e, (short)0x0b0f, (short)0x0c03, (short)0x0c04, 
				(short)0x0c05, (short)0x0c06, (short)0x0c07, (short)0x0c08, (short)0x0c09, (short)0x0c0a, (short)0x0c0b, (short)0x0c0c, 
				(short)0x0c0d, (short)0x0c0e, (short)0x0c0f, (short)0x0c10, (short)0x0d02, (short)0x0d03, (short)0x0d04, (short)0x0d05, 
				(short)0x0d06, (short)0x0d07, (short)0x0d08, (short)0x0d09, (short)0x0d0a, (short)0x0d0b, (short)0x0d0c, (short)0x0d0d, 
				(short)0x0d0e, (short)0x0d0f, (short)0x0d10, (short)0x0d11, (short)0x0e01, (short)0x0e02, (short)0x0e03, (short)0x0e04, 
				(short)0x0e05, (short)0x0e06, (short)0x0e07, (short)0x0e08, (short)0x0e09, (short)0x0e0a, (short)0x0e0b, (short)0x0e0c, 
				(short)0x0e0d, (short)0x0e0e, (short)0x0e0f, (short)0x0e10, (short)0x0e11, (short)0x0e12, (short)0x0f00, (short)0x0f01, 
				(short)0x0f02, (short)0x0f03, (short)0x0f04, (short)0x0f05, (short)0x0f06, (short)0x0f07, (short)0x0f08, (short)0x0f0b, 
				(short)0x0f0c, (short)0x0f0d, (short)0x0f0e, (short)0x0f0f, (short)0x0f10, (short)0x0f11, (short)0x0f12, (short)0x0f13, 
				(short)0x1001, (short)0x1002, (short)0x1003, (short)0x1004, (short)0x1005, (short)0x1006, (short)0x1007, (short)0x100c, 
				(short)0x100d, (short)0x100e, (short)0x100f, (short)0x1010, (short)0x1011, (short)0x1012, (short)0x1102, (short)0x1103, 
				(short)0x1104, (short)0x1105, (short)0x1106, (short)0x110d, (short)0x110e, (short)0x110f, (short)0x1110, (short)0x1111, 
				(short)0x1203, (short)0x1204, (short)0x1205, (short)0x120e, (short)0x120f, (short)0x1210, (short)0x1304, (short)0x130f
			), "退出");
		this.mSignMap.put(new TrafficSign(
				(short)0x0008, (short)0x0009, (short)0x000a, (short)0x000b, (short)0x0108, (short)0x0109, (short)0x010a, (short)0x010b, 
				(short)0x0208, (short)0x0209, (short)0x020a, (short)0x020b, (short)0x0300, (short)0x0301, (short)0x0302, (short)0x0303, 
				(short)0x0304, (short)0x0305, (short)0x0308, (short)0x0309, (short)0x030a, (short)0x030b, (short)0x030e, (short)0x030f, 
				(short)0x0310, (short)0x0311, (short)0x0312, (short)0x0313, (short)0x0400, (short)0x0401, (short)0x0402, (short)0x0403, 
				(short)0x0404, (short)0x0405, (short)0x0408, (short)0x0409, (short)0x040a, (short)0x040b, (short)0x040e, (short)0x040f, 
				(short)0x0410, (short)0x0411, (short)0x0412, (short)0x0413, (short)0x0500, (short)0x0501, (short)0x0502, (short)0x0503, 
				(short)0x0504, (short)0x0505, (short)0x0508, (short)0x0509, (short)0x050a, (short)0x050b, (short)0x050e, (short)0x050f, 
				(short)0x0510, (short)0x0511, (short)0x0512, (short)0x0513, (short)0x0600, (short)0x0601, (short)0x0602, (short)0x0603, 
				(short)0x0604, (short)0x0605, (short)0x0608, (short)0x0609, (short)0x060a, (short)0x060b, (short)0x060e, (short)0x060f, 
				(short)0x0610, (short)0x0611, (short)0x0612, (short)0x0613, (short)0x0700, (short)0x0701, (short)0x0702, (short)0x0703, 
				(short)0x0708, (short)0x0709, (short)0x070a, (short)0x070b, (short)0x0710, (short)0x0711, (short)0x0712, (short)0x0713, 
				(short)0x0800, (short)0x0801, (short)0x0802, (short)0x0803, (short)0x0808, (short)0x0809, (short)0x080a, (short)0x080b, 
				(short)0x0810, (short)0x0811, (short)0x0812, (short)0x0813, (short)0x0900, (short)0x0901, (short)0x0902, (short)0x0903, 
				(short)0x0908, (short)0x0909, (short)0x090a, (short)0x090b, (short)0x0910, (short)0x0911, (short)0x0912, (short)0x0913, 
				(short)0x0a00, (short)0x0a01, (short)0x0a02, (short)0x0a03, (short)0x0a08, (short)0x0a09, (short)0x0a0a, (short)0x0a0b, 
				(short)0x0a10, (short)0x0a11, (short)0x0a12, (short)0x0a13, (short)0x0b00, (short)0x0b01, (short)0x0b02, (short)0x0b03, 
				(short)0x0b08, (short)0x0b09, (short)0x0b0a, (short)0x0b0b, (short)0x0b10, (short)0x0b11, (short)0x0b12, (short)0x0b13, 
				(short)0x0c00, (short)0x0c01, (short)0x0c02, (short)0x0c03, (short)0x0c10, (short)0x0c11, (short)0x0c12, (short)0x0c13, 
				(short)0x0d00, (short)0x0d01, (short)0x0d02, (short)0x0d03, (short)0x0d10, (short)0x0d11, (short)0x0d12, (short)0x0d13, 
				(short)0x0e00, (short)0x0e01, (short)0x0e02, (short)0x0e03, (short)0x0e10, (short)0x0e11, (short)0x0e12, (short)0x0e13, 
				(short)0x0f00, (short)0x0f01, (short)0x0f02, (short)0x0f03, (short)0x0f10, (short)0x0f11, (short)0x0f12, (short)0x0f13, 
				(short)0x1000, (short)0x1001, (short)0x1002, (short)0x1003, (short)0x1004, (short)0x1005, (short)0x1006, (short)0x1007, 
				(short)0x1008, (short)0x1009, (short)0x100a, (short)0x100b, (short)0x100c, (short)0x100d, (short)0x100e, (short)0x100f, 
				(short)0x1010, (short)0x1011, (short)0x1012, (short)0x1013, (short)0x1100, (short)0x1101, (short)0x1102, (short)0x1103, 
				(short)0x1104, (short)0x1105, (short)0x1106, (short)0x1107, (short)0x1108, (short)0x1109, (short)0x110a, (short)0x110b, 
				(short)0x110c, (short)0x110d, (short)0x110e, (short)0x110f, (short)0x1110, (short)0x1111, (short)0x1112, (short)0x1113, 
				(short)0x1200, (short)0x1201, (short)0x1202, (short)0x1203, (short)0x1204, (short)0x1205, (short)0x1206, (short)0x1207, 
				(short)0x1208, (short)0x1209, (short)0x120a, (short)0x120b, (short)0x120c, (short)0x120d, (short)0x120e, (short)0x120f, 
				(short)0x1210, (short)0x1211, (short)0x1212, (short)0x1213, (short)0x1300, (short)0x1301, (short)0x1302, (short)0x1303, 
				(short)0x1304, (short)0x1305, (short)0x1306, (short)0x1307, (short)0x1308, (short)0x1309, (short)0x130a, (short)0x130b, 
				(short)0x130c, (short)0x130d, (short)0x130e, (short)0x130f, (short)0x1310, (short)0x1311, (short)0x1312, (short)0x1313
			), "关机");
	}
	
	/**
	 * 询问用户对所处理图片希望使用的分辨率。
	 */
	private void initCameraSize() {
		// 取得所有可用分辨率
		final List<Camera.Size> sizes = this.mPreviewer.getSupportedPreviewSizes();
		// 按从大到小排序所有可用分辨率
		Collections.sort(sizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Size lhs, Size rhs) {
				return -lhs.width * lhs.height + rhs.width * rhs.height;
			}
		});
		// 从保存内容中取出选择的位置
		SharedPreferences pref = this.getPreferences(MODE_PRIVATE);
		this.mSelectedSizeIndex = pref.getInt(SEL_SIZE_KEY, -1);
		// 创建用来显示的数组
		String[] strSizes = new String[sizes.size()];
		int i = 0;
		for (Camera.Size size : sizes) {
			// 计算默认选定分辨率的位置
			if (mSelectedSizeIndex < 0 && i > 1 && size.width * size.height < MIN_RES) {
				mSelectedSizeIndex = i - 1;
			}
			strSizes[i++] = String.format("%d x %d", size.width, size.height);
		}
		// 确定默认分辨率
		final Camera.Size defaultSize = sizes.get(mSelectedSizeIndex);
		// 为Spinner创建适配器
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strSizes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 取得Spinner
		Spinner spinner = (Spinner) this.findViewById(R.id.res_list);
		// 设置适配器，以设置数据
		spinner.setAdapter(adapter);
		// 设置选择时的事件
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mSelectedSizeIndex = position;
				// 取得分辨率
				Camera.Size size = sizes.get(position);
				// 设置分辨率
				setSize(size);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				mSelectedSizeIndex = -1;
				// 什么都没选的时候，用默认分辨率
				setSize(defaultSize);
			}
		});
		// 默认选择默认分辨率
		spinner.setSelection(mSelectedSizeIndex);
	}
	
	/**
	 * 根据选取的相机分辨率来设置各种大小尺寸
	 * @param size 分辨率大小
	 */
	private void setSize(Camera.Size size) {
		// 设置预览分辨率
		mPreviewer.setPreviewSize(size);
		// 由于手机默认的相机方向是横向，旋转90度。
		mPreviewer.setOrientation(CameraPreviewer.Orientation.Portraite);
		// 设置预览区显示大小，由于旋转90度，长宽颠倒；同时设置大小为实际图像大小的1/16
		mPreviewer.setDisplaySize(size.height >> 2, size.width >> 2);
		// 设置信息显示区大小
		mImageHolder.setFixedSize(size.height, size.width);
		// 设置待检测图像大小
		mDetector.setImageSize(size.height, size.width);
		// 设置旋转90度
		mDetector.setRotation(TrafficSignDetector.Rotation.Degree90);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化控件
		this.initCtrls();
		this.initComponents();
		// 初始化网络客户端
		this.initNetClient();
	}
	/**
	 * 初始化控件
	 */
	private void initCtrls() {
		this.mCover = (ViewGroup) this.findViewById(R.id.cover);
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
			this.mPreviewer.startCameraPreview();
			break;
		case Disconnected:
			this.mCover.setVisibility(View.VISIBLE);
			this.mPreviewer.stopCameraPreview();
			break;
		case Connecting:
			enableBluetooth();
			break;
		}
		this.invalidateOptionsMenu();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
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

	@Override
	protected void onPause() {
		super.onPause();
		this.mPreviewer.stopCameraPreview();
		SharedPreferences.Editor editor = this.getPreferences(MODE_PRIVATE).edit();
		editor.putInt(SEL_SIZE_KEY, this.mSelectedSizeIndex);
		editor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.mBtConnectState == BtConnectState.Connected) {
			this.mPreviewer.startCameraPreview();
		}
	}
}
