import { User } from './user.model';

export interface Comment {
  text: string;
  createdAt: string; // LocalDateTime sa backenda dolazi kao string
  user: User;
}
