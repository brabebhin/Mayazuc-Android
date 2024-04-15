package ionic.mayazuc

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.nio.ByteBuffer
import java.util.stream.Collectors


object Utilities {
    fun GetUriResource(resourceId: Int, context: Context): Uri {
        val resources: Resources = context.getResources()
        var uri: Uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(resourceId))
            .appendPath(resources.getResourceTypeName(resourceId))
            .appendPath(resources.getResourceEntryName(resourceId))
            .build()

        return uri;
    }

    fun GetAndroidAutoCoverResource(resourceName: String): Uri? {
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AlbumArtContentProvider2.Authority)
            .appendPath(resourceName)
            .build()

        return uri;
    }

    fun GetBitmapForResourceId(resourceId: Int): Bitmap? {
        return BitmapFactory.decodeResource(MCApplication.context?.resources, resourceId)
    }

    fun Bitmap.convertToByteArray(): ByteArray {
        //minimum number of bytes that can be used to store this bitmap's pixels
        val size = this.byteCount

        //allocate new instances which will hold bitmap
        val buffer = ByteBuffer.allocate(size)
        val bytes = ByteArray(size)

        //copy the bitmap's pixels into the specified buffer
        this.copyPixelsToBuffer(buffer)

        //rewinds buffer (buffer position is set to zero and the mark is discarded)
        buffer.rewind()

        //transfer bytes from buffer into the given destination array
        buffer.get(bytes)

        //return bitmap's pixels
        return bytes
    }


    fun MutableList<MediaItem>.getPlaybaleItems(): MutableList<MediaItem> {
        //command items are always at the start.
        return this.stream().filter({ it.mediaMetadata.folderType == MediaMetadata.FOLDER_TYPE_NONE}).collect(Collectors.toList());
    }

    fun FormatMilisecondsToHoursMinutesSeconds(miliseconds: Long): String {
        var x: Long = miliseconds / 1000
        val seconds = x % 60
        x /= 60
        val minutes = x % 60
        x /= 60
        val hours = x % 24
        var returnValue = "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}";
        if (hours > 0)
            returnValue = "${String.format("%02d", hours)}:${returnValue}";
      return returnValue;

    }
}

