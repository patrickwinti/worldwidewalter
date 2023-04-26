import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RankingDto } from "../../../../dto/result-dto";

@Component({
  selector: 'www-ranking-display',
  templateUrl: './ranking-display.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RankingDisplayComponent {
  @Input() sortedResults: RankingDto[];
}
