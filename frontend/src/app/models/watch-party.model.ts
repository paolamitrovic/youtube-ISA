import { Video } from './video.model';

export interface WatchParty {
  id: number;
  name: string;
  video?: Video;
  members: WatchPartyMember[];
  creatorId?: number;
}

export interface WatchPartyMember {
  id: number;
  userId: number;
  username: string;
  joinedAt: string;
}

export interface CreateWatchPartyRequest {
  name: string;
  videoId?: number;
}

export interface PlayVideoMessage {
  watchPartyId: number;
  videoId: number;
}
