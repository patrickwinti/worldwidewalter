import { ChangeDetectionStrategy, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { GameDto } from "../../dto/game-dto";
import { InitializationState } from "../../model/initialization-state";

@Component({
  selector: 'www-initialization-container',
  templateUrl: './initialization-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InitializationContainerComponent implements OnInit {
  @Output() startGameEmitter = new EventEmitter<GameDto>();
  state: InitializationState;
  InitializationState = InitializationState;

  ngOnInit(): void {
    this.state = InitializationState.WELCOME_PAGE;
  }

  setState(state: InitializationState) {
    this.state = state;
    if (state === InitializationState.DONE) {
      this.startGameEmitter.emit();
    }
  }
}
