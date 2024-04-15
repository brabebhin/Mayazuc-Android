package ionic.mayazuc

import android.app.Application
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Paths

class MCApplication : Application() {
    override fun onCreate() {
        instance = this
        CopyDefaultAlbumArtResources();
        super.onCreate()
        AlbumArtScanWorker.StartWorking(false, applicationContext)
    }

    private fun CopyDefaultAlbumArtResources() {
        val resourcesFolder = ResourcesFolder()

        PlayViewGenericPath = CopyResourceToFolder(
            Paths.get(resourcesFolder.absolutePath, "playviewgeneric.png").toString(),
            com.mcosmin.MayazucLite.R.drawable.playviewgeneric
        );
        FolderIconPath = CopyResourceToFolder(
            Paths.get(resourcesFolder.absolutePath, "foldericon.png").toString(),
            com.mcosmin.MayazucLite.R.drawable.foldericon
        );
        MissingAlbumArtIconPath = CopyResourceToFolder(
            Paths.get(resourcesFolder.absolutePath, "androidmissingalbumart.png").toString(),
            com.mcosmin.MayazucLite.R.drawable.androidmissingalbumart
        );

        EnqueueViewGenericPath = CopyResourceToFolder(
            Paths.get(resourcesFolder.absolutePath, "enqueueviewgeneric.png").toString(),
            com.mcosmin.MayazucLite.R.drawable.addtonowplayinggeneric
        )
    }

    @Synchronized
    private fun CopyResourceToFolder(pathToSave: String, resourceId: Int): String {
        val input: InputStream = resources.openRawResource(resourceId)
        val out = FileOutputStream(pathToSave)

        try {
            input.copyTo(out)
        } finally {
            input.close()
            out.close()
        }

        return pathToSave
    }

    companion object {

        var instance: MCApplication? = null
            private set

        // or return instance.getApplicationContext();
        val context: Context?
            get() = instance
        // or return instance.getApplicationContext();

        var PlayViewGenericPath: String = "";
        var FolderIconPath: String = "";
        var MissingAlbumArtIconPath: String = "";
        var EnqueueViewGenericPath: String = "";

        @Synchronized
        fun AlbumArtFolder(): File {
            val file = context?.getDir("AlbumArt", MODE_PRIVATE)
            return file!!;
        }

        @Synchronized
        fun ResourcesFolder(): File {
            val file = context?.getDir("ArtResources", MODE_PRIVATE)
            return file!!;
        }
    }
}