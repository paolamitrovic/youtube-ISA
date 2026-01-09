import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Comment } from '../models/comment.model';

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  private apiUrl = 'http://localhost:8080/comments';

  constructor(private http: HttpClient) {}

  getCommentsByVideoId(videoId: number): Observable<Comment[]> {
    const url = `${this.apiUrl}/video/${videoId}`;
    console.log('🔍 CommentService: Making request to', url);
    return this.http.get<Comment[]>(url).pipe(
      tap({
        next: (comments) => console.log('✅ CommentService: Comments loaded:', comments?.length || 0, comments),
        error: (err) => console.error('❌ CommentService: Error loading comments', err)
      })
    );
  }
}
