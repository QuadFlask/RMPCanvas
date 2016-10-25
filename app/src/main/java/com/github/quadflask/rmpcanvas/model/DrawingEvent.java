package com.github.quadflask.rmpcanvas.model;

import android.view.MotionEvent;

import io.realm.RealmObject;

public class DrawingEvent extends RealmObject {
	public long userUUID;
	public long timestamp = System.currentTimeMillis();
	public int action;
	public float x, y;
	public int color;

	public DrawingEvent() {
	}

	public static DrawingEvent fromMotionEvent(MotionEvent motionEvent) {
		final DrawingEvent drawingEvent = new DrawingEvent();
		drawingEvent.action = motionEvent.getAction();
		drawingEvent.x = motionEvent.getX();
		drawingEvent.y = motionEvent.getY();
		return drawingEvent;
	}
}