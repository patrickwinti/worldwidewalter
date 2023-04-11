import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GameContainerComponent } from './game-container.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { getGameServiceMock, getStateServiceMock } from "../../testing/mock-services";
import { StateService } from "../../service/state.service";
import { GameService } from "../../service/game.service";

describe('GameContainerComponent', () => {
  let component: GameContainerComponent;
  let fixture: ComponentFixture<GameContainerComponent>;
  let stateService = getStateServiceMock();
  let gameService = getGameServiceMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GameContainerComponent],
      providers: [
        {provide: StateService, useValue: stateService},
        {provide: GameService, useValue: gameService},
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(GameContainerComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
