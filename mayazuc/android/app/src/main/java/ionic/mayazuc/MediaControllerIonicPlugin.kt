package ionic.mayazuc

import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.*
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.google.common.util.concurrent.MoreExecutors
import com.google.gson.Gson


@CapacitorPlugin(name = "AndroidMediaController")
class MediaControllerIonicPlugin : Plugin() {

    @PluginMethod
    fun openMediaId(call: PluginCall) {

        Handler(Looper.getMainLooper()).post(Runnable {

        val mediaId = call.getString("value");
        val resultOperation = MediaServiceConnector.openMediaId(mediaId);
        resultOperation.addListener({
            val result = JSObject();
            val items = ArrayList<MediaItemDTO>();
            resultOperation.get().value?.forEach { items.add(MediaItemDTO.createFromMediaItem(it)) }
            val converter = Gson();
            val json = converter.toJson(items);
            result.put("value", json)
            call.resolve(result);
        }, MoreExecutors.directExecutor());
        })
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

data class MediaItemDTO(val mediaId: String, val title: String, val imageUrl: String, val type: String) {
    companion object {
        fun createFromMediaItem(item: MediaItem): MediaItemDTO {
            var type = "FOLDER_TYPE_MIXED";
            val metadata = item.mediaMetadata;
            if(metadata.folderType == FOLDER_TYPE_MIXED)
            {
                type="FOLDER_TYPE_MIXED"; // folders
            }
            else if(metadata.folderType == FOLDER_TYPE_TITLES)
            {
                type = "FOLDER_TYPE_TITLES"; //play commands
            }
            else if(metadata.folderType == FOLDER_TYPE_NONE)
            {
                type = "FOLDER_TYPE_NONE"; // single file
            }
            return MediaItemDTO(
                item.mediaId,
                item.mediaMetadata.title.toString(),
                item.mediaMetadata.artworkUri!!.path!!,
                type
            );
        }
    }
}