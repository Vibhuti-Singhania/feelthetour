package com.example.android.feelthetour;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import com.example.android.feelthetour.ui.BaseActivity;
import com.example.android.feelthetour.ui.FullScreenPlayerActivity;
import com.example.android.feelthetour.ui.MediaBrowserFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.TextUtils;

import com.example.android.feelthetour.R;
import com.example.android.feelthetour.utils.LogHelper;

public class WelcomeActivity extends BaseActivity
        implements MediaBrowserFragment.MediaFragmentListener {
//    private Button mlogoutfb;
//    private FirebaseAuth mAuth;

    private NavigationView navigationView;
    private SlidingUpPanelLayout panelLayout;
    private Handler navDrawerRunnable = new Handler();

    private static final String TAG = LogHelper.makeLogTag(WelcomeActivity.class);
    private static final String SAVED_MEDIA_ID="com.example.android.feelthetour.MEDIA_ID";
    private static final String FRAGMENT_TAG = "feelthetour_list_container";

    public static final String EXTRA_START_FULLSCREEN =
            "com.example.android.feelthetour.EXTRA_START_FULLSCREEN";

    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
            "com.example.android.feelthetour.CURRENT_MEDIA_DESCRIPTION";

    private Bundle mVoiceSearchParams;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "Activity onCreate");
        setContentView(R.layout.activity_welcome);

        //if the user is not logged in
        //starting the login activity
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        initializeToolbar();
        initializeFromParams(savedInstanceState, getIntent());

        // Only check if a full screen player is needed on the first time:
        if (savedInstanceState == null) {
            startFullScreenActivityIfNeeded(getIntent());
        }

    }
//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_shuffle) {
            return true;
        }
        if (id == R.id.action_equalizer) {
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String mediaId = getMediaId();
        if (mediaId != null) {
            outState.putString(SAVED_MEDIA_ID, mediaId);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        LogHelper.d(TAG, "onMediaItemSelected, mediaId=" + item.getMediaId());
        if (item.isPlayable()) {
            MediaControllerCompat.getMediaController(WelcomeActivity.this).getTransportControls()
                    .playFromMediaId(item.getMediaId(), null);
        } else if (item.isBrowsable()) {
            navigateToBrowser(item.getMediaId());
        } else {
            LogHelper.w(TAG, "Ignoring MediaItem that is neither browsable nor playable: ",
                    "mediaId=", item.getMediaId());
        }
    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        LogHelper.d(TAG, "Setting toolbar title to ", title);
        if (title == null) {
            title = getString(R.string.app_name);
        }
        setTitle(title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogHelper.d(TAG, "onNewIntent, intent=" + intent);
        initializeFromParams(null, intent);
        startFullScreenActivityIfNeeded(intent);
    }

    private void startFullScreenActivityIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_START_FULLSCREEN, false)) {
            MediaDescriptionCompat description =
                    intent.getParcelableExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION);

            Intent fullScreenIntent = new Intent(this, FullScreenPlayerActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
            startActivity(fullScreenIntent);
        }
    }

    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        String mediaId = null;
        // check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.getAction() != null
                && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            mVoiceSearchParams = intent.getExtras();
            LogHelper.d(TAG, "Starting from voice search query=",
                    mVoiceSearchParams.getString(SearchManager.QUERY));
        } else {
            if (savedInstanceState != null) {
                // If there is a saved media ID, use it
                mediaId = savedInstanceState.getString(SAVED_MEDIA_ID);
            }
        }
        navigateToBrowser(mediaId);
    }

    private void navigateToBrowser(String mediaId) {
        LogHelper.d(TAG, "navigateToBrowser, mediaId=" + mediaId);
        MediaBrowserFragment fragment = getBrowseFragment();

        if (fragment == null || !TextUtils.equals(fragment.getMediaId(), mediaId)) {
            fragment = new MediaBrowserFragment();
            fragment.setMediaId(mediaId);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.animator.slide_in_from_right, R.animator.slide_out_to_left,
                    R.animator.slide_in_from_left, R.animator.slide_out_to_right);
            transaction.replace(R.id.container, fragment, FRAGMENT_TAG);
            // If this is not the top level media (root), we add it to the fragment back stack,
            // so that actionbar toggle and Back will work appropriately:
            if (mediaId != null) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        }
    }

    public String getMediaId() {
        MediaBrowserFragment fragment = getBrowseFragment();
        if (fragment == null) {
            return null;
        }
        return fragment.getMediaId();
    }

    private MediaBrowserFragment getBrowseFragment() {
        return (MediaBrowserFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    @Override
    protected void onMediaControllerConnected() {
        if (mVoiceSearchParams != null) {
            // If there is a bootstrap parameter to start from a search query, we
            // send it to the media session and set it to null, so it won't play again
            // when the activity is stopped/started or recreated:
            String query = mVoiceSearchParams.getString(SearchManager.QUERY);
            MediaControllerCompat.getMediaController(WelcomeActivity.this).getTransportControls()
                    .playFromSearch(query, mVoiceSearchParams);
            mVoiceSearchParams = null;

        }
        getBrowseFragment().onConnected();

    }

}
