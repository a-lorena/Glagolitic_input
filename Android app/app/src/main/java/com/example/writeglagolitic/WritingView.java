package com.example.writeglagolitic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class WritingView extends View {
	private Paint paint;
	private Path path;

	public WritingView(Context context) {
		super(context);
		init();
	}

	public WritingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(15);
		path = new Path();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawPath(path, paint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:
				path.moveTo(event.getX(), event.getY());
				break;

			case MotionEvent.ACTION_MOVE:
				path.lineTo(event.getX(), event.getY());
				invalidate();
				break;

			case MotionEvent.ACTION_UP:
				break;
		}

		return true;
	}

	public void clear() {
		path.reset();
		invalidate();
	}

	public Bitmap save() {
		this.setDrawingCacheEnabled(true);

		Bitmap b2 = Bitmap.createScaledBitmap(getDrawingCache(), 100, 100, false);
		Bitmap newBitmap = Bitmap.createBitmap(100, 100, b2.getConfig());


		Canvas newCanvas = new Canvas(newBitmap);
		newCanvas.drawColor(Color.WHITE);
		newCanvas.drawBitmap(b2, 0, 0, null);

		this.setDrawingCacheEnabled(false);
		b2.recycle();

		return newBitmap;
	}
}
