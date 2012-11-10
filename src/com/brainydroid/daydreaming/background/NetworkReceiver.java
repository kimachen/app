package com.brainydroid.daydreaming.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

	private static String TAG = "NetworkReceiver";

	private StatusManager status;

	@Override
	public void onReceive(Context context, Intent intent) {

		// Debug
		Log.d(TAG, "[fn] onReceive");

		status = StatusManager.getInstance(context);
		String action = intent.getAction();

		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

			// Info
			Log.i(TAG, "Received CONNECTIVITY_ACTION");

			if (status.isFirstLaunchCompleted() && status.isDataEnabled()) {

				// Info
				Log.i(TAG, "first launch is completed");
				Log.i(TAG, "will start SyncService");

				// TODO: add a call to SyncService
			}
		}
	}
}
