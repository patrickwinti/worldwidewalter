import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
  HttpStatusCode
} from '@angular/common/http';
import { finalize, Observable, retry, timer } from 'rxjs';
import { StateService } from "./state.service";
import { Injectable } from "@angular/core";
import { LoadingService } from "./loading.service";

@Injectable()
export class HttpPollingInterceptor implements HttpInterceptor {
  private readonly RETRIES = 100;

  constructor(private stateService: StateService,
              private loadingService: LoadingService) { 
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {

    const requestWithHeader = this.addPlayerIdToHeader(request);

    this.loadingService.startLoading();
    
    return next.handle(requestWithHeader)
      .pipe(
        retry({count: this.RETRIES, delay: this.shouldRetry}),
        finalize(() => {
          this.loadingService.stopLoading();
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
