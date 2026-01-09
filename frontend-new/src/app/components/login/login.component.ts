import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  standalone: false
})
export class LoginComponent {
  form: FormGroup;
  submitted = false;
  errorMessage: string | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  onSubmit() {
    this.submitted = true;
    this.errorMessage = null;

    if (this.form.valid) {
      this.authService.login(this.form.value)
        .subscribe({
          next: (data) => {
            console.log('Login successful', data);
            this.router.navigate(['/']); // Preusmeri na home
          },
          error: (error) => {
            console.error('Login failed', error);
            this.submitted = false;
            // Prilagodi prema tvom backend odgovoru
            this.errorMessage = error.error?.message || 'Incorrect email or password.';
          }
        });
    }
  }

  get email() {
    return this.form.get('email');
  }

  get password() {
    return this.form.get('password');
  }
}