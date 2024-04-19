import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonFab, IonFabButton, IonIcon, useIonViewDidEnter, useIonViewDidLeave } from '@ionic/react';
import './MediaBrowserPage.css';
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

const MediaBrowserPage: React.FC = () => {

  const currentItem = useRef<MediaItemDto | null>(null);
  const [mediaItemsFiles, setmediaItemsFiles] = useState<MediaItemDto[]>([]);
  const [mediaItemsMixed, setmediaItemsMixed] = useState<MediaItemDto[]>([]);
  const [mediaItemsTitles, setmediaItemsTitles] = useState<MediaItemDto[]>([]);

  const { id } = useParams<{ id?: string }>();
  const history = useHistory();

  const rootMediaItem = () => {
    let item: MediaItemDto = new MediaItemDto();
    item.mediaId = "[rootID]";
    item.type = "FOLDER_TYPE_MIXED";
    return item;
  };

  useEffect(() => {
    // Code to run on first initialization
    componentDidMount();
  }, []); 


  const componentDidMount = async () => {
    if (id == null || id == undefined) {
      await loadMediaID(rootMediaItem().mediaId);
    }
    else {
      await loadMediaID(decodeURIComponent(id));
    }
  };

  const loadMediaItem = async (item: MediaItemDto): Promise<void> => {

    console.log("OPENING MEDIA ITEM " + item.mediaId);
    
    if (item.type == FODLER_TYPE_MIXED) {      
      let finalMediaIdRoute = item.mediaId;
      if (finalMediaIdRoute.startsWith('/')) {
        finalMediaIdRoute = finalMediaIdRoute.substring(1, finalMediaIdRoute.length);
      }
      let route = `/mediabrowser/${encodeURIComponent(finalMediaIdRoute)}`
      history.push(route);
      currentItem.current = item;
    }
    else {
      await MediaController.playMediaId({ value: item.mediaId });
    }

  };


  const loadMediaID = async (mediaItem: string): Promise<void> => {

    const { value } = await MediaController.openMediaId({ value: mediaItem });

    var newMixedItems: MediaItemDto[] = [];
    var newMFileItems: MediaItemDto[] = [];
    var newTitleItems: MediaItemDto[] = [];


    var newItems: MediaItemDto[] = JSON.parse(value);
    for (let i = 0; i < newItems.length; i++) {
      var item = newItems[i];
      if (item.type == FODLER_TYPE_MIXED) {
        newMixedItems.push(item);
      }
      else if (item.type == FOLDER_TYPE_TITLES) {
        newTitleItems.push(item);
      }
      else if (item.type == FOLDER_TYPE_NONE) {
        newMFileItems.push(item);
      }
    }

    setmediaItemsMixed(newMixedItems);
    setmediaItemsTitles(newTitleItems);
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
              {mediaItemsTitles.map((value) => {
                const labelId = `checkbox-list-secondary-label-${value}`;
                return (
                  <ListItem
                    key={value.mediaId}
                    disablePadding
                  >
                    <ListItemButton onClick={() => { loadMediaItem(value); }}>
                      <ListItemAvatar>
                        <Avatar
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

            <List dense sx={{ width: '100%' }}>
              {mediaItemsMixed.map((value) => {
                const labelId = `checkbox-list-secondary-label-${value}`;
                return (
                  <ListItem
                    key={value.mediaId}

                    disablePadding
                  >
                    <ListItemButton onClick={() => { loadMediaItem(value); }}>
                      <ListItemAvatar>
                        <Avatar
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

export default MediaBrowserPage;
