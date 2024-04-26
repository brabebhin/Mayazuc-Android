import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonFab, IonFabButton, IonIcon, useIonViewDidEnter, useIonViewDidLeave } from '@ionic/react';
import './NowPlayingQueuePage.css';
import { FODLER_TYPE_MIXED, FOLDER_TYPE_NONE, FOLDER_TYPE_TITLES, MediaItemDto } from '../MediaItemDto';
import MediaController from '../android-media-controller';
import React, { Component, useEffect, useState, useRef } from 'react';
import { render } from 'react-dom';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Checkbox from '@mui/material/Checkbox';
import Avatar from '@mui/material/Avatar';
import { Box } from '@mui/material';
import { Capacitor } from '@capacitor/core'
import { App, BackButtonListenerEvent } from '@capacitor/app';
import { useParams, useHistory } from 'react-router-dom';

const NowPlayingQueuePage: React.FC = () => {

    const [mediaItemsFiles, setmediaItemsFiles] = useState<MediaItemDto[]>([]);

    useEffect(() => {
        // Code to run on first initialization
        componentDidMount();
    }, []);


    const componentDidMount = async () => {
        loadNowPlayingQueue();
    };

    const loadMediaItem = async (item: MediaItemDto): Promise<void> => {

        await MediaController.skipToQueueItem({ value: item.mediaId });
    };


    const loadNowPlayingQueue = async (): Promise<void> => {

        const { value } = await MediaController.getPlaybackQueue({ value: "nowplayingqueue" });

        var newMFileItems: MediaItemDto[] = [];

        var newItems: MediaItemDto[] = JSON.parse(value);
        for (let i = 0; i < newItems.length; i++) {
            var item = newItems[i];
            newMFileItems.push(item);
        }

        setmediaItemsFiles(newMFileItems);
    };


    const handleToggle = (value: MediaItemDto): void => {
        value.isSelected = !value.isSelected;
    };
    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonTitle>Media Browser</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent fullscreen>
                <IonHeader collapse="condense">
                    <IonToolbar>
                        <IonTitle size="large">Tab 1</IonTitle>
                    </IonToolbar>
                </IonHeader>

                <IonContent>
                    <Box sx={{ width: '100%' }}>
                        <List dense sx={{ width: '100%' }}>
                            {mediaItemsFiles.map((value) => {
                                const labelId = `checkbox-list-secondary-label-${value}`;
                                return (
                                    <ListItem
                                        key={value.mediaId}
                                        secondaryAction={
                                            <Checkbox
                                                edge="end"
                                                onChange={() => { handleToggle(value); }}
                                                checked={value.isSelected}
                                                inputProps={{ 'aria-labelledby': labelId }}
                                            />
                                        }
                                        disablePadding
                                    >
                                        <ListItemButton onClick={() => { loadMediaItem(value); }}>
                                            <ListItemAvatar>
                                                <Avatar
                                                    variant='square'
                                                    sizes='large'
                                                    alt={`${value.title}`}
                                                    src={Capacitor.convertFileSrc(decodeURI(value.imageUrl))}
                                                />
                                            </ListItemAvatar>
                                            <ListItemText id={labelId} primary={`${value.title}`} />
                                        </ListItemButton>
                                    </ListItem>
                                );
                            })}
                        </List>

                    </Box>
                </IonContent>
            </IonContent>
        </IonPage>
    );
};

export default NowPlayingQueuePage;
