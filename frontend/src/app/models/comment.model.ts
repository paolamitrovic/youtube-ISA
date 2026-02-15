import { User } from './user.model';

export interface Comment {
  id?: number;
  text: string;
  createdAt: string; // LocalDateTime sa backenda dolazi kao string
  user: User;
}

export interface CommentPageResponse {
  content: Comment[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface CreateCommentRequest {
  text: string;
  videoId: number;
}
