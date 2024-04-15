package ionic.mayazuc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import ionic.mayazuc.MCApplication.Companion.context
import ionic.mayazuc.UiUtilities.MayazucScaffold
import ionic.mayazuc.ui.theme.MayazucLiteTheme
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.checkboxPreference
import me.zhanghai.compose.preference.preference

class SettingsActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        renderUI()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun renderUI() {
        setContent {

            MayazucLiteTheme {
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

                MayazucScaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(
                                    text = "Settings",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            scrollBehavior = scrollBehavior
                        )
                    },
                    bottomBar = {
                                BottomAppBar() {
                                    BottomAppBarRenderer.CreateBottomAppBar(this@SettingsActivityCompose, 3)
                                }
                    },
                    content = {

                        ProvidePreferenceLocals {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {

                                preference(
                                    key = "resetcache",
                                    title = { Text(text = "Reset album art cache") },
                                    onClick = {
                                        if (context != null) {
                                            AlbumArtScanWorker.StartWorking(true, context!!)
                                        }
                                    }
                                )

                                checkboxPreference(
                                    key = getString(R.string.ignore_leading_numbers_key),
                                    defaultValue = SettingsWrapper.IgnoreLeadingNumbersInFileNames(),
                                    title = { Text(text = "Ignore leading numbers in file names") },
                                    summary = { Text(text = if (it) "On" else "Off") }
                                )

                                checkboxPreference(
                                    key = getString(R.string.skip_to_queue_item_in_external_controller),
                                    defaultValue = SettingsWrapper.SkipToQueueItemInExternalControllers(),
                                    title = { Text(text = "Skip to queue item in external controllers") },
                                    summary = { Text(text = if (it) "On" else "Off") }
                                )

                                checkboxPreference(
                                    key = getString(R.string.show_hierarchy_commands_in_browser),
                                    defaultValue = SettingsWrapper.ShowHierarchyCommands(),
                                    title = { Text(text = "Show folder hierarchy commands (play and enqueue) in media browser\"") },
                                    summary = { Text(text = if (it) "On" else "Off") }
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        }
    }
}



