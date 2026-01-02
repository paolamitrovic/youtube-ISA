import { User } from "./user.model";

export interface Video {
  id: number;
  title: string;
  description: string;
  thumbnailPath: string;
  videoPath: string;
  createdAt: string; // LocalDateTime dolazi kao string
  views: number;
  user: User;         // kasnije ćemo tipizirati
  comments: any[];
  likes: any[];
  tags: any[];
}

