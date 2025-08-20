import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('jwt'); // or sessionStorage
  console.log('Request URL:', req.url);
  console.log('Request Token:', token);
  console.log('Request Headers:',localStorage.getItem('currentUser'));
  const isApi = req.url.startsWith('/api');    // works with Angular proxy

  if (token && isApi) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req);
};
