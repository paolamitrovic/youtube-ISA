import { User } from "./user.model";

export interface Video {
  id: number;
  title: string;
  description: string;
  thumbnailPath: string;
  videoPath: string;
  createdAt: string;
  views: number;
  user: User;
  comments: any[];
  likes: any[];
  tags: any[];
}

