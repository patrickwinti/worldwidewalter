import { ChangeDetectionStrategy, Component } from '@angular/core';
import { LoadingService } from "../../service/loading.service";

@Component({
  selector: 'www-overlay-spinner',
  templateUrl: './overlay-spinner.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class OverlaySpinnerComponent {
  isLoading$ = this.loadingService.isLoading$;
  displayText = 'loading';

  constructor(private loadingService: LoadingService) {
  }

}
