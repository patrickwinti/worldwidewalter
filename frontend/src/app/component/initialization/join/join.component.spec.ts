import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JoinComponent } from './join.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { GameService } from "../../../service/game.service";
import { getGameServiceMock, getStateServiceMock } from "../../../testing/mock-services";
import { StateService } from "../../../service/state.service";
import SpyObj = jasmine.SpyObj;

describe('JoinComponent', () => {
  let component: JoinComponent;
  let stateService: SpyObj<StateService>;
  let fixture: ComponentFixture<JoinComponent>;

  beforeEach(async () => {
    const gameService = getGameServiceMock();
    stateService = getStateServiceMock();

    await TestBed.configureTestingModule({
      declarations: [JoinComponent],
      providers: [
        {provide: GameService, useValue: gameService},
        {provide: StateService, useValue: stateService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(JoinComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should set gameIdIsReadOnly to true if gameId presetValue present', () => {
      // arrange
      stateService.getGameId.and.returnValue('presetId');

      // act
      component.ngOnInit();

      // assert
      expect(component.gameIdIsReadOnly).toBeTrue();
      expect(component.joinGameId).toEqual('presetId');
    });

    it('should set gameIdIsReadOnly to false if no gameId presetValue present', () => {
      // arrange
      stateService.getGameId.and.returnValue('');

      // act
      component.ngOnInit();

      // assert
      expect(component.gameIdIsReadOnly).toBeFalse();
      expect(component.joinGameId).toEqual('');
    });
  })
});
