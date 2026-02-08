import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { WatchParty } from '../../models/watch-party.model';
import { WatchPartyService } from '../../services/watch-party.service';

@Component({
  selector: 'app-watch-party-list',
  templateUrl: './watch-party-list.component.html',
  styleUrls: ['./watch-party-list.component.css'],
  standalone: false
})
export class WatchPartyListComponent implements OnInit {

  watchParties: WatchParty[] = [];
  loading: boolean = true;

  constructor(
    private watchPartyService: WatchPartyService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadWatchParties();
  }

  loadWatchParties(): void {
    this.loading = true;
    this.watchPartyService.getAllWatchParties().subscribe({
      next: (data) => {
        this.watchParties = data || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ WatchPartyListComponent: Error loading watch parties', err);
        this.watchParties = [];
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  createWatchParty(): void {
    this.router.navigate(['/watch-party/create']);
  }

  joinWatchParty(id: number): void {
    this.router.navigate(['/watch-party', id]);
  }

  trackByWatchPartyId(index: number, watchParty: WatchParty): number {
    return watchParty.id;
  }
}
