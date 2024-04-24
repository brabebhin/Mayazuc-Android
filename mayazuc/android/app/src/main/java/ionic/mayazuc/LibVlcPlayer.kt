package ionic.mayazuc

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Looper
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import org.videolan.libvlc.*

public const val SeekBackPreviousMaxLength: Long = 5000
public const val SeekBackIncrement: Long = 5000
public const val SeekForwardIcrement: Long = 15000
const val TIME_CURRENT_POSITION = -3423901L;

@UnstableApi
class LibVlcPlayer(
    val looper: Looper,
    val libVLC: LibVLC,
    val context: Context,
    val categoryChangeCallback: Callback
) :
    SimpleBasePlayer(looper), AudioManager.OnAudioFocusChangeListener {
    private var actualPositionWhenPaused = C.TIME_UNSET;

    private val mediaItemsList: ArrayList<MediaItem> = ArrayList()
    private var player: MediaPlayer = MediaPlayer(libVLC);

    private var playWhenReady: Boolean = false;
    private var repeatMode: Int = REPEAT_MODE_ALL;

    private var shuffleModeEnabled: Boolean = false;
    private var currentIndex: Int = 0;
    private var randomizer = RandomQueueNavigator();

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var pausedBecauseNoisy: Boolean? = null;

    private var pausedCauseOfVolumeOut: Boolean = false;

    private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA).build()
        )
        .setAcceptsDelayedFocusGain(true)
        .setOnAudioFocusChangeListener(this)
        .build()

    private var currentMediaItem: MediaItem? = null;
    private var currentMediaItemMetadata: MediaMetadata? = null;
    private var hasAudioFocus = false;

    private val AvailableCommands: Player.Commands =
        Player.Commands.Builder()
            .addAllCommands()
            .build()

    init {
        player.setAudioDigitalOutputEnabled(false)
        setDefaultPlayerEventListener()
    }

    private fun setDefaultPlayerEventListener() {

        player.setEventListener { event ->
            when (event?.type) {
                (MediaPlayer.Event.Playing) -> {
                    pausedBecauseNoisy = false;
                    pausedCauseOfVolumeOut = false;
                    super.invalidateState();
                }

                (MediaPlayer.Event.MediaChanged) -> {
                    if (currentMediaItem != null)
                        getMediaItemMediaMetadataArtworkBytes(currentMediaItem!!);
                    super.invalidateState();
                }

                (MediaPlayer.Event.TimeChanged) -> {
                    if (player.isPlaying)
                        super.invalidateState();
                }

                (MediaPlayer.Event.Paused) -> {
                    actualPositionWhenPaused = player.time
                    super.invalidateState();
                }

                (MediaPlayer.Event.EndReached) -> {
                    preparePlayback(
                        GetNextIndex(
                            mediaItemsList.size,
                            currentIndex,
                            false,
                            repeatMode,
                            shuffleModeEnabled
                        ), 0, true
                    )
                    super.invalidateState();
                }

                (MediaPlayer.Event.Stopped) -> {

                }

                (MediaPlayer.Event.LengthChanged) -> super.invalidateState()
            }
        };
    }

    fun seekToNext2() {
        preparePlayback(
            GetNextIndex(
                mediaItemsList.size,
                currentIndex,
                false,
                repeatMode,
                shuffleModeEnabled
            ), 0, true
        )
    }

    fun isAtEndOfQueue(): Boolean {
        if (shuffleModeEnabled) {
            return randomizer.isAtEndOfQueue();
        } else {
            return currentIndex == mediaItemsList.size - 1;
        }
    }

    override fun handleSetDeviceMuted(muted: Boolean, flags: Int): ListenableFuture<*> {
        //player.volume = 0;
        //HandleMuteEx()
        return super.handleSetDeviceMuted(muted, flags)
    }

    override fun handleSetDeviceVolume(deviceVolume: Int, flags: Int): ListenableFuture<*> {
        /*player.volume = deviceVolume;
        if(player.volume == 0)
        {
            HandleMuteEx()
        }
        else if(pausedCauseOfVolumeOut && playWhenReady)
        {
            player.play()
        }*/
        return super.handleSetDeviceVolume(deviceVolume, flags)
    }

    private fun HandleMuteEx() {
        if (player.isPlaying) {
            pausedCauseOfVolumeOut = true;
            player.pause()
        }
    }

    override fun getState(): State {
        try {
            val position = getContentPositionInternal();
            return State.Builder().setAvailableCommands(AvailableCommands)
                .setPlayWhenReady(
                    getPlayWhenReadyInternal(),
                    if (pausedBecauseNoisy != null && pausedBecauseNoisy!!) PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY else
                        if (hasAudioFocus) PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST else PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS
                )
                .setCurrentMediaItemIndex(currentIndex)
                .setContentPositionMs { position; }
                .setContentBufferedPositionMs { duration; }
                .setRepeatMode(repeatMode)
                .setShuffleModeEnabled(shuffleModeEnabled)
                .setSeekBackIncrementMs(SeekBackIncrement)
                .setSeekForwardIncrementMs(SeekForwardIcrement)
                .setMaxSeekToPreviousPositionMs(SeekBackPreviousMaxLength)
                .setAudioAttributes(
                    getAudioAttributesEx()
                )
                .setTotalBufferedDurationMs { duration }
                .setPlaylist(getPlaylist())
                .setIsLoading(false)
                .setPlaybackState(getPlaybackStateInternal())
                .build()
        } catch (ex: Exception) {
            return State.Builder().setAvailableCommands(AvailableCommands)
                .setPlayWhenReady(
                    false,
                    PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
                )
                .setCurrentMediaItemIndex(C.INDEX_UNSET)
                .setContentPositionMs { C.TIME_UNSET; }
                .setContentBufferedPositionMs { C.TIME_UNSET; }
                .setRepeatMode(repeatMode)
                .setShuffleModeEnabled(shuffleModeEnabled)
                .setSeekBackIncrementMs(SeekBackIncrement)
                .setSeekForwardIncrementMs(SeekForwardIcrement)
                .setMaxSeekToPreviousPositionMs(SeekBackPreviousMaxLength)
                .setAudioAttributes(
                    getAudioAttributesEx()
                )
                .setTotalBufferedDurationMs { C.TIME_UNSET }
                .setPlaylist(getPlaylist())
                .setIsLoading(false)
                .setPlaybackState(getPlaybackStateInternal())
                .build()
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (!hasAudioFocus) {
            val result = audioManager.requestAudioFocus(audioFocusRequest);
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
        return hasAudioFocus;
    }

    private fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest);
        hasAudioFocus = false;
    }

    private fun getAudioAttributesEx() = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build()

    private fun getPlaylist(): MutableList<SimpleBasePlayer.MediaItemData> {
        val returnList = mutableListOf<SimpleBasePlayer.MediaItemData>()
        if (!player.isReleased) {
            mediaItemsList.forEachIndexed(action = { index, mediaItem ->
                val duration =
                    if (index == currentIndex) getContentDurationUsInternal() else C.TIME_UNSET;
                returnList.add(
                    SimpleBasePlayer.MediaItemData.Builder(mediaItem.hashCode())
                        .setMediaItem(mediaItem)
                        .setDurationUs(duration)
                        .setIsSeekable(true)
                        .setLiveConfiguration(null)
                        .setDefaultPositionUs(0)
                        .setMediaMetadata(currentMediaItemMetadata)
                        .setIsPlaceholder((index == currentIndex))
                        .setPeriods(getPeriods(mediaItem, duration))
                        .build()
                );
            });
        }
        return returnList;
    }

    private fun getMediaItemMediaMetadataArtworkBytes(mediaItem: MediaItem): MediaMetadata {

        if (mediaItem.mediaMetadata.artworkUri != null) {
            currentMediaItemMetadata = mediaItem.mediaMetadata.buildUpon().setArtworkData(
                MCApplication.context?.contentResolver?.openInputStream(mediaItem.mediaMetadata.artworkUri!!)
                    ?.use { it.buffered().readBytes() }, MediaMetadata.PICTURE_TYPE_MEDIA
            ).build();

            return currentMediaItemMetadata!!;
        }

        currentMediaItemMetadata = mediaItem.mediaMetadata;
        return currentMediaItemMetadata!!;
    }

    private fun getPeriods(
        item: MediaItem,
        durationUs: Long
    ): MutableList<SimpleBasePlayer.PeriodData> {
        val value =
            SimpleBasePlayer.PeriodData.Builder(item.hashCode())
                .setIsPlaceholder(false)
                .setDurationUs(durationUs).build()
        return mutableListOf(value);
    }

    override fun handleSetPlaybackParameters(playbackParameters: PlaybackParameters): ListenableFuture<*> {
        player.rate = playbackParameters.speed

        return Futures.immediateFuture(true);
    }

    override fun handleAddMediaItems(
        index: Int,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<*> {
        return Futures.immediateFuture(addMediaItemsInternal(index, mediaItems));
    }

    override fun handleMoveMediaItems(
        fromIndex: Int,
        toIndex: Int,
        newIndex: Int
    ): ListenableFuture<*> {
        return Futures.immediateFuture(moveMediaItemsInternal(fromIndex, toIndex, newIndex));
    }

    override fun handlePrepare(): ListenableFuture<*> {
        return Futures.immediateFuture(prepareInternal())
    }

    override fun getPlaceholderMediaItemData(mediaItem: MediaItem): MediaItemData {
        return super.getPlaceholderMediaItemData(mediaItem)
    }

    override fun handleRelease(): ListenableFuture<*> {

        Log.w("Mayazuc media player", "Releasing media service");
        player.setEventListener { null };
        player.media?.release();
        player.media = null;
        player.release();

        return Futures.immediateFuture(true);
    }

    override fun handleRemoveMediaItems(fromIndex: Int, toIndex: Int): ListenableFuture<*> {
        return Futures.immediateFuture(removeMediaItemsEx(fromIndex, toIndex))
    }

    override fun handleSeek(
        mediaItemIndex: Int,
        positionMs: Long,
        seekCommand: Int
    ): ListenableFuture<*> {

        seekToInternal(mediaItemIndex, positionMs)
        return Futures.immediateFuture(Any())
    }

    override fun handleSetMediaItems(
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<*> {
        return Futures.immediateFuture(
            setMediaItemsInternal(
                mediaItems,
                startIndex,
                startPositionMs
            )
        )
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        if (!playWhenReady) {
            abandonAudioFocus()
        }
        return Futures.immediateFuture(setPlayWhenReadyInternal(playWhenReady))
    }

    override fun handleSetRepeatMode(repeatMode: Int): ListenableFuture<*> {
        return Futures.immediateFuture(setRepeatModeInternal(repeatMode))
    }

    override fun handleSetShuffleModeEnabled(shuffleModeEnabled: Boolean): ListenableFuture<*> {
        return Futures.immediateFuture(setShuffleModeEnabledInternal(shuffleModeEnabled))
    }

    private fun preparePlayback(index: Int, startPositionMs: Long, startPlayback: Boolean) {
        if (mediaItemsList.size <= index || index < 0) return;
        val mediaItem = mediaItemsList[index];
        val media = Media(libVLC, mediaItem.SafeMediaId());
        setPlayerMedia(media, index, mediaItem)
        if (startPlayback && requestAudioFocus())
            player.play()
    }

    private fun setPlayerMedia(media: Media, index: Int, mediaItem: MediaItem) {
        media.addOption(":no-video")
        currentMediaItem = mediaItem
        player.media = media;
        currentIndex = index;
    }

    private fun getCurrentPositionInternal(): Long {
        if (player.isReleased) return C.TIME_UNSET;
        if (!player.isPlaying) return actualPositionWhenPaused;

        if (player.length >= 0 && player.time >= 0) {
            return player.time
        } else return C.TIME_UNSET;
    }

    private fun stopPlayback() {
        player.setEventListener { null };
        player.stop();
        setDefaultPlayerEventListener();
    }

    private fun setMediaItemsInternal(
        mediaItems: MutableList<MediaItem>, startIndex: Int, startPositionMs: Long
    ) {
        val requiresPlaybackRestart = startPositionMs != TIME_CURRENT_POSITION;
        mediaItemsList.clear()
        mediaItemsList.addAll(mediaItems);
        randomizer.resetIndices(mediaItems.size, currentIndex)
        if (requiresPlaybackRestart) {
            preparePlayback(if (startIndex < 0) 0 else startIndex, startPositionMs, true);
        }
    }

    private fun addMediaItemsInternal(index: Int, mediaItems: MutableList<MediaItem>) {
        mediaItemsList.addAll(index, mediaItems);
        randomizer.resetIndices(mediaItems.size, currentIndex)
        if (currentMediaItem != null)
            currentIndex = findCurrentIndex(currentMediaItem!!.mediaId, mediaItemsList);
    }

    private fun moveMediaItemsInternal(fromIndex: Int, toIndex: Int, newIndex: Int) {

        val list = removeItemsFromMediaList(toIndex, fromIndex)

        mediaItemsList.addAll(newIndex, list);
        if (currentMediaItem != null)
            currentIndex = findCurrentIndex(currentMediaItem!!.mediaId, mediaItemsList);

        randomizer.resetIndices(mediaItemsList.size, currentIndex);
    }

    private fun findCurrentIndex(currentMediaId: String, items: MutableList<MediaItem>): Int {

        for (i in items.indices) {
            if (items[i].mediaId == currentMediaId)
                return i;
        }

        return -1;
    }

    private fun removeItemsFromMediaList(
        toIndex: Int,
        fromIndex: Int
    ): MutableList<MediaItem> {
        var noItemsToRemove = toIndex - fromIndex;
        val list = mutableListOf<MediaItem>()
        while (noItemsToRemove > 0) {
            list.add(mediaItemsList.removeAt(fromIndex));
            noItemsToRemove--;
        }
        randomizer.resetIndices(mediaItemsList.size, currentIndex);
        return list
    }

    override fun handleClearVideoOutput(videoOutput: Any?): ListenableFuture<*> {
        return Futures.immediateFuture(true)
    }

    private fun removeMediaItemsEx(fromIndex: Int, toIndex: Int) {
        removeItemsFromMediaList(toIndex, fromIndex)
    }

    private fun prepareInternal() {
        if (player.media == null)
        {
            currentIndex = (0..mediaItemsList.size).random();
            preparePlayback(currentIndex, 0, playWhenReady);
        }
    }

    private fun getPlaybackStateInternal(): Int {
        if (player.isReleased) return Player.STATE_ENDED;
        when (player.playerState) {
            (0) -> return Player.STATE_IDLE;
            (1) -> return Player.STATE_BUFFERING
            (2) -> return Player.STATE_READY;
            (3) -> return Player.STATE_READY;
            (4) -> return Player.STATE_READY;
            (5) -> return Player.STATE_ENDED;
            (6) -> return Player.STATE_ENDED;
        }
        return Player.STATE_IDLE;
    }

    override fun handleStop(): ListenableFuture<*> {
        stopPlayback()
        return Futures.immediateFuture(Any());
    }

    private fun setPlayWhenReadyInternal(playWhenReady: Boolean) {
        this.playWhenReady = playWhenReady;
        if (player.hasMedia()) {
            if (playWhenReady) {
                if (requestAudioFocus())
                    player.play()
            } else {
                player.pause()
            }
        }
    }

    private fun getPlayWhenReadyInternal(): Boolean {
        if (player.hasMedia())
            return player.isPlaying;
        return playWhenReady;
    }

    private fun setRepeatModeInternal(repeatMode: Int) {
        this.repeatMode = repeatMode;
    }


    private fun setShuffleModeEnabledInternal(shuffleModeEnabled: Boolean) {
        this.shuffleModeEnabled = shuffleModeEnabled;
        randomizer.resetIndices(mediaItemsList.size, currentIndex);
        randomizer.seek(currentIndex);
    }

    private fun seekToInternal(positionMs: Long) {
        // 1 ... duration
        //X...positionms
        if (!player.isPlaying) {
            actualPositionWhenPaused = positionMs;
        }
        player.nativeSetTime(positionMs, true);
    }

    private fun seekToInternal(mediaItemIndex: Int, positionMs: Long) {
        if (mediaItemIndex == currentIndex && positionMs > 0L) {
            seekToInternal(positionMs)
        } else {
            preparePlayback(if (mediaItemIndex < 0) 0 else mediaItemIndex, positionMs, true);

            randomizer.seek(currentIndex);
        }
    }

    private fun getContentDurationUsInternal(): Long {
        if (!player.isReleased && player.length >= 0)
            return player.length * 1000;
        return C.TIME_UNSET;
    }

    private fun getContentPositionInternal(): Long {
        return getCurrentPositionInternal()
    }

    fun notifyBecomingNoisy() {
        pausedBecauseNoisy = true;
        pause();
    }

    private fun GetNextIndex(
        maxSize: Int,
        currentIndex: Int,
        userAction: Boolean,
        repeatMode: Int,
        shuffleModeEnabled: Boolean
    ): Int {
        if (userAction) {

            when (repeatMode) {
                (REPEAT_MODE_ALL) -> return RepeatModeAllNextIndex(
                    shuffleModeEnabled,
                    repeatMode,
                    currentIndex,
                    maxSize
                );
                (REPEAT_MODE_ONE) -> return RepeatModeAllNextIndex(
                    shuffleModeEnabled,
                    repeatMode,
                    currentIndex,
                    maxSize
                );
                (REPEAT_MODE_OFF) -> {
                    return HandleRepeatModeOffNextIndexWithCategoryChange(
                        currentIndex,
                        maxSize,
                        shuffleModeEnabled,
                        repeatMode
                    )
                }
            }

            return simpleIncrementWithOverflow(currentIndex, maxSize)
        } else {
            when (repeatMode) {
                (REPEAT_MODE_ALL) -> {
                    return RepeatModeAllNextIndex(
                        shuffleModeEnabled,
                        repeatMode,
                        currentIndex,
                        maxSize
                    );
                }

                (REPEAT_MODE_ONE) -> return currentIndex;
                (REPEAT_MODE_OFF) -> {
                    return HandleRepeatModeOffNextIndexWithCategoryChange(
                        currentIndex,
                        maxSize,
                        shuffleModeEnabled,
                        repeatMode
                    )
                }
            }

            return simpleIncrementWithOverflow(currentIndex, maxSize);
        }
    }

    private fun HandleRepeatModeOffNextIndexWithCategoryChange(
        currentIndex: Int,
        maxSize: Int,
        shuffleModeEnabled: Boolean,
        repeatMode: Int
    ): Int {
        return if (isAtEndOfQueue()) requestChangeInPlaybackQueue(
            currentIndex,
            maxSize
        );
        else return RepeatModeAllNextIndex(shuffleModeEnabled, repeatMode, currentIndex, maxSize);
    }

    private fun RepeatModeAllNextIndex(
        shuffleModeEnabled: Boolean,
        repeatMode: Int,
        currentIndex: Int,
        maxSize: Int
    ): Int {
        return if (shuffleModeEnabled) getRandomIndex(
            repeatMode,
            currentIndex,
            maxSize
        )
        else simpleIncrementWithOverflow(currentIndex, maxSize)
    }

    private fun getRandomIndex(repeatMode: Int, currentIndex: Int, maxSize: Int): Int {
        if (randomizer.isAtEndOfQueue() && repeatMode == REPEAT_MODE_OFF) {
            return requestChangeInPlaybackQueue(currentIndex, maxSize);
        } else {
            return randomizer.getNextIndex();
        }
    }

    private fun requestChangeInPlaybackQueue(currentIndex: Int, maxSize: Int): Int {
        // when repeat mode is off, when reaching the end of the current queue, switch to the next folder.
        categoryChangeCallback.NotifyCategoryChange(currentMediaItem);

        return -1;
    }

    fun changeCategory() {
        categoryChangeCallback.NotifyCategoryChange(currentMediaItem);
    }

    private fun simpleIncrementWithOverflow(currentIndex: Int, maxSize: Int): Int {
        var computed = currentIndex + 1;
        if (computed >= maxSize) computed = 0;
        return computed;
    }

    override fun onAudioFocusChange(p0: Int) {
        when (p0) {
            (AudioManager.AUDIOFOCUS_GAIN) -> setPlayWhenReadyInternal(true);
            (AudioManager.AUDIOFOCUS_LOSS) -> setPlayWhenReady(false);
            (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) -> setPlayWhenReadyInternal(false);
            (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) -> setPlayWhenReadyInternal(false)
        }
    }

    public interface Callback {
        fun NotifyCategoryChange(currentMediaItem: MediaItem?);

    }
}

class RandomQueueNavigator {
    private val shuffledIndices = ArrayList<Int>();
    private var currentIndex: Int = 0;
    fun resetIndices(size: Int, currentPlaybackIndex: Int) {
        regenerateRandomizerSequence(size, currentPlaybackIndex);
    }

    fun seek(currentPlaybackIndex: Int) {
        shuffledIndices.forEachIndexed { index, i ->
            if (shuffledIndices[index] == currentPlaybackIndex)
                currentIndex = index;
        };
    }

    private fun regenerateRandomizerSequence(size: Int, currentPlaybackIndex: Int) {
        shuffledIndices.clear();
        shuffledIndices.addAll((0 until size))
        shuffledIndices.shuffle();
        if (currentIndex >= 0)
            currentIndex = 0;

        shuffledIndices.remove(currentPlaybackIndex);
        shuffledIndices.add(currentPlaybackIndex);
    }

    fun getNextIndex(): Int {
        currentIndex = (currentIndex + 1) % shuffledIndices.size;
        return shuffledIndices[currentIndex];
    }

    fun peekNextIndex(): Int {
        val peekIndex = (currentIndex + 1) % shuffledIndices.size;
        return shuffledIndices[peekIndex];
    }

    fun isAtEndOfQueue(): Boolean {
        return currentIndex == shuffledIndices.size - 1;
    }
}