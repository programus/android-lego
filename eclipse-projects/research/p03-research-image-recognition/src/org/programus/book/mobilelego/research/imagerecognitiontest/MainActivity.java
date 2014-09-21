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
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends Activity {
	
	private SurfaceView mCameraPreviewView;
	
	private CameraPreviewer mPreviewer;
	
	private SurfaceHolder mImageHolder;
	
	private SurfaceView mSignView;
	private SurfaceHolder mSignHolder;
	
	private TrafficSign mSign;
	
	private Camera.PreviewCallback mCamPrevCallback = new Camera.PreviewCallback() {
//		private boolean[] sign = new boolean[20 * 20];
//		@Override
//		public void onPreviewFrame(byte[] data, Camera camera) {
//			Camera.Size size = mPreviewer.getPreviewSize();
//			byte[] monoData = getMonoImageData(data, size.width, size.height);
//			Bitmap bmp = getMonoImage(monoData, size.width, size.height);
//			List<Point> corners = mSignDetector.findPattern(monoData, size.width, size.height, 0, 0);
//			List<Point> samples = mSignDetector.getSamples(corners);
//			if (mSignDetector.getSign(monoData, size.width, size.height, sign)) {
//				drawCapturedPattern(sign, 20, 10);
//			}
//			// List<Point> corners = getCorners(monoData, mCamSize.width, mCamSize.height, 20, 2);
//			drawMonoImage(bmp, corners, samples);
//			if (mTakingPic && mFocused) {
//				capturePattern(monoData, bmp);
//				mTakingPic = false;
//			} else {
//				bmp.recycle();
//			}
//			mFocused = false;
//			
//			if (mPatternData != null) {
//				mSimilar.setText(String.valueOf(similarDegree(monoData, mPatternData)));
//			}
//		}
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (mSign != null) {
				mSign.updateRawBuffer(data, TrafficSign.Rotation.Degree90);
				if (mSign.detectTrafficSign()) {
					mSignView.setVisibility(View.VISIBLE);
					drawDetectedSign(mSign);
				} else {
					mSignView.setVisibility(View.GONE);
				}
				drawMonoImage(mSign);
			}
		}
	};
	
	private void drawMonoImage(TrafficSign sign) {
		Canvas canvas = this.mImageHolder.lockCanvas();
		if (canvas != null) {
			try {
				sign.drawMonoImage(canvas);
			} finally {
				this.mImageHolder.unlockCanvasAndPost(canvas);
			}	
		}
	}
	
	private void drawDetectedSign(TrafficSign sign) {
		Canvas canvas = this.mSignHolder.lockCanvas();
		if (canvas != null) {
			try {
				sign.drawDetectedSign(canvas);
			} finally {
				this.mSignHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
//	
//	private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
//		@Override
//		public void onAutoFocus(boolean success, Camera camera) {
//			if (success) {
//				mFocused = true;
//                camera.cancelAutoFocus();
//			} else {
//				camera.autoFocus(this);
//			}
//		}
//	};
//	
//	private void drawCapturedPattern(boolean[] sign, int len, int size) {
//		int[] colors = new int[sign.length * size * size];
//		Arrays.fill(colors, Color.WHITE);
//		int w = len * size;
//		for (int y = 0; y < len; y++) {
//			int base = y * len;
//			for (int x = 0; x < len; x++) {
//				int index = base + x;
//				if (sign[index]) {
//					for (int yy = y * size; yy < (y + 1) * size; yy++) {
//						Arrays.fill(colors, yy * w + x * size, yy * w + (x + 1) * size, Color.BLACK);
//					}
//				}
//			}
//		}
//		Bitmap bm = Bitmap.createBitmap(colors, w, w, Bitmap.Config.ARGB_8888);
//		this.mSignView.setImageBitmap(bm);
//		ViewGroup.LayoutParams lp = this.mSignView.getLayoutParams();
//		lp.width = bm.getWidth();
//		lp.height = bm.getHeight();
//		this.mSignView.setLayoutParams(lp);
//	}
//
//	private byte[] getMonoImageData(byte[] data, int width, int height) {
//		int size = width * height;
//		double avg = 0;
//		int t = 0;
//		for (byte b : data) {
//			int v = b & 0xff;
//			avg += (v - avg) / ++t;
//		}
//		
//		byte[] pixels = new byte[size];
//		for (int i = 0; i < size; i++) {
//			int v = data[i] & 0xff;
//			pixels[i] = (byte) (v < avg ? 0 : 0xff);
//		}
//		
//		return pixels;
//	}
//	
//	private Bitmap getMonoImage(byte[] monoData, int width, int height) {
//		int[] pixels = new int[monoData.length];
//		for (int i = 0; i < monoData.length; i++) {
//			pixels[i] = monoData[i] == 0 ? 0xff000000 : 0xffffffff;
//		}
//		
//		return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
//	}
//	
//	private double similarDegree(byte[] mono, byte[] pattern) {
//		int n = Math.min(mono.length, pattern.length);
//		int block = n / 1000;
//		int count = 0;
//		int cc = 0;
//		for (int i = 0; i < n; i++) {
//			if (mono[i] == pattern[i]) {
//				cc++;
//			} else {
//				if (cc >= block) {
//					count += cc;
//				}
//				cc = 0;
//			}
//		}
//		return (double) count / (double) n;
//	}
//	
	private void initComponents() {
		this.mCameraPreviewView = (SurfaceView) this.findViewById(R.id.camera_preview);
		this.mPreviewer = new CameraPreviewer();
		this.mPreviewer.setPreviewView(this.mCameraPreviewView);
		this.mPreviewer.setPreviewCallback(this.mCamPrevCallback);
		SurfaceView imageView = (SurfaceView) this.findViewById(R.id.monochrome_image);
		this.mImageHolder = imageView.getHolder();
		this.mSignView = (SurfaceView) this.findViewById(R.id.sign);
		this.mSignHolder = this.mSignView.getHolder();
		
		this.mSign = new TrafficSign();
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
				mSign.setImageSize(size.height, size.width);
			}
		}).create();
		Log.d(this.getClass().getName(), "Created size select dialog.");
		dialog.show();
		Log.d(this.getClass().getName(), "Displayed size select dialog.");
	}
	
//	private void drawMonoImage(Bitmap bmp, List<Point> corners, List<Point> samples) {
//		Canvas canvas = this.mImageHolder.lockCanvas();
//		if (canvas != null) {
//			canvas.drawColor(Color.BLACK);
//			if (bmp != null) {
//				canvas.drawBitmap(bmp, 0, 0, null);
//			}
//			if (corners != null && corners.size() == CORNER_COUNT) {
//				Log.d("CORNER", corners.toString());
//				drawCorners(canvas, corners);
//			}
//			if (samples != null) {
//				drawSamples(canvas, samples);
//			}
//            this.mImageHolder.unlockCanvasAndPost(canvas);
//		}
//	}
//	
//	private void drawSamples(Canvas canvas, List<Point> samples) {
//		Paint p = new Paint();
//		p.setStyle(Paint.Style.STROKE);
//		p.setStrokeWidth(2);
//		p.setColor(Color.GREEN);
//		for (Point pt : samples) {
//			canvas.drawPoint(pt.x, pt.y, p);
//		}
//	}
//	
//	private void drawCorners(Canvas canvas, List<Point> corners) {
//		if (canvas != null) {
//			int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN};
//			Paint p = new Paint();
//			p.setStyle(Paint.Style.STROKE);
//			p.setStrokeWidth(4);
//			int i = 0;
//			for (Point corner : corners) {
//				p.setColor(colors[i++ % colors.length]);
//				canvas.drawPoint(corner.x, corner.y, p);
//			}
////			float[] points = new float[corners.size() * 2];
////			int i = 0;
////			for (Point corner : corners) {
////				points[i++] = corner.x;
////				points[i++] = corner.y;
////			}
//////			canvas.drawLines(points, p);
////			canvas.drawPoints(points, p);
//			p.setColor(Color.GREEN);
//			p.setStrokeWidth(1);
//			canvas.drawText(String.format("P: %d", corners.size()), 100, 100, p);
//		}
//	}
//	
//	private void capturePattern(byte[] monoData, Bitmap bmp) {
//		this.mPatternData = monoData;
//		this.mSignView.setImageBitmap(bmp);
//		ViewGroup.LayoutParams lp = this.mSignView.getLayoutParams();
//		lp.width = bmp.getWidth();
//		lp.height = bmp.getHeight();
//		this.mSignView.setLayoutParams(lp);
//	}
//	
//	private void capturePic() {
//        this.mTakingPic = true;
////        this.mCamera.autoFocus(mAutoFocusCallback);
//	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        this.initComponents();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.action_settings:
//			this.capturePic();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

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
