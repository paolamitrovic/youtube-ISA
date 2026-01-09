import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent {

  user?: User;         // korisnik će biti učitan
  username!: string;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // uzimamo username iz URL-a
    this.username = this.route.snapshot.paramMap.get('username')!;

    // učitavamo informacije o korisniku
    this.userService.getUserByUsername(this.username).subscribe({
      next: (data) => {
        this.user = data;
      },
      error: (err) => {
        console.error('User not found', err);
        this.user = undefined;
      }
    });
  }

}