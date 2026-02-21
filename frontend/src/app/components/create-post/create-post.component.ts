import { Component, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { VideoService } from '../../services/video.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-create-post',
  templateUrl: './create-post.component.html',
  styleUrl: './create-post.component.css',
  standalone: false
})
export class CreatePostComponent implements OnDestroy {
  createPostForm: FormGroup;
  loading = false;
  selectedThumbnail: File | null = null;
  selectedVideo: File | null = null;
  thumbnailPreview: string | null = null;
  videoPreviewUrl: string | null = null;
  videoSizeError: string | null = null;
  tagsInput: string = '';

  constructor(
    private fb: FormBuilder,
    private videoService: VideoService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private authService: AuthService
  ) {
    this.createPostForm = this.fb.group({
      title: ['', [Validators.required]],
      description: ['', [Validators.required]],
      location: [''],
      tags: ['']
    });
  }

  onThumbnailSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedThumbnail = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.thumbnailPreview = e.target.result;
        this.cdr.detectChanges(); // ažurira prikaz odmah (FileReader je van Angular zone)
      };
      reader.readAsDataURL(file);
    }
  }

  onVideoSelected(event: any) {
    const file = event.target.files[0];
    // Oslobodi prethodni video URL
    if (this.videoPreviewUrl) {
      URL.revokeObjectURL(this.videoPreviewUrl);
      this.videoPreviewUrl = null;
    }
    if (file) {
      // Check file type
      if (!file.name.toLowerCase().endsWith('.mp4')) {
        this.videoSizeError = 'Video mora biti u MP4 formatu';
        this.selectedVideo = null;
        return;
      }

      // Check file size (200MB max)
      const maxSize = 200 * 1024 * 1024; // 200MB in bytes
      if (file.size > maxSize) {
        this.videoSizeError = 'Video je prevelik. Maksimalna veličina je 200MB';
        this.selectedVideo = null;
        return;
      }

      this.selectedVideo = file;
      this.videoSizeError = null;
      this.videoPreviewUrl = URL.createObjectURL(file);
      this.cdr.detectChanges();
    }
  }

  onSubmit() {
    if (this.createPostForm.invalid) {
      Object.keys(this.createPostForm.controls).forEach(key => {
        this.createPostForm.get(key)?.markAsTouched();
      });
      return;
    }

    if (!this.selectedThumbnail) {
      alert('Molimo izaberite thumbnail sliku');
      return;
    }

    if (!this.selectedVideo) {
      alert('Molimo izaberite video fajl');
      return;
    }

    // Check if user is authenticated
    if (!this.authService.tokenIsPresent()) {
      alert('Morate biti prijavljeni da biste kreirali objavu');
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;

    const formData = new FormData();
    formData.append('title', this.createPostForm.get('title')?.value);
    formData.append('description', this.createPostForm.get('description')?.value);
    
    const location = this.createPostForm.get('location')?.value;
    if (location && location.trim() !== '') {
      formData.append('location', location.trim());
    }

    // Parse tags from comma-separated string
    const tagsString = this.tagsInput.trim();
    const tags = tagsString ? tagsString.split(',').map(tag => tag.trim()).filter(tag => tag !== '') : [];
    if (tags.length > 0) {
      formData.append('tags', JSON.stringify(tags));
    }

    formData.append('thumbnail', this.selectedThumbnail);
    formData.append('video', this.selectedVideo);

    this.videoService.createVideo(formData).subscribe({
      next: (response: any) => {
        this.loading = false;
        alert('Video je uspešno postavljen!');
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading = false;
        const errorMessage = err.error?.message || err.message || 'Došlo je do greške pri kreiranju objave. Pokušajte ponovo.';
        alert(errorMessage);
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.createPostForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  ngOnDestroy(): void {
    if (this.videoPreviewUrl) {
      URL.revokeObjectURL(this.videoPreviewUrl);
    }
  }

  getErrorMessage(fieldName: string): string {
    const field = this.createPostForm.get(fieldName);
    
    if (!field || !field.errors) return '';

    if (field.errors['required']) {
      return 'Ovo polje je obavezno';
    }

    return '';
  }
}
