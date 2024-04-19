package ionic.mayazuc

import android.content.ComponentName
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.getcapacitor.BridgeActivity
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

abstract class MediaControllerActivity : BridgeActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    protected val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0);
    }

    override fun onStop() {
        super.onStop()
        releaseController()
    }

    private fun initializeController() {
        controllerFuture = MediaController.Builder(
            this,
            SessionToken(this, ComponentName(this, PlaybackService::class.java))
        ).buildAsync()
        controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())
    }

    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }

    protected abstract fun setController()

    protected fun getPlaylistFromController(): ImmutableList<MediaItem> {
        val controller = this.controller ?: return ImmutableList.of()

        return MediaServiceConnector.GetMediaItemsFromPlayer(controller)
    }
}