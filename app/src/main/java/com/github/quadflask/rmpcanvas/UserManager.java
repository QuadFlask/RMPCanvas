/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.quadflask.rmpcanvas;

import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncUser;

public class UserManager {

	// Configure Realm for the current active user
	public static void setActiveUser(SyncUser user) {
		SyncConfiguration defaultConfig = new SyncConfiguration.Builder(user, CanvasApplication.REALM_URL).build();
		Realm.setDefaultConfiguration(defaultConfig);
	}
}
