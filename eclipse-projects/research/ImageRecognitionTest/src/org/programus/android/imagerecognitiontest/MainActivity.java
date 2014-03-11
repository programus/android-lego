package org.programus.android.imagerecognitiontest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
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
	
	private final static double SQRT_2 = Math.sqrt(2);
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
	
	private Bitmap mCalibratePattern;
	
	private boolean mTakingPic;
	private boolean mFocused;
	
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
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			byte[] monoData = getMonoImageData(data, mCamSize.width, mCamSize.height);
			Bitmap bmp = getMonoImage(monoData, mCamSize.width, mCamSize.height);
			drawMonoImage(bmp);
			List<Point> corners = getCorners(monoData, mCamSize.width, mCamSize.height, 20, 2);
			if (corners != null) {
				Log.d("CORNER", corners.toString());
				drawCorners(corners);
			}
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
	
	private Bitmap getCalibrateBm(int r) {
		Bitmap bm = Bitmap.createBitmap((r + 1) * 2, (r + 1) * 2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);
		canvas.drawColor(Color.WHITE);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		canvas.drawCircle(r + 1, r + 1, r, paint);
		
		return bm;
	}
	
	private List<Point> getCorners(byte[] data, int w, int h, int size, int tolerance, Bitmap calibrate) {
		int[] cal = new int[calibrate.getWidth() * calibrate.getHeight()];
		calibrate.getPixels(cal, 0, calibrate.getWidth(), 0, 0, calibrate.getWidth(), calibrate.getHeight());
		List<Point> list = new ArrayList<Point>(CORNER_COUNT);
		int stepX = 1;
		int stepY = 1;
		for (int y = 0; y < h; y += stepY) {
			for (int x = 0; x < w; x += stepX) {
				stepX = stepY = 1;
				if (match(data, x, y, w, h, cal, calibrate.getWidth(), calibrate.getHeight())) {
					int px = x + calibrate.getWidth() / 2;
					int py = y + calibrate.getHeight() / 2;
					list.add(new Point(px, py));
					stepX = calibrate.getWidth();
					stepY = calibrate.getHeight();
				}
			}
		}
		return list.size() == CORNER_COUNT ? list : null;
	}
	
	private boolean match(byte[] data, int x, int y, int w, int h, int[] cal, int cw, int ch) {
		int[] res = new int[cal.length];
		for (int cy = 0; cy < ch; cy++) {
			for (int cx = 0; cx < cw; cx++) {
				int d = this.getPixel(data, x + cx, y + cy, w, h) & 0xff;
				int index = cx + cy * cw;
				res[index] = cal[index] ^ d;
			}
		}
		int r = cw / 2 - 1;
		for (int cy = r / 4; cy < r - r / 4; cy++) {
			for (int cx = r / 4; cx < r - r / 4; cx++) {
				if (res[cx + cy * cw] != 0) {
					return false;
				}
			}
		}
		int[] indice = new int[] {
			1 + cw, 
			2 * cw - 2,
			1 + cw * (ch - 2), 
			(ch - 1) * cw - 2
		};
		for (int index : indice) {
			if (res[index] != 0) {
				return false;
			}
		}
		return true;
	}
	
	private List<Point> getCorners(byte[] data, int w, int h, int size, int tolerance) {
		List<Point> list = new ArrayList<Point>(CORNER_COUNT);
		int stepX = 1;
		int stepY = 1;
		Log.d("CORNER", "====");
//		for (int y = 0; y < h; y += stepY) {
//			stepY = 1;
//			for (int x = 0; x < w; x += stepX) {
//				if (this.isCalibratePoint(data, x, y, w, h, size, tolerance)) {
//					list.add(new Point(x, y));
//					stepX = size;
//				}
//			}
//		}

		int r = size / 2;
		for (int y = r; y < h - r; y += stepY) {
			int countB = 0;
			int countW = 0;
			stepY = 1;
			for (int x = r; x < w - r; x += stepX) {
				int p = data[x + w * y];
				stepX = 1;
				if (p == 0) {
					countB++;
				} else {
					if (countB > 0) {
                        Log.d("CORNER", String.format("B->W: (%d, %d)/%d", x - countB / 2, y, countB));
					}
					if (Math.abs(countB - size) < tolerance){
                        int px = x - countB / 2;
                        int py = y;
                        Log.d("CORNER", String.format("Candidate: %d, %d", px, py));
                        if (this.isCalibratePoint(data, px, py, w, h, size, tolerance)) {
                            list.add(new Point(px, py));
                            stepX = size;
                        }
					}
					if (countB > 0) {
						countW = 0;
					}
					countB = 0;
					countW++;
				}
			}
		}
		return list.size() > 0 ? list : null;
	}
	
	private boolean isCalibratePoint(byte[] data, int x, int y, int w, int h, int size, int tolerance) {
		float r = size / 2.0f;
		PointF[] ps = new PointF[] {
			new PointF(-1, 0),
			new PointF(1, 0),
			new PointF(0, -1),
			new PointF(0, 1),
			new PointF(-0.8f, -0.6f),
			new PointF(-0.8f, 0.6f),
			new PointF(0.8f, -0.6f),
			new PointF(0.8f, 0.6f),
			new PointF(-0.6f, -0.8f),
			new PointF(-0.6f, 0.8f),
			new PointF(0.6f, -0.8f),
			new PointF(0.6f, 0.8f),
		};
		
		StringBuilder sb = new StringBuilder(size * 2 + 3);
		Log.d("CORNER", "Data:");
		sb.append(" ");
		for (int xx = x - size; xx < x + size; xx++) {
			int v = Math.abs(xx - x);
			sb.append(v % 10);
//			sb.append((char)(v > 9 ? v - 10 + 'A' : v + '0'));
		}
		Log.d("CORNER", sb.toString());
		sb.delete(0, sb.length());
		for (int yy = y - size; yy < y + size; yy++) {
			int v = Math.abs(yy - y);
			sb.append(v % 10);
//			sb.append((char)(v > 9 ? v - 10 + 'A' : v + '0'));
            for (int xx = x - size; xx < x + size; xx++) {
				byte d = this.getPixel(data, xx, yy, w, h);
				sb.append(d == 0 ? "0" : "X");
			}
            Log.d("CORNER", sb.toString());
            sb.delete(0, sb.length());
		}
		
		for (PointF p : ps) {
			int px = (int) (x + (r - tolerance) * p.x);
			int py = (int) (y + (r - tolerance) * p.y);
			int pd = this.getPixel(data, px, py, w, h);
			Log.d("CORNER", String.format("I: (%d, %d).(%f, %f).(%d, %d) -> %d", 
					px, py, p.x, p.y, (int)((r - tolerance) * p.x), (int)((r - tolerance) * p.y), pd));
			if (pd != 0) {
				return false;
			}
			
			int ox = (int) (x + (r + tolerance) * p.x);
			int oy = (int) (y + (r + tolerance) * p.y);
			int od = this.getPixel(data, ox, oy, w, h);
			Log.d("CORNER", String.format("O: (%d, %d).(%f, %f).(%d, %d) -> %d", 
					ox, oy, p.x, p.y, (int)((r + tolerance) * p.x), (int)((r + tolerance) * p.y), od));
			if (od == 0) {
				return false;
			}
		}
		
		return true;
	}
	
//	private boolean isCalibratePoint(byte[] data, int x, int y, int w, int h, int size, int tolerance) {
//		int countYu = 0;
//		int countYd = 0;
//		int count13 = 0;
//		int count24 = 0;
//		int max = size + tolerance;
//		double sizeX = size / SQRT_2;
//		for (int i = 0; i < max; i++) {
//			if (this.getPixel(data, x, y - i, w, h) == 0) {
//				countYu++;
//			} else {
//				break;
//			}
//		}
//		for (int i = 0; i < max; i++) {
//			if (this.getPixel(data, x, y + i, w, h) == 0) {
//				countYd++;
//			} else {
//				break;
//			}
//		}
//		if (Math.abs(countYu - countYd) > tolerance || Math.abs(countYu + countYd - size) >= tolerance) {
//			return false;
//		}
////		for (int i = 0; i < max; i++) {
////			if (this.getPixel(data, x - i, y + i, w, h) == 0) {
////				count13++;
////			} else {
////				break;
////			}
////		}
////		for (int i = 0; i < max; i++) {
////			if (this.getPixel(data, x + i, y - i, w, h) == 0) {
////				count13++;
////			} else {
////				break;
////			}
////		}
////		for (int i = 0; i < max; i++) {
////			if (this.getPixel(data, x - i, y - i, w, h) == 0) {
////				count24++;
////			} else {
////				break;
////			}
////		}
////		for (int i = 0; i < max; i++) {
////			if (this.getPixel(data, x + i, y + i, w, h) == 0) {
////				count24++;
////			} else {
////				break;
////			}
////		}
////		if (Math.abs(count13 - sizeX) >= tolerance && Math.abs(count24 - sizeX) >= tolerance) {
////			return false;
////		}
//		
//		return true;
//	}
	
	private byte getPixel(byte[] data, int x, int y, int w, int h) {
		return x >= 0 && x < w && y >= 0 && y < h ? data[x + w * y] : (byte)0xff;
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
	
	private void drawMonoImage(Bitmap bmp) {
		Canvas canvas = this.mImageHolder.lockCanvas();
		if (canvas != null) {
			canvas.drawColor(Color.BLACK);
			if (bmp != null) {
				Bitmap bm = this.getCalibrateBm(20);
				canvas.drawBitmap(bmp, 0, 0, null);
				canvas.drawBitmap(bm, 0, 0, null);
				bm.recycle();
			}
            this.mImageHolder.unlockCanvasAndPost(canvas);
		}
	}
	
	private void drawCorners(List<Point> corners) {
		Canvas canvas = this.mImageHolder.lockCanvas();
		if (canvas != null) {
			Paint p = new Paint();
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(2);
			p.setColor(Color.GREEN);
			float[] points = new float[corners.size() * 2];
			int i = 0;
			for (Point corner : corners) {
				points[i++] = corner.x;
				points[i++] = corner.y;
			}
			canvas.drawLines(points, p);
			this.mImageHolder.unlockCanvasAndPost(canvas);
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
