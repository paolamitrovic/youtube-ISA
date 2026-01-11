import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { Video } from '../../models/video.model';
import { VideoService } from '../../services/video.service';

@Component({
  selector: 'app-video-list',
  templateUrl: './video-list.component.html',
  styleUrls: ['./video-list.component.css'],
  standalone: false
})
export class VideoListComponent implements OnInit {

  videos: Video[] = [];

  constructor(
    private videoService: VideoService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadVideos();
  }

  loadVideos(): void {
    this.videoService.getAllVideos().subscribe({
      next: (data) => {
        this.videos = data || [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ VideoListComponent: Error loading videos', err);
        this.videos = [];
        this.cdr.detectChanges();
      }
    });
  }

  goToUser(username: string) {
    this.router.navigate(['/user', username]);
  }

  goToVideo(id: number) {
    this.router.navigate(['/video', id]);
  }

  trackByVideoId(index: number, video: Video): number {
    return video.id;
  }
}