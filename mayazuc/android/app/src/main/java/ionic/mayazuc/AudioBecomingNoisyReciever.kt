package ionic.mayazuc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.media3.common.util.UnstableApi

@UnstableApi class AudioBecomingNoisyReciever(val player: LibVlcPlayer) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (intent.action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            player.notifyBecomingNoisy()
        }
    }
}