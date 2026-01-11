import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: false
})
export class LoginComponent {
  form: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  // ista logika kao signup
  isFieldInvalid(fieldName: string): boolean {
    const field = this.form.get(fieldName);
    return !!(field && field.invalid && (field.touched || this.loading));
  }

  getErrorMessage(fieldName: string): string {
    const field = this.form.get(fieldName);
    if (!field || !field.errors) return '';

    if (field.errors['required']) return 'Ovo polje je obavezno';
    if (field.errors['email']) return 'Unesite validnu e-mail adresu';
    if (field.errors['minlength']) return `Minimalno ${field.errors['minlength'].requiredLength} karaktera`;

    return '';
  }

  onSubmit() {
    if (this.form.invalid) {
      Object.keys(this.form.controls).forEach(key => {
        this.form.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;

    this.authService.login(this.form.value).subscribe({
      next: (res) => {
        this.loading = false;
        this.router.navigate(['/']); // preusmeri na home
      },
      error: (err) => {
        this.loading = false;

        if (err.status === 401) {
            // pogrešna lozinka ili korisnik ne postoji
            alert('Pogrešna e-mail adresa ili lozinka.');
        } else if (err.status === 429) {
            // previše pokušaja sa iste IP
            alert('Previše pokušaja prijave. Pokušajte ponovo za minut.');
            // opciono: blokiraj dugme na minut
            this.loading = true;
            setTimeout(() => {
                this.loading = false;
            }, 60000);
        } else {
            alert('Došlo je do greške. Pokušajte kasnije.');
        }
    }

    });
  }

  get email() {
    return this.form.get('email');
  }

  get password() {
    return this.form.get('password');
  }
}
