package org.programus.book.mobilelego.research.imagerecognitiontest;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
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
	
	private Camera.PreviewCallback mCamPrevCallback = new Camera.PreviewCallback() {
		
		private long time;
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			long nt = System.currentTimeMillis();
			System.out.println(1000. / (nt - time));
			time = nt;
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
			} finally {
				this.mImageHolder.unlockCanvasAndPost(canvas);
			}	
		}
	}
	
	private void drawDetectedSign(TrafficSignDetector detector) {
		Canvas canvas = this.mSignHolder.lockCanvas();
		if (canvas != null) {
			try {
				detector.getDetectedSign().draw(canvas, !detector.isSignDetected());
			} finally {
				this.mSignHolder.unlockCanvasAndPost(canvas);
			}
		}
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
		
		this.mDetector = new TrafficSignDetector();
		this.mSign = new TrafficSign();
		this.mDetector.setSign(this.mSign);
		this.askCameraSize();
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
