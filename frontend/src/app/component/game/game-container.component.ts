import { Component, OnInit } from '@angular/core';
import { GameState } from "../../model/game-state";
import { StateService } from "../../service/state.service";

@Component({
  selector: 'www-game-container',
  templateUrl: './game-container.component.html'
})
export class GameContainerComponent implements OnInit {
  gameState: GameState;
  GameState = GameState;

  constructor(private stateService: StateService) {
  }

  ngOnInit(): void {
    this.stateService.getStateObservable()
      .subscribe(next => {
        this.gameState = next;
      });
  }
}
