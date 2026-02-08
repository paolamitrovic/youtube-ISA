import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Video } from '../../models/video.model';
import { WatchParty } from '../../models/watch-party.model';
import { VideoService } from '../../services/video.service';
import { WatchPartyService } from '../../services/watch-party.service';

@Component({
  selector: 'app-watch-party-create',
  templateUrl: './watch-party-create.component.html',
  styleUrls: ['./watch-party-create.component.css'],
  standalone: false
})
export class WatchPartyCreateComponent implements OnInit {

  watchPartyForm: FormGroup;
  videos: Video[] = [];
  loading: boolean = false;
  videosLoading: boolean = true;

  constructor(
    private fb: FormBuilder,
    private watchPartyService: WatchPartyService,
    private videoService: VideoService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.watchPartyForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      videoId: [null]
    });
  }

  ngOnInit(): void {
    this.loadVideos();
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
        console.error('❌ WatchPartyCreateComponent: Error loading videos', err);
        this.videos = [];
        this.videosLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onSubmit(): void {
    if (this.watchPartyForm.valid && !this.loading) {
      this.loading = true;
      const formValue = this.watchPartyForm.value;
      
      const request = {
        name: formValue.name,
        videoId: formValue.videoId || undefined
      };

      this.watchPartyService.createWatchParty(request).subscribe({
        next: (watchParty: WatchParty) => {
          this.loading = false;
          if (watchParty && watchParty.id) {
            this.router.navigate(['/watch-party', watchParty.id]);
          } else {
            console.error('❌ WatchPartyCreateComponent: Watch party ID is missing', watchParty);
            alert('Greška: Watch Party soba je kreirana ali ID nije dostupan.');
            this.router.navigate(['/watch-party']);
          }
        },
        error: (err) => {
          console.error('❌ WatchPartyCreateComponent: Error creating watch party', err);
          alert('Greška pri kreiranju Watch Party sobe. Pokušajte ponovo.');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/watch-party']);
  }
}
