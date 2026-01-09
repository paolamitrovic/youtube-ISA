import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';
import { ConfigService } from './config.service';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiService = inject(ApiService);
  private config = inject(ConfigService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  private access_token: string | null = null;

  login(user: { email: string; password: string }) {
    const loginHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
    
    const body = {
      'email': user.email,  // <-- EMAIL umesto username
      'password': user.password
    };
    
    return this.apiService.post(this.config.login_url, JSON.stringify(body), loginHeaders)
      .pipe(map((res: any) => {
        console.log('Login success');
        // Prilagodi prema tvom backend odgovoru
        // Možda je res.body.accessToken ili res.accessToken ili res.token
        //this.access_token = res.body?.accessToken || res.accessToken || res.token;
        const token = res.body?.accessToken || res.accessToken;
        if (token) {
          this.access_token = token;
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem("jwt", token);
          }
        } else {
          console.error('Token not found in response:', res);
        }
        
        return res;
      }));
  }

  signup(user: any) {
    const signupHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
    return this.apiService.post(this.config.signup_url, JSON.stringify(user), signupHeaders)
      .pipe(map(() => {
        console.log('Sign up success');
      }));
  }

  // TODO: Dodaj activateAccount metodu kada budeš spremna za aktivaciju
  // activateAccount(token: string) {
  //   const url = `${this.config.activate_url}?token=${token}`;
  //   return this.apiService.get(url);
  // }

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem("jwt");
      localStorage.removeItem("token");
    }
    this.access_token = null;
    this.router.navigate(['/login']);
  }

  tokenIsPresent(): boolean {
    console.log('🔍 tokenIsPresent() called');
    console.log('🔍 access_token in memory:', this.access_token ? this.access_token.substring(0, 20) + '...' : 'null');
    console.log('🔍 isPlatformBrowser:', isPlatformBrowser(this.platformId));
    
    // Prvo proveri memoriju
    if (this.access_token) {
      console.log('✅ Token found in memory');
      return true;
    }
    
    // Ako nema u memoriji, učitaj iz localStorage (samo u browseru)
    if (isPlatformBrowser(this.platformId)) {
      const storedToken = localStorage.getItem("jwt");
      console.log('🔍 Token from localStorage:', storedToken ? storedToken.substring(0, 20) + '...' : 'null');
      
      if (storedToken) {
        this.access_token = storedToken;
        console.log('✅ Token loaded from localStorage to memory');
        return true;
      } else {
        console.log('❌ No token in localStorage');
      }
    } else {
      console.log('⚠️ Not in browser, cannot access localStorage');
    }
    
    console.log('❌ tokenIsPresent() returning false');
    return false;
  }

  getToken(): string | null {
    console.log('🔍 getToken() called');
    
    // Prvo proveri memoriju
    if (this.access_token) {
      console.log('✅ Token found in memory');
      return this.access_token;
    }
    
    // Ako nema u memoriji, učitaj iz localStorage (samo u browseru)
    if (isPlatformBrowser(this.platformId)) {
      const storedToken = localStorage.getItem("jwt");
      console.log('🔍 Token from localStorage:', storedToken ? storedToken.substring(0, 20) + '...' : 'null');
      
      if (storedToken) {
        this.access_token = storedToken;
        console.log('✅ Token loaded from localStorage to memory');
        return storedToken;
      }
    }
    
    console.log('❌ getToken() returning null');
    return null;
  }
}