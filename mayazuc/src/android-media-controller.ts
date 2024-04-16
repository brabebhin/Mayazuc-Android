import { registerPlugin } from '@capacitor/core';

export interface MediaControllerIonicPlugin {
    openMediaId(options: { value: string }): Promise<{value: string}>;
    autoPlayPause(options: { value: string }): Promise<{ value: string }>;
    skipNext(options: { value: string }): Promise<{ value: string }>;
    skipPrevious(options: { value: string }): Promise<{ value: string }>;
    seek(options: { value: string }): Promise<{ value: string }>;
}

const MediaController = registerPlugin<MediaControllerIonicPlugin>('AndroidMediaController');

 export default MediaController;