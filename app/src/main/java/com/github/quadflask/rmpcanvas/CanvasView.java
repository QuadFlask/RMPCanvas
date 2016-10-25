package com.github.quadflask.rmpcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.quadflask.rmpcanvas.model.DrawingEvent;
import com.google.common.collect.Queues;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class CanvasView extends View {
	private Bitmap bitmap;
	private Canvas bitmapCanvas;
	private Paint bitmapPaint;

	private Path path = new Path();
	private Paint brushPaint;

	private Path path2 = new Path();
	private Paint brushPaint2;

	private Queue<DrawingEvent> eventsBuffer = Queues.newLinkedBlockingQueue();
	private final Subject<MotionEvent, MotionEvent> subject = new SerializedSubject<>(PublishSubject.create());
	private Subscription subscription;

	private int numberOfMirror = 5;
	private int cx, cy;

	public CanvasView(Context context) {
		super(context);
		init();
	}

	public CanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		brushPaint = createPaint();
		brushPaint.setColor(0xffffffff);
		brushPaint2 = createPaint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);

		canvas.drawPath(path, brushPaint);
		canvas.drawPath(path2, brushPaint2);

		for (int i = 1; i < numberOfMirror; i++) {
			canvas.rotate(360f / numberOfMirror, cx, cy);
			canvas.drawPath(path, brushPaint);
			canvas.drawPath(path2, brushPaint2);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		bitmapCanvas = new Canvas(bitmap);
		bitmapPaint = new Paint(Paint.DITHER_FLAG);
		cx = w / 2;
		cy = h / 2;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				path.moveTo(x, y);
				subject.onNext(event);
				break;
			case MotionEvent.ACTION_MOVE:
				path.lineTo(x, y);
				subject.onNext(event);
				break;
			case MotionEvent.ACTION_UP:
				path.lineTo(x, y);
				drawToCanvas(path, brushPaint);
				path.reset();
				subject.onNext(event);
				break;
			default:
				return false;
		}

		invalidate();
		return true;
	}

	public void drawDrawingEvents(List<DrawingEvent> drawingEvents) {
		if (subscription == null)
			subscription = Observable.interval(20, TimeUnit.MILLISECONDS)
					.filter(l -> bitmapCanvas != null)
					.filter(l -> !eventsBuffer.isEmpty())
					.map(l -> eventsBuffer.poll())
					.filter(event -> event != null)
					.onBackpressureDrop()
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(this::drawDrawingEvent, Throwable::printStackTrace);

		eventsBuffer.addAll(drawingEvents);
	}

	private void drawDrawingEvent(DrawingEvent event) {
		switch (event.action) {
			case MotionEvent.ACTION_DOWN:
				brushPaint2.setColor(event.color);
				path2.moveTo(event.x, event.y);
				break;
			case MotionEvent.ACTION_MOVE:
				path2.lineTo(event.x, event.y);
				break;
			case MotionEvent.ACTION_UP:
				path2.lineTo(event.x, event.y);
				brushPaint2.setColor(event.color);
				drawToCanvas(path2, brushPaint2);
				path2.reset();
				break;
			default:
				return;
		}
		invalidate(); // TODO rendering too slow...
	}

	private void drawToCanvas(Path path, Paint brushPaint) {
		bitmapCanvas.drawPath(path, brushPaint);
		for (int i = 1; i < numberOfMirror; i++) {
			bitmapCanvas.rotate(360f / numberOfMirror, cx, cy);
			bitmapCanvas.drawPath(path, brushPaint);
		}
	}

	public int getCurrentBrushColor() {
		return brushPaint.getColor();
	}

	public void setCurrentBrushColor(int color) {
		brushPaint.setColor(color);
	}

	private Paint createPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStrokeWidth(8f);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeJoin(Paint.Join.ROUND);
		p.setStrokeCap(Paint.Cap.ROUND);
		return p;
	}

	@Override
	protected void onDetachedFromWindow() {
		if (subscription != null && subscription.isUnsubscribed())
			subscription.unsubscribe();
		super.onDetachedFromWindow();
	}

	public Observable<MotionEvent> asObservable() {
		return subject;
	}
}