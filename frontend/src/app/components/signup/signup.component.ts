import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-signup.component',
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css',
  standalone: false
})
export class SignupComponent {
  signupForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {
    this.signupForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      address: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit() {
    if (this.signupForm.invalid) {
      Object.keys(this.signupForm.controls).forEach(key => {
        this.signupForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { confirmPassword, ...userData } = this.signupForm.value;

    this.http.post('http://localhost:8080/auth/signup', userData)
      .subscribe({
        next: (response: any) => {
          this.loading = false;
          alert(response.message || 'Registracija uspešna! Molim Vas proverite email za aktivaciju.');
          this.signupForm.reset();
          
          // Redirect to login after 3 seconds
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (err) => {
          this.loading = false;
          alert(err.error?.message || 'Došlo je do greške pri registraciji. Pokušajte ponovo.');
        }
      });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.signupForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.signupForm.get(fieldName);
    
    if (!field || !field.errors) return '';

    if (field.errors['required']) {
      return 'Ovo polje je obavezno';
    }
    if (field.errors['email']) {
      return 'Unesite validnu email adresu';
    }
    if (field.errors['minlength']) {
      return `Minimalno ${field.errors['minlength'].requiredLength} karaktera`;
    }
    if (field.errors['passwordMismatch']) {
      return 'Lozinke se ne poklapaju';
    }

    return '';
  }
}