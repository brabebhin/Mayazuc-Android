package ionic.mayazuc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.ui.R
import ionic.mayazuc.Utilities.FormatMilisecondsToHoursMinutesSeconds


object PlayerControlBar {

    @Composable
    fun playerSeekBar(
        normalizedCurrentPosition: MutableState<Float> = remember { mutableStateOf(0f) },
        humanReadablePosition: MutableState<Long> = remember { mutableStateOf(0L) },
        totalDuration: MutableState<Long> = remember { mutableStateOf(0L) },
        remainingTime: MutableState<Long> = remember { mutableStateOf(0L) },
        onSeekCallback: (Float) -> Unit,
        isSeekBarPressedState: MutableState<Boolean>
    ) {

        val interactionSource = remember { MutableInteractionSource() }
        val isSeekBarPressed by interactionSource.collectIsDraggedAsState()
        isSeekBarPressedState.value = isSeekBarPressed;
        Column {
            Slider(
                value = normalizedCurrentPosition.value,
                onValueChangeFinished = {
                    onSeekCallback(normalizedCurrentPosition.value);
                },
                onValueChange = { normalizedCurrentPosition.value = it; },
                interactionSource = interactionSource,
            )
            Row() {
                Text(text = FormatMilisecondsToHoursMinutesSeconds(humanReadablePosition.value))
                Text(text = " - ")
                Text(text = FormatMilisecondsToHoursMinutesSeconds(remainingTime.value))
                Text(text = " - ")
                Text(text = FormatMilisecondsToHoursMinutesSeconds(totalDuration.value))

            }
        }
    }

    @Composable
    fun playerCommandBar(
        isPlaying: MutableState<Boolean> = remember {
            mutableStateOf(false)
        },
        onPreviousCallback: () -> Unit,
        onRewindCallback: () -> Unit,
        onPlayPauseCallback: () -> Unit,
        onFastForwardCallback: () -> Unit,
        onNextCallback: () -> Unit,
        onRepeatCallback: () -> Unit,
        onShuffleCallback: () -> Unit,
        repeatMode: MutableState<Int> = mutableStateOf(Player.REPEAT_MODE_ONE),
        isShuffleEnabled: MutableState<Boolean> = mutableStateOf(false)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
        ) {

            IconButton(
                modifier = Modifier.weight(1F),
                onClick = {
                    onRepeatCallback();
                })
            {
                var repeatIcon = androidx.media3.ui.R.drawable.exo_icon_repeat_one;
                if(repeatMode.value == Player.REPEAT_MODE_OFF)
                {
                    repeatIcon = androidx.media3.ui.R.drawable.exo_icon_repeat_off;
                }
                else if(repeatMode.value == Player.REPEAT_MODE_ALL)
                {
                    repeatIcon = R.drawable.exo_icon_repeat_all;
                }
                Image(
                    painterResource(id = repeatIcon),
                    contentDescription = "Skip to queue item",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                modifier = Modifier.weight(1F),
                onClick = {
                    onPreviousCallback();
                })
            {
                Image(
                    painterResource(id = androidx.media3.ui.R.drawable.exo_ic_skip_previous),
                    contentDescription = "Skip to queue item",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                modifier = Modifier.weight(1F),
                onClick = {
                    onRewindCallback();
                })
            {
                Image(
                    painterResource(id = androidx.media3.ui.R.drawable.exo_ic_rewind),
                    contentDescription = "Skip to queue item",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                modifier = Modifier.weight(1F),
                onClick = {
                    onPlayPauseCallback();
                })
            {
                Image(
                    painterResource(id = if (!isPlaying.value) androidx.media3.ui.R.drawable.exo_icon_play else androidx.media3.ui.R.drawable.exo_icon_pause),
                    contentDescription = "Skip to queue item",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                modifier = Modifier.weight(1F),
                onClick = {
                    onFastForwardCallback();
                })
            {
                Image(
                    painterResource(id = androidx.media3.ui.R.drawable.exo_ic_forward),
                    contentDescription = "Skip to queue item",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                modifier = Modifier.weight(1F),
                onClick = {
                    onNextCallback();
                })
            {
                Image(
                    painterResource(id = androidx.media3.ui.R.drawable.exo_ic_skip_next),
                    contentDescription = "Skip to queue item",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                modifier = Modifier.weight(1F),
                onClick = {
                    onShuffleCallback();
                })
            {
                Image(
                    painterResource(id = if (isShuffleEnabled.value) R.drawable.exo_icon_shuffle_on else R.drawable.exo_icon_shuffle_off),
                    contentDescription = "Skip to queue item",
                    modifier = Modifier.size(32.dp)
                )
            };

        }
    }
}