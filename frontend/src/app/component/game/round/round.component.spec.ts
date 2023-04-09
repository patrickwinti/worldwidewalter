import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoundComponent } from './round.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { StateService } from "../../../service/state.service";
import { GameService } from "../../../service/game.service";
import { getGameServiceMock, getStateServiceMock } from "../../../testing/mock-services";

describe('RoundComponent', () => {
  let component: RoundComponent;
  let fixture: ComponentFixture<RoundComponent>;
  let stateService = getStateServiceMock();
  let gameService = getGameServiceMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RoundComponent],
      providers: [
        {provide: StateService, useValue: stateService},
        {provide: GameService, useValue: gameService},
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(RoundComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
