package ionic.mayazuc

import android.annotation.SuppressLint
import android.content.ComponentName
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.work.await
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import ionic.mayazuc.MediaItemTree.ROOT_ID
import ionic.mayazuc.Utilities.getPlaybaleItems
import okhttp3.internal.immutableListOf

object MediaServiceConnector {
    private var browserFuture: ListenableFuture<MediaBrowser>? = null
    private val browser: MediaBrowser?
        get() = if (browserFuture?.isDone!!) browserFuture?.get() else null

    @SuppressLint("UnsafeOptInUsageError")
    fun initializeBrowser(): ListenableFuture<MediaBrowser> {
        synchronized(this) {
            if (browserFuture == null) {
                browserFuture =
                    MediaBrowser.Builder(
                        MCApplication.context!!,
                        SessionToken(
                            MCApplication.context!!,
                            ComponentName(MCApplication.context!!, PlaybackService::class.java)
                        )
                    )
                        .buildAsync()
            }

            return browserFuture!!
        }
    }

    fun releaseBrowser() {
        synchronized(this) {
            if (browserFuture != null) {
                MediaBrowser.releaseFuture(browserFuture!!)
                browserFuture = null;
            }
        }
    }

    fun openMediaId(mediaId: String?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        var finalMediaId = ROOT_ID
        if (mediaId != null)
            finalMediaId = mediaId;
        val returnValue = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>();
        Futures.addCallback(initializeBrowser(), object: FutureCallback<MediaBrowser>
        {
            override fun onSuccess(result: MediaBrowser?) {
                val children = browser?.getChildren(finalMediaId, 0, Int.MAX_VALUE, null);
                Futures.addCallback(children, object: FutureCallback<LibraryResult<ImmutableList<MediaItem>>>
                {
                    override fun onSuccess(result: LibraryResult<ImmutableList<MediaItem>>?) {
                        returnValue.set(result);
                    }
                    override fun onFailure(t: Throwable) {
                        returnValue.setException(t);
                    }
                }, MoreExecutors.directExecutor());
            }

            override fun onFailure(t: Throwable) {
                returnValue.setException(t);
            }
        }, MoreExecutors.directExecutor())

        return returnValue;
    }

    fun playMediaItemsAsync(subItemMediaList: List<MediaItem>) {

        Futures.addCallback(
            initializeBrowser(), object : FutureCallback<MediaBrowser> {
                override fun onFailure(t: Throwable) {
                    Log.e(t.message, "");
                }

                override fun onSuccess(result: MediaBrowser?) {

                    playMediaItems(result, subItemMediaList)
                }

            }, MoreExecutors.directExecutor()
        )
    }

    fun playMediaItemAsync(mediaItem: MediaItem) {
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
            initializeBrowser(), object : FutureCallback<MediaBrowser> {
                override fun onFailure(t: Throwable) {
                    Log.e(t.message, "");
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

    fun skipToQueueItemAsync(item: MediaItem): ListenableFuture<ImmutableList<MediaItem>> {
        val returnValue = SettableFuture.create<ImmutableList<MediaItem>>();
        Futures.addCallback(
            initializeBrowser(), object : FutureCallback<MediaBrowser> {
                override fun onFailure(t: Throwable) {
                    returnValue.setException(t);
                }

                override fun onSuccess(result: MediaBrowser?) {
                    returnValue.set(SkipToQueueItem(result!!, item))
                }

            }, MoreExecutors.directExecutor()
        )

        return returnValue;
    }

    fun SkipToQueueItem(
        result: Player,
        item: MediaItem
    ): ImmutableList<MediaItem> {
        val currentPlaylist = GetMediaItemsFromPlayer(result);

        var currentIndex = -1;
        currentPlaylist.forEachIndexed { index, element ->
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

        val returnValue = ImmutableList.copyOf(currentPlaylist + immutableListOf(item));
        return returnValue;
    }

    fun getCurrentPlaybackQueue(): ListenableFuture<ImmutableList<MediaItem>> {
        val returnValue = SettableFuture.create<ImmutableList<MediaItem>>()
        var currentPlayer = initializeBrowser();
        Futures.addCallback(currentPlayer, object : FutureCallback<MediaBrowser> {

            override fun onFailure(t: Throwable){
                returnValue.setException(t);
            }

            override fun onSuccess(result: MediaBrowser?) {
                val values = GetMediaItemsFromPlayer(currentPlayer.get());
                returnValue.set(values);
            }

        }, MoreExecutors.directExecutor());

        return returnValue;
    }

    fun GetMediaItemsFromPlayer(controller: Player): ImmutableList<MediaItem> {
        val subItemMediaList = LinkedHashSet<MediaItem>()
        for (i in 0 until controller.mediaItemCount) {
            subItemMediaList.add(controller.getMediaItemAt(i))
        }

        return ImmutableList.copyOf(subItemMediaList);
    }
}