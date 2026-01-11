import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { ApiService } from './api.service';
import { ConfigService } from './config.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(
    private apiService: ApiService,
    private config: ConfigService
  ) {}

  getUserByUsername(username: string): Observable<User> {
    return this.apiService.get(this.config.getUserByUsernameUrl(username));
  }
}
