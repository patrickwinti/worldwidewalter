import {
  HttpContextToken,
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

/**
 * Set on a request's HttpContext to suppress the loading overlay for that request while still
 * getting the 425 retry behaviour (used e.g. for the sphinx's background "everyone selected" poll,
 * so the answers stay readable instead of being hidden behind the spinner).
 */
export const SKIP_LOADING = new HttpContextToken<boolean>(() => false);

@Injectable()
export class HttpPollingInterceptor implements HttpInterceptor {
  private activeRequests: number = 0;

  constructor(private stateService: StateService,
              private loadingService: LoadingService) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {

    const requestWithHeader = this.addPlayerIdToHeader(request);
    const skipLoading = request.context.get(SKIP_LOADING);

    if (!skipLoading) {
      if (this.activeRequests === 0) {
        this.loadingService.startLoading();
      }
      this.activeRequests++;
    }

    return next.handle(requestWithHeader)
      .pipe(
        retry({delay: (response: HttpResponse<unknown>) => this.shouldRetry(response, skipLoading)}),
        finalize(() => {
          if (!skipLoading) {
            this.activeRequests--;
            if (this.activeRequests === 0) {
              this.loadingService.stopLoading();
            }
          }
        })
      )
  }

  /**
   * "Too early" means the backend is waiting for the other players, not that something went
   * wrong: keep polling, and let the loading overlay say so instead of looking like a slow
   * request.
   */
  private shouldRetry(response: HttpResponse<unknown>, skipLoading: boolean) {
    if (response.status === HttpStatusCode.TooEarly) {
      if (!skipLoading) {
        this.loadingService.setWaitingForPlayers(true);
      }
      return timer(1000);
    }
    throw response;
  }

  private addPlayerIdToHeader(request: HttpRequest<unknown>): HttpRequest<unknown> {
    return request.clone({
      headers: request.headers.set('X-PLAYER-ID', this.stateService.getPlayerId())
    });
  }
}
