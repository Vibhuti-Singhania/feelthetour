
package com.example.android.feelthetour.ui;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.example.android.feelthetour.WelcomeActivity;
import com.example.android.feelthetour.utils.LogHelper;

/**
 * The activity for the Now Playing Card PendingIntent.
 * https://developer.android.com/training/tv/playback/now-playing.html
 *
 * This activity determines which activity to launch based on the current UI mode.
 */
public class NowPlayingActivity extends Activity {

    private static final String TAG = LogHelper.makeLogTag(NowPlayingActivity.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "onCreate");
        Intent newIntent = null;
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() != Configuration.UI_MODE_TYPE_TELEVISION) {
            LogHelper.d(TAG, "Running on a non-TV Device");
            newIntent = new Intent(this, WelcomeActivity.class);
        }
        startActivity(newIntent);
        finish();
    }
}
