package org.programus.book.mobilelego.research.imagerecognitiontest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class MainActivity extends Activity {
	
	private SurfaceView mCameraPreviewView;
	
	private CameraPreviewer mPreviewer;
	
	private SurfaceHolder mImageHolder;
	
	private SurfaceView mSignView;
	private SurfaceHolder mSignHolder;
	
	private TrafficSignDetector mDetector;
	private TrafficSign mSign;
	
	private Paint mPaint;
	private float mFps;
	
	private Map<TrafficSign, String> mSignMap = new HashMap<TrafficSign, String>();
	
	private Camera.PreviewCallback mCamPrevCallback = new Camera.PreviewCallback() {
		
		private long time;
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			long now = System.currentTimeMillis();
			mFps = 1000f / (now - time);
			time = now;
			if (mDetector != null) {
				mDetector.updateRawBuffer(data, TrafficSignDetector.Rotation.Degree90);
				mDetector.detectTrafficSign();
				camera.addCallbackBuffer(data);
				drawMonoImage(mDetector);
				drawDetectedSign(mDetector);
			} else {
				camera.addCallbackBuffer(data);
			}
		}
	};
	
	private void drawMonoImage(TrafficSignDetector sign) {
		Canvas canvas = this.mImageHolder.lockCanvas();
		if (canvas != null) {
			try {
				sign.drawMonoImage(canvas);
				canvas.drawText(String.format("FPS: %.2f", mFps), canvas.getWidth() >> 1, canvas.getHeight(), mPaint);
			} finally {
				this.mImageHolder.unlockCanvasAndPost(canvas);
			}	
		}
	}
	
	private void drawDetectedSign(TrafficSignDetector detector) {
		Canvas canvas = this.mSignHolder.lockCanvas();
		if (canvas != null) {
			try {
				TrafficSign sign = detector.getDetectedSign();
				sign.draw(canvas, !detector.isSignDetected());
				this.drawKnownSign(canvas, sign);
			} finally {
				this.mSignHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	private void drawKnownSign(Canvas canvas, TrafficSign sign) {
		String signName = this.mSignMap.get(sign);
		if (signName == null) {
			signName = "未知";
		}
		
		canvas.drawText(signName, canvas.getWidth() >> 1, canvas.getHeight() >> 1, mPaint);
	}

	private void initComponents() {
		this.mCameraPreviewView = (SurfaceView) this.findViewById(R.id.camera_preview);
		this.mPreviewer = new CameraPreviewer();
		this.mPreviewer.setPreviewView(this.mCameraPreviewView);
		this.mPreviewer.setPreviewCallback(this.mCamPrevCallback);
		SurfaceView imageView = (SurfaceView) this.findViewById(R.id.monochrome_image);
		this.mImageHolder = imageView.getHolder();
		this.mSignView = (SurfaceView) this.findViewById(R.id.sign);
		this.mSignHolder = this.mSignView.getHolder();
		this.mSignHolder.setFixedSize(TrafficSign.SIGN_EDGE_LEN * TrafficSignDetector.BLOCK_SIZE, TrafficSign.SIGN_EDGE_LEN * TrafficSignDetector.BLOCK_SIZE);
		
		this.mPaint = new Paint();
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(50);
		mPaint.setColor(0x7f00ff00);
		
		this.mDetector = new TrafficSignDetector();
		this.mSign = new TrafficSign();
		this.mDetector.setSign(this.mSign);
		this.initKnownSign();
		this.askCameraSize();
	}
	
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
		
		for (Map.Entry<TrafficSign, String> e : this.mSignMap.entrySet()) {
			System.out.printf("%s: %d\n", e.getValue(), e.getKey().hashCode());
		}
	}
	
	private void askCameraSize() {
		final List<Camera.Size> sizes = this.mPreviewer.getSupportedPreviewSizes();
		String[] strSizes = new String[sizes.size()];
		int i = 0;
		for (Camera.Size size : sizes) {
			strSizes[i++] = String.format("%d x %d", size.width, size.height);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog dialog = builder.setTitle("Select size").setItems(strSizes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Camera.Size size = sizes.get(which);
				mPreviewer.setPreviewSize(size);
				mPreviewer.startCameraPreview();
				mPreviewer.setOrientation(CameraPreviewer.Orientation.Portraite);
				mPreviewer.setDisplaySize(size.height >> 2, size.width >> 2);
				mImageHolder.setFixedSize(size.height, size.width);
				mDetector.setImageSize(size.height, size.width);
				mDetector.setMinUnit(5);
			}
		}).create();
		Log.d(this.getClass().getName(), "Created size select dialog.");
		dialog.show();
		Log.d(this.getClass().getName(), "Displayed size select dialog.");
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        this.initComponents();
    }

	@Override
	protected void onPause() {
		Log.d(this.getClass().getSimpleName(), "Paused");
		super.onPause();
		this.mPreviewer.stopCameraPreview();
	}

	@Override
	protected void onResume() {
		Log.d(this.getClass().getSimpleName(), "Resumed");
		super.onResume();
		this.mPreviewer.startCameraPreview();
	}
    
}
