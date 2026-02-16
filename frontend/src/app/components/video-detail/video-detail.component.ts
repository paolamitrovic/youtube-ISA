import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Video } from '../../models/video.model';
import { ActivatedRoute, Router } from '@angular/router';
import { VideoService } from '../../services/video.service';
import { Comment } from '../../models/comment.model';
import { CommentService } from '../../services/comment.service';
import { AuthService } from '../../services/auth.service';
import { ConfigService } from '../../services/config.service';


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
  
  // Pagination
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;
  hasNext: boolean = false;
  hasPrevious: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private videoService: VideoService,
    private commentService: CommentService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private config: ConfigService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadVideo(+id);
      } else {
        console.error('❌ VideoDetailComponent: No id parameter found');
        this.loading = false;
      }
    });
  }

  loadVideo(id: number) {
    this.loading = true;
    this.cdr.detectChanges();
    this.videoService.getVideoById(id).subscribe({
      next: (data) => {
        this.video = data;
        console.log('✅ Video loaded:', data);
        console.log('✅ Video URL:', this.getVideoUrl(data.id));
        this.loading = false;
        this.cdr.detectChanges();
        this.loadComments(id);
        // Inkrement broja pregleda kada korisnik uđe na stranicu
        this.incrementVideoViews(id);
      },
      error: (err) => {
        console.error('❌ VideoDetailComponent: Error loading video', err);
        this.loading = false;
        this.video = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  incrementVideoViews(videoId: number) {
    this.videoService.incrementViews(videoId).subscribe({
      next: (updatedVideo) => {
        // Ažuriraj broj pregleda u prikazanom videu
        if (this.video) {
          this.video.views = updatedVideo.views;
          this.cdr.detectChanges();
        }
        console.log('✅ Views incremented:', updatedVideo.views);
      },
      error: (err) => {
        // Ne prikazuj grešku korisniku - pregledi nisu kritični
        console.warn('⚠️ Failed to increment views:', err);
      }
    });
  }

  loadComments(videoId: number, page: number = 0) {
    this.commentsLoading = true;
    this.currentPage = page;
    this.cdr.detectChanges();

    this.commentService.getCommentsByVideoIdPaginated(videoId, page, this.pageSize).subscribe({
      next: (data) => {
        this.comments = data.content || [];
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.hasNext = data.hasNext;
        this.hasPrevious = data.hasPrevious;
        this.commentsLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ VideoDetailComponent: Error loading comments', err);
        this.commentsLoading = false;
        this.comments = [];
        this.cdr.detectChanges();
      }
    });
  }
  
  loadNextPage() {
    if (this.hasNext && this.video) {
      this.loadComments(this.video.id, this.currentPage + 1);
    }
  }
  
  loadPreviousPage() {
    if (this.hasPrevious && this.video) {
      this.loadComments(this.video.id, this.currentPage - 1);
    }
  }

  goToUser(username: string) {
    this.router.navigate(['/user', username]);
  }

  trackByCommentId(index: number, comment: Comment): any {
    return comment.id || comment.createdAt;
  }

  onLikeClick() {
    if (!this.authService.tokenIsPresent()) {
      alert('Morate biti prijavljeni da biste lajkovali video.');
      this.router.navigate(['/login']);
      return;
  }

    // TODO: Implementiraj like funkcionalnost
    console.log('✅ User is logged in, like functionality will be implemented');
    // this.videoService.likeVideo(this.video!.id).subscribe(...)
  }

  onSubmitComment() {
    if (!this.authService.tokenIsPresent()) {
      alert('Morate biti prijavljeni da biste postavili komentar.');
      this.router.navigate(['/login']);
      return;
    }

    if (!this.commentTextValue || !this.commentTextValue.trim()) {
      alert('Molimo unesite tekst komentara');
      return;
    }

    if (!this.video) {
      alert('Video nije učitan');
      return;
    }

    const commentText = this.commentTextValue.trim();
    this.commentService.createComment(commentText, this.video.id).subscribe({
      next: (newComment) => {
        console.log('✅ Comment created:', newComment);
        this.commentTextValue = '';
        // Reload comments from first page to show the new comment
        this.loadComments(this.video!.id, 0);
      },
      error: (err) => {
        console.error('❌ Error creating comment:', err);
        if (err.status === 429 || err.status === 403) {
          alert('Prekoračili ste limit od 60 komentara po satu. Molimo sačekajte.');
        } else if (err.status === 401) {
          alert('Morate biti prijavljeni da biste postavili komentar.');
          this.router.navigate(['/login']);
        } else {
          alert('Greška pri postavljanju komentara: ' + (err.error?.message || err.message || 'Nepoznata greška'));
        }
      }
    });
  }

  getVideoUrl(videoId: number): string {
    return this.config.getVideoUrl(videoId);
  }
}
