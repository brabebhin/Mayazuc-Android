package ionic.mayazuc

import android.app.Application
import androidx.media3.common.MediaItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.LinkedList
import java.util.stream.Collectors

object PlaylistManager {
    val lockMutex = Any()

    @Synchronized
    fun loadPlaylist(playlistFilePath: File): List<String> {
        synchronized(lockMutex) {
            val playlistFile = playlistFilePath
            if (playlistFile.exists()) {
                val jsonObjectin = playlistFile.readBytes()
                if (jsonObjectin != null) {
                    val listOfMyClassObject: Type =
                        object : TypeToken<ArrayList<String>>() {}.getType()
                    val jsonObject = String(jsonObjectin, StandardCharsets.UTF_8)
                    val items =
                        Gson().fromJson<ArrayList<String>>(jsonObject, listOfMyClassObject);
                    return items;
                }
            }

            return ArrayList<String>()
        }
    }

    @Synchronized
    fun addToPlaylist(toAdd: List<MediaItem>, playlistFilePath: File): List<String> {
        synchronized(lockMutex) {
            val existing = loadPlaylist(playlistFilePath)

            val incoming = toAdd.stream().map { x -> x.mediaId }.collect(
                Collectors.toList()
            )

            val toSave = LinkedList(existing)
            toSave.addAll(incoming)

            savePlaylistInternal(toSave, playlistFilePath)

            return toSave;
        }
    }

    fun savePlaylist(mowPlaying: List<MediaItem>, playlistFilePath: File): List<String> {
        synchronized(NowPlayingManager.lockMutex) {
            val resultToSave = mowPlaying.stream().map { x -> x.mediaId }.collect(
                Collectors.toList()
            )
            savePlaylistInternal(resultToSave, playlistFilePath)

            return resultToSave;
        }
    }

    fun getPlaylists(): List<PlaylistItem> {
        val folder = playlistsFolder();
        return (folder.listFiles().toList().stream().filter({ it.exists() && it.isFile() })
            .map { x -> PlaylistItem(x) }.collect(Collectors.toList()));
    }

    fun getPlaylistFileForName(name: String): File{
        val folder = playlistsFolder();
        val path = folder.path;
        val newFilePath = path + File.separator + name + ".json";
        val newFile = File(path)

        newFile.parentFile.mkdirs()
        newFile.createNewFile()

        return newFile;
    }

    private fun savePlaylistInternal(resultToSave: List<String>?, playlistFilePath: File) {
        val jsonObject = Gson().toJson(resultToSave);
        Files.write(playlistFilePath.toPath(), jsonObject?.toByteArray(StandardCharsets.UTF_8))
    }

    @Synchronized
    private fun playlistsFolder(): File {
        val file = MCApplication.context?.getDir("playlists", Application.MODE_PRIVATE)
        return file!!;
    }
}