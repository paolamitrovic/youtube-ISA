import { Component, OnInit, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { Video } from '../../models/video.model';
import { VideoService } from '../../services/video.service';

@Component({
  selector: 'app-video-list',
  templateUrl: './video-list.component.html',
  styleUrls: ['./video-list.component.css'],
  standalone: false
})
export class VideoListComponent implements OnInit, AfterViewInit {

  videos: Video[] = [];

  constructor(
    private videoService: VideoService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('🔍 VideoListComponent: ngOnInit() called');
    console.log('🔍 VideoListComponent: Initial videos.length:', this.videos.length);
    this.loadVideos();
  }
  
  ngAfterViewInit(): void {
    console.log('🔍 VideoListComponent: ngAfterViewInit() called');
    console.log('🔍 VideoListComponent: videos.length in ngAfterViewInit:', this.videos.length);
  }

  loadVideos(): void {
    console.log('🔍 VideoListComponent: loadVideos() called');
    this.videoService.getAllVideos().subscribe({
      next: (data) => {
        console.log('✅ VideoListComponent: Videos received:', data?.length || 0, data);
        this.videos = data || [];
        console.log('✅ VideoListComponent: videos array updated, length:', this.videos.length);
        // Force change detection
        this.cdr.detectChanges();
        console.log('✅ VideoListComponent: Change detection triggered');
      },
      error: (err) => {
        console.error('❌ VideoListComponent: Error loading videos', err);
        this.videos = [];
        this.cdr.detectChanges();
      }
    });
  }

  goToUser(username: string) {
    console.log('🔍 VideoListComponent: Navigating to user:', username);
    this.router.navigate(['/user', username]);
  }

  goToVideo(id: number) {
    console.log('🔍 VideoListComponent: Navigating to video:', id);
    this.router.navigate(['/video', id]);
  }

  trackByVideoId(index: number, video: Video): number {
    return video.id;
  }
}