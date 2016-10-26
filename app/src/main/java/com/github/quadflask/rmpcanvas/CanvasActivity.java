package com.github.quadflask.rmpcanvas;

import android.support.design.widget.FloatingActionButton;
import android.widget.ImageButton;

import com.github.quadflask.rmpcanvas.model.DrawingEvent;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.SyncUser;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CanvasActivity extends BaseActivity {
	@BindView(R.id.ib_back)
	ImageButton backBtn;
	@BindView(R.id.fab)
	FloatingActionButton fab;
	@BindView(R.id.canvasView)
	CanvasView canvasView;

	private Realm realm;
	private long drewTimestamp = 0L;

	private static final int[] COLORS = new int[]{
			R.color.openColors0,
			R.color.openColors1,
			R.color.openColors2,
			R.color.openColors3,
			R.color.openColors4,
			R.color.openColors5,
			R.color.openColors6,
			R.color.openColors7,
			R.color.openColors8,
			R.color.openColors9,
			R.color.openColors10,
			R.color.openColors11,
			R.color.openColors12,
	};

	@Override
	protected void onStart() {
		super.onStart();
		realm = Realm.getDefaultInstance();

		drawToCanvasFromRealmResults(realm);

		realm.addChangeListener(this::drawToCanvasFromRealmResults);

		onFabClick();
		canvasView.asObservable()
				.subscribeOn(Schedulers.newThread())
				.compose(bindToLifecycle())
				.onBackpressureBuffer()
				.map(motionEvent -> {
					final DrawingEvent drawingEvent = DrawingEvent.fromMotionEvent(motionEvent);
					drawingEvent.color = canvasView.getCurrentBrushColor();
					drawingEvent.userUUID = CanvasApplication.getCurrentUUID();
					return drawingEvent;
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(drawingEvent -> {
					realm.executeTransactionAsync(_ream -> _ream.copyToRealm(drawingEvent));
				}, Throwable::printStackTrace);
	}

	@Override
	protected void onStop() {
		realm.removeAllChangeListeners();
		realm.close();
		realm = null;
		SyncUser.currentUser().logout();
		super.onStop();
	}

	@Override
	protected int getContentViewResId() {
		return R.layout.activity_main;
	}

	@OnClick(R.id.fab)
	void onFabClick() {
		canvasView.setCurrentBrushColor(getResources().getColor(COLORS[(int) (Math.random() * COLORS.length)]));
	}

	@OnClick(R.id.ib_back)
	void onBackBtn() {
		finish();
	}

	private void drawToCanvasFromRealmResults(Realm realm) {
		realm.where(DrawingEvent.class)
				.notEqualTo("userUUID", CanvasApplication.getCurrentUUID())
				.greaterThan("timestamp", drewTimestamp)
				.findAllAsync()
				.asObservable()
				.filter(drawingEvents -> !drawingEvents.isEmpty())
				.subscribe(drawingEvents -> {
					canvasView.drawDrawingEvents(drawingEvents);
					drewTimestamp = drawingEvents.last().timestamp;
				}, Throwable::printStackTrace);
	}
}
