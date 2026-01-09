import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private baseUrl = 'http://localhost:8080/users';

  constructor(private http: HttpClient) {}

  getUserByUsername(username: string): Observable<User> {
    const url = `${this.baseUrl}/${username}`;
    console.log('🔍 UserService: Making request to', url);
    return this.http.get<User>(url).pipe(
      tap({
        next: (user) => console.log('✅ UserService: User loaded:', user),
        error: (err) => console.error('❌ UserService: Error loading user', err)
      })
    );
  }
}
