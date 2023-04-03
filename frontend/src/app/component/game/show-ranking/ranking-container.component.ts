import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { RoundDto } from "../../../dto/round-dto";
import { Observable } from "rxjs";
import { GameService } from "../../../service/game.service";
import { ResultsDto } from "../../../dto/results-dto";

@Component({
  selector: 'www-ranking-container',
  templateUrl: './ranking-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RankingContainerComponent implements OnInit{
  @Input() round: RoundDto;
  ranking$: Observable<ResultsDto>;

  constructor(private stateService: StateService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.ranking$ = this.gameService.getResults(this.stateService.getGameId());
  }

  continue() {
    this.stateService.goToNextState();
  }

  sortRanking(ranking: Map<string, number>): Map<string, number> {
    return new Map([...ranking.entries()].sort((a, b) => b[1] - a[1]))
  }
}
