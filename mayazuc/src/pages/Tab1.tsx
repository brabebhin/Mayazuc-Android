import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonFab, IonFabButton, IonIcon, useIonViewDidEnter, useIonViewDidLeave } from '@ionic/react';
import './Tab1.css';
import { MediaItemDto } from '../MediaItemDto';
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
import { App } from '@capacitor/app';
import { forceUpdate } from 'ionicons/dist/types/stencil-public-runtime';

const Tab1: React.FC = () => {
  const backStack = useRef<MediaItemDto[]>([]);

  const currentItem = useRef<MediaItemDto | null>(null);
  const [mediaItemsFiles, setmediaItemsFiles] = useState<MediaItemDto[]>([]);
  const [mediaItemsMixed, setmediaItemsMixed] = useState<MediaItemDto[]>([]);
  const [mediaItemsTitles, setmediaItemsTitles] = useState<MediaItemDto[]>([]);

  const rootMediaItem = () => {
    let item: MediaItemDto = new MediaItemDto();
    item.mediaId = "[rootID]";
    return item;
  };

  useEffect(() => {
    // Code to run on first initialization
    console.log('Component initialized');
    componentDidMount();
  }, []); // Empty dependency array ensures that the effect runs only once on component mount


  const handleBackButton = async () => {
    // Your logic to handle the back button press
    console.log('Android back button pressed');
    console.log('backstack has ' + backStack.current.length);

    var previousItem = backStack.current.pop();
  
    console.log('privious item id ' + previousItem!.mediaId);
    if (previousItem != null) {
      await loadMediaItem(previousItem!, false);
      return true;
    }
    else
      return false;
    
  };

  // Register the back button handler when the page enters the view stack
  useIonViewDidEnter(() => {

    App.addListener('backButton', handleBackButton);
  });

  useIonViewDidLeave(() => {
    App.removeAllListeners();
  });

  const componentDidMount = async () => {
    // Perform initialization tasks, data fetching, etc.
    await loadMediaItem(rootMediaItem(), true);
    //alert("loading back button handler");

    // document.addEventListener('ionBackButton', (ev) => {
    //   (ev as CustomEvent).detail.register(10, async (processNextHandler: () => void) => {
    //     alert("back button pressed");

    //     var previousItem = backStack.pop();
    //     if (previousItem != null)
    //       await loadMediaItem(previousItem!, false);
    //     else
    //       processNextHandler();
    //   });
    // });
  };

  const componentWillUnmount = () => {
    // Perform cleanup tasks, unsubscribe from events, etc.
    //alert("unloading back button handler");
    //document.removeEventListener('ionBackButton', this.backButtonHandler);
  };

  const loadMediaItem = async (item: MediaItemDto, addToBackStack: boolean): Promise<void> => {
    
    if (currentItem.current != null && addToBackStack) {
      console.log('pushing to backstack ' + currentItem!.current!.mediaId);
      backStack.current.push(currentItem!.current!);
    }

    await loadMediaID(item.mediaId);
    currentItem.current = item;
    console.log('current item id = ' + currentItem!.current!.mediaId);

  };


  const loadMediaID = async (mediaItem: string): Promise<void> => {

    const { value } = await MediaController.openMediaId({ value: mediaItem });
    console.log("B:AB:ABLA" + value);

    var newMixedItems: MediaItemDto[] = [];
    var newMFileItems: MediaItemDto[] = [];
    var newTitleItems: MediaItemDto[] = [];


    var newItems: MediaItemDto[] = JSON.parse(value);
    for (let i = 0; i < newItems.length; i++) {
      var item = newItems[i];
      if (item.type == "FOLDER_TYPE_MIXED") {
        newMixedItems.push(item);
      }
      else if (item.type == "FOLDER_TYPE_TITLES") {
        newTitleItems.push(item);
      }
      else if (item.type == "FOLDER_TYPE_NONE") {
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
                    <ListItemButton onClick={() => { loadMediaItem(value, true); }}>
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
                    <ListItemButton onClick={() => { loadMediaItem(value, true); }}>
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
                        onChange={() => { handleToggle(value); alert(value.isSelected.toString()) }}
                        checked={value.isSelected}
                        inputProps={{ 'aria-labelledby': labelId }}
                      />
                    }
                    disablePadding
                  >
                    <ListItemButton onClick={() => { loadMediaItem(value, true); }}>
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

export default Tab1;
