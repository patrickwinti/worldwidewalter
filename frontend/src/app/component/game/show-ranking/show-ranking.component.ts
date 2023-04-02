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
  sortedRanking = new Map<string, number>();

  constructor(private stateService: StateService) {
    let playerPointMap = new Map<string, number>();
    playerPointMap.set('player 0', 4);
    playerPointMap.set('player 1', 12);
    playerPointMap.set('player 2', 7);
    playerPointMap.set('player 3', 0);

    this.sortedRanking = new Map([...playerPointMap.entries()].sort((a, b) => b[1] - a[1]))
  }

  continue() {
    this.stateService.goToNextState();
  }
}
