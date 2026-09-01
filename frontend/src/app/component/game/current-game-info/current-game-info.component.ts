import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'www-current-game-info',
  templateUrl: './current-game-info.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
})
export class CurrentGameInfoComponent {
  @Input() playerName: string;
  @Input() gameId: string;
}
