package ionic.mayazuc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaBrowser;

import com.getcapacitor.BridgeActivity;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

public class MainActivity extends MediaControllerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        registerPlugin(MediaControllerIonicPlugin.class);
        super.onCreate(savedInstanceState);
        CheckPermissions();
    }

    @Override
    public void onStart() {
        setDarkMode();
        super.onStart();
    }

    @Override
    public void onResume() {
        setDarkMode();
        super.onResume();
    }

    void setDarkMode() {
        // Android "fix" for enabling dark mode
        // @see: https://github.com/ionic-team/capacitor/discussions/1978
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        WebSettings webSettings = this.bridge.getWebView().getSettings();
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // As of Android 10, you can simply force the dark mode
                webSettings.setForceDark(WebSettings.FORCE_DARK_ON);
            } else
                this.bridge.getWebView().evaluateJavascript("document.body.classList.toggle('dark', true);", null);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                webSettings.setForceDark(WebSettings.FORCE_DARK_OFF);
            } else
                this.bridge.getWebView().evaluateJavascript("document.body.classList.toggle('dark', false);", null);
        }
    }

    private void CheckPermissions() {

        var filesPermission = getFileManagementPermission();

        var permission =
                ContextCompat.checkSelfPermission(this, filesPermission);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{filesPermission},
                    69
            );
        } else {
            HandleNewIntenet(getIntent());
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 69 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            HandleNewIntenet(getIntent());
        } else {
            HandleNewIntenet(getIntent());
        }
    }

    private void HandleNewIntenet(Intent intent) {
        AlbumArtScanWorker.Companion.StartWorking(false, MCApplication.Companion.getContext());
    }

    private String getFileManagementPermission() {
        var filesPermision = Manifest.permission.MANAGE_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= 33)
            filesPermision = Manifest.permission.READ_MEDIA_AUDIO;
        return filesPermision;
    }
}
