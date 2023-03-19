import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { GameDto } from "../../../dto/game-dto";
import { Subscription } from "rxjs";
import { GameState } from "../../../model/game-state";

@Component({
  selector: 'www-waiting-page',
  templateUrl: './waiting-page.component.html'
})
export class WaitingPageComponent implements OnInit, OnDestroy {
  @Input() game: GameDto;
  @Output() gameStartEmitter = new EventEmitter<GameDto>();
  private gameSubscription = new Subscription;

  constructor(private gameService: GameService) {
  }

  ngOnInit(): void {
    this.gameSubscription = this.gameService.getGameAsSoonAsInGivenState(
      this.game.id, GameState.READY, 100, 1000)
      .subscribe((result) => {
          this.game = result;
        }
      );
  };

  ngOnDestroy() {
    this.stopPolling();
  }

  stopPolling() {
    this.gameSubscription.unsubscribe();
  }

}
