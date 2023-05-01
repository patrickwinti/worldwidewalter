import { Component, Input } from '@angular/core';

@Component({
  selector: 'www-current-game-info',
  templateUrl: './current-game-info.component.html'
})
export class CurrentGameInfoComponent {
  @Input() playerName: string;
  @Input() gameId: string;
}
