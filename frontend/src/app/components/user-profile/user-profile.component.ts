import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
  standalone: false
})
export class UserProfileComponent implements OnInit {

  user?: User;         
  username!: string;
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.username = params.get('username')!;
      
      if (this.username) {
        this.loadUser(this.username);
      } else {
        console.error('❌ UserProfileComponent: No username parameter found');
      }
    });
  }

  loadUser(username: string) {
    this.loading = true;
    this.cdr.detectChanges();
    this.userService.getUserByUsername(username).subscribe({
      next: (data) => {
        this.user = data;
        this.loading = false;
        this.cdr.detectChanges();
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