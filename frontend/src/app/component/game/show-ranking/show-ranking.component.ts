import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { RoundDto } from "../../../dto/round-dto";

@Component({
  selector: 'www-show-ranking',
  templateUrl: './show-ranking.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShowRankingComponent {
  @Input() round: RoundDto;

  constructor(private stateService: StateService) {
  }

  continue() {
    this.stateService.goToNextState();
  }
}
