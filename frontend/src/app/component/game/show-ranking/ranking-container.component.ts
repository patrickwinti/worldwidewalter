import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { RoundDto } from "../../../dto/round-dto";
import { firstValueFrom, Observable } from "rxjs";
import { GameService } from "../../../service/game.service";
import { ResultDto } from "../../../dto/results-dto";
import { GameState } from "../../../model/game-state";

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
    this.stateService.setState(GameState.ENTERING_ROUND);
  }

  sortRanking(ranking: ResultDto[]): ResultDto[] {
    return ranking.sort((a, b) => b.points - a.points);
  }

  leaveGame() {
    firstValueFrom(this.gameService.leaveGame(this.stateService.getPlayerId(), this.stateService.getGameId())).then(() => {
      this.stateService.leaveGame()
    });
  }
}
