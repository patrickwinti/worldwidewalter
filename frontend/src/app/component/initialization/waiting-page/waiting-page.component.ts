import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {GameService} from "../../../service/game.service";
import {GameDto} from "../../../dto/game-dto";

@Component({
  selector: 'www-waiting-page',
  templateUrl: './waiting-page.component.html'
})
export class WaitingPageComponent implements OnInit{
  @Input() game: GameDto;
  @Output() gameStartEmitter = new EventEmitter<GameDto>();

  constructor(private gameService: GameService) {
  }

  ngOnInit(): void {
    this.gameService.getGameStatus(this.game.id).then(
      r => {console.log(r)});
  }
}
