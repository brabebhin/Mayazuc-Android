import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonFab, IonFabButton, IonIcon} from '@ionic/react';
import ExploreContainer from '../components/ExploreContainer';
import { camera, trash, close } from 'ionicons/icons';
import './Tab1.css';
import '../MediaItemDto';
import MediaController from '../android-media-controller';
import React, { useEffect } from 'react';



const Tab1: React.FC = () => {

  let mediaItems  : MediaItemDto[] = [];

  function takePhoto(): void {
    loadMediaItem();
  }
  
  async function loadMediaItem(mediaItem: string = "ROOT_ID"): Promise<void>
  {    
      const { value } = await MediaController.openMediaId({value: mediaItem});
      mediaItems = JSON.parse(value);
  }

  useEffect(() => {
    const handleLoad = () => {
      loadMediaItem();
    };
    window.addEventListener('load', handleLoad);
    return () => {
      window.removeEventListener('load', handleLoad);
    };
  }, []);

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Tab 354345</IonTitle>
      
        </IonToolbar>
      </IonHeader>
      <IonContent fullscreen>
        <IonHeader collapse="condense">
          <IonToolbar>
            <IonTitle size="large">Tab 1</IonTitle>
          </IonToolbar>
        </IonHeader>

        <IonContent>
          <IonFab vertical="bottom" horizontal="center" slot="fixed">
            <IonFabButton onClick={() => takePhoto()}>
              <IonIcon icon={camera}></IonIcon>
            </IonFabButton>
          </IonFab>
        </IonContent>
      </IonContent>
    </IonPage>
  );
};

export default Tab1;
