import {ComponentFixture, TestBed} from '@angular/core/testing';

import {WelcomeComponent} from './welcome.component';
import {getGameServiceMock} from "src/app/testing/mock-services"
import {firstValueFrom, of} from "rxjs";
import {GameService} from "../../service/game.service";
import {GameDto} from "../../dto/GameDto";

describe('TestComponent', () => {
  let component: WelcomeComponent;
  let fixture: ComponentFixture<WelcomeComponent>;
  let gameService = getGameServiceMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WelcomeComponent],
      providers: [
        {provide: GameService, useValue: gameService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(WelcomeComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('createGame should call gameService', () => {
    // arrange
    gameService.requestNewGame.and.returnValue(firstValueFrom(of({} as GameDto)));

    // act
    component.createGame();

    // assert
    expect(gameService.requestNewGame).toHaveBeenCalled();
  })
});
