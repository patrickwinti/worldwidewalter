import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { RoundDto } from "../../../dto/round-dto";
import { firstValueFrom, Observable } from "rxjs";
import { GameService } from "../../../service/game.service";
import { RankingDto, ResultDto } from "../../../dto/result-dto";
import { ResultDto } from "../../../dto/results-dto";
import { GameState } from "../../../model/game-state";

@Component({
  selector: 'www-result-container',
  templateUrl: './result-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResultContainerComponent implements OnInit {
  @Input() round: RoundDto;
  result$: Observable<ResultDto>;

  constructor(private stateService: StateService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.result$ = this.gameService.getResults(this.stateService.getGameId());
  }

  continue() {
    this.stateService.setState(GameState.ENTERING_ROUND);
  }

  sortRanking(ranking: RankingDto[]): RankingDto[] {
    return ranking.sort((a, b) => b.points - a.points);
  }

  leaveGame() {
    firstValueFrom(this.gameService.leaveGame(this.stateService.getPlayerId(), this.stateService.getGameId())).then(() => {
      this.stateService.leaveGame()
    });
  }
}
