import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  
  private _auth_url = 'http://localhost:8080/auth';  // <-- Promeni port ako treba
  private _base_url = 'http://localhost:8080';

  private _login_url = this._auth_url + '/login';
  get login_url(): string {
    return this._login_url;
  }

  private _signup_url = this._auth_url + '/signup';
  get signup_url(): string {
    return this._signup_url;
  }

  private _activate_url = this._auth_url + '/activate';
  get activate_url(): string {
    return this._activate_url;
  }

  private _videos_url = this._base_url + '/videos';
  get videos_url(): string {
    return this._videos_url;
  }

  getVideoByIdUrl(id: number): string {
    return `${this._videos_url}/${id}`;
  }

  // User endpoints
  private _users_url = this._base_url + '/users';
  get users_url(): string {
    return this._users_url;
  }

  getUserByUsernameUrl(username: string): string {
    return `${this._users_url}/${username}`;
  }

  // Comment endpoints
  private _comments_url = this._base_url + '/comments';
  get comments_url(): string {
    return this._comments_url;
  }

  getCommentsByVideoIdUrl(videoId: number): string {
    return `${this._comments_url}/video/${videoId}`;
  }
}