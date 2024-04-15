package ionic.mayazuc

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
class LibVlcForwardingPlayer(val player: LibVlcPlayer) : ForwardingPlayer(player) {

    override fun hasNext(): Boolean {
        return true;
    }

    override fun hasPrevious(): Boolean {
        return true;
    }

    override fun seekToNext() {
        if (repeatMode == Player.REPEAT_MODE_OFF) {
            if (player.isAtEndOfQueue())
                player.changeCategory()
        }
        player.seekToNext2()
    }

    override fun seekToPrevious() {
        super.seekToPrevious()
    }

    override fun getCurrentMediaItemIndex(): Int {
        return super.getCurrentMediaItemIndex()
    }

    override fun hasNextMediaItem(): Boolean {
        return true;
    }

    override fun hasPreviousMediaItem(): Boolean {
        return true;
    }

    override fun seekToNextMediaItem() {
        super.seekToNextMediaItem()
    }

    override fun seekToPreviousMediaItem() {
        super.seekToPreviousMediaItem()
    }

    override fun getRepeatMode(): Int {
        return super.getRepeatMode()
    }
}