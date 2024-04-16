package ionic.mayazuc

import android.content.ComponentName
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.gson.Gson


@CapacitorPlugin(name = "AndroidMediaController")
class MediaControllerIonicPlugin : Plugin() {

    @PluginMethod
    fun openMediaId(call: PluginCall) {
        val mediaId = call.getString("mediaId");
        val resultOperation = MediaServiceConnector.openMediaId(mediaId);
        resultOperation.addListener({
            val result = JSObject();
            val items = ArrayList<MediaItemDTO>();
            resultOperation.get().value?.forEach { items.add(MediaItemDTO.createFromMediaItem(it)) }
            val converter = Gson();
            val json = converter.toJson(items);
            result.put("result", json)
            call.resolve(result);
        }, MoreExecutors.directExecutor());
    }

    @PluginMethod
    fun autoPlayPause(call: PluginCall) {


    }

    @PluginMethod
    fun skipNext(call: PluginCall) {

    }

    @PluginMethod
    fun skipPrevious(call: PluginCall) {

    }

    @PluginMethod
    fun seek(call: PluginCall) {

    }
}

data class MediaItemDTO(val mediaId: String, val title: String, val imageUrl: String) {
    companion object {
        fun createFromMediaItem(item: MediaItem): MediaItemDTO {
            return MediaItemDTO(
                item.mediaId,
                item.mediaMetadata.title.toString(),
                item.mediaMetadata.artworkUri.toString()
            );
        }
    }
}