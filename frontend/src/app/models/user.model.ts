export interface User {
  id: number;
  username: string;
  firstName?: string;  // opcionalno, jer možda ponekad može biti null
  lastName?: string;   // isto
  email?: string;
  address?: string;
}
