import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VideoListComponent } from './components/video-list/video-list.component';
import { VideoDetailComponent } from './components/video-detail/video-detail.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { LoginComponent } from './components/login/login.component';
import { ActivateComponent } from './components/activate/activate.component';
import { SignupComponent } from './components/signup/signup.component';
import { CreatePostComponent } from './components/create-post/create-post.component';
import { WatchPartyListComponent } from './components/watch-party-list/watch-party-list.component';
import { WatchPartyCreateComponent } from './components/watch-party-create/watch-party-create.component';
import { WatchPartyRoomComponent } from './components/watch-party-room/watch-party-room.component';

const routes: Routes = [
  { path: '', component: VideoListComponent },
  { path: 'login', component: LoginComponent },
  { path: 'user/:username', component: UserProfileComponent },
  { path: 'video/:id', component: VideoDetailComponent },
  { path: 'activate', component: ActivateComponent},
  { path: 'signup', component: SignupComponent},
  { path: 'create-post', component: CreatePostComponent},
  { path: 'watch-party', component: WatchPartyListComponent },
  { path: 'watch-party/create', component: WatchPartyCreateComponent },
  { path: 'watch-party/:id', component: WatchPartyRoomComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
