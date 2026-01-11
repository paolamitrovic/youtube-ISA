import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Video } from '../models/video.model';
import { ConfigService } from './config.service';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class VideoService {

  constructor(
    private apiService: ApiService,
    private config: ConfigService
  ) {}

  getAllVideos(): Observable<Video[]> {
    return this.apiService.get(this.config.videos_url);
  }

  getVideoById(id: number): Observable<Video> {
    return this.apiService.get(this.config.getVideoByIdUrl(id));
  }
}
