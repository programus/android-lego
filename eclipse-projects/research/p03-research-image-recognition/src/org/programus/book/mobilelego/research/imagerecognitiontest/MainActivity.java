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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class MainActivity extends Activity {
	
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
		
		this.initKnownSign();
		this.askCameraSize();
	}
	
	/**
	 * 初始化已知路标与名称对照表
	 */
	private void initKnownSign() {
		this.mSignMap.put(new TrafficSign(
				"          .         " + 
				"         ...        " + 
				"        .....       " + 
				"       .......      " + 
				"      .........     " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       " + 
				"        .....       "
			), "前进");
		this.mSignMap.put(new TrafficSign(
				"    .               " + 
				"   ..               " + 
				"  ................  " + 
				" .................. " + 
				"...................." + 
				" ..................." + 
				"  .................." + 
				"   ..          ....." + 
				"    .          ....." + 
				"               ....." + 
				"               ....." + 
				"               ....." + 
				"               ....." + 
				"               ....." + 
				"               ....." + 
				"               ....." + 
				"               ....." + 
				"               ....." + 
				"               ....." + 
				"               ....."
			), "左转");
		this.mSignMap.put(new TrafficSign(
				"               .    " + 
				"               ..   " + 
				"  ................  " + 
				" .................. " + 
				"...................." + 
				"................... " + 
				"..................  " + 
				".....          ..   " + 
				".....          .    " + 
				".....               " + 
				".....               " + 
				".....               " + 
				".....               " + 
				".....               " + 
				".....               " + 
				".....               " + 
				".....               " + 
				".....               " + 
				".....               " + 
				".....               "
			), "右转");
		this.mSignMap.put(new TrafficSign(
				"       ........     " + 
				"      ..........    " + 
				"     ............   " + 
				"    ..............  " + 
				"    ......  ......  " + 
				"    .....    .....  " + 
				"    .....    .....  " + 
				"    .....    .....  " + 
				"    .....    .....  " + 
				"    .....    .....  " + 
				"    .....    .....  " + 
				"    .....    .....  " + 
				"    .....    .....  " + 
				"    .....    .....  " + 
				"    .....    .....  " + 
				"    .....  ........." + 
				"    .....   ....... " + 
				"    .....    .....  " + 
				"    .....     ...   " + 
				"    .....      .    "
			), "掉头");
		this.mSignMap.put(new TrafficSign(
				"        ..          " + 
				"       .  .         " + 
				"     ...  ...       " + 
				"    .  .  .  .      " + 
				"    .  .  .  ...    " + 
				"    .  .  .  .  .   " + 
				"    .  .  .  .  .   " + 
				"... .  .  .  .  .   " + 
				".  ..  .  .  .  .   " + 
				".  ..  .  .  .  .   " + 
				".  ..           .   " + 
				".  .            .   " + 
				".  .            .   " + 
				".               .   " + 
				".               .   " + 
				" .              .   " + 
				"  .             .   " + 
				"   .           .    " + 
				"    .         .     " + 
				"     .........      "
			), "停止");
		this.mSignMap.put(new TrafficSign(
				"    .          .    " + 
				"   ...        ...   " + 
				"  .....      .....  " + 
				" .......    ....... " + 
				".........  ........." + 
				" .................. " + 
				"  ................  " + 
				"   ..............   " + 
				"    ............    " + 
				"     ..........     " + 
				"     ..........     " + 
				"    ............    " + 
				"   ..............   " + 
				"  ................  " + 
				" .................. " + 
				".........  ........." + 
				" .......    ....... " + 
				"  .....      .....  " + 
				"   ...        ...   " + 
				"    .          .    "
			), "退出");
		this.mSignMap.put(new TrafficSign(
				"        ....        " + 
				"        ....        " + 
				"        ....        " + 
				"......  ....  ......" + 
				"......  ....  ......" + 
				"......  ....  ......" + 
				"......  ....  ......" + 
				"....    ....    ...." + 
				"....    ....    ...." + 
				"....    ....    ...." + 
				"....    ....    ...." + 
				"....    ....    ...." + 
				"....            ...." + 
				"....            ...." + 
				"....            ...." + 
				"....            ...." + 
				"...................." + 
				"...................." + 
				"...................." + 
				"...................."
			), "关机");
	}
	
	/**
	 * 询问用户对所处理图片希望使用的分辨率。
	 */
	private void askCameraSize() {
		// 取得所有可用分辨率
		final List<Camera.Size> sizes = this.mPreviewer.getSupportedPreviewSizes();
		String[] strSizes = new String[sizes.size()];
		int i = 0;
		for (Camera.Size size : sizes) {
			strSizes[i++] = String.format("%d x %d", size.width, size.height);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog dialog = builder.setTitle("Select size").setItems(strSizes, new DialogInterface.OnClickListener() {
			/**
			 * 选择后的处理
			 */
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 取得分辨率
				Camera.Size size = sizes.get(which);
				// 设置预览分辨率
				mPreviewer.setPreviewSize(size);
				// 开始预览
				mPreviewer.startCameraPreview();
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
				// 设置最小检测宽度
				mDetector.setMinUnit(5);
			}
		}).create();
		
		dialog.show();
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
		super.onPause();
		this.mPreviewer.stopCameraPreview();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.mPreviewer.startCameraPreview();
	}
    
}
