package ionic.mayazuc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.common.io.BaseEncoding
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap


object AlbumArtManager {

    private var isDeletingMutex = Mutex();

    suspend fun ExtractAlbumArtFileInternal(targetFile: File): String {

        try {
            isDeletingMutex.withLock {
                synchronized(FilePathLockObjectManager.GetLock(targetFile.absolutePath)) {
                    val finalPath =
                        GetAlbumArtPathForFile(targetFile)

                    MediaMetadataRetriever().use {
                        it.setDataSource(targetFile.absolutePath);
                        var image = it.embeddedPicture
                        if (image != null) {
                            val embeddedBitmap = BitmapFactory.decodeByteArray(image, 0, image.size)

                            val out = FileOutputStream(finalPath.toString())

                            try {
                                embeddedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            } finally {
                                out.close()
                            }
                            return finalPath.toString()
                        }
                        return MCApplication.MissingAlbumArtIconPath
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
        }
        return MCApplication.MissingAlbumArtIconPath
    }

    fun TryGetAlbumArtForFile(targetFile: File): String {

        synchronized(FilePathLockObjectManager.GetLock(targetFile.absolutePath)) {
            var albumArtFile = GetAlbumArtPathForFile(targetFile).toString();
            val file = File(albumArtFile)
            if (file.exists())
                return albumArtFile
            else return MCApplication.MissingAlbumArtIconPath
        }
    }

    private fun GetAlbumArtPathForFile(targetFile: File): Path {
        val md: MessageDigest = MessageDigest.getInstance("SHA-512")
        val digest: ByteArray = md.digest(targetFile.absolutePath.toByteArray())
        val fileName = BaseEncoding.base32().encode(digest);
        val finalPath =
            Paths.get(MCApplication.AlbumArtFolder().absolutePath, fileName + ".png")
        return finalPath
    }

    fun GetAllFiles(): MutableList<File> {
        val rootDirectory = Environment.getExternalStorageDirectory()
        val directories = ArrayDeque<File>()
        var files = ArrayList<File>()

        directories.add(rootDirectory)

        while (!directories.isEmpty()) {
            val targetChild = directories.first()

            val children = targetChild.listFiles()
            if (children != null) {
                for (c in children) {
                    if (c.isDirectory())
                        directories.addLast(c)
                    if (allSupportedMusicExtensionFormats.contains(c.extension) && c.isFile() && !c.isHidden()) {
                        files.add(c)
                    }
                }
            }
            directories.removeFirst()
        }

        return files;
    }

    suspend fun ClearAlbumrtCache() {
        try {
            isDeletingMutex.lock()
            val cacheFolder = MCApplication.AlbumArtFolder()
            val files = cacheFolder.listFiles()
            for (f in files!!) {
                try {
                    if (f.isFile)
                        f.delete();
                } catch (e: Exception) {

                }
            }
        } catch (e: Exception) {

        } finally {
            if (isDeletingMutex.isLocked)
                isDeletingMutex.unlock();
        }

    }
}

class FilePathLockObject(val FilePathName: String) {

}

object FilePathLockObjectManager {
    val map: ConcurrentHashMap<String, FilePathLockObject> =
        ConcurrentHashMap<String, FilePathLockObject>()

    fun GetLock(key: String): FilePathLockObject {
        if (map.containsKey(key)) return map[key]!!
        synchronized(key.intern()) {
            val newLock = FilePathLockObject(key)
            map.putIfAbsent(key, newLock)
            return newLock;
        }
    }
}

class AlbumArtScanWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    companion object {
        const val DeleteParameter = "delete"

        fun StartWorking(deleteCache: Boolean, context: Context) {
            WorkManager.getInstance(context)
                .beginUniqueWork(
                    context.getString(R.string.album_art_job_name),
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequest.Builder(AlbumArtScanWorker::class.java).setInputData(
                        Data.Builder().putBoolean(
                            DeleteParameter, deleteCache
                        ).build()
                    ).build()
                ).enqueue()
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {

        val deleteCache = inputData.getBoolean(DeleteParameter, false)

        var progress = "Deleting cache..."

        if (deleteCache) {
            setForeground(createForegroundInfo(progress))
            AlbumArtManager.ClearAlbumrtCache()
        }

        progress = "Scanning files..."
        // Mark the Worker as important
        setForeground(createForegroundInfo(progress))
        work()
        return Result.success()
    }


    private suspend fun work() {

        val files = AlbumArtManager.GetAllFiles()
        var filesProcessedSoFar = 0;

        setForeground(createForegroundInfo("Files: " + filesProcessedSoFar + "/" + files.size))

        for (p in files) {

            AlbumArtManager.ExtractAlbumArtFileInternal(p)
            filesProcessedSoFar++

            setForeground(createForegroundInfo("Files: " + filesProcessedSoFar + " / " + files.size))
        }
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val notificationId = 284723908;
        val id = applicationContext.getString(R.string.notification_channel_id)
        val title = applicationContext.getString(R.string.notification_title)
        val cancel = applicationContext.getString(R.string.cancel_album_art_scan)
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.androidmissingalbumart)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           return ForegroundInfo(
                notificationId,
                notification,
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            return ForegroundInfo(notificationId, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        // Create a Notification channel

        val notificationChannel = NotificationChannel(
            applicationContext.getString(R.string.notification_channel_id),
            applicationContext.getString(R.string.notification_title),
            android.app.NotificationManager.IMPORTANCE_LOW
        )
        notificationChannel.description = "MC Media Center album art scan worker notification"
        notificationManager.createNotificationChannel(notificationChannel)
    }
}
