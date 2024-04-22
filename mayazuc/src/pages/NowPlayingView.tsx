import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/react';
import './NowPlayingView.css';

const NowPlayingPage: React.FC = () => {
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Now Playing</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent fullscreen>
        <IonHeader collapse="condense">
          <IonToolbar>
            <IonTitle size="large">Tab 2</IonTitle>
          </IonToolbar>
        </IonHeader>

        <div className="main-container">
          <div className="half-containers">
            <div className="grid-item">1</div>
          </div>
          <div className="half-containers">

            <div className="grid-container-5">
              <div className="grid-item">1</div>
              <div className="grid-item">2</div>
              <div className="grid-item">3</div>
              <div className="grid-item">4</div>
              <div className="grid-item">5</div>
            </div>
          </div>
        </div>



      </IonContent>
    </IonPage>
  );
};

export default NowPlayingPage;
