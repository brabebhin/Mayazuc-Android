package ionic.mayazuc

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import ionic.mayazuc.BottomAppBarRenderer.CreateBottomAppBar
import ionic.mayazuc.UiUtilities.MayazucScaffold
import ionic.mayazuc.ui.theme.MayazucLiteTheme

class PlaybackQueueActivity : MediaControllerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MayazucLiteTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //empty content
                }
            }
        }
    }

    override fun setController() {
        updateCurrentPlaylistUI()
    }


    private fun updateCurrentPlaylistUI() {

        renderUI(getPlaylistFromController())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun renderUI(items: List<MediaItem>) {
        setContent {
            MayazucLiteTheme {
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                val showPlayingNameInput = remember { mutableStateOf(false) }
                val playlistName = remember  { mutableStateOf("new playlist") }

                MayazucScaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(
                                    text = "Playback queue",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            actions = {
                                UiUtilities.ElementWithPlainTooltip(tooltip = "Save as playlist") {
                                    IconButton(onClick = {
                                        showPlayingNameInput.value = true;
                                    }) {
                                        Image(
                                            painterResource(id = R.drawable.playviewgeneric),
                                            contentDescription = "Save as playlist",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    };
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    },
                    bottomBar = {
                        BottomAppBar() {
                            CreateBottomAppBar(this@PlaybackQueueActivity, 2)
                        }
                    },
                    content = {
                        Column {
                            LazyColumn() {
                                itemsIndexed(items = items, itemContent = { index, item ->
                                    MediaBrowserMediaItemRenderer.RenderListView(
                                        data = item, index = index + 1, clickMethod = {
                                            controller?.seekTo(items.indexOf(item), 0)
                                            controller?.prepare()
                                            controller?.play()
                                        }
                                    )
                                })
                            }
                        }

                        UiUtilities.textInputDialog(
                            showPlayingNameInput,
                            playlistName,
                            label = "Input playlist name",
                            onOkCallback = {}
                        );
                    },
                    scrollBehavior
                )
            }
        }
    }
}
