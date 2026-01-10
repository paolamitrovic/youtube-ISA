import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VideoListComponent } from './components/video-list/video-list.component';
import { VideoDetailComponent } from './components/video-detail/video-detail.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { LoginComponent } from './components/login/login.component';
import { ActivateComponent } from './components/activate/activate.component';
import { SignupComponent } from './components/signup/signup.component';

const routes: Routes = [
  { path: '', component: VideoListComponent },
  { path: 'login', component: LoginComponent },
  { path: 'user/:username', component: UserProfileComponent },
  { path: 'video/:id', component: VideoDetailComponent },
  { path: 'activate', component: ActivateComponent},
  { path: 'signup', component: SignupComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
