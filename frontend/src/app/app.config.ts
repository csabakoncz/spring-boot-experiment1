import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
} from '@angular/common/http';

import { routes } from './app.routes';
import { BASE_PATH } from '../api';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(
      withFetch(),
      withInterceptors([
        (req, next) => {
          const copiedReq = req.clone({
            withCredentials: true,
          });
          return next(copiedReq);
        },
      ])
    ),
    {
      provide: BASE_PATH,
      useValue: 'http://localhost:8080',
    },
  ],
};
