package ionic.mayazuc

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.*
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import org.videolan.libvlc.LibVLC
import java.util.LinkedList
import java.util.concurrent.Executors

@UnstableApi
class PlaybackService : MediaLibraryService() {

    private lateinit var librarySessionCallback: CustomMediaLibrarySessionCallback;
    private lateinit var player: Player
    private lateinit var mediaLibrarySession: MediaLibrarySession
    private lateinit var audioBecomingNoisyReceiver: AudioBecomingNoisyReciever;

    private lateinit var binder: PlaybackServiceBinder
    private var currentMediaItemsClone: ImmutableList<MediaItem> =
        ImmutableList.copyOf(emptyList<MediaItem>());
    private lateinit var LibVLC: LibVLC;

    override fun onCreate() {
        super.onCreate()
        binder = PlaybackServiceBinder(this);
        initializeSessionAndPlayer()
        RestoreNowPlaying()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (intent?.action == null) return binder;
        return super.onBind(intent)
    }

    override fun onDestroy() {
        player.release()
        mediaLibrarySession.release()
        unregisterReceiver(audioBecomingNoisyReceiver);
        super.onDestroy()
    }

    private fun RestoreNowPlaying() {
        val savedItems = NowPlayingManager.loadNowPlaying()
        if (savedItems.isNotEmpty()) {
            val items = MediaItemTree.getItems(savedItems)
            currentMediaItemsClone = ImmutableList.copyOf(items);

            player.addMediaItems(ArrayList<MediaItem>(items));
            player.prepare();
        }
    }

    private fun initializeSessionAndPlayer() {
        LibVLC = LibVLC(this);
        val libVlcPlayer = LibVlcPlayer(mainLooper, LibVLC, this, LibVlcPlayerCallback(this));
        player = LibVlcForwardingPlayer(libVlcPlayer);
        librarySessionCallback = CustomMediaLibrarySessionCallback(player)
        audioBecomingNoisyReceiver = AudioBecomingNoisyReciever(libVlcPlayer as LibVlcPlayer)
        registerReceiver(
            audioBecomingNoisyReceiver,
            IntentFilter("android.media.AUDIO_BECOMING_NOISY")
        );

        MediaItemTree.initialize()
        val sessionActivityPendingIntent = TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))

            val immutableFlag = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
            getPendingIntent(0, immutableFlag or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        player.shuffleModeEnabled = SettingsWrapper.ShuffleEnabled()
        player.repeatMode = SettingsWrapper.RepeatMode()
        player.addListener(object : Player.Listener {

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                SettingsWrapper.ShuffleEnabled(shuffleModeEnabled)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                super.onRepeatModeChanged(repeatMode)
                SettingsWrapper.RepeatMode(repeatMode)
            }

        })

        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, librarySessionCallback)
                .setSessionActivity(sessionActivityPendingIntent).build()
        setMediaNotificationProvider(MediaNotificationBuilder(context = MCApplication.context!!))
    }

    private inner class CustomMediaLibrarySessionCallback(private val player: Player) :
        MediaLibrarySession.Callback {

        override fun onConnect(
            session: MediaSession, controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(), connectionResult.availablePlayerCommands
            )
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(
                LibraryResult.ofItem(
                    MediaItemTree.getRootItem(), params
                )
            )
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val item = MediaItemTree.getItem(mediaId.SafeMediaId())
            if (item == null)
                return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
            return Futures.immediateFuture(LibraryResult.ofItem(item, /* params= */ null))
        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            val children =
                MediaItemTree.getChildren(parentId, false) ?: return Futures.immediateFuture(
                    LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                )
            session.notifyChildrenChanged(browser, parentId, children.size, params)
            return Futures.immediateFuture(LibraryResult.ofVoid())
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val children =
                MediaItemTree.getChildren(parentId, false) ?: return Futures.immediateFuture(
                    LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                )
            return Futures.immediateFuture(LibraryResult.ofItemList(children, params))
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {

            val updatedMediaItems = OnAddMediaItemsInternal(
                controller.packageName.contains("com.mcosmin.MayazucLite"),
                mediaItems
            )
            return updatedMediaItems;
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>
        ): ListenableFuture<List<MediaItem>> {

            val returnFuture = SettableFuture.create<List<MediaItem>>()
            val updatedMediaItems = OnAddMediaItemsInternal(
                controller.packageName.contains("com.mcosmin.MayazucLite"),
                mediaItems
            )
            updatedMediaItems.addListener({
                returnFuture.set(updatedMediaItems.get().mediaItems)
            }, MoreExecutors.directExecutor());

            return returnFuture;
        }

        fun OnAddMediaItemsInternal(
            isLocal: Boolean,
            mediaItems: List<MediaItem>
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {

            val returnValueFuture =
                SettableFuture.create<MediaSession.MediaItemsWithStartPosition>();
            val playerCurrentIndex = player.currentPeriodIndex;
            val playerCurrentTime = TIME_CURRENT_POSITION;

            Executors.newCachedThreadPool().execute(Runnable {

                var startIndex = 0;
                var startTime = 0L;

                val updatedMediaItems = LinkedHashSet<MediaItem>();
                for (item in mediaItems) {
                    val itemId = item.mediaId;
                    //play command incoming from subscribers (e.g. Android Auto). This is always a play command
                    if (itemId.isPlayCommand()) {
                        updatedMediaItems.addAll(
                            MediaItemTree.getChildren(
                                item.mediaId, true
                            )!!
                        )

                        if (itemId.isStartFromItemCommand()) {

                            //we need to start playback from a specific item in this list
                            updatedMediaItems.forEachIndexed { index, mediaItem ->
                                if (mediaItem.SafeMediaId() == item.SafeMediaId()) {
                                    startIndex = index;
                                    return@forEachIndexed
                                }
                            }
                        }
                    } else if (itemId.isEnqueueCommand()) {

                        val existingQueue = currentMediaItemsClone;
                        updatedMediaItems.addAll(existingQueue);

                        updatedMediaItems.addAll(
                            MediaItemTree.getChildren(
                                item.mediaId, true
                            )!!
                        )

                        startIndex = playerCurrentIndex;
                        startTime = playerCurrentTime;
                    }
                    //all other commands
                    else {

                        val mediaItemInQuestion = MediaItemTree.getItem(itemId.SafeMediaId())!!;

                        if (SettingsWrapper.SkipToQueueItemInExternalControllers() && !isLocal) {
                            val existingQueue = currentMediaItemsClone;
                            val indexOfExisting = existingQueue.indexOf(mediaItemInQuestion);
                            updatedMediaItems.addAll(existingQueue);

                            if (indexOfExisting == -1) {
                                //somehow signal the player to skip to the existing index
                                updatedMediaItems.add(mediaItemInQuestion)
                            } else {
                                startIndex = indexOfExisting;
                            }

                        } else {
                            updatedMediaItems.add(mediaItemInQuestion)
                        }
                    }
                }

                currentMediaItemsClone = ImmutableList.copyOf(updatedMediaItems);

                NowPlayingManager.saveNowPlaying(updatedMediaItems.toList())

                returnValueFuture.set(
                    MediaSession.MediaItemsWithStartPosition(
                        updatedMediaItems.toList(),
                        startIndex,
                        startTime
                    )
                );
            });

            return returnValueFuture;
        }

    }

    private inner class LibVlcPlayerCallback(
        private val playbackService: PlaybackService
    ) : LibVlcPlayer.Callback {
        override fun NotifyCategoryChange(currentMediaItem: MediaItem?) {
            if (currentMediaItem == null)
                return;

            val items = LinkedList<MediaItem>();
            //get the next category

            val nextMediaItem = MediaItemTree.getNextCategory(currentMediaItem);
            val nextItemTask = playbackService.librarySessionCallback.OnAddMediaItemsInternal(
                true,
                mutableListOf(nextMediaItem)
            );
            nextItemTask.addListener({
                val handler = Handler(player.applicationLooper);
                handler.post({
                    playbackService.player.setMediaItems(nextItemTask.get().mediaItems);
                })
            }, MoreExecutors.directExecutor());
        }
    }
}

