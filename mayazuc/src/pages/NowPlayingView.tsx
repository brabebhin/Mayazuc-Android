import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, withIonLifeCycle } from '@ionic/react';
import './NowPlayingView.css';
import React, { FC, useEffect, useState } from 'react';
// or
import { Slider } from '@mui/material';
import MediaController from '../android-media-controller';
import { MediaStateInfo } from '../MediaStateInfo';

class NowPlayingPage extends React.Component
{
  mediaSlider: React.RefObject<HTMLInputElement>;
  timeoutTicket: number = 0;  

  constructor(props: any) {
    super(props);
    this.mediaSlider = React.createRef();
  }

  ionViewWillEnter()
  {
    this.timeoutTicket = window.setInterval(async ()=>{
      console.log('timer tick');

      const { value } = await MediaController.openMediaId({ value: "" });
      var newState: MediaStateInfo = JSON.parse(value);


      this.mediaSlider.current!!.value = newState.timelineProgress.toString();

    }, 100);
  }

  ionViewWillLeave()
  {
    window.clearInterval(this.timeoutTicket)
  }

    render()
    {
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
                <div className="slidecontainer">
                  <input type='range' id='mediaSlider' min="0" max="100" value="0"  ref={this.mediaSlider} className='slider grid-item-5'></input>
                </div>
                <div className="grid-container-5">    
                  <div className="grid-item">1</div>
                  <div className="grid-item">2</div>
                  <div className="grid-item">3</div>
                  <div className="grid-item">4</div>
                  <div className="grid-item">5</div>
                </div>

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
    }
}


export default withIonLifeCycle(NowPlayingPage);
