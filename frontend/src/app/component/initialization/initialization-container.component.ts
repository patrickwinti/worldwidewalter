import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GameDto } from "../../dto/game-dto";
import { InitializationState } from "../../model/initialization-state";
import { StateService } from "../../service/state.service";

@Component({
  selector: 'www-initialization-container',
  templateUrl: './initialization-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class InitializationContainerComponent implements OnInit {
  @Output() startGameEmitter = new EventEmitter<GameDto>();
  @Input() joinWithId: boolean;
  state: InitializationState;
  InitializationState = InitializationState;
  id: string;

  constructor(private stateService: StateService) {
  }

  ngOnInit(): void {
    if (this.joinWithId && this.stateService.getGameId() != '') {
      this.state = InitializationState.JOIN_GAME;
    } else {
      this.state = InitializationState.WELCOME_PAGE;
    }
  }

  setState(state: InitializationState) {
    this.state = state;
    if (state === InitializationState.DONE) {
      this.startGameEmitter.emit();
    }
  }
}
