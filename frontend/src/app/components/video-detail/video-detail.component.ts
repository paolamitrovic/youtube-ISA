import { Component, OnInit } from '@angular/core';
import { Video } from '../../models/video.model';
import { ActivatedRoute } from '@angular/router';
import { VideoService } from '../../services/video.service';
import { Comment } from '../../models/comment.model';
import { CommentService } from '../../services/comment.service';


@Component({
  selector: 'app-video-detail',
  templateUrl: './video-detail.component.html',
  styleUrl: './video-detail.component.css',
  standalone: false
})
export class VideoDetailComponent implements OnInit {

  video?: Video;
  loading: boolean = true;
  comments: Comment[] = [];
  commentsLoading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private videoService: VideoService,
    private commentService: CommentService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadVideo(+id);
      }
    });
  }

  loadVideo(id: number) {
    this.loading = true;
    this.videoService.getVideoById(id).subscribe({
      next: (data) => {
        this.video = data;
        this.loading = false;
        this.loadComments(id);
      },
      error: (err) => {
        console.error('Video not found', err);
        this.loading = false;
      }
    });
  }

  loadComments(videoId: number) {
    this.commentsLoading = true;

    this.commentService.getCommentsByVideoId(videoId).subscribe({
      next: (data) => {
        this.comments = data;
        this.commentsLoading = false;
      },
      error: (err) => {
        console.error('Comments not found', err);
        this.commentsLoading = false;
      }
    });
  }

  goToUser(username: string) {
    window.location.href = `/user/${username}`;
  }
}
