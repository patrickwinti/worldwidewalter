import {Component, OnInit} from '@angular/core';
import {GameState} from "../../model/game-state";

@Component({
  selector: 'www-game-container',
  templateUrl: './game-container.component.html'
})
export class GameContainerComponent implements OnInit{
  gameState: GameState;
  GameState = GameState;

  ngOnInit(): void {
    this.gameState = GameState.START;
  }
}
