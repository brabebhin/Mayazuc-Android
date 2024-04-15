@file:OptIn(ExperimentalMaterial3Api::class)

package ionic.mayazuc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.FOLDER_TYPE_TITLES
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import ionic.mayazuc.MediaItemTree.ROOT_ID
import ionic.mayazuc.Utilities.getPlaybaleItems

private const val savedState_backstack = "backstack"

private const val savedState_CurrentMediaId = "currentMediaId"

private const val savedState_TargetMediaId = "TargetMediaId"

@UnstableApi
class MainActivityOld : MediaControllerActivity() {
    var currentMediaId: String = ROOT_ID;
    var currentMediaItems: List<MediaItem> = ArrayList<MediaItem>();
    val backSack = BackSack();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            val backStackState = savedInstanceState.getString(savedState_backstack)
            backSack.LoadFromJson(backStackState!!)
            val savedCurrentMediaId = savedInstanceState.getString(savedState_CurrentMediaId)
            if (savedCurrentMediaId != null) {
                currentMediaId = savedCurrentMediaId
            }
        }
        CheckPermissions();
    }

    private fun HandleNewIntenet(intent: Intent?) {
        val mediaId = intent?.extras?.getString(savedState_TargetMediaId);
        if (mediaId != null)
            currentMediaId = mediaId;
        Futures.addCallback(
            MediaServiceConnector.initializeBrowser(),
            object : FutureCallback<MediaBrowser> {
                override fun onSuccess(result: MediaBrowser?) {
                    MediaServiceConnector.openMediaId(currentMediaId)?.let {
                        Futures.addCallback(
                            it,
                            object : FutureCallback<LibraryResult<ImmutableList<MediaItem>>> {
                                override fun onSuccess(result: LibraryResult<ImmutableList<MediaItem>>) {
                                    // handle success
                                    pushRoot(result.value!!)
                                }

                                override fun onFailure(t: Throwable) {
                                    // handle failure
                                }
                            }, MoreExecutors.directExecutor()
                        )
                    }
                }

                override fun onFailure(t: Throwable) {
                    TODO("Not yet implemented")
                }

            },
            MoreExecutors.directExecutor()
        )
    }


    override fun onBackPressed() {
        var currentBackStackItem = backSack.Pop();
        if (currentBackStackItem == null) {
            super.onBackPressed()
        } else openMediaItem(currentBackStackItem);
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val backStackState = backSack.SaveToJson();
        outState.putString(savedState_backstack, backStackState)
        outState.putString(savedState_CurrentMediaId, currentMediaId);
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        HandleNewIntenet(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        pushRoot(currentMediaItems)
    }

    private fun pushRoot(mediaItemsResult: List<MediaItem>) {

    }

    private fun CheckPermissions() {

        var filesPermision = getFileManagementPermission()

        val permission =
            ContextCompat.checkSelfPermission(this, filesPermision);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(filesPermision),
                69
            )
        } else {
            HandleNewIntenet(intent)
        }
    }

    private fun getFileManagementPermission(): String {
        var filesPermision = Manifest.permission.MANAGE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= 33)
            filesPermision = Manifest.permission.READ_MEDIA_AUDIO
        return filesPermision
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 69 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            HandleNewIntenet(intent)
        } else {
            HandleNewIntenet(intent)
        }
    }

    private fun openMediaItem(mediaId: String) {
        val intent = Intent(this, MainActivity::class.java)
        val parameters = Bundle()
        parameters.putString(savedState_TargetMediaId, mediaId);

        intent.putExtras(parameters);
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        MediaServiceConnector.initializeBrowser()
    }

    override fun setController() {

    }
}