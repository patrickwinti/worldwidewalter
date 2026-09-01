import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { RankingDto, ResultDto, RoundDto } from "@api";
import { firstValueFrom, Observable } from "rxjs";
import { GameService } from "../../../service/game.service";
import { GameState } from "../../../model/game-state";

@Component({
  selector: 'www-result-container',
  templateUrl: './result-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ResultContainerComponent implements OnInit {
  @Input() round: RoundDto;
  result$: Observable<ResultDto>;

  constructor(private stateService: StateService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.result$ = this.gameService.getResultsForRound(this.round.id);
  }

  continue() {
    this.stateService.setState(GameState.REQUEST_NEW_ROUND);
  }

  sortRanking(ranking: RankingDto[]): RankingDto[] {
    return ranking.sort((a, b) => b.points - a.points);
  }

  leaveGame() {
    firstValueFrom(this.gameService.leaveGame(this.stateService.getPlayerId(), this.stateService.getGameId())).then(() => {
      this.stateService.leaveGame()
    });
  }

  get prompt(): string {
    return this.stateService.getPrompt();
  }

  getSphinxProposition(result: ResultDto) {
    return result.selections.find(selection => selection.sphinxResponse)?.gaps ?? [''];
  }
}
