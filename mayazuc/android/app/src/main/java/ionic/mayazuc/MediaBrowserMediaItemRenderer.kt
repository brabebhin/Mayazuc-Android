package ionic.mayazuc

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import coil.compose.AsyncImage
import coil.request.ImageRequest

object MediaBrowserMediaItemRenderer {
    @Composable
    fun RenderListView(data: MediaItem, clickMethod: (MediaItem) -> Unit) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable {
                clickMethod(data)
            }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                var expanded by remember { mutableStateOf(false) }

                Row() {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.mediaMetadata.artworkUri).build(),
                        contentDescription = data.mediaMetadata.title.toString(),
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp)
                    )

                    Column(Modifier.weight(1.0F)) {
                        Text(
                            data.mediaMetadata.title.toString(),
                            modifier = Modifier.padding(5.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (data.mediaMetadata.subtitle != null) Text(
                            data.mediaMetadata.subtitle.toString(),
                            modifier = Modifier.padding(5.dp)
                        )
                    }

                    if (!data.isCommand() && data.mediaMetadata.isPlayable!!) {
                        Column() {
                            Box() {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Localized description"
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Play from here") },
                                        onClick = { MediaPlayerUtilities.playMediaItemAsync(data.CreatePlayCommandFromItem()) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Play hierarchy from here") },
                                        onClick = { MediaPlayerUtilities.playMediaItemAsync(data.CreatePlayHierarchyCommandFromItem()) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun RenderListView(data: MediaItem, index: Int, clickMethod: (MediaItem) -> Unit) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable {
                clickMethod(data)
            }) {

            Row() {
                Text(
                    text = "# $index",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row() {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(data.mediaMetadata.artworkUri).build(),
                    contentDescription = data.mediaMetadata.title.toString(),
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp)
                )

                Column(Modifier.fillMaxWidth()) {
                    Text(
                        data.mediaMetadata.title.toString(),
                        modifier = Modifier.padding(5.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (data.mediaMetadata.subtitle != null) Text(
                        data.mediaMetadata.subtitle.toString(),
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun RenderSingleItem(
        data: MediaItem,
        isHorizontal: Boolean,
        playMethod: (MediaItem) -> Unit,
        enqueueMethod: (MediaItem) -> Unit,
        addToPlaylist: (MediaItem) -> Unit,
        SkipToIndexMethod: (MediaItem) -> Unit
    ) {

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            if (isHorizontal) {
                RenderSingleItemHorizontal(
                    data, playMethod, enqueueMethod, SkipToIndexMethod, addToPlaylist
                )
            } else {
                RenderSingleItemVertical(
                    data, playMethod, enqueueMethod, SkipToIndexMethod, addToPlaylist
                )
            }
        }
    }

    @Composable
    private fun RenderSingleItemHorizontal(
        data: MediaItem,
        playMethod: (MediaItem) -> Unit,
        enqueueMethod: (MediaItem) -> Unit,
        SkipToIndexMethod: (MediaItem) -> Unit,
        addToPlaylist: (MediaItem) -> Unit
    ) {
        Column() {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    data.mediaMetadata.title.toString(),
                    modifier = Modifier.padding(5.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (data.mediaMetadata.subtitle != null)
                    Text(
                        data.mediaMetadata.subtitle.toString(),
                        modifier = Modifier.padding(5.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
            }
            Row() {
                Column(Modifier.weight(1f, true)) {
                    Button(onClick = { playMethod(data) }) {
                        Image(
                            painterResource(id = R.drawable.playviewgeneric),
                            contentDescription = "Play file",
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = "Play file", Modifier.padding(start = 10.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(onClick = { enqueueMethod(data) }) {
                        Image(
                            painterResource(id = R.drawable.addtonowplayinggeneric),
                            contentDescription = "Add to now playing",
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = "Add to now playing", Modifier.padding(start = 10.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(onClick = { SkipToIndexMethod(data) }) {
                        Image(
                            painterResource(id = R.drawable.skiptoqueueitem),
                            contentDescription = "Skip to queue item",
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = "Skip to queue item", Modifier.padding(start = 10.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(onClick = { addToPlaylist(data) }) {
                        Image(
                            painterResource(id = R.drawable.foldericon),
                            contentDescription = "Add to playlist",
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = "Add to playlist", Modifier.padding(start = 10.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Column(Modifier.weight(1f, true)) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.mediaMetadata.artworkUri).build(),
                        contentDescription = data.mediaMetadata.title.toString(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    @Composable
    private fun RenderSingleItemVertical(
        data: MediaItem,
        playMethod: (MediaItem) -> Unit,
        enqueueMethod: (MediaItem) -> Unit,
        SkipToIndexMethod: (MediaItem) -> Unit,
        addToPlaylist: (MediaItem) -> Unit
    ) {
        Column() {

            Column(Modifier.fillMaxWidth()) {
                Text(
                    data.mediaMetadata.title.toString(),
                    modifier = Modifier.padding(5.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (data.mediaMetadata.subtitle != null)
                    Text(
                        data.mediaMetadata.subtitle.toString(),
                        modifier = Modifier.padding(5.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
            }


            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(data.mediaMetadata.artworkUri).build(),
                contentDescription = data.mediaMetadata.title.toString(),
                modifier = Modifier.fillMaxWidth()
            )


            Button(onClick = { playMethod(data) }) {
                Image(
                    painterResource(id = R.drawable.playviewgeneric),
                    contentDescription = "Play file",
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Play file", Modifier.padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(onClick = { enqueueMethod(data) }) {
                Image(
                    painterResource(id = R.drawable.addtonowplayinggeneric),
                    contentDescription = "Add to now playing",
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Add to now playing", Modifier.padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(onClick = { SkipToIndexMethod(data) }) {
                Image(
                    painterResource(id = R.drawable.skiptoqueueitem),
                    contentDescription = "Skip to queue item",
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Skip to queue item", Modifier.padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(onClick = { addToPlaylist(data) }) {
                Image(
                    painterResource(id = R.drawable.foldericon),
                    contentDescription = "Add to playlist",
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Add to playlist", Modifier.padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}