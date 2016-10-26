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
		realm.close(); // TODO close not working
//		Caused by: java.lang.IllegalStateException: A Realm controlled by this user is still open. Close all Realms before logging out: /data/data/com.github.quadflask.rmpcanvas/files/realm-object-server/d12d00f6f5811a3b13fe80ffd91e424d/canvas
//		at io.realm.SyncUser.logout(SyncUser.java:250)
//		at com.github.quadflask.rmpcanvas.CanvasActivity.onStop(CanvasActivity.java:73)
//		at android.app.Instrumentation.callActivityOnStop(Instrumentation.java:1278)
//		at android.app.Activity.performStop(Activity.java:6395)
//		at android.app.ActivityThread.performDestroyActivity(ActivityThread.java:3790)
//		at android.app.ActivityThread.handleDestroyActivity(ActivityThread.java:3849) 
//		at android.app.ActivityThread.-wrap5(ActivityThread.java) 
//		at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1398) 
//		at android.os.Handler.dispatchMessage(Handler.java:102) 
//		at android.os.Looper.loop(Looper.java:148) 
//		at android.app.ActivityThread.main(ActivityThread.java:5417) 
//		at java.lang.reflect.Method.invoke(Native Method) 
//		at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:726) 
//		at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:616) 
//		10-26 11:21:49.982 800-14933/? W/ActivityManager:   Force finishing activity com.github.quadflask.rmpcanvas/.EnterRoomActivity
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
