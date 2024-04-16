package ionic.mayazuc

import android.content.ComponentName
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import ionic.mayazuc.MediaItemTree.ROOT_ID

object MediaServiceConnector {
    private var browserFuture: ListenableFuture<MediaBrowser>? = null
    private val treePathStack: ArrayDeque<MediaItem> = ArrayDeque()
    private val browser: MediaBrowser?
        get() = if (browserFuture?.isDone!!) browserFuture?.get() else null

    fun initializeBrowser(): ListenableFuture<MediaBrowser> {
        synchronized(this) {
            if (browserFuture == null) {
                browserFuture =
                    MediaBrowser.Builder(
                        MCApplication.context!!,
                        SessionToken(MCApplication.context!!, ComponentName(MCApplication.context!!, PlaybackService::class.java))
                    )
                        .buildAsync()
            }

            browserFuture?.addListener({ pushRoot() }, MoreExecutors.directExecutor())
            return browserFuture!!
        }
    }

    fun releaseBrowser() {
        synchronized(this) {
            if (browserFuture != null)
                MediaBrowser.releaseFuture(browserFuture!!)
        }
    }

    fun openMediaId(mediaId: String?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>>
    {
        var finalMediaId = ROOT_ID
        if(mediaId != null)
            finalMediaId = mediaId!!.SafeMediaId()
        val  returnValue = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>();
        initializeBrowser().addListener({
            val children =browser?.getChildren(finalMediaId, 0, Int.MAX_VALUE, null);
            children?.addListener({
                  returnValue.set(children.get());
            }, MoreExecutors.directExecutor());
        },MoreExecutors.directExecutor())

        return returnValue;
    }

    fun getLibraryRoot(): ListenableFuture<LibraryResult<MediaItem>>?
    {
        return browser?.getLibraryRoot(null);
    }


    private fun pushRoot() {
        this.toString()
        // browser can be initialized many times
        // only push root at the first initialization
        if (!treePathStack.isEmpty()) {
            return
        }
    }
}