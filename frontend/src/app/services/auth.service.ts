import { Injectable, inject } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';
import { ConfigService } from './config.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiService = inject(ApiService);
  private config = inject(ConfigService);
  private router = inject(Router);

  private access_token: string | null = null;

  login(user: { email: string; password: string }) {
    const loginHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
    
    const body = {
      email: user.email,
      password: user.password
    };
    
    return this.apiService.post(this.config.login_url, JSON.stringify(body), loginHeaders)
      .pipe(map((res: any) => {
        console.log('Login success');
        
        // Backend vraća { accessToken: "...", expiresIn: 1800000 }
        const token = res.body?.accessToken || res.accessToken;
        
        if (token) {
          this.access_token = token;
          localStorage.setItem("jwt", token);
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
      .pipe(map((res: any) => {
        console.log('Sign up success');
        return res;
      }));
  }

  activate(token: string) {
    const url = `${this.config.activate_url}?token=${token}`;
    return this.apiService.get(url);
  }


  logout() {
    localStorage.removeItem("jwt");
    localStorage.removeItem("token");
    this.access_token = null;
    this.router.navigate(['/']);
  }

  tokenIsPresent(): boolean {
    // Prvo proveri memoriju
    if (this.access_token) {
      return true;
    }
    
    // Ako nema u memoriji, učitaj iz localStorage
    const storedToken = localStorage.getItem("jwt");
    if (storedToken) {
      this.access_token = storedToken;
      return true;
    }
    
    return false;
  }

  getToken(): string | null {
    // Prvo proveri memoriju
    if (this.access_token) {
      return this.access_token;
    }
    
    // Ako nema u memoriji, učitaj iz localStorage
    const storedToken = localStorage.getItem("jwt");
    if (storedToken) {
      this.access_token = storedToken;
      return storedToken;
    }
    
    return null;
  }
}