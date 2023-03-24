import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
  HttpStatusCode
} from '@angular/common/http';
import { Observable, retry, timer } from 'rxjs';

export class HttpPollingInterceptor implements HttpInterceptor {
  private readonly RETRIES = 100;

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request)
      .pipe(
        retry({count: this.RETRIES, delay: this.shouldRetry})
      )
  }

  shouldRetry(response: HttpResponse<unknown>) {
    if (response.status === HttpStatusCode.TooEarly) {
      return timer(4000);
    }
    throw response;
  }
}
