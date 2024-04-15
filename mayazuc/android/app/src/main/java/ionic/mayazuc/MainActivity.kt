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
import ionic.mayazuc.UiUtilities.ElementWithPlainTooltip
import ionic.mayazuc.UiUtilities.MayazucScaffold
import ionic.mayazuc.Utilities.getPlaybaleItems

private const val savedState_backstack = "backstack"

private const val savedState_CurrentMediaId = "currentMediaId"

private const val savedState_TargetMediaId = "TargetMediaId"

@UnstableApi
class MainActivity : MediaControllerActivity() {
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

        val playableItems = mediaItemsResult.toMutableList().getPlaybaleItems();
        setContent {

            val scrollBehavior =
                TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
            MayazucScaffold(
                topBar = {
                    MediumTopAppBar(
                        title = {
                            Text(
                                text = currentMediaId,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer

                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { openMediaItem(ROOT_ID) }) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home"
                                )
                            }
                        },
                        actions = {
                            if (playableItems.isNotEmpty()) {
                                ElementWithPlainTooltip(tooltip = "Play") {
                                    IconButton(onClick = {
                                        MediaPlayerUtilities.playMediaItemsAsync(
                                            playableItems
                                        );
                                    }) {
                                        Image(
                                            painterResource(id = R.drawable.playviewgeneric),
                                            contentDescription = "Play",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    };
                                }

                                ElementWithPlainTooltip(tooltip = "Add to now playing") {
                                    IconButton(onClick = {
                                        MediaPlayerUtilities.addMediaItemsToNowPlayingAsync(
                                            playableItems
                                        )
                                    }) {
                                        Image(
                                            painterResource(id = R.drawable.addtonowplayinggeneric),
                                            contentDescription = "Add to now playing",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                ElementWithPlainTooltip(tooltip = "Add to playlist") {
                                    IconButton(
                                        onClick = { /*add to playlist*/ }) {
                                        Image(
                                            painterResource(id = R.drawable.foldericon),
                                            contentDescription = "Add to playlist",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                if (playableItems.size == 1) {
                                    ElementWithPlainTooltip(tooltip = "Skip to queue item") {
                                        IconButton(
                                            onClick = {
                                                MediaPlayerUtilities.skipToQueueItemAsync(
                                                    playableItems[0]
                                                )
                                            }) {
                                            Image(
                                                painterResource(id = R.drawable.skiptoqueueitem),
                                                contentDescription = "Skip to queue item",
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }

                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                bottomBar = {
                    BottomAppBar(
                    ) {
                        BottomAppBarRenderer.CreateBottomAppBar(this@MainActivity, 0)
                    }
                },
                content = {  RenderScreen(mediaItemsResult)
                },
                scrollBehavior
            );
        }
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

    @Composable
    fun RenderScreen(mediaItemsResult: List<MediaItem>) {
        // A surface container using the 'background' color from the theme

        currentMediaItems = mediaItemsResult;
        Column {

            Column(Modifier.weight(1f)) {
                if (mediaItemsResult.size == 1 && mediaItemsResult.get(0).mediaMetadata.folderType == MediaMetadata.FOLDER_TYPE_NONE) {

                    val item = mediaItemsResult.get(0)
                    MediaBrowserMediaItemRenderer.RenderSingleItem(
                        data = item,
                        isHorizontal = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
                        playMethod = {
                            MediaPlayerUtilities.playMediaItemsAsync(
                                currentMediaItems
                            )
                        },
                        enqueueMethod = {
                            MediaPlayerUtilities.addMediaItemsToNowPlayingAsync(
                                currentMediaItems
                            )
                        },
                        addToPlaylist = {},
                        SkipToIndexMethod = {
                            MediaPlayerUtilities.skipToQueueItemAsync(
                                currentMediaItems.get(0)
                            )
                        }
                    )

                } else {
                    val listState = rememberLazyListState()
                    LaunchedEffect(mediaItemsResult.size) {
                        listState.scrollToItem(0)
                    }
                    LazyColumn(state = listState) {
                        items(items = mediaItemsResult, itemContent = { item ->

                            MediaBrowserMediaItemRenderer.RenderListView(
                                data = item, clickMethod = {
                                    if (item.mediaMetadata.folderType == FOLDER_TYPE_TITLES) {

                                        MediaPlayerUtilities.playMediaItemsAsync(listOf(item));

                                    } else {
                                        backSack.Push(currentMediaId!!);
                                        openMediaItem(it.mediaId)
                                    }
                                }
                            )
                        })
                    }
                }
            }
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