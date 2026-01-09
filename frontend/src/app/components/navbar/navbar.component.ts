import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
  standalone: false
})
export class NavbarComponent {

  constructor(
    private authService: AuthService,
    public router: Router
  ) {}

  isLoggedIn(): boolean {
    return this.authService.tokenIsPresent();
  }

  logout() {
    this.authService.logout();
  }
}
