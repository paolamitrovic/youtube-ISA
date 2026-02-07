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
    console.log('🔍 Request method:', request.method);
    console.log('🔍 Request body type:', request.body instanceof FormData ? 'FormData' : typeof request.body);
    
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
      
      // For FormData requests, we need to be careful not to set Content-Type
      // Let the browser set it automatically with boundary
      if (request.body instanceof FormData) {
        console.log('📎 FormData detected - preserving automatic Content-Type');
        request = request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
          // Don't set Content-Type - browser will set it with boundary
        });
      } else {
        request = request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        });
      }
    } else {
      console.warn('⚠️ No token found for authenticated route:', request.url);
    }
    
    return next.handle(request);
  }
}