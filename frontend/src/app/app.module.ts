import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// Importuj komponente
import { VideoListComponent } from './components/video-list/video-list.component';
import { VideoDetailComponent } from './components/video-detail/video-detail.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { LoginComponent } from './components/login/login.component';
import { NavbarComponent } from './components/navbar/navbar.component';

// Importuj servise
import { AuthService } from './services/auth.service';
import { VideoService } from './services/video.service';
import { UserService } from './services/user.service';
import { CommentService } from './services/comment.service';
import { ApiService } from './services/api.service';
import { ConfigService } from './services/config.service';

// Importuj interceptor
import { TokenInterceptor } from './interceptor/token.interceptor';
import { SignupComponent } from './components/signup/signup.component';
import { ActivateComponent } from './components/activate/activate.component';

@NgModule({
  declarations: [
    AppComponent,
    VideoListComponent,
    VideoDetailComponent,
    UserProfileComponent,
    LoginComponent,
    NavbarComponent,
    SignupComponent,
    ActivateComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    HttpClientModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    },
    AuthService,
    VideoService,
    UserService,
    CommentService,
    ApiService,
    ConfigService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
