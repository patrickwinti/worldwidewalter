import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { RoundDto } from "../../../dto/round-dto";
import { Observable } from "rxjs";
import { GameService } from "../../../service/game.service";
import { ResultDto } from "../../../dto/results-dto";

@Component({
  selector: 'www-ranking-container',
  templateUrl: './ranking-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RankingContainerComponent implements OnInit {
  @Input() round: RoundDto;
  ranking$: Observable<ResultDto[]>;

  constructor(private stateService: StateService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.ranking$ = this.gameService.getResults(this.stateService.getGameId());
  }

  continue() {
    this.stateService.goToNextState();
  }

  sortRanking(ranking: ResultDto[]): ResultDto[] {
    return ranking.sort((a, b) => b.points - a.points);
  }
}
