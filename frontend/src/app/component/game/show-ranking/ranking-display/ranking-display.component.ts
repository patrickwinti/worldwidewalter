import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RankingDto } from "@api";

@Component({
  selector: 'www-ranking-display',
  templateUrl: './ranking-display.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class RankingDisplayComponent {
  @Input() sortedResults: RankingDto[];
}
