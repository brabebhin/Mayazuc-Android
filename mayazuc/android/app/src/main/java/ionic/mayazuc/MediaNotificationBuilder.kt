package ionic.mayazuc

import android.content.Context
import android.os.Bundle
import androidx.media3.common.Player.*
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList

class MediaNotificationBuilder(val context: Context) : DefaultMediaNotificationProvider(context) {

    override fun getMediaButtons(
        session: MediaSession,
        playerCommands: Commands,
        customLayout: ImmutableList<CommandButton>,
        showPauseButton: Boolean
    ): ImmutableList<CommandButton> {

        session.setSessionExtras(Bundle().apply {  });
        val mediaButtons = mutableListOf<CommandButton>()
        val skipPreviousCommandButton = CommandButton.Builder().setPlayerCommand(COMMAND_SEEK_TO_PREVIOUS).setEnabled(true).setIconResId(androidx.media3.ui.R.drawable.exo_ic_skip_previous)
            .setExtras(Bundle().apply { putInt("androidx.media3.session.command.COMPACT_VIEW_INDEX", 0)}).build();

        val playCommandButton = CommandButton.Builder().setPlayerCommand(COMMAND_PLAY_PAUSE).setEnabled(true).setIconResId(if(showPauseButton) androidx.media3.ui.R.drawable.exo_icon_pause else androidx.media3.ui.R.drawable.exo_icon_play)
            .setExtras(Bundle().apply { putInt("androidx.media3.session.command.COMPACT_VIEW_INDEX", 1)}).build();

        val skipNextCommandButton = CommandButton.Builder().setPlayerCommand(COMMAND_SEEK_TO_NEXT).setEnabled(true).setIconResId(androidx.media3.ui.R.drawable.exo_ic_skip_next)
            .setExtras(Bundle().apply { putInt("androidx.media3.session.command.COMPACT_VIEW_INDEX", 2)}).build();

        mediaButtons.clear()
        mediaButtons.add(skipPreviousCommandButton)
        mediaButtons.add(playCommandButton)
        mediaButtons.add(skipNextCommandButton)
        return ImmutableList.copyOf(mediaButtons);
    }
}
