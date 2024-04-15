package ionic.mayazuc

import android.app.Application
import androidx.media3.common.MediaItem
import java.io.File
import java.nio.file.Paths

object NowPlayingManager {

    val lockMutex = Any()

    @Synchronized
    fun loadNowPlaying(): List<String> {
        synchronized(lockMutex) {
            val nowPlayingFile = GetNowPlayingFile()
            if (nowPlayingFile.exists()) {
                return PlaylistManager.loadPlaylist(nowPlayingFile);
            }

            return ArrayList<String>()
        }
    }

    @Synchronized
    fun addToNowPlaying(toAdd: List<MediaItem>): List<String> {
        synchronized(lockMutex) {
            val nowPlayingFile = GetNowPlayingFile()
            return PlaylistManager.addToPlaylist(toAdd, nowPlayingFile)
        }
    }

    fun saveNowPlaying(mowPlaying: List<MediaItem>): List<String> {
        synchronized(lockMutex) {
            val nowPlayingFile = GetNowPlayingFile()
            return PlaylistManager.savePlaylist(mowPlaying, nowPlayingFile)
        }
    }


    @Synchronized
    private fun GetNowPlayingFile(): File {
        val nowPlayingFolder = NowPlayingFolder()
        return Paths.get(nowPlayingFolder.absolutePath, "nowplaying.json").toFile()
    }

    @Synchronized
    private fun NowPlayingFolder(): File {
        val file = MCApplication.context?.getDir("nowplaying", Application.MODE_PRIVATE)
        return file!!;
    }
}

