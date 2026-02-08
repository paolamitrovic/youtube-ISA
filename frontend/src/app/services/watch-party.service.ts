import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { WatchParty, CreateWatchPartyRequest, PlayVideoMessage } from '../models/watch-party.model';
import { ConfigService } from './config.service';
import { ApiService } from './api.service';

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class WatchPartyService {
  private stompClient: Client | null = null;
  private isConnected: boolean = false;

  constructor(
    private apiService: ApiService,
    private config: ConfigService,
    private http: HttpClient
  ) {}

  getAllWatchParties(): Observable<WatchParty[]> {
    return this.apiService.get(this.config.watch_parties_url)
      .pipe(map((res: any) => {
        // ApiService returns HttpResponse, extract body
        return res.body || res || [];
      }));
  }

  getWatchPartyById(id: number): Observable<WatchParty> {
    return this.apiService.get(this.config.getWatchPartyByIdUrl(id))
      .pipe(map((res: any) => {
        // ApiService returns HttpResponse, extract body
        return res.body || res;
      }));
  }

  createWatchParty(request: CreateWatchPartyRequest): Observable<WatchParty> {
    return this.apiService.post(this.config.watch_parties_url, JSON.stringify(request))
      .pipe(map((res: any) => {
        // ApiService returns HttpResponse, extract body
        return res.body || res;
      }));
  }

  joinWatchParty(id: number): Observable<WatchParty> {
    return this.http.post<WatchParty>(this.config.getJoinWatchPartyUrl(id), {});
  }

  playVideo(id: number, videoId: number): Observable<void> {
    const message: PlayVideoMessage = {
      watchPartyId: id,
      videoId: videoId
    };
    return this.http.post<void>(this.config.getPlayVideoUrl(id), message);
  }

  // WebSocket methods
  connect(callback?: () => void): void {
    if (this.isConnected && this.stompClient) {
      if (callback) callback();
      return;
    }

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(this.config.getSocketUrl()) as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.isConnected = true;
        if (callback) callback();
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this.isConnected = false;
      },
      onWebSocketClose: () => {
        this.isConnected = false;
      }
    });

    this.stompClient.activate();
  }

  disconnect(): void {
    if (this.stompClient && this.isConnected) {
      this.stompClient.deactivate();
      this.isConnected = false;
      this.stompClient = null;
    }
  }

  subscribeToWatchParty(watchPartyId: number, callback: (message: PlayVideoMessage) => void): void {
    if (!this.isConnected || !this.stompClient) {
      this.connect(() => {
        this.subscribeToWatchParty(watchPartyId, callback);
      });
      return;
    }

    this.stompClient.subscribe(`/socket-publisher/watch-party/${watchPartyId}`, (message) => {
      if (message.body) {
        const playMessage: PlayVideoMessage = JSON.parse(message.body);
        callback(playMessage);
      }
    });
  }

  isWebSocketConnected(): boolean {
    return this.isConnected;
  }
}
