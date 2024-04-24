import { IonContent, IonHeader, IonIcon, IonPage, IonTitle, IonToolbar, withIonLifeCycle } from '@ionic/react';
import './NowPlayingView.css';
import React, { FC, useEffect, useState } from 'react';
// or
import { Button, Icon, IconButton, Slider } from '@mui/material';
import MediaController from '../android-media-controller';
import { MediaStateInfo } from '../MediaStateInfo';
import { pause, play } from 'ionicons/icons';
import { FastForward, FastRewind, Image, Pause, PlayArrow, SkipNext, SkipPrevious } from '@mui/icons-material';
import PlayPauseButton from '../components/PlayPauseButton';
import { Capacitor } from '@capacitor/core';

class NowPlayingPage extends React.Component {
  mediaSlider: React.RefObject<HTMLInputElement>;
  timeoutTicket: number = 0;
  sliderUserInteracts: boolean = false;

  state = {
    isPlaying: false,
    albumArtUrl: ""
  };
  albumArtImage: React.RefObject<HTMLImageElement>;

  constructor(props: any) {
    super(props);
    this.mediaSlider = React.createRef();
    this.albumArtImage = React.createRef();
  }



  async ionViewWillEnter() {
    await this.HandleMediaState();
    this.timeoutTicket = window.setInterval(async () => {

      await this.HandleMediaState();

    }, 1000);
  }

  private async HandleMediaState() {
    const { value } = await MediaController.playbackState({ value: "" });
    console.log(value);
    var newState: MediaStateInfo = JSON.parse(value);

    if (this.sliderUserInteracts) {
      console.log('media slider hovered');
    } else {
      this.mediaSlider.current!!.value = newState.timelineProgress.toString();
    }
    
    var albumArtUrlNew = Capacitor.convertFileSrc(decodeURI(newState.albumArtUrl));;
    this.albumArtImage.current!!.src = albumArtUrlNew;
    this.setState(prevState => ({ isPlaying: newState.isPlaying, albumArtUrl:albumArtUrlNew }));
  }

  seekToPosition(value: string) {
    MediaController.seek({ value: value });
    this.sliderUserInteracts = false;
  }


  ionViewWillLeave() {
    window.clearInterval(this.timeoutTicket)
  }

  render() {
    console.log("rendering now playing view");
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
              <img ref={this.albumArtImage} src={this.state.albumArtUrl} className="grid-item">

              </img>
            </div>
            <div className="half-containers">
              <div className="slidecontainer">
                <input type='range' id='mediaSlider' min="0" max="100" ref={this.mediaSlider} onPointerDown={(event) => { console.log('mousedown'); this.sliderUserInteracts = true }} onPointerUp={(event) => { console.log('mouseup ' + this.mediaSlider.current!!.value); this.seekToPosition(this.mediaSlider.current!!.value); }} className='slider'></input>
              </div>


              <div className="grid-container-5">
                <div className="grid-item">
                  <IconButton aria-label="skip previous" size='large'>
                    <SkipPrevious  fontSize="large"/>
                  </IconButton>
                </div>
                <div className="grid-item">
                  <IconButton aria-label="rewind" size='large'>
                    <FastRewind  fontSize="large"/>
                  </IconButton>
                </div>
                <div className="grid-item">
                   <PlayPauseButton isPlaying={this.state.isPlaying}></PlayPauseButton> 
                </div>
                <div className="grid-item">
                  <IconButton aria-label="fast forward"  size='large'>
                    <FastForward  fontSize="large"/>
                  </IconButton>
                </div>
                <div className="grid-item">
                  <IconButton aria-label="skip next"  size='large'>
                    <SkipNext  fontSize="large"/>
                  </IconButton>
                </div>
              </div>
            </div>
          </div>



        </IonContent>
      </IonPage>
    );
  }

}


export default withIonLifeCycle(NowPlayingPage);


