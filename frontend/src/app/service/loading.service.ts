import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private readonly isLoading$: Observable<boolean>;
  private isLoading = new BehaviorSubject<boolean>(false);
  private waitingForPlayers = new BehaviorSubject<boolean>(false);

  constructor() {
    this.isLoading$ = this.isLoading.asObservable();
  }

  startLoading(): void {
    this.isLoading.next(true);
  }

  stopLoading(): void {
    this.isLoading.next(false);
    this.waitingForPlayers.next(false);
  }

  getIsLoadingObservable(): Observable<boolean> {
    return this.isLoading$;
  }

  /**
   * Marks the current wait as a wait for other players rather than a request that is simply
   * still in flight. Set when the backend answers "too early", i.e. when it is holding the
   * request until everybody else has done their part.
   */
  setWaitingForPlayers(waiting: boolean): void {
    this.waitingForPlayers.next(waiting);
  }

  /**
   * Whether the current wait is caused by other players rather than by a slow request.
   */
  getIsWaitingForPlayersObservable(): Observable<boolean> {
    return this.waitingForPlayers.asObservable();
  }
}
