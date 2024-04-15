package ionic.mayazuc

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.*
import com.google.common.collect.ImmutableList
import ionic.mayazuc.MediaItemTree.PlayHierarchyFromStartingItem
import ionic.mayazuc.MediaItemTree.PlayShallowFolderFromStartingItem
import java.io.File
import java.io.IOException
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.LinkedList
import java.util.UUID
import java.util.stream.Collectors
import kotlin.io.path.extension


/**
 * A sample media catalog that represents media items as a tree.
 *
 * It fetched the data from {@code catalog.json}. The root's children are folders containing media
 * items from the same album/artist/genre.
 *
 * Each app should have their own way of representing the tree. MediaItemTree is used for
 * demonstration purpose only.
 */

object MediaItemTree {
    private var defaultTreeNodes: MutableMap<String, MediaItemNode> = mutableMapOf()
    private var titleMap: MutableMap<String, MediaItemNode> = mutableMapOf()

    private var isInitialized = false
    const val ROOT_ID = "[rootID]"
    private const val ALBUM_ID = "[albumID]"
    private const val ARTIST_ID = "[artistID]"
    private const val FOLDERS_ID = "[foldersID]"

    const val PlayCommandPrefix = "273d7b57-852f-471e-a1d0-581dca54cbbd"
    const val PlayDeepCommandPrefix = "24fb2381-d13d-45e8-b065-9afad1a6617f"
    const val EnqueueCommandPrefix = "9bf6b54f-c06a-4223-b1df-4fe1011d035b"
    const val EnqueueDeepCommandPrefix = "f783a124-f70a-4d57-ae18-a0f421e676a4"
    const val PlayShallowFolderFromStartingItem = "5cc6c7eb-3eb5-43c9-8964-40271aa9e1c4"
    const val PlayHierarchyFromStartingItem = "d9d65b95-bd54-4a0b-91ba-ad0a33efb7c2"


    private const val ALBUM_PREFIX = "[album]"
    private const val GENRE_PREFIX = "[genre]"
    private const val ARTIST_PREFIX = "[artist]"
    private const val ITEM_PREFIX = "[item]"

    /*
    * Creates a media item from a file
    * Inputs:
    *   the File object
    *   boolean to determine if global settings should be applied to this item.
    *
    * */
    private fun CreateMediaItemFromFile(file: File, ignoreGlobalOptions: Boolean): MediaItem {
        return buildMediaItem(
            title = if (ignoreGlobalOptions) file.name else IgnoreLeadingNumbersInFileNamesService.GetFileNameTrimmedOrDefault(
                file.name
            ),
            mediaId = CreateSafeMediaId(file.absolutePath),
            isPlayable = true,
            folderType = FOLDER_TYPE_NONE,
            imageUri = Utilities.GetAndroidAutoCoverResource(
                AlbumArtManager.TryGetAlbumArtForFile(
                    file
                )
            ),// Utilities.GetAndroidAutoCoverResource(MCApplication.MissingAlbumArtIconPath),
            sourceUri = Uri.fromFile(file),
        )
    }

    private fun CreateMediaItemFromFolder(directory: File) =
        buildMediaItem(
            title = directory.name,
            mediaId = directory.absolutePath,
            isPlayable = false,
            folderType = FOLDER_TYPE_MIXED,
            imageUri = Utilities.GetAndroidAutoCoverResource(MCApplication.FolderIconPath)
        )

    private fun CreatePlayDirectoryCommandItem(directory: File): MediaItem {
        return buildMediaItem(
            title = "Play directory",
            mediaId = CreateSafeMediaId(directory.absolutePath, PlayCommandPrefix),
            isPlayable = true,
            folderType = FOLDER_TYPE_TITLES,
            imageUri = Utilities.GetAndroidAutoCoverResource(MCApplication.PlayViewGenericPath)
        )
    }

    private fun CreatePlayHierarchyDirectoryCommandItem(directory: File): MediaItem {
        return buildMediaItem(
            title = "Play hierarchy",
            mediaId = CreateSafeMediaId(directory.absolutePath, PlayDeepCommandPrefix),
            isPlayable = true,
            folderType = FOLDER_TYPE_TITLES,
            imageUri = Utilities.GetAndroidAutoCoverResource(MCApplication.PlayViewGenericPath)
        )
    }

    private fun CreateEnqueueHierarchyDirectoryCommandItem(directory: File): MediaItem {
        return buildMediaItem(
            title = "Enqueue hierarchy",
            mediaId = CreateSafeMediaId(directory.absolutePath, EnqueueDeepCommandPrefix),
            isPlayable = true,
            folderType = FOLDER_TYPE_TITLES,
            imageUri = Utilities.GetAndroidAutoCoverResource(MCApplication.EnqueueViewGenericPath)
        )
    }


    private fun CreateEnqueueDirectoryCommandItem(directory: File): MediaItem {
        return buildMediaItem(
            title = "Enqueue directory",
            mediaId = CreateSafeMediaId(directory.absolutePath, EnqueueCommandPrefix),
            isPlayable = true,
            folderType = FOLDER_TYPE_TITLES,
            imageUri = Utilities.GetAndroidAutoCoverResource(MCApplication.EnqueueViewGenericPath)
        )
    }

    private open class MediaItemNode(val item: MediaItem) {
        private val children: MutableList<MediaItem> = ArrayList()

        fun addChild(childID: String) {
            this.children.add(defaultTreeNodes[childID]!!.item)
        }

        open fun getChildren(): List<MediaItem> {
            return ImmutableList.copyOf(children)
        }
    }

    private class FileFolderMediaItemNode(
        item: MediaItem,
        val rootPath: String?,
        val filesOnly: Boolean
    ) :
        MediaItemNode(item) {
        override fun getChildren(): List<MediaItem> {

            val children: MutableList<MediaItem> = ArrayList()
            val fileChildren: MutableList<File> = ArrayList();
            val folderChildren: MutableList<File> = ArrayList();

            val directory: File;
            if (rootPath == null) {
                directory = Environment.getExternalStorageDirectory()
            } else {
                directory = File(rootPath!!)
            }
            if (!directory.exists())
                return children
            if (directory.isFile) {
                children.add(CreateMediaItemFromFile(directory, false))
            } else {
                val fsChildren = LinkedList<File>();
                fsChildren.addAll(directory.listFiles())

                for (file in fsChildren) {

                    if (file.isHidden) continue;
                    if (!file.canRead()) continue;

                    if (file.isDirectory && !filesOnly) {
                        folderChildren.add(file)
                    }
                    if (file.isFile && allSupportedMusicExtensionFormats.contains(file.extension.lowercase()))
                        fileChildren.add(file)
                }

                if (fileChildren.size > 0 && !filesOnly) {
                    children.add(CreatePlayDirectoryCommandItem(directory));
                    children.add(CreateEnqueueDirectoryCommandItem(directory))
                }
                if (folderChildren.size > 0 && !filesOnly && SettingsWrapper.ShowHierarchyCommands()) {
                    children.add(CreatePlayHierarchyDirectoryCommandItem(directory = directory));
                    children.add(CreateEnqueueHierarchyDirectoryCommandItem(directory = directory));
                }
                for (file in folderChildren)
                    children.add(CreateMediaItemFromFolder(file))
                for (file in fileChildren.stream().sorted(FileNameComparator())
                    .collect(Collectors.toList()))
                    children.add(CreateMediaItemFromFile(file, false))
            }
            return ImmutableList.copyOf(children)
        }
    }

    private class FileFolderHierarchyMediaItemNode(item: MediaItem, val rootPath: String?) :
        MediaItemNode(item) {
        override fun getChildren(): List<MediaItem> {

            val children: MutableList<MediaItem> = ArrayList()

            val directory: File;
            if (rootPath == null) {
                directory = Environment.getExternalStorageDirectory()
            } else {
                directory = File(rootPath!!)
            }
            if (!directory.exists())
                return children
            if (directory.isFile) {
                children.add(CreateMediaItemFromFile(directory, false))
            } else {
                val foldersToParse = LinkedList<File>();
                val filesDetected = LinkedList<File>();

                foldersToParse.add(directory);
                while (foldersToParse.isNotEmpty()) {
                    val currentDirectory = foldersToParse.removeFirst();
                    val currentChildren = currentDirectory.listFiles();
                    val fileChildrenOfCurrentDirectory = LinkedList<File>();


                    for (file in currentChildren) {

                        if (file.isHidden) continue;
                        if (!file.canRead()) continue;

                        if (file.isDirectory) {
                            foldersToParse.add(file)
                        }
                        if (file.isFile && allSupportedMusicExtensionFormats.contains(file.extension.lowercase()))
                            fileChildrenOfCurrentDirectory.add(file)
                    }

                    filesDetected.addAll(
                        fileChildrenOfCurrentDirectory.stream().sorted(FileNameComparator())
                            .collect(Collectors.toList())
                    )
                }

                for (file in filesDetected) {
                    children.add(CreateMediaItemFromFile(file, false));
                }
            }

            return ImmutableList.copyOf(children)
        }
    }

    private fun buildMediaItem(
        title: String,
        mediaId: String,
        isPlayable: Boolean,
        @FolderType folderType: Int,
        album: String? = null,
        artist: String? = null,
        genre: String? = null,
        sourceUri: Uri? = null,
        imageUri: Uri? = null
    ): MediaItem {

        val extraProps = Bundle();

        extraProps.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
        extraProps.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);
        extraProps.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, imageUri?.path);
        val metadataBuilder =
            Builder()
                .setAlbumTitle(album)
                .setTitle(title)
                .setArtist(artist)
                .setGenre(genre)
                .setDisplayTitle(title)
                .setDisplayTitle(title)
                .setFolderType(folderType)
                .setIsPlayable(isPlayable)
                .setArtworkUri(imageUri);

        metadataBuilder.setExtras(extraProps)
        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setMediaMetadata(metadataBuilder.build())
            .setUri(sourceUri)
            .build()
    }

    fun initialize() {
        if (isInitialized) return
        isInitialized = true
        // create root and folders for album/artist/genre.
        defaultTreeNodes[ROOT_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Root Folder",
                    mediaId = ROOT_ID,
                    isPlayable = false,
                    folderType = FOLDER_TYPE_MIXED,
                    imageUri = Utilities.GetUriResource(
                        R.drawable.foldericon,
                        context = MCApplication.context!!
                    )
                )
            )
        defaultTreeNodes[FOLDERS_ID] =
            FileFolderMediaItemNode(
                buildMediaItem(
                    title = "Files + Folders",
                    mediaId = FOLDERS_ID,
                    isPlayable = false,
                    folderType = FOLDER_TYPE_MIXED,
                    imageUri = Utilities.GetUriResource(
                        R.drawable.foldericon,
                        context = MCApplication.context!!
                    ),
                ), rootPath = null,
                filesOnly = false
            )

        defaultTreeNodes[ROOT_ID]!!.addChild(FOLDERS_ID)
    }


    //Expects a safe ID (no GUID)
    fun getItem(id: String): MediaItem? {
        val file = File(id)
        if (file.exists() && file.isFile && !file.isHidden) {
            return CreateMediaItemFromFile(file, false)
        } else return null
    }

    fun getItems(ids: List<String>): List<MediaItem> {
        val items = LinkedList<MediaItem>();

        for (id in ids) {
            val item = getItem(id.SafeMediaId());
            if (item != null)
                items.add(item)
        }

        return items;
    }

    fun getRootItem(): MediaItem {
        return defaultTreeNodes[ROOT_ID]!!.item
    }

    fun getChildren(id: String, filesOnly: Boolean): List<MediaItem>? {

        if (defaultTreeNodes.containsKey(id))
            return defaultTreeNodes[id]?.getChildren()
        else {
            val finalId = id.SafeMediaId();
            if (id.isStartFromItemCommand()) {
                //this is a start from item command.
                //the actual children depend on the item's directory, not on the item itself.

                val targetPath = Paths.get(finalId).parent.toString()
                var mappedCommand = PlayCommandPrefix;
                if (id.startsWith(PlayHierarchyFromStartingItem))
                    mappedCommand = PlayDeepCommandPrefix;
                return getChildren(mappedCommand + targetPath, true);
            }
            if (id.isHierarchyCommand()) {
                return FileFolderHierarchyMediaItemNode(
                    buildMediaItem(
                        title = finalId,
                        mediaId = finalId,
                        isPlayable = false,
                        folderType = FOLDER_TYPE_MIXED
                    ), rootPath = finalId
                ).getChildren()
            } else {
                return FileFolderMediaItemNode(
                    buildMediaItem(
                        title = finalId,
                        mediaId = finalId,
                        isPlayable = false,
                        folderType = FOLDER_TYPE_MIXED
                    ), rootPath = finalId, filesOnly = filesOnly
                ).getChildren()
            }
        }
    }

    @SuppressLint("Range")
    fun getAllMusicFilesFromMediaStore(context: Context): ArrayList<String>? {
        val musicInfos: ArrayList<String> = ArrayList<String>()
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        ) ?: return null
        for (i in 0 until cursor.count) {
            cursor.moveToNext()
            val isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC))
            if (isMusic != 0) {
                val path = cursor.getString(
                    cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                )
                if (!File(path).exists()) {
                    continue
                }

                musicInfos.add(path)
            }
        }
        return musicInfos
    }

    fun getNextCategory(currentMediaItem: MediaItem): MediaItem {

        val mediaId = currentMediaItem.SafeMediaId();
        val file = File(mediaId);

        if (!file.exists()) return currentMediaItem;

        val parentDirectory = file.parent;
        val parent = File(parentDirectory);

        if (!parent.exists()) return currentMediaItem;

        var categoryRootFolder = File(SettingsWrapper.CategoryRootFolder());
        if (!categoryRootFolder.exists()) {
            categoryRootFolder =
                File(Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_MUSIC);
        }
        //get the children of root category folder
        val childrenOfCategoryRootFolder = LinkedList<String>();

        try {
            val directories = Files.walk(Paths.get(categoryRootFolder.path), Int.MAX_VALUE)
                .filter { path: Path? ->
                    path != null && allSupportedMusicExtensionFormats.contains(path.extension);
                }
                .map { it.parent.toString() }
                .collect(Collectors.toSet())
            childrenOfCategoryRootFolder.addAll(directories);
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //find the index of the current folder in the root category folder
        var index = childrenOfCategoryRootFolder.indexOf(parentDirectory);
        if (index == -1) return currentMediaItem;

        val childrenSize = childrenOfCategoryRootFolder.size;
        index = (index + 1).mod(childrenSize);

        //finally
        val mediaItem = CreatePlayDirectoryCommandItem(File(childrenOfCategoryRootFolder[index]));
        return mediaItem;
    }

    private class DirectoriesFilter : DirectoryStream.Filter<Path?> {
        @Throws(IOException::class)
        override fun accept(entry: Path?): Boolean {
            return Files.isDirectory(entry)
        }
    }
}

fun MediaItem.SafeMediaId(): String {
    val uuidTest = this.mediaId.extractTestUUID()
    if (uuidTest != null)
        return this.mediaId.removeRange(0, 36);
    return this.mediaId;
}

fun String.SafeMediaId(): String {

    val uuidTest = extractTestUUID()
    if (uuidTest != null)
        return this.removeRange(0, 36);
    return this;
}

private fun String.extractTestUUID(): UUID? {
    try {
        val substring = this.substring(0, 36);
        val uuidTest = UUID.fromString(substring);
        return uuidTest

    } catch (e: Exception) {
        return null;
    }
}

fun CreateSafeMediaId(mediaID: String): String {
    return UUID.randomUUID().toString() + mediaID;
}

fun CreateSafeMediaId(mediaID: String, uuid: String): String {
    return uuid.toString() + mediaID;
}


fun MediaItem.isCommand(): Boolean {
    return mediaId.startsWith(MediaItemTree.PlayCommandPrefix) || mediaId.startsWith(
        MediaItemTree.EnqueueCommandPrefix
    ) || mediaId.startsWith(MediaItemTree.PlayDeepCommandPrefix) || mediaId.startsWith(
        MediaItemTree.EnqueueDeepCommandPrefix
    );
}

fun MediaItem.isPlayCommand(): Boolean {
    return mediaId.startsWith(MediaItemTree.PlayCommandPrefix) || mediaId.startsWith(
        MediaItemTree.EnqueueDeepCommandPrefix
    );
}

fun MediaItem.isEnqueueCommand(): Boolean {
    return this.mediaId.startsWith(MediaItemTree.EnqueueCommandPrefix) || this.mediaId.startsWith(
        MediaItemTree.EnqueueDeepCommandPrefix
    );
}

fun MediaItem.isHierarchyCommand(): Boolean {
    return mediaId.startsWith(MediaItemTree.EnqueueDeepCommandPrefix);
}

fun MediaItem.isEnqueueHierarchyCommand(): Boolean {
    return mediaId.startsWith(MediaItemTree.EnqueueDeepCommandPrefix);
}

fun String.isPlayCommand(): Boolean {
    return startsWith(MediaItemTree.PlayCommandPrefix) || startsWith(MediaItemTree.PlayDeepCommandPrefix) || isStartFromItemCommand()
}

fun String.isEnqueueCommand(): Boolean {
    return startsWith(MediaItemTree.EnqueueCommandPrefix) || startsWith(MediaItemTree.EnqueueDeepCommandPrefix);
}

fun String.isHierarchyCommand(): Boolean {
    return startsWith(MediaItemTree.PlayDeepCommandPrefix) || startsWith(MediaItemTree.EnqueueDeepCommandPrefix) || startsWith(
        MediaItemTree.PlayHierarchyFromStartingItem
    );
}

fun String.isStartFromItemCommand(): Boolean {
    return startsWith(MediaItemTree.PlayShallowFolderFromStartingItem) || startsWith(MediaItemTree.PlayHierarchyFromStartingItem);
}

fun MediaItem.CreatePlayCommandFromItem(): MediaItem {
    val rootId = this.SafeMediaId()
    return this.buildUpon().setMediaId(PlayShallowFolderFromStartingItem + rootId).build();
}

fun MediaItem.CreatePlayHierarchyCommandFromItem(): MediaItem {
    val rootId = this.SafeMediaId()
    return this.buildUpon().setMediaId(PlayHierarchyFromStartingItem + rootId).build();
}
