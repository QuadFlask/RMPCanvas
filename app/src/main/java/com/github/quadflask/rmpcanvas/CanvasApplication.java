package com.github.quadflask.rmpcanvas;

import android.app.Application;
import android.util.Log;

import java.util.UUID;

import io.realm.Realm;
import io.realm.log.AndroidLogger;
import io.realm.log.RealmLog;

public class CanvasApplication extends Application {
	public static final String AUTH_URL = "http://" + BuildConfig.OBJECT_SERVER_IP + ":9080/auth";
	public static final String REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/canvas";
	private static long currentUUID = UUID.randomUUID().getMostSignificantBits();

	@Override
	public void onCreate() {
		super.onCreate();
		Realm.init(this);

//		RealmLog.add(new AndroidLogger(Log.INFO));
	}

	public static long getCurrentUUID() {
		return currentUUID;
	}

	public static void updateUUID() {
		currentUUID = UUID.randomUUID().getMostSignificantBits();
	}
}