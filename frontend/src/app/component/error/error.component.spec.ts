import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorComponent } from './error.component';
import { StateService } from "../../service/state.service";
import { GameService } from "../../service/game.service";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { getGameServiceMock, getLoadingServiceMock, getStateServiceMock } from "../../testing/mock-services";
import { LoadingService } from "../../service/loading.service";

describe('ErrorComponent', () => {
  let component: ErrorComponent;
  let fixture: ComponentFixture<ErrorComponent>;
  let stateService = getStateServiceMock();
  let gameService = getGameServiceMock();
  let loadingService = getLoadingServiceMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ErrorComponent],
      providers: [
        {provide: StateService, useValue: stateService},
        {provide: GameService, useValue: gameService},
        {provide: LoadingService, useValue: loadingService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ErrorComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
