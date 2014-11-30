package org.programus.book.mobilelego.research.imagerecognitiontest;

import java.io.IOException;
import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreviewer {
	public enum Orientation {
		Portraite, 
		Landscape,
	}
	private Camera mCamera;
	private Camera.Size mCamSize;
	
	private SurfaceView mCameraPreviewView;
	private SurfaceHolder mCameraPreviewHolder;
	
	private List<Camera.Size> mSupportedSizes;
	
	private Orientation mOrientation = Orientation.Portraite;
	
	private Camera.PreviewCallback mPreviewCallback;
	
	private byte[] mBuffer;
	
	public void setPreviewView(SurfaceView previewView) {
		this.mCameraPreviewView = previewView;
		this.mCameraPreviewHolder = this.mCameraPreviewView.getHolder();
		this.mCameraPreviewHolder.addCallback(holderCallback);
		if (this.isPreviewing()) {
			this.stopCameraPreview();
			this.startCameraPreview();
		}
	}
	
	public void setPreviewCallback(Camera.PreviewCallback callback) {
		this.mPreviewCallback = callback;
	}
	
	public void setPreviewSize(Camera.Size size) {
		this.mCamSize = size;
	}
	
	public void setDisplaySize(int width, int height) {
		this.mCameraPreviewHolder.setFixedSize(width, height);
	}
	
	public void setOrientation(Orientation orientation) {
		this.mOrientation = orientation;
	}
	
	public List<Camera.Size> getSupportedPreviewSizes() {
		if (this.mSupportedSizes == null) {
			Camera cam = Camera.open();
			this.mSupportedSizes = cam.getParameters().getSupportedPreviewSizes();
			cam.release();
		}
		return this.mSupportedSizes;
	}
	
	public Camera.Size getPreviewSize() {
		return this.mCamSize;
	}
	
	public boolean isPreviewing() {
		return this.mCamera != null;
	}
	
	private SurfaceHolder.Callback holderCallback = new SurfaceHolder.Callback() {
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(this.getClass().getSimpleName(), "Preview surface destroied");
			stopCameraPreview();
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(this.getClass().getSimpleName(), "Preview surface created");
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
	
	private void setupCameraParams(final Camera.Parameters params) {
		List<String> focusModes = params.getSupportedFocusModes();
		String focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
		if (focusModes.contains(focusMode)) {
            params.setFocusMode(focusMode);
		}
		params.setPreviewFormat(ImageFormat.NV21);
		params.setPictureFormat(ImageFormat.NV21);
		params.setPreviewSize(this.mCamSize.width, this.mCamSize.height);
		params.setPictureSize(this.mCamSize.width, this.mCamSize.height);
		
		int capacity = (this.mCamSize.width * this.mCamSize.height * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) >> 3;
		if (this.mBuffer == null || capacity > this.mBuffer.length) {
			this.mBuffer = new byte[capacity];
		}
	}
	
	public void startCameraPreview() {
		if (this.mCamera == null) {
			this.mCamera = Camera.open();
			Camera.Parameters params = this.mCamera.getParameters();
            if (this.mCamSize == null) {
                this.stopCameraPreview();
            } else {
                this.setupCameraParams(params);
                this.mCamera.setParameters(params);
                try {
                    this.mCamera.setPreviewDisplay(mCameraPreviewHolder);
                } catch (IOException e) {
                    Log.d(this.getClass().getName(), "Error when set preview display\n", e);
                }
                this.mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
                this.mCamera.addCallbackBuffer(mBuffer);
                if (this.mOrientation == Orientation.Portraite) {
	                this.mCamera.setDisplayOrientation(90);
                }
                this.mCamera.startPreview();
                Log.d(this.getClass().getSimpleName(), "Started Preview");
            }
		}
	}
	
	public void stopCameraPreview() {
		if (this.mCamera != null) {
			Camera cam = this.mCamera;
			this.mCamera = null;
			cam.stopPreview();
			cam.setPreviewCallback(null);
			cam.release();
		}
	}
}
