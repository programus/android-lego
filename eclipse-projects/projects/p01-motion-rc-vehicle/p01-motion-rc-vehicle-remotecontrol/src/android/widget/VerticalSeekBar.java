package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class VerticalSeekBar extends SeekBar {
	private OnSeekBarChangeListener myListener;

	public VerticalSeekBar(Context context) {
		super(context);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener) {
		this.myListener = mListener;
	}

	protected void onDraw(Canvas c) {
		c.save();
		c.rotate(-90);
		c.translate(-getHeight(), 0);

		super.onDraw(c);
		c.restore();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setPressed(true);
			if (myListener != null)
				myListener.onStartTrackingTouch(this);
			break;
		case MotionEvent.ACTION_MOVE:
			setPressed(true);
			setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
			onSizeChanged(getWidth(), getHeight(), 0, 0);
			if (myListener != null) {
                myListener.onProgressChanged(this, getMax() - (int) (getMax() * event.getY() / getHeight()), true);
			}
			break;
		case MotionEvent.ACTION_UP:
			setPressed(false);
			if (myListener != null) {
                myListener.onStopTrackingTouch(this);
			}
			break;

		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return true;
	}
}
