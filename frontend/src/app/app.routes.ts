import { Routes } from '@angular/router';
import { VideoListComponent } from './components/video-list/video-list.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { VideoDetailComponent } from './components/video-detail/video-detail.component';


export const routes: Routes = [
    { path: '', component: VideoListComponent },
    { path: 'user/:username', component: UserProfileComponent },
    { path: 'video/:id', component: VideoDetailComponent}
];
