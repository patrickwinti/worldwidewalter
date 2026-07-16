import {Component} from '@angular/core';

@Component({
  selector: 'www-info',
  templateUrl: './info.component.html',
  standalone: false,
})
export class InfoComponent {
  collapsed: boolean = true;
}
