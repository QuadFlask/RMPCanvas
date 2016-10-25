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

public class EnterRoomActivity extends BaseActivity {
	@BindView(R.id.ib_create_room)
	ImageButton createRoomBtn;
	@BindView(R.id.et_name)
	EditText nameTextbox;
	@BindView(R.id.et_password)
	EditText passwordTextbox;
	@BindView(R.id.ib_enter_room)
	Button enterBtn;

	private boolean isInProgress = false;

	@Override
	protected void afterBind(@Nullable Bundle savedInstanceState) {
		nameTextbox.setText("room1");
		passwordTextbox.setText("1");
		RxView.clicks(enterBtn)
				.compose(bindToLifecycle())
				.filter(v -> !isNullOrEmpty(nameTextbox.getText().toString()))
				.filter(v -> !isNullOrEmpty(passwordTextbox.getText().toString()))
				.filter(v -> !isInProgress)
				.subscribe(v -> {
					isInProgress = true;

					tryEnterRoom(
							nameTextbox.getText().toString(),
							passwordTextbox.getText().toString()
					);

					enterBtn.setEnabled(false);
					passwordTextbox.setText("");
				}, Throwable::printStackTrace);
	}

	private void tryEnterRoom(String username, String password) {
		SyncUser.loginAsync(SyncCredentials.usernamePassword(username, password, false), CanvasApplication.AUTH_URL, new SyncUser.Callback() {
			@Override
			public void onSuccess(SyncUser user) {
				CanvasApplication.updateUUID();
				UserManager.setActiveUser(user);
				startActivity(new Intent(EnterRoomActivity.this, CanvasActivity.class));
				isInProgress = false;
				enterBtn.setEnabled(true);
			}

			@Override
			public void onError(ObjectServerError error) {
				switch (error.getErrorCode()) {
					case UNKNOWN_ACCOUNT:
						Toast.makeText(EnterRoomActivity.this, "방 이름이 잘못되었어요", Toast.LENGTH_LONG).show();
						break;
					case INVALID_CREDENTIALS:
						Toast.makeText(EnterRoomActivity.this, "비밀번호가 틀렸어요", Toast.LENGTH_LONG).show();
						break;
					default:
						Toast.makeText(EnterRoomActivity.this, error.toString(), Toast.LENGTH_LONG).show();
				}
				isInProgress = false;
				enterBtn.setEnabled(true);
			}
		});
	}

	@OnClick(R.id.ib_create_room)
	void openCreateRoom() {
		startActivity(new Intent(this, CreateRoomActivity.class));
	}

	@Override
	protected int getContentViewResId() {
		return R.layout.activity_enter_room;
	}
}