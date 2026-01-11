import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Comment } from '../models/comment.model';
import { ApiService } from './api.service';
import { ConfigService } from './config.service';

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  constructor(
    private apiService: ApiService,
    private config: ConfigService
  ) {}

  getCommentsByVideoId(videoId: number): Observable<Comment[]> {
    return this.apiService.get(this.config.getCommentsByVideoIdUrl(videoId));
  }
}
