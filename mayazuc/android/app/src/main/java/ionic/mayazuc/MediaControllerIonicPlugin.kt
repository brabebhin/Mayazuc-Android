package ionic.mayazuc

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.*
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.gson.Gson
import ionic.mayazuc.Utilities.getPlaybaleItems


@CapacitorPlugin(name = "AndroidMediaController")
class MediaControllerIonicPlugin : Plugin() {
    override fun handleOnPause() {
        Handler(Looper.getMainLooper()).post(Runnable {
            MediaServiceConnector.initializeBrowser();
        });
        super.handleOnPause()
    }

    override fun handleOnStart() {
        Handler(Looper.getMainLooper()).post(Runnable {
            MediaServiceConnector.releaseBrowser();
        });
        super.handleOnStart()
    }

    @PluginMethod
    fun openMediaId(call: PluginCall) {
        Handler(Looper.getMainLooper()).post(Runnable {
            val mediaId = call.getString("value");
            val resultOperation = MediaServiceConnector.openMediaId(mediaId);

            Futures.addCallback(
                resultOperation,
                object : FutureCallback<LibraryResult<ImmutableList<MediaItem>>> {
                    override fun onSuccess(result: LibraryResult<ImmutableList<MediaItem>>?) {
                        val result = JSObject();
                        val items = ArrayList<MediaItemDTO>();
                        resultOperation.get().value?.forEach {
                            items.add(
                                MediaItemDTO.createFromMediaItem(
                                    it
                                )
                            )
                        }
                        val converter = Gson();
                        val json = converter.toJson(items);
                        result.put("value", json);
                        call.resolve(result);
                    }

                    override fun onFailure(t: Throwable) {
                        genericErrorReturn(call)
                    }

                },
                MoreExecutors.directExecutor()
            )
        })
    }

    @PluginMethod
    fun playMediaId(call: PluginCall) {
        Handler(Looper.getMainLooper()).post(Runnable {

            val mediaId = call.getString("value");
            val resultOperation = MediaServiceConnector.openMediaId(mediaId);

            Futures.addCallback(
                resultOperation,
                object : FutureCallback<LibraryResult<ImmutableList<MediaItem>>> {
                    override fun onSuccess(result: LibraryResult<ImmutableList<MediaItem>>?) {
                        val items = resultOperation.get().value?.getPlaybaleItems()!!

                        if (mediaId!!.isPlayCommand()) {
                            MediaServiceConnector.playMediaItemsAsync(items);
                        } else if (mediaId!!.isEnqueueCommand()) {
                            MediaServiceConnector.addMediaItemsToNowPlayingAsync(items)
                        }

                        call.resolve();
                    }

                    override fun onFailure(t: Throwable) {
                        call.resolve();
                    }

                },
                MoreExecutors.directExecutor()
            )
        })
    }

    @PluginMethod
    fun getPlaybackQueue(call: PluginCall) {

        Handler(Looper.getMainLooper()).post {

            val resultOperation = MediaServiceConnector.getCurrentPlaybackQueue()
            Futures.addCallback(resultOperation, object : FutureCallback<ImmutableList<MediaItem>> {
                override fun onSuccess(result: ImmutableList<MediaItem>?) {
                    mediaItemListReturn(resultOperation, call)
                }

                override fun onFailure(t: Throwable) {
                    genericErrorReturn(call)
                }

            }, MoreExecutors.directExecutor())
        }
    }

    @PluginMethod
    fun skipToQueueItem(call: PluginCall) {
        Handler(Looper.getMainLooper()).post(Runnable {

            val mediaId = call.getString("value");
            val resultOperation = MediaServiceConnector.openMediaId(mediaId);

            Futures.addCallback(
                resultOperation,
                object : FutureCallback<LibraryResult<ImmutableList<MediaItem>>> {
                    override fun onSuccess(result: LibraryResult<ImmutableList<MediaItem>>?) {
                        if (resultOperation.get().value!!.isEmpty()) {
                            genericErrorReturn(call)
                        }

                        val skipItem = resultOperation.get()!!.value!!.get(0);
                        var skipToQueueItemOperation =
                            MediaServiceConnector.skipToQueueItemAsync(skipItem);

                        Futures.addCallback(
                            skipToQueueItemOperation,
                            object : FutureCallback<ImmutableList<MediaItem>> {
                                override fun onSuccess(result: ImmutableList<MediaItem>?) {
                                    mediaItemListReturn(skipToQueueItemOperation, call)
                                }

                                override fun onFailure(t: Throwable) {
                                    genericErrorReturn(call)
                                }

                            },
                            MoreExecutors.directExecutor()
                        );
                    }

                    override fun onFailure(t: Throwable) {
                        genericErrorReturn(call)
                    }

                },
                MoreExecutors.directExecutor()
            )

            resultOperation.addListener({


            }, MoreExecutors.directExecutor());
        })
    }

    private fun mediaItemListReturn(
        resultOperation: ListenableFuture<ImmutableList<MediaItem>>,
        call: PluginCall
    ) {
        val result = JSObject();
        val items = ArrayList<MediaItemDTO>();
        resultOperation.get()
            .forEach { items.add(MediaItemDTO.createFromMediaItem(it)) }
        val converter = Gson();
        val json = converter.toJson(items);
        result.put("value", json)
        call.resolve(result);
    }

    private fun genericErrorReturn(call: PluginCall) {
        val result = JSObject();
        result.put("value", "");
        call.resolve(result);
    }

    @PluginMethod
    fun playbackState(call: PluginCall) {
        Handler(Looper.getMainLooper()).post(Runnable {
            Futures.addCallback(
                MediaServiceConnector.initializeBrowser(),
                object : FutureCallback<MediaBrowser> {
                    override fun onSuccess(result: MediaBrowser?) {

                        val resultObj = JSObject();
                        val converter = Gson();
                        val timelineInfo = MediaStateInfo(
                            result!!.contentPosition,
                            result!!.contentDuration,
                            result!!.isPlaying(),
                            result.mediaMetadata.artworkUri!!.path!!,
                        );
                        val json = converter.toJson(timelineInfo);
                        resultObj.put("value", json)
                        call.resolve(resultObj);
                    }

                    override fun onFailure(t: Throwable) {
                        genericErrorReturn(call);
                    }

                },
                MoreExecutors.directExecutor()
            )
        })
    }

    @PluginMethod
    fun autoPlayPause(call: PluginCall) {
        Handler(Looper.getMainLooper()).post(Runnable {
            Futures.addCallback(
                MediaServiceConnector.initializeBrowser(),
                object : FutureCallback<MediaBrowser> {
                    override fun onSuccess(result: MediaBrowser?) {

                        if(result!!.isPlaying) result.pause() else result.play();
                        call.resolve();
                    }

                    override fun onFailure(t: Throwable) {
                        genericErrorReturn(call);
                    }

                },
                MoreExecutors.directExecutor()
            )
        })

    }

    @PluginMethod
    fun skipNext(call: PluginCall) {

    }

    @PluginMethod
    fun skipPrevious(call: PluginCall) {

    }

    @PluginMethod
    fun seek(call: PluginCall) {
        val seekPosition = call.getString("value")?.toFloat();
        if(seekPosition==null)
        {
            genericErrorReturn(call);
            return;
        }
        Handler(Looper.getMainLooper()).post(Runnable {
            Futures.addCallback(
                MediaServiceConnector.initializeBrowser(),
                object : FutureCallback<MediaBrowser> {
                    override fun onSuccess(result: MediaBrowser?) {
                       result?.seekTo((result!!.contentDuration * (seekPosition / 100)).toLong());
                    }

                    override fun onFailure(t: Throwable) {
                        genericErrorReturn(call);
                    }

                },
                MoreExecutors.directExecutor()
            )
        })
    }
}

data class MediaStateInfo(
    val position: Long,
    val duration: Long,
    val isPlaying: Boolean,
    val albumArtUrl: String,
    val timelineProgress: Float = if(duration!=0L) (position.toFloat() / duration.toFloat()) * 100 else 0F
) {

}

data class MediaItemDTO(
    val mediaId: String,
    val title: String,
    val imageUrl: String,
    val type: String
) {
    companion object {
        fun createFromMediaItem(item: MediaItem): MediaItemDTO {
            var type = "FOLDER_TYPE_MIXED";
            val metadata = item.mediaMetadata;
            if (metadata.folderType == FOLDER_TYPE_MIXED) {
                type = "FOLDER_TYPE_MIXED"; // folders
            } else if (metadata.folderType == FOLDER_TYPE_TITLES) {
                type = "FOLDER_TYPE_TITLES"; //play commands
            } else if (metadata.folderType == FOLDER_TYPE_NONE) {
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