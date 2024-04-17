import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonFab, IonFabButton, IonIcon } from '@ionic/react';
import './Tab1.css';
import { MediaItemDto } from '../MediaItemDto';
import MediaController from '../android-media-controller';
import { Component } from 'react';
import { render } from 'react-dom';
import * as React from 'react';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Checkbox from '@mui/material/Checkbox';
import Avatar from '@mui/material/Avatar';
import { Box } from '@mui/material';
import { App as CapacitorApp } from '@capacitor/app'
import { backspace } from 'ionicons/icons';

class Tab1 extends Component {

  private mediaItemsMixed: MediaItemDto[] = [];
  private mediaItemsTitles: MediaItemDto[] = [];
  private mediaItemsFiles: MediaItemDto[] = []
  private backStack: MediaItemDto[] = [];
  private currentItem: MediaItemDto | undefined = undefined;

  rootMediaItem(): MediaItemDto {
    let item: MediaItemDto = new MediaItemDto();
    item.mediaId = "[rootID]";
    return item;
  }

  constructor(props: {}) {
    super(props);
  }


  async componentDidMount() {
    // Perform initialization tasks, data fetching, etc.
    await this.loadMediaItem(this.rootMediaItem(), true);
    //alert("loading back button handler");

    document.addEventListener('ionBackButton', (ev) => {
      (ev as CustomEvent).detail.register(10, async (processNextHandler: () => void) => {
        //alert("back button pressed");

        var previousItem = this.backStack.pop();
        if (previousItem != null)
          await this.loadMediaItem(previousItem!, false);
        else
          processNextHandler();
      });
    });
  }

  componentWillUnmount() {
    // Perform cleanup tasks, unsubscribe from events, etc.
    //alert("unloading back button handler");
    //document.removeEventListener('ionBackButton', this.backButtonHandler);
  }

  async loadMediaItem(item: MediaItemDto, addToBackStack: boolean): Promise<void> {

    if (this.currentItem != undefined && addToBackStack) {
      this.backStack.push(this.currentItem!);
    }

    await this.loadMediaID(item.mediaId);
    this.currentItem = item;
  }


  async loadMediaID(mediaItem: string): Promise<void> {

    this.mediaItemsMixed.splice(0, this.mediaItemsMixed.length);
    this.mediaItemsTitles.splice(0, this.mediaItemsTitles.length);
    this.mediaItemsFiles.splice(0, this.mediaItemsFiles.length);

    const { value } = await MediaController.openMediaId({ value: mediaItem });
    console.log("B:AB:ABLA" + value);

    var newItems: MediaItemDto[] = JSON.parse(value);
    for (let i = 0; i < newItems.length; i++) {
      var item = newItems[i];
      if (item.type == "FOLDER_TYPE_MIXED") {
        this.mediaItemsMixed.push(item);
      }
      else if (item.type == "FOLDER_TYPE_TITLES") {
        this.mediaItemsTitles.push(item);
      }
      else if (item.type == "FOLDER_TYPE_NONE") {
        this.mediaItemsFiles.push(item);
      }
    }
    this.forceUpdate();
  }


  handleToggle(value: MediaItemDto): void {
    value.isSelected = !value.isSelected;
  }

  render() {
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
            <Box sx={{ width: '100%', maxWidth: 360, bgcolor: 'background.paper' }}>
              <List dense sx={{ width: '100%', maxWidth: 360, bgcolor: 'background.paper' }}>
                {this.mediaItemsTitles.map((value) => {
                  const labelId = `checkbox-list-secondary-label-${value}`;
                  return (
                    <ListItem
                      key={value.mediaId}
                      disablePadding
                    >
                      <ListItemButton onClick={() => { this.loadMediaItem(value, true); }}>
                        <ListItemAvatar>
                          <Avatar
                            alt={`${value.title}`}
                            src={value.imageUrl}
                          />
                        </ListItemAvatar>
                        <ListItemText id={labelId} primary={`${value.title}`} />
                      </ListItemButton>
                    </ListItem>
                  );
                })}
              </List>

              <List dense sx={{ width: '100%', maxWidth: 360, bgcolor: 'background.paper' }}>
                {this.mediaItemsMixed.map((value) => {
                  const labelId = `checkbox-list-secondary-label-${value}`;
                  return (
                    <ListItem
                      key={value.mediaId}

                      disablePadding
                    >
                      <ListItemButton onClick={() => { this.loadMediaItem(value, true); }}>
                        <ListItemAvatar>
                          <Avatar
                            alt={`${value.title}`}
                            src={value.imageUrl}
                          />
                        </ListItemAvatar>
                        <ListItemText id={labelId} primary={`${value.title}`} />
                      </ListItemButton>
                    </ListItem>
                  );
                })}
              </List>

              <List dense sx={{ width: '100%', maxWidth: 360, bgcolor: 'background.paper' }}>
                {this.mediaItemsFiles.map((value) => {
                  const labelId = `checkbox-list-secondary-label-${value}`;
                  return (
                    <ListItem
                      key={value.mediaId}
                      secondaryAction={
                        <Checkbox
                          edge="end"
                          onChange={() => { this.handleToggle(value); alert(value.isSelected.toString()) }}
                          checked={value.isSelected}
                          inputProps={{ 'aria-labelledby': labelId }}
                        />
                      }
                      disablePadding
                    >
                      <ListItemButton onClick={() => { this.loadMediaItem(value, true); }}>
                        <ListItemAvatar>
                          <Avatar
                            alt={`${value.title}`}
                            src={value.imageUrl}
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
  }

}



export default Tab1;
