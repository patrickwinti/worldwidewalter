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
import { Subject, takeUntil, timer } from "rxjs";

@Component({
  selector: 'www-count-down',
  templateUrl: './count-down.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CountDownComponent implements OnInit, OnDestroy {
  @Input() timeoutString: string;
  @Output() timeoutEmitter = new EventEmitter<void>();

  public secondsToTimeout: number;
  public minutesToTimeout: number;

  private timeDifference: number;
  private timeout: number;
  private startTime: number;

  /** Circumference of the progress ring (r = 23). */
  readonly ringCircumference = 2 * Math.PI * 23;

  private readonly milliSecondsInASecond = 1000;
  private readonly secondsInAMinute = 60;
  private readonly minutesInAnHour = 60;
  private readonly TIMEOUT_REDUCTION = 2 * this.milliSecondsInASecond;

  private readonly destroy$ = new Subject<boolean>();

  constructor(private cd: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    timer(0, 1000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.timeDifference = this.getTimeDifference();
        this.allocateTimeUnits();
        this.cd.markForCheck();
        if (this.timeDifference <= 1000 && this.timeDifference >= 0) {
          this.timeoutEmitter.emit();
        } else if (this.timeDifference < 0) {
          this.timeoutEmitter.emit();
          this.secondsToTimeout = 0;
          this.minutesToTimeout = 0;
          this.destroy$.next(true);
          this.destroy$.complete();
        }
      });
    this.timeout = this.getUTCMilliseconds(this.timeoutString) - this.TIMEOUT_REDUCTION;
    this.startTime = new Date().getTime();
  }

  /** Fraction of time still remaining, clamped to [0, 1]. */
  get remainingRatio(): number {
    const total = this.timeout - this.startTime;
    if (!total || total <= 0) {
      return 0;
    }
    return Math.min(1, Math.max(0, this.timeDifference / total));
  }

  get ringDashOffset(): number {
    return this.ringCircumference * (1 - this.remainingRatio);
  }

  get isUrgent(): boolean {
    return this.timeDifference <= 15000;
  }

  get clock(): string {
    const minutes = Math.max(0, this.minutesToTimeout || 0);
    const seconds = Math.max(0, this.secondsToTimeout || 0);
    return minutes + ':' + (seconds < 10 ? '0' + seconds : seconds);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
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
