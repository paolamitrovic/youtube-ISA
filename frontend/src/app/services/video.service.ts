import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
    private config: ConfigService,
    private http: HttpClient
  ) {}

  getAllVideos(): Observable<Video[]> {
    return this.apiService.get(this.config.videos_url);
  }

  getVideoById(id: number): Observable<Video> {
    return this.apiService.get(this.config.getVideoByIdUrl(id));
  }

  createVideo(formData: FormData): Observable<Video> {
    // For multipart/form-data, Angular will automatically set Content-Type with boundary
    // The TokenInterceptor will automatically add Authorization header
    // Just use http.post directly - don't set any headers manually
    return this.http.post<Video>(this.config.videos_url, formData);
  }
}
