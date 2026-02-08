// Polyfill for 'global' object (required by sockjs-client)
(window as any).global = window;

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppModule } from './app/app.module';

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch((err: any) => console.error(err));
