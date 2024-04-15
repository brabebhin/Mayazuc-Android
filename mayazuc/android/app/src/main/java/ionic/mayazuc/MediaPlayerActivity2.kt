package ionic.mayazuc

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL
import androidx.media3.common.util.RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerControlView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ionic.mayazuc.UiUtilities.MayazucScaffold
import ionic.mayazuc.ui.theme.MayazucLiteTheme
import java.util.Timer
import java.util.TimerTask

@UnstableApi
class MediaPlayerActivity2 : MediaControllerActivity() {

    var albumArtState: MutableState<String> = mutableStateOf("")
    var mediaTitleState: MutableState<String> = mutableStateOf("Title")
    var mediaSubtitleState: MutableState<String> = mutableStateOf("Subtitle")

    var normalizedPosition: MutableState<Float> = mutableStateOf(0F)
    var humanReadablePosition: MutableState<Long> = mutableStateOf(0L)
    var totalDuration: MutableState<Long> = mutableStateOf(0L)
    var remainingTime: MutableState<Long> = mutableStateOf(0L)

    var isPlaying: MutableState<Boolean> = mutableStateOf(false);
    lateinit var isSeekBarPressed: State<Boolean>;
    val isSeekBarPressedState: MutableState<Boolean> = mutableStateOf(false);

    val repeatMode: MutableState<Int> = mutableStateOf(Player.REPEAT_MODE_ONE);
    val isShuffleEnabled: MutableState<Boolean> = mutableStateOf(false);

    lateinit var timer: Timer;

    override fun setController() {
        super.controller?.addListener(playerCallback)
        RenderUI()
        val playerInstance = super.controller!!;
        timer =
            Timer().apply {
                val task = object : TimerTask() {
                    override fun run() {
                        Handler(playerInstance.applicationLooper).post(Runnable {

                            isPlaying.value = playerInstance.isPlaying;
                            isShuffleEnabled.value = playerInstance.shuffleModeEnabled;
                            repeatMode.value = playerInstance.repeatMode;

                            if (playerInstance.contentDuration == 0L) {
                                if (!isSeekBarPressedState.value) {
                                    normalizedPosition.value = 0F
                                }
                                humanReadablePosition.value = 0L;
                                totalDuration.value = 0L;
                                remainingTime.value = 0L;
                            };
                            else {
                                if (!isSeekBarPressedState.value) {
                                    normalizedPosition.value =
                                        playerInstance.contentPosition.toFloat() / playerInstance.contentDuration.toFloat();
                                }
                                humanReadablePosition.value = playerInstance.contentPosition
                                totalDuration.value = playerInstance.contentDuration
                                remainingTime.value =
                                    playerInstance.contentDuration - playerInstance.contentPosition
                            }
                        })
                    }
                }
                scheduleAtFixedRate(task, 10L, 100L)
            }
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel();
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun RenderUI() {

        setContent {
            MayazucLiteTheme {

                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                MayazucScaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(
                                    text = "Now playing",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            scrollBehavior = scrollBehavior,

                            )
                    },
                    bottomBar = {
                        BottomAppBar() {
                            BottomAppBarRenderer.CreateBottomAppBar(this@MediaPlayerActivity2, 1)
                        }
                    },
                    content = {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {

                            Row(modifier = Modifier.fillMaxWidth())
                            {
                                Row(modifier = Modifier.weight(1f))
                                {
                                    RenderAlbumArt()
                                }
                                Row(modifier = Modifier.weight(1f))
                                {
                                    PlayerControlBarSeekBar()
                                }
                            }
                        } else {
                            Column {
                                Column() {
                                    RenderTitleSubtitle()
                                }
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .align(alignment = Alignment.CenterHorizontally)
                                ) {
                                    RenderAlbumArt()
                                }

                                PlayerControlBarSeekBar()


                                /* Column(Modifier.height(230.dp)) {
                                     MediaPlayerController(
                                         exoPlayer = super.controller!!
                                     )
                                 }*/
                            }
                        }
                    },
                    scrollBehavior
                )

                // A surface container using the 'background' color from the theme

            }
        }

        albumArtState.value = super.controller?.mediaMetadata?.artworkUri.toString();
        mediaTitleState.value = super.controller?.mediaMetadata?.title.toString();
        mediaSubtitleState.value =
            if (super.controller?.mediaMetadata?.subtitle != null) super.controller?.mediaMetadata?.subtitle.toString() else "";

    }

    @Composable
    private fun PlayerControlBarSeekBar() {
        Column() {


            PlayerControlBar.playerSeekBar(
                normalizedCurrentPosition = normalizedPosition,
                humanReadablePosition = humanReadablePosition,
                totalDuration = totalDuration,
                remainingTime = remainingTime,
                onSeekCallback = {
                    if (controller?.contentDuration != 0L) {
                        controller?.seekTo((it * controller?.contentDuration!!).toLong());
                    }
                },
                isSeekBarPressedState
            );

            PlayerControlBar.playerCommandBar(
                isPlaying = isPlaying,
                onPreviousCallback = {
                    controller?.seekToPrevious();
                },
                onFastForwardCallback = {
                    controller?.seekForward();
                },
                onPlayPauseCallback = {
                    if (controller?.isPlaying!!) controller?.pause() else controller?.play();
                },
                onNextCallback = {
                    controller?.seekToNext();
                },
                onRewindCallback = {
                    controller?.seekBack();
                },
                onRepeatCallback = {
                    val repeatMode = controller?.repeatMode!!;
                    if (repeatMode == Player.REPEAT_MODE_ALL) {
                        controller?.repeatMode = Player.REPEAT_MODE_ONE;
                    }
                    if (repeatMode == Player.REPEAT_MODE_ONE) {
                        controller?.repeatMode = Player.REPEAT_MODE_OFF;
                    }
                    if (repeatMode == Player.REPEAT_MODE_OFF) {
                        controller?.repeatMode = Player.REPEAT_MODE_ALL;
                    }
                },
                onShuffleCallback = {
                    controller?.shuffleModeEnabled =
                        !controller?.shuffleModeEnabled!!;
                },
                repeatMode = repeatMode,
                isShuffleEnabled = isShuffleEnabled
            )
        }
    }

    val playerCallback = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {

            //updateMediaMetadataUI(mediaItem?.mediaMetadata ?: MediaMetadata.EMPTY)
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
            mediaTitleState.value = mediaMetadata.title.toString();
            mediaSubtitleState.value =
                if (mediaMetadata.subtitle != null) mediaMetadata.subtitle.toString() else "";
            albumArtState.value = mediaMetadata.artworkUri.toString();
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MayazucLiteTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    //empty content
                }
            }
        }
    }

    @Composable
    fun RenderTitleSubtitle() {
        Column() {
            Text(
                mediaTitleState.value, modifier = Modifier.padding(5.dp), maxLines = 2
            )
            Text(
                mediaSubtitleState.value, modifier = Modifier.padding(5.dp), maxLines = 1
            )
        }
    }

    @Composable
    fun RenderAlbumArt() {
        val stringState = remember { albumArtState }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(Uri.parse(stringState.value))
                .build(),
            contentDescription = "Album art",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(1f)

        )
    }

    @Composable
    fun MediaPlayerController(
        exoPlayer: Player,
    ) {
        val context = LocalContext.current

        val androidView = AndroidView(modifier = Modifier
            .height(230.dp)
            .fillMaxWidth()
            .fillMaxHeight(1f)
            .background(Color.Transparent),
            factory = {
                PlayerControlView(context).apply {
                    this.player = exoPlayer
                    showSubtitleButton = false
                    setShowPreviousButton(true)
                    showSubtitleButton = false
                    setShowNextButton(true)
                    showShuffleButton = true
                    repeatToggleModes = REPEAT_TOGGLE_MODE_ALL or REPEAT_TOGGLE_MODE_ONE
                    showTimeoutMs = 0
                }
            }, update = {
                it.apply {
                    this.player = exoPlayer
                    showSubtitleButton = false
                    setShowPreviousButton(true)
                    setShowNextButton(true)
                    showShuffleButton = true
                    repeatToggleModes = REPEAT_TOGGLE_MODE_ALL or REPEAT_TOGGLE_MODE_ONE
                    showTimeoutMs = 0
                    setBackgroundColor(Color.Transparent.toArgb())
                }
            })
        DisposableEffect(key1 = androidView) {
            onDispose {
                exoPlayer.release()
            }
        }
    }
}