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
  @Input() timeoutString: string;
  @Output() timeoutEmitter = new EventEmitter<void>();

  public secondsToTimeout: number;
  public minutesToTimeout: number;

  private timeDifference: number;
  private subscription: Subscription;
  private timeout: number;

  private readonly milliSecondsInASecond = 1000;
  private readonly secondsInAMinute = 60;
  private readonly minutesInAnHour = 60;
  private readonly TIMEOUT_REDUCTION = 2 * this.milliSecondsInASecond;

  constructor(private cd: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.subscription = timer(0, 1000)
      .subscribe(() => {
        this.timeDifference = this.getTimeDifference();
        this.allocateTimeUnits();
        this.cd.markForCheck();
        if (this.timeDifference <= 1000) {
          this.timeoutEmitter.emit();
        }
      });
    this.timeout = this.getUTCMilliseconds(this.timeoutString) - this.TIMEOUT_REDUCTION;
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private getTimeDifference(): number {
    return this.timeout - new Date().getTime();
  }

  private allocateTimeUnits(): void {
    this.secondsToTimeout = Math.floor((this.timeDifference) / (this.milliSecondsInASecond) % this.secondsInAMinute);
    this.minutesToTimeout = Math.floor((this.timeDifference) / (this.milliSecondsInASecond * this.minutesInAnHour) % this.secondsInAMinute);
  }

  private getUTCMilliseconds(dateString: string): number {
    // dateString format will be "YYYY-MM-DD HH:mm:ss"
    var [date, time] = dateString.split(" ");
    var [year, month, day] = date.split("-");
    var [hours, minutes, seconds] = time.split(":");
    // month is 0 indexed in Date operations, subtract 1 when converting string to Date object
    return Date.UTC(Number.parseInt(year), Number.parseInt(month) - 1, Number.parseInt(day), Number.parseInt(hours), Number.parseInt(minutes), Number.parseInt(seconds));
  }
}
