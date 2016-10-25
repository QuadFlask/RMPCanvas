package com.github.quadflask.rmpcanvas;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends RxAppCompatActivity {
	private static final String TAG = "BaseActivity";
	private Unbinder bind;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewResId());
		bind = ButterKnife.bind(this);
		afterBind(savedInstanceState);
	}

	protected void afterBind(@Nullable Bundle savedInstanceState) {
	}

	@LayoutRes
	protected abstract int getContentViewResId();
}