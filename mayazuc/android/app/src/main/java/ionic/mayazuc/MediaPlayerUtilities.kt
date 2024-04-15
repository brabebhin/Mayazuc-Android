package ionic.mayazuc

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import okhttp3.internal.immutableListOf

object MediaPlayerUtilities {
    fun playMediaItemsAsync(subItemMediaList: List<MediaItem>) {

        Futures.addCallback(
            MediaServiceConnector.initializeBrowser(), object : FutureCallback<MediaBrowser> {
                override fun onFailure(t: Throwable) {
                    TODO("Not yet implemented")
                }

                override fun onSuccess(result: MediaBrowser?) {

                    playMediaItems(result, subItemMediaList)
                }

            }, MoreExecutors.directExecutor()
        )
    }

    fun playMediaItemAsync(mediaItem: MediaItem)
    {
        playMediaItemsAsync(listOf(mediaItem));
    }

    private fun playMediaItems(
        result: Player?,
        subItemMediaList: List<MediaItem>
    ) {
        result?.mediaItemCount.toString()
        result?.setMediaItems(subItemMediaList)
        result?.prepare()
        result?.play()
    }

    fun addMediaItemsToNowPlayingAsync(subItemMediaList: List<MediaItem>) {
        Futures.addCallback(
            MediaServiceConnector.initializeBrowser(), object : FutureCallback<MediaBrowser> {
                override fun onFailure(t: Throwable) {
                    TODO("Not yet implemented")
                }

                override fun onSuccess(result: MediaBrowser?) {
                    AddMediaItems(result, subItemMediaList)
                }

            }, MoreExecutors.directExecutor()
        )
    }

    private fun AddMediaItems(
        result: Player?,
        subItemMediaList: List<MediaItem>
    ) {
        result?.addMediaItems(subItemMediaList)

        result?.prepare()
        result?.play()
    }

    fun skipToQueueItemAsync(item: MediaItem) {
        Futures.addCallback(
            MediaServiceConnector.initializeBrowser(), object : FutureCallback<MediaBrowser> {
                override fun onFailure(t: Throwable) {
                    TODO("Not yet implemented")
                }

                override fun onSuccess(result: MediaBrowser?) {

                    if (result != null) {
                        SkipToQueueItem(result, item)
                    }
                }

            }, MoreExecutors.directExecutor()
        )
    }

    fun SkipToQueueItem(
        result: Player,
        item: MediaItem
    ) {
        val currentPlaylist = GetMediaItemsFromPlayer(result);

        var currentIndex = -1;
        currentPlaylist.forEachIndexed { index, element ->
            // ...
            if (element.SafeMediaId() == item.SafeMediaId()) {
                currentIndex = index;
            }
        }

        if (currentIndex == -1) {
            AddMediaItems(result, immutableListOf(item));
            currentIndex = result.mediaItemCount - 1;
        }

        result.prepare()
        result.seekTo(currentIndex, 0);
        result.play()
    }

    fun GetMediaItemsFromPlayer(controller: Player): ImmutableList<MediaItem> {
        val subItemMediaList = LinkedHashSet<MediaItem>()
        for (i in 0 until controller.mediaItemCount) {
            subItemMediaList.add(controller.getMediaItemAt(i))
        }

        return ImmutableList.copyOf(subItemMediaList);
    }
}