import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WatchParty, PlayVideoMessage } from '../../models/watch-party.model';
import { Video } from '../../models/video.model';
import { WatchPartyService } from '../../services/watch-party.service';
import { VideoService } from '../../services/video.service';
import { ConfigService } from '../../services/config.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-watch-party-room',
  templateUrl: './watch-party-room.component.html',
  styleUrls: ['./watch-party-room.component.css'],
  standalone: false
})
export class WatchPartyRoomComponent implements OnInit, OnDestroy {

  watchParty?: WatchParty;
  currentVideo?: Video;
  loading: boolean = true;
  videos: Video[] = [];
  videosLoading: boolean = true;
  isCreator: boolean = false;
  currentUserId?: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private watchPartyService: WatchPartyService,
    private videoService: VideoService,
    private config: ConfigService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadWatchParty(+id);
        this.loadVideos();
        this.setupWebSocket(+id);
      } else {
        this.router.navigate(['/watch-party']);
      }
    });
  }

  ngOnDestroy(): void {
    this.watchPartyService.disconnect();
  }

  loadWatchParty(id: number): void {
    this.loading = true;
    this.watchPartyService.getWatchPartyById(id).subscribe({
      next: (data) => {
        this.watchParty = data;
        if (data.video) {
          this.currentVideo = data.video;
        }
        this.checkIfCreator();
        // Automatically join the watch party if not already a member
        this.autoJoinWatchParty(id);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ WatchPartyRoomComponent: Error loading watch party', err);
        alert('Greška pri učitavanju Watch Party sobe.');
        this.router.navigate(['/watch-party']);
      }
    });
  }

  autoJoinWatchParty(id: number): void {
    // Try to join - if already a member, it will just return the watch party
    this.watchPartyService.joinWatchParty(id).subscribe({
      next: (updatedWatchParty) => {
        this.watchParty = updatedWatchParty;
        this.checkIfCreator();
        this.cdr.detectChanges();
      },
      error: (err) => {
        // If error, user might already be a member or there's an issue
        console.log('Note: Could not auto-join watch party (might already be a member)', err);
      }
    });
  }

  loadVideos(): void {
    this.videosLoading = true;
    this.videoService.getAllVideos().subscribe({
      next: (data) => {
        this.videos = data || [];
        this.videosLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ WatchPartyRoomComponent: Error loading videos', err);
        this.videos = [];
        this.videosLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  setupWebSocket(watchPartyId: number): void {
    this.watchPartyService.connect(() => {
      this.watchPartyService.subscribeToWatchParty(watchPartyId, (message: PlayVideoMessage) => {
        // When video is played, navigate to video page
        if (message.videoId) {
          this.router.navigate(['/video', message.videoId]);
        }
      });
    });
  }

  checkIfCreator(): void {
    // Check if current user is the creator (first member)
    // For now, we'll show video selector to all members
    // Backend will enforce that only creator can play videos
    if (this.watchParty && this.watchParty.members && this.watchParty.members.length > 0) {
      // Show video selector to all members - backend will handle authorization
      this.isCreator = true;
    }
  }

  playVideo(videoId: number): void {
    if (!this.watchParty) return;

    this.watchPartyService.playVideo(this.watchParty.id, videoId).subscribe({
      next: () => {
        // Video play message sent successfully
        // The WebSocket will notify all members
        this.router.navigate(['/video', videoId]);
      },
      error: (err) => {
        console.error('❌ WatchPartyRoomComponent: Error playing video', err);
        alert('Samo osoba koja je napravila sobu može da menja video koji se gleda!');
      }
    });
  }

  joinWatchParty(): void {
    if (!this.watchParty) return;

    this.watchPartyService.joinWatchParty(this.watchParty.id).subscribe({
      next: (updatedWatchParty) => {
        this.watchParty = updatedWatchParty;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ WatchPartyRoomComponent: Error joining watch party', err);
        alert('Greška pri pridruživanju sobi.');
      }
    });
  }

  getVideoUrl(videoId: number): string {
    return this.config.getVideoUrl(videoId);
  }

  getThumbnailUrl(videoId: number): string {
    return this.config.getThumbnailUrl(videoId);
  }

  goToVideo(videoId: number): void {
    this.router.navigate(['/video', videoId]);
  }
}
