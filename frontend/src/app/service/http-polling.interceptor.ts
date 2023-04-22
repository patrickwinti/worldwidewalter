import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
  HttpStatusCode
} from '@angular/common/http';
import { finalize, Observable, retry, takeUntil, timer } from 'rxjs';
import { StateService } from "./state.service";
import { Injectable } from "@angular/core";
import { LoadingService } from "./loading.service";
import { HttpCancelService } from "./http-cancel.service";

@Injectable()
export class HttpPollingInterceptor implements HttpInterceptor {
  private readonly RETRIES: number = 100;
  private activeRequests: number = 0;

  constructor(private stateService: StateService,
              private loadingService: LoadingService,
              private httpCancelService: HttpCancelService) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {

    const requestWithHeader = this.addPlayerIdToHeader(request);

    if (this.activeRequests === 0) {
      this.loadingService.startLoading();
    }
    this.activeRequests++;

    return next.handle(requestWithHeader)
      .pipe(
        retry({count: this.RETRIES, delay: this.shouldRetry}),
        takeUntil(this.httpCancelService.onCancelPendingRequests()),
        finalize(() => {
          this.activeRequests--;
          if(this.activeRequests === 0) {
            this.loadingService.stopLoading();
          }
        })
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
