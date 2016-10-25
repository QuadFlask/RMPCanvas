package com.github.quadflask.rmpcanvas;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.ObjectServerError;
import io.realm.SyncCredentials;
import io.realm.SyncUser;

import static com.google.common.base.Strings.isNullOrEmpty;

public class CreateRoomActivity extends BaseActivity {
	@BindView(R.id.ib_back)
	ImageButton backBtn;
	@BindView(R.id.et_name)
	EditText nameTextbox;
	@BindView(R.id.et_password)
	EditText passwordTextbox;
	@BindView(R.id.btn_create)
	Button createBtn;

	private boolean isInProgress = false;

	@Override
	protected void afterBind(@Nullable Bundle savedInstanceState) {
		RxView.clicks(createBtn)
				.compose(bindToLifecycle())
				.filter(v -> !isNullOrEmpty(nameTextbox.getText().toString()))
				.filter(v -> !isNullOrEmpty(passwordTextbox.getText().toString()))
				.filter(v -> !isInProgress)
				.subscribe(v -> {
					isInProgress = true;

					tryCreateRoom(
							nameTextbox.getText().toString(),
							passwordTextbox.getText().toString()
					);

					createBtn.setEnabled(false);
					passwordTextbox.setText("");
				}, Throwable::printStackTrace);
	}

	private void tryCreateRoom(String username, String password) {
		SyncUser.loginAsync(SyncCredentials.usernamePassword(username, password, true), CanvasApplication.AUTH_URL, new SyncUser.Callback() {
			@Override
			public void onSuccess(SyncUser user) {
				UserManager.setActiveUser(user);
				startActivity(new Intent(CreateRoomActivity.this, CanvasActivity.class));
				isInProgress = false;
				createBtn.setEnabled(true);
			}

			@Override
			public void onError(ObjectServerError error) {
				switch (error.getErrorCode()) {
					case EXISTING_ACCOUNT:
						Toast.makeText(CreateRoomActivity.this, "이미 이름 같은 방이 있어요", Toast.LENGTH_LONG).show();
						break;
					default:
						Toast.makeText(CreateRoomActivity.this, error.toString(), Toast.LENGTH_LONG).show();
				}
				isInProgress = false;
				createBtn.setEnabled(true);
			}
		});
	}

	@OnClick(R.id.ib_back)
	void onBackbtn() {
		finish();
	}

	@Override
	protected int getContentViewResId() {
		return R.layout.activity_create_room;
	}
}