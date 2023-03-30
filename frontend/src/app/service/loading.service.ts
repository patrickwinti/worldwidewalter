import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  isLoading$: Observable<boolean>;
  private isLoading = new BehaviorSubject<boolean>(false);

  constructor() {
    this.isLoading$ = this.isLoading.asObservable();
  }

  startLoading(): void {
    this.isLoading.next(true);
    console.log('start polling');
  }

  stopLoading(): void {
    this.isLoading.next(false);
    console.log('finish polling');
  }
}
