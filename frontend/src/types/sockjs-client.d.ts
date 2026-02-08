declare module 'sockjs-client' {
  interface SockJSOptions {
    [key: string]: any;
  }

  class SockJS {
    constructor(url: string, protocols?: string | string[] | null, options?: SockJSOptions);
    readyState: number;
    protocol: string;
    url: string;
    onopen: ((event: any) => void) | null;
    onmessage: ((event: any) => void) | null;
    onclose: ((event: any) => void) | null;
    onerror: ((event: any) => void) | null;
    send(data: string): void;
    close(code?: number, reason?: string): void;
  }

  export default SockJS;
}
