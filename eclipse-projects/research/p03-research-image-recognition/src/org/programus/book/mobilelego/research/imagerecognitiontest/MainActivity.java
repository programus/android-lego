package org.programus.book.mobilelego.research.imagerecognitiontest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.programus.book.mobilelego.research.imagerecognitiontest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private final static int CORNER_COUNT = 4;
	
	private Camera mCamera;
	private Camera.Size mCamSize;
	
	private SurfaceView mCameraPreviewView;
	private SurfaceHolder mCameraPreviewHolder;
	
	private SurfaceView mImageView;
	private SurfaceHolder mImageHolder;
	
	private ImageView mPatternView;
	
	private TextView mSimilar;
	
	private byte[] mPatternData;
	
	private boolean mTakingPic;
	private boolean mFocused;
	
	private SignDetector mSignDetector = new SignDetector(3);
	
	private SurfaceHolder.Callback holderCallback = new SurfaceHolder.Callback() {
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			stopCameraPreview();
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			startCameraPreview();
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (holder.getSurface() == null) {
				return;
			} else {
				stopCameraPreview();
				startCameraPreview();
			}
		}
	};
	
	private Camera.PreviewCallback mCamPrevCallback = new Camera.PreviewCallback() {
		private boolean[] sign = new boolean[20 * 20];
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			byte[] monoData = getMonoImageData(data, mCamSize.width, mCamSize.height);
			Bitmap bmp = getMonoImage(monoData, mCamSize.width, mCamSize.height);
			List<Point> corners = mSignDetector.findPattern(monoData, mCamSize.width, mCamSize.height, 0, 0);
			List<Point> samples = mSignDetector.getSamples(corners);
			if (mSignDetector.getSign(monoData, mCamSize.width, mCamSize.height, sign)) {
				drawCapturedPattern(sign, 20, 10);
			}
			// List<Point> corners = getCorners(monoData, mCamSize.width, mCamSize.height, 20, 2);
			drawMonoImage(bmp, corners, samples);
			if (mTakingPic && mFocused) {
				capturePattern(monoData, bmp);
				mTakingPic = false;
			} else {
				bmp.recycle();
			}
			mFocused = false;
			
			if (mPatternData != null) {
				mSimilar.setText(String.valueOf(similarDegree(monoData, mPatternData)));
			}
		}
	};
	
	private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if (success) {
				mFocused = true;
                camera.cancelAutoFocus();
			} else {
				camera.autoFocus(this);
			}
		}
	};
	
	private void drawCapturedPattern(boolean[] sign, int len, int size) {
		int[] colors = new int[sign.length * size * size];
		Arrays.fill(colors, Color.WHITE);
		int w = len * size;
		for (int y = 0; y < len; y++) {
			int base = y * len;
			for (int x = 0; x < len; x++) {
				int index = base + x;
				if (sign[index]) {
					for (int yy = y * size; yy < (y + 1) * size; yy++) {
						Arrays.fill(colors, yy * w + x * size, yy * w + (x + 1) * size, Color.BLACK);
					}
				}
			}
		}
		Bitmap bm = Bitmap.createBitmap(colors, w, w, Bitmap.Config.ARGB_8888);
		this.mPatternView.setImageBitmap(bm);
		ViewGroup.LayoutParams lp = this.mPatternView.getLayoutParams();
		lp.width = bm.getWidth();
		lp.height = bm.getHeight();
		this.mPatternView.setLayoutParams(lp);
	}

	private byte[] getMonoImageData(byte[] data, int width, int height) {
		int size = width * height;
		double avg = 0;
		int t = 0;
		for (byte b : data) {
			int v = b & 0xff;
			avg += (v - avg) / ++t;
		}
		
		byte[] pixels = new byte[size];
		for (int i = 0; i < size; i++) {
			int v = data[i] & 0xff;
			pixels[i] = (byte) (v < avg ? 0 : 0xff);
		}
		
		return pixels;
	}
	
	private Bitmap getMonoImage(byte[] monoData, int width, int height) {
		int[] pixels = new int[monoData.length];
		for (int i = 0; i < monoData.length; i++) {
			pixels[i] = monoData[i] == 0 ? 0xff000000 : 0xffffffff;
		}
		
		return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
	}
	
	private double similarDegree(byte[] mono, byte[] pattern) {
		int n = Math.min(mono.length, pattern.length);
		int block = n / 1000;
		int count = 0;
		int cc = 0;
		for (int i = 0; i < n; i++) {
			if (mono[i] == pattern[i]) {
				cc++;
			} else {
				if (cc >= block) {
					count += cc;
				}
				cc = 0;
			}
		}
		return (double) count / (double) n;
	}
	
	@SuppressWarnings("deprecation")
	private void initComponents() {
		this.mCameraPreviewView = (SurfaceView) this.findViewById(R.id.camera_preview);
		this.mCameraPreviewHolder = this.mCameraPreviewView.getHolder();
		this.mCameraPreviewHolder.addCallback(holderCallback);
		this.mCameraPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		this.mImageView = (SurfaceView) this.findViewById(R.id.monochrome_image);
		this.mImageHolder = this.mImageView.getHolder();
		this.mImageHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
		this.mPatternView = (ImageView) this.findViewById(R.id.pattern_image);
		this.mSimilar = (TextView) this.findViewById(R.id.similar);
	}
	
	private void startCameraPreview() {
		if (this.mCamera == null) {
			this.mCamera = Camera.open();
			Camera.Parameters params = this.mCamera.getParameters();
            if (this.mCamSize == null) {
                this.askCameraSize(params);
                this.stopCameraPreview();
            } else {
                this.setupCameraParams(params);
                this.mCamera.setParameters(params);
                try {
                    this.mCamera.setPreviewDisplay(mCameraPreviewHolder);
                } catch (IOException e) {
                    Log.d(this.getClass().getName(), "Error when set preview display\n", e);
                }
                this.mCamera.setPreviewCallback(mCamPrevCallback);
                this.mCamera.startPreview();
                this.resetSurfaces();
            }
		}
	}
	
	private void stopCameraPreview() {
		if (this.mCamera != null) {
			Camera cam = this.mCamera;
			this.mCamera = null;
			cam.stopPreview();
			cam.setPreviewCallback(null);
			cam.release();
		}
	}
	
	private void setupCameraParams(final Camera.Parameters params) {
		List<String> focusModes = params.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		}
		params.setPreviewFormat(ImageFormat.NV21);
		params.setPictureFormat(ImageFormat.NV21);
		params.setPreviewSize(this.mCamSize.width, this.mCamSize.height);
		params.setPictureSize(this.mCamSize.width, this.mCamSize.height);
	}
	
	private void resetSurfaces() {
		if (this.mCamSize != null) {
			this.mCameraPreviewHolder.setFixedSize(this.mCamSize.width, this.mCamSize.height);
			this.mImageHolder.setFixedSize(this.mCamSize.width, this.mCamSize.height);
		}
	}
	
	private void askCameraSize(Camera.Parameters params) {
		final List<Camera.Size> sizes = params.getSupportedPreviewSizes();
		String[] strSizes = new String[sizes.size()];
		int i = 0;
		for (Camera.Size size : sizes) {
			strSizes[i++] = String.format("%d x %d", size.width, size.height);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog dialog = builder.setTitle("Select size").setItems(strSizes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCamSize = sizes.get(which);
				startCameraPreview();
			}
		}).create();
		Log.d(this.getClass().getName(), "Created size select dialog.");
		dialog.show();
		Log.d(this.getClass().getName(), "Displayed size select dialog.");
	}
	
	private void drawMonoImage(Bitmap bmp, List<Point> corners, List<Point> samples) {
		Canvas canvas = this.mImageHolder.lockCanvas();
		if (canvas != null) {
			canvas.drawColor(Color.BLACK);
			if (bmp != null) {
				canvas.drawBitmap(bmp, 0, 0, null);
			}
			if (corners != null && corners.size() == CORNER_COUNT) {
				Log.d("CORNER", corners.toString());
				drawCorners(canvas, corners);
			}
			if (samples != null) {
				drawSamples(canvas, samples);
			}
            this.mImageHolder.unlockCanvasAndPost(canvas);
		}
	}
	
	private void drawSamples(Canvas canvas, List<Point> samples) {
		Paint p = new Paint();
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(2);
		p.setColor(Color.GREEN);
		for (Point pt : samples) {
			canvas.drawPoint(pt.x, pt.y, p);
		}
	}
	
	private void drawCorners(Canvas canvas, List<Point> corners) {
		if (canvas != null) {
			int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN};
			Paint p = new Paint();
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(4);
			int i = 0;
			for (Point corner : corners) {
				p.setColor(colors[i++ % colors.length]);
				canvas.drawPoint(corner.x, corner.y, p);
			}
//			float[] points = new float[corners.size() * 2];
//			int i = 0;
//			for (Point corner : corners) {
//				points[i++] = corner.x;
//				points[i++] = corner.y;
//			}
////			canvas.drawLines(points, p);
//			canvas.drawPoints(points, p);
			p.setColor(Color.GREEN);
			p.setStrokeWidth(1);
			canvas.drawText(String.format("P: %d", corners.size()), 100, 100, p);
		}
	}
	
	private void capturePattern(byte[] monoData, Bitmap bmp) {
		this.mPatternData = monoData;
		this.mPatternView.setImageBitmap(bmp);
		ViewGroup.LayoutParams lp = this.mPatternView.getLayoutParams();
		lp.width = bmp.getWidth();
		lp.height = bmp.getHeight();
		this.mPatternView.setLayoutParams(lp);
	}
	
	private void capturePic() {
        this.mTakingPic = true;
        this.mCamera.autoFocus(mAutoFocusCallback);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        this.initComponents();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			this.capturePic();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.stopCameraPreview();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.startCameraPreview();
	}
    
}
