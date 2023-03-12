import {ChangeDetectionStrategy, Component} from '@angular/core';

@Component({
  selector: 'www-initialization-container',
  templateUrl: './initialization-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InitializationContainerComponent {

  newGameCreated() {

  }
}
