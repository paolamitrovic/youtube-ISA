import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Video } from '../models/video.model';

@Injectable({
  providedIn: 'root'
})
export class VideoService {

  private apiUrl = 'http://localhost:8080/videos';

  constructor(private http: HttpClient) {}

  getAllVideos(): Observable<Video[]> {
    console.log('🔍 VideoService: Making request to', this.apiUrl);
    return this.http.get<Video[]>(this.apiUrl).pipe(
      tap({
        next: (videos) => console.log('✅ VideoService: Videos loaded:', videos?.length || 0, videos),
        error: (err) => console.error('❌ VideoService: Error loading videos', err)
      })
    );
  }

  getVideoById(id: number): Observable<Video> {
    const url = `${this.apiUrl}/${id}`;
    console.log('🔍 VideoService: Making request to', url);
    return this.http.get<Video>(url).pipe(
      tap({
        next: (video) => console.log('✅ VideoService: Video loaded:', video),
        error: (err) => console.error('❌ VideoService: Error loading video', err)
      })
    );
  }
}
