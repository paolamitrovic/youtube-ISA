import { Component, OnInit } from '@angular/core';
import { Video } from '../../models/video.model';
import { VideoService } from '../../services/video.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-video-list',
  templateUrl: './video-list.component.html',
  styleUrls: ['./video-list.component.css'],
  standalone: true,
  imports: [CommonModule]
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
        console.log(this.videos); // 🔍 OBAVEZNO za proveru
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
