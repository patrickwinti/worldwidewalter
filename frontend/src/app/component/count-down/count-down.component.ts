import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { Subscription, timer } from "rxjs";

@Component({
  selector: 'www-count-down',
  templateUrl: './count-down.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CountDownComponent implements OnInit, OnDestroy {
  @Input() timeout = new Date().getTime() + 15000;
  @Output() timeoutEmitter = new EventEmitter<void>();

  public secondsToTimeout: number;
  public minutesToTimeout: number;

  private subscription: Subscription;

  private readonly milliSecondsInASecond = 1000;
  private readonly SecondsInAMinute = 60;
  private readonly minutesInAnHour = 60;


  constructor(private cd: ChangeDetectorRef) {
  }

  ngOnInit(): void {

    this.subscription = timer(0, 1000)
      .subscribe(() => {
        let timeDifference = this.getTimeDifference();
        this.allocateTimeUnits(timeDifference);
        this.cd.markForCheck();
        if (timeDifference <= 1000) {
          this.timeoutEmitter.emit();
        }
      });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private getTimeDifference(): number {
    return this.timeout - new Date().getTime();
  }

  private allocateTimeUnits(timeDifference: number): void {
    this.secondsToTimeout = Math.floor((timeDifference) / (this.milliSecondsInASecond) % this.SecondsInAMinute);
    this.minutesToTimeout = Math.floor((timeDifference) / (this.milliSecondsInASecond * this.minutesInAnHour) % this.SecondsInAMinute);
  }
}
