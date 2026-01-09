import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
  standalone: false
})
export class UserProfileComponent {

  user?: User;         // korisnik će biti učitan
  username!: string;
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('🔍 UserProfileComponent: ngOnInit() called');
    
    // Koristi paramMap.subscribe umesto snapshot za bolju navigaciju
    this.route.paramMap.subscribe(params => {
      this.username = params.get('username')!;
      console.log('🔍 UserProfileComponent: Route param username:', this.username);
      
      if (this.username) {
        this.loadUser(this.username);
      } else {
        console.error('❌ UserProfileComponent: No username parameter found');
      }
    });
  }

  loadUser(username: string) {
    console.log('🔍 UserProfileComponent: loadUser() called with username:', username);
    this.loading = true;
    this.cdr.detectChanges();
    this.userService.getUserByUsername(username).subscribe({
      next: (data) => {
        console.log('✅ UserProfileComponent: User loaded:', data);
        this.user = data;
        this.loading = false;
        this.cdr.detectChanges();
        console.log('✅ UserProfileComponent: loading set to false, change detection triggered');
      },
      error: (err) => {
        console.error('❌ UserProfileComponent: Error loading user', err);
        this.user = undefined;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

}