import {Component, ChangeDetectionStrategy} from '@angular/core';

@Component({
  selector: 'www-info',
  templateUrl: './info.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
})
export class InfoComponent {
  collapsed: boolean = true;
}
