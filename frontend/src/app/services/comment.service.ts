import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Comment, CommentPageResponse, CreateCommentRequest } from '../models/comment.model';
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

  getCommentsByVideoIdPaginated(videoId: number, page: number = 0, size: number = 10): Observable<CommentPageResponse> {
    return this.apiService.get(this.config.getCommentsByVideoIdPaginatedUrl(videoId, page, size));
  }

  createComment(text: string, videoId: number): Observable<Comment> {
    const request: CreateCommentRequest = {
      text: text,
      videoId: videoId
    };
    return this.apiService.post(this.config.getCreateCommentUrl(), request);
  }
}
