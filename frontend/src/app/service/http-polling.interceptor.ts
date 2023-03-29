import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
  HttpStatusCode
} from '@angular/common/http';
import { Observable, retry, timer } from 'rxjs';
import { StateService } from "./state.service";
import { Injectable } from "@angular/core";

@Injectable()
export class HttpPollingInterceptor implements HttpInterceptor {
  private readonly RETRIES = 100;

  constructor(private stateService: StateService) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {

    const requestWithHeader = this.addPlayerIdToHeader(request);

    return next.handle(requestWithHeader)
      .pipe(
        retry({count: this.RETRIES, delay: this.shouldRetry})
      )
  }

  private shouldRetry(response: HttpResponse<unknown>) {
    if (response.status === HttpStatusCode.TooEarly) {
      return timer(4000);
    }
    throw response;
  }

  private addPlayerIdToHeader(request: HttpRequest<unknown>): HttpRequest<unknown> {
    return request.clone({
      headers: request.headers.set('X-PLAYER-ID', this.stateService.getPlayerId())
    });
  }
}
