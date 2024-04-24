import { Pause, PlayArrow } from "@mui/icons-material";
import { IconButton } from "@mui/material";
import MediaController from "../android-media-controller";

interface PlayPauseButtonProps {
    isPlaying: boolean;
}

const playPauseButton: React.FC<PlayPauseButtonProps> = ({ isPlaying }) => {
    return (
        <IconButton size="large"  color='primary'  aria-label="Play Pause" onClick={() => {
            MediaController.autoPlayPause({ value: "" });
        }} >
            {isPlaying ? <Pause fontSize="large"/> : <PlayArrow  fontSize="large"/>}
        </IconButton>
    );
};

export default playPauseButton;
