import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import IonicModule from '@ionic/react'
import { FluentProvider, webLightTheme } from '@fluentui/react-components';
const container = document.getElementById('root');
const root = createRoot(container!);
root.render(
  
  <React.StrictMode>
    <App />
  </React.StrictMode>
);