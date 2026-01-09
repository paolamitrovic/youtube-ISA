import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Video } from '../../models/video.model';
import { ActivatedRoute, Router } from '@angular/router';
import { VideoService } from '../../services/video.service';
import { Comment } from '../../models/comment.model';
import { CommentService } from '../../services/comment.service';
import { AuthService } from '../../services/auth.service';


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
  commentTextValue: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private videoService: VideoService,
    private commentService: CommentService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('🔍 VideoDetailComponent: ngOnInit() called');
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      console.log('🔍 VideoDetailComponent: Route param id:', id);
      if (id) {
        this.loadVideo(+id);
      } else {
        console.error('❌ VideoDetailComponent: No id parameter found');
        this.loading = false;
      }
    });
  }

  loadVideo(id: number) {
    console.log('🔍 VideoDetailComponent: loadVideo() called with id:', id);
    this.loading = true;
    this.cdr.detectChanges();
    this.videoService.getVideoById(id).subscribe({
      next: (data) => {
        console.log('✅ VideoDetailComponent: Video loaded:', data);
        this.video = data;
        this.loading = false;
        this.cdr.detectChanges();
        console.log('✅ VideoDetailComponent: loading set to false, change detection triggered');
        this.loadComments(id);
      },
      error: (err) => {
        console.error('❌ VideoDetailComponent: Error loading video', err);
        this.loading = false;
        this.video = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  loadComments(videoId: number) {
    console.log('🔍 VideoDetailComponent: loadComments() called with videoId:', videoId);
    this.commentsLoading = true;
    this.cdr.detectChanges();

    this.commentService.getCommentsByVideoId(videoId).subscribe({
      next: (data) => {
        console.log('✅ VideoDetailComponent: Comments loaded:', data?.length || 0, data);
        this.comments = data || [];
        this.commentsLoading = false;
        this.cdr.detectChanges();
        console.log('✅ VideoDetailComponent: commentsLoading set to false, change detection triggered');
      },
      error: (err) => {
        console.error('❌ VideoDetailComponent: Error loading comments', err);
        this.commentsLoading = false;
        this.comments = [];
        this.cdr.detectChanges();
      }
    });
  }

  goToUser(username: string) {
    console.log('🔍 VideoDetailComponent: Navigating to user:', username);
    this.router.navigate(['/user', username]);
  }

  trackByCommentDate(index: number, comment: Comment): any {
    return comment.createdAt;
  }

  onLikeClick() {
    console.log('🔍 VideoDetailComponent: Like clicked');
    
    if (!this.authService.tokenIsPresent()) {
      console.log('❌ User not logged in, redirecting to login');
      this.router.navigate(['/login']);
      return;
    }

    // TODO: Implementiraj like funkcionalnost kada budeš spremna
    console.log('✅ User is logged in, like functionality will be implemented');
    // this.videoService.likeVideo(this.video!.id).subscribe(...)
  }

  onSubmitComment() {
    console.log('🔍 VideoDetailComponent: Submit comment clicked - START');
    console.log('🔍 commentTextValue:', this.commentTextValue);
    
    if (!this.authService.tokenIsPresent()) {
      console.log('❌ User not logged in, redirecting to login');
      this.router.navigate(['/login']);
      return;
    }

    if (!this.commentTextValue || !this.commentTextValue.trim()) {
      console.log('❌ Comment text is empty');
      alert('Molimo unesite tekst komentara');
      return;
    }

    // TODO: Implementiraj postavljanje komentara kada budeš spremna
    console.log('✅ User is logged in, submitting comment:', this.commentTextValue);
    // this.commentService.postComment(this.video!.id, this.commentTextValue.trim()).subscribe({
    //   next: () => {
    //     this.commentTextValue = '';
    //     this.loadComments(this.video!.id);
    //   }
    // });
  }
}
