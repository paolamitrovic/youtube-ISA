import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpInterceptor,
  HttpEvent
} from '@angular/common/http';
import { AuthService } from '../services/auth.service';
import { Observable } from 'rxjs';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  constructor(public auth: AuthService) { }
  
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    
    console.log('🔍 Request URL:', request.url);
    // Lista javnih ruta koje NE trebaju token
    const publicRoutes = ['/auth/login', '/auth/signup', '/auth/activate'];
    
    // Proveri da li je trenutni request za neku od javnih ruta
    const isPublicRoute = publicRoutes.some(route => request.url.includes(route));
    
    // Ako je javna ruta, ne dodavaj token
    if (isPublicRoute) {
      console.log('✅ Skipping token for public route');
      return next.handle(request);
    }
    
    // Za sve ostale rute, dodaj token ako postoji
    if (this.auth.tokenIsPresent()) {
      console.log('🔐 Adding token to request'); 
      const token = this.auth.getToken();
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    return next.handle(request);
  }
}