import { Component, Input } from '@angular/core';

@Component({
  selector: 'www-ranking-display',
  templateUrl: './ranking-display.component.html',
  styleUrls: ['./ranking-display.component.css']
})
export class RankingDisplayComponent {
  @Input() sortedResults: Map<string, number>;
}
