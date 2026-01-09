import { Component, OnInit } from '@angular/core';
import { Video } from '../../models/video.model';
import { VideoService } from '../../services/video.service';

@Component({
  selector: 'app-video-list',
  templateUrl: './video-list.component.html',
  styleUrls: ['./video-list.component.css']
})
export class VideoListComponent implements OnInit {

  videos: Video[] = [];

  constructor(
    private videoService: VideoService
  ) {}

  ngOnInit(): void {
    this.loadVideos();
  }

  loadVideos(): void {
    this.videoService.getAllVideos().subscribe({
      next: (data) => {
        this.videos = data;
      },
      error: (err) => {
        console.error('Error in loading videos', err);
      }
    });
  }

  goToUser(username: string) {
    window.location.href = `/user/${username}`;
  }

  goToVideo(id: number) {
    window.location.href = `/video/${id}`;
  }
}