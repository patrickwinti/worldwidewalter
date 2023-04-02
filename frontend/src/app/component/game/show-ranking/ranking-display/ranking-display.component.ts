import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'www-ranking-display',
  templateUrl: './ranking-display.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RankingDisplayComponent {
  @Input() sortedResults: Map<string, number>;
}
