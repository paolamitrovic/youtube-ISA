import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Video } from '../../models/video.model';
import { ActivatedRoute } from '@angular/router';
import { VideoService } from '../../services/video.service';

@Component({
  selector: 'app-video-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './video-detail.component.html',
  styleUrl: './video-detail.component.css',
})
export class VideoDetailComponent implements OnInit {

  video?: Video;
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private videoService: VideoService
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
      },
      error: (err) => {
        console.error('Video not found', err);
        this.loading = false;
      }
    });
  }

  goToUser(username: string) {
    window.location.href = `/user/${username}`;
  }
}
