import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JoinComponent } from './join.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { GameService } from "../../../service/game.service";
import { getGameServiceMock } from "../../../testing/mock-services";

describe('JoinComponent', () => {
  let component: JoinComponent;
  let fixture: ComponentFixture<JoinComponent>;

  beforeEach(async () => {
    const gameService = getGameServiceMock();
    await TestBed.configureTestingModule({
      declarations: [JoinComponent],
      providers: [
        {provide: GameService, useValue: gameService},
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

  describe('ngOninit', () => {
    it('should set gameIdIsReadOnly to true if gameId presetValue present', () => {
      // arrange
      component.presetGameId = 'presetId';

      // act
      component.ngOnInit();

      // assert
      expect(component.gameIdIsReadOnly).toBeTrue();
      expect(component.gameId).toEqual('presetId');
    });

    it('should set gameIdIsReadOnly to false if no gameId presetValue present', () => {
      // arrange && act
      component.ngOnInit();

      // assert
      expect(component.gameIdIsReadOnly).toBeFalse();
      expect(component.gameId).toEqual('');
    });
  })
});
