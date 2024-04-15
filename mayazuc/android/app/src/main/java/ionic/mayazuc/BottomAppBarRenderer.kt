package ionic.mayazuc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat.startActivity
import ionic.mayazuc.ui.theme.MayazucLiteTheme

object BottomAppBarRenderer {
    @Composable
    public fun CreateBottomAppBar(context: Activity, selectedIndex: Int) {

        val selectedButtonColor = IconButtonDefaults.iconButtonColors(
            contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
            containerColor = Color.Transparent
        )

        val unselectedButtonColor = IconButtonDefaults.iconButtonColors(
            contentColor = Color.Gray,
            containerColor = Color.Transparent
        )

        MayazucLiteTheme {
            Row(modifier = Modifier.background(Color.Transparent)) {

                IconButton(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent),
                    colors = if (selectedIndex == 0) selectedButtonColor else unselectedButtonColor,
                    onClick = {
                        openMediaBrowserActivity(context)
                    }) {
                    Row() {
                        Text(text = "Library")
                    }
                }

                IconButton(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent),
                    colors = if (selectedIndex == 1) selectedButtonColor else unselectedButtonColor,
                    onClick = {
                        openPlayerActivity(context)
                    }) {
                    Row() {
                        Text(text = "Playing")
                    }
                }

                IconButton(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent),
                    colors = if (selectedIndex == 2) selectedButtonColor else unselectedButtonColor,
                    onClick = {
                        openPlaybackQueueActivity(context)
                    }) {
                    Row() {
                        Text(text = "Queue")
                    }
                }

                IconButton(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent),
                    colors = if (selectedIndex == 3) selectedButtonColor else unselectedButtonColor,
                    onClick = {
                        openSettingsActivity(context)
                    }) {
                    Row() {
                        Text(text = "Tools")
                    }
                }
            }
        }
    }

    private fun openSettingsActivity(context: Activity) {
        val intent = Intent(context, SettingsActivityCompose::class.java)
        startActivity(context, intent, Bundle.EMPTY)
        context.overridePendingTransition(0, 0);
    }

    private fun openPlayerActivity(context: Activity) {
        val intent = Intent(context, MediaPlayerActivity2::class.java)
        startActivity(context, intent, Bundle.EMPTY)
        context.overridePendingTransition(0, 0);

    }

    private fun openMediaBrowserActivity(context: Activity) {
        val intent = Intent(context, MainActivity::class.java)
        startActivity(context, intent, Bundle.EMPTY)
        context.overridePendingTransition(0, 0);

    }

    private fun openPlaybackQueueActivity(context: Activity) {
        val intent = Intent(context, PlaybackQueueActivity::class.java)
        startActivity(context, intent, Bundle.EMPTY)
        context.overridePendingTransition(0, 0);

    }
}