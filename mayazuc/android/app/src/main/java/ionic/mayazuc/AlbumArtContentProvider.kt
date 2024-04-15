package ionic.mayazuc

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import java.io.File


class AlbumArtContentProvider : ContentProvider() {

    companion object {
        public val Authority: String = "MayazucLite";
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        TODO("Implement this to handle requests to delete one or more rows")
    }

    override fun getType(uri: Uri): String? {
        val filePath = uri.toString()
        // Extension with dot .jpg, .png
        return when (filePath.substring(filePath.lastIndexOf("."))) {
            ".png" -> "image/png"
            ".jpg", ".jpeg" -> "image/jpeg"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Implement this to handle requests to insert a new row.")
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        try {

            val path = uri.path;

            val file = File(path);
            if (file.exists()) {
                val parcel = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

                return parcel;
            }
        } catch (_: Exception) {

        }

        return super.openFile(uri, mode)
    }

    override fun openFile(
        uri: Uri,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor? {
        return openFile(uri, mode);
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        TODO("Implement this to handle query requests from clients.")
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        TODO("Implement this to handle requests to update one or more rows.")
    }
}