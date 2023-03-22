import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { GameService } from './game.service';
import { BehaviorSubject, Observer, of } from "rxjs";
import { getHttpClientMock } from "../testing/mock-services";
import { HttpClient } from "@angular/common/http";
import { GameState } from "../model/game-state";
import { GameDto } from "../dto/game-dto";

describe('GameService', () => {
  let service: GameService;
  let httpMock = getHttpClientMock();

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {provide: HttpClient, useValue: httpMock}
      ]
    }).compileComponents();

    service = TestBed.inject(GameService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call http.get on click createGame', () => {
    // arrange
    httpMock.post.and.returnValue(of(undefined));

    // act
    service.requestNewGame();

    // assert
    expect(httpMock.post).toHaveBeenCalledWith('http://localhost:8080/api/games', {})
  });

  describe('getGameAsSoonAsInGivenState', () => {
    it('should send http requests until condition is met', fakeAsync(() => {
      // arrange
      let gameReady = {
        state: GameState.READY,
        id: '1'
      } as GameDto;
      let gameWaiting = {
        state: GameState.WAITING_FOR_PLAYERS,
        id: '2'
      } as GameDto;

      httpMock.get.and.returnValues(
        of(
          gameWaiting,
          gameWaiting,
          gameWaiting,
          gameReady,
          gameWaiting
        )
      );

      let res = new BehaviorSubject<GameDto>({} as GameDto);

      // act
      service.getGameAsSoonAsInGivenState('1', GameState.READY, 5, 0)
        .subscribe({
          next: (val) => {
            res.next(val);
          }
        });

      tick(1000);
      let game = res.getValue();

      // assert
      expect(game.state).toBe(GameState.READY);
      expect(game.id).toBe('1');
    }));


    it('should send http requests until max retries', fakeAsync(() => {
      // arrange
      let gameWaiting1 = {
        id: '1',
        state: GameState.WAITING_FOR_PLAYERS
      } as GameDto;
      let gameWaiting2 = {
        id: '2',
        state: GameState.WAITING_FOR_PLAYERS
      } as GameDto;

      httpMock.get.and.returnValues(
        of(
          gameWaiting1,
          gameWaiting1,
          gameWaiting1,
          gameWaiting2,
          gameWaiting1
        )
      );

      let res = new BehaviorSubject<GameDto>({} as GameDto);

      // act
      let game$ = service.getGameAsSoonAsInGivenState('1', GameState.READY, 4, 1);

      game$.subscribe({
        next: (val) => {
          res.next(val);
        }
      } as Observer<GameDto>);

      tick(1000);
      let game = res.getValue();

      // assert
      expect(game.state).toBe(GameState.WAITING_FOR_PLAYERS);
      expect(game.id).toBe('2');
    }));
  });
});
