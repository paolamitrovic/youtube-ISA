import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfigService } from '../../services/config.service';

@Component({
  selector: 'app-activate.component',
  templateUrl: './activate.component.html',
  styleUrl: './activate.component.css',
  standalone: false
})
export class ActivateComponent implements OnInit {
  loading = true;
  success = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private config: ConfigService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParams['token'];
    
    console.log('🔍 Activation token:', token);
    
    if (!token) {
      this.error = 'Aktivacioni link je nevalidan';
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    const url = `${this.config.activate_url}?token=${token}`;
    console.log('🔍 Activation URL:', url);

    this.http.get(url, { observe: 'response', responseType: 'text' })
      .subscribe({
        next: (response: any) => {
          console.log('✅ Activation success:', response);
          this.success = true;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('❌ Activation error:', err);
          
          let errorMsg = 'Aktivacija naloga je neuspešna';
          
          if (err.status === 400) {
            if (err.error && typeof err.error === 'string') {
              if (err.error.includes('Invalid activation token')) {
                errorMsg = 'Token je već iskorišćen ili ne postoji';
              } else if (err.error.includes('expired')) {
                errorMsg = 'Aktivacioni link je istekao (validnost 24h)';
              } else {
                errorMsg = err.error;
              }
            }
          }
          
          this.error = errorMsg;
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  goToSignup() {
    this.router.navigate(['/signup']);
  }

}