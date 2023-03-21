import { fakeAsync, flush, TestBed, tick } from '@angular/core/testing';
import { GameService } from './game.service';
import { Observer, of } from "rxjs";
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
        state: GameState.READY
      } as GameDto;
      let gameWaiting = {
        state: GameState.WAITING_FOR_PLAYERS
      } as GameDto;
      let spy = spyOn(service, 'getGame').and.returnValues(
        of(gameWaiting),
        of(gameWaiting),
        of(gameReady),
        of(gameReady)
      );

      // act
      service.getGameAsSoonAsInGivenState('1', GameState.READY, 5, 1)
        .subscribe({
          next: (val) => {
            expect(val.state === GameState.READY);
          },
          complete: () => {
            expect(spy).toHaveBeenCalledTimes(3);
          }
        });

      tick(10);
      flush();
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
      let spy = spyOn(service, 'getGame').and.returnValues(
        of(gameWaiting1),
        of(gameWaiting1),
        of(gameWaiting1),
        of(gameWaiting1),
        of(gameWaiting2),
        of(gameWaiting1),
      );

      // act
      let game$ = service.getGameAsSoonAsInGivenState('1', GameState.READY, 4, 1);

      let count = 0;
      game$.subscribe({
        next: (val) => {
          count++;
          if (count < 5) {
            expect(val.id).toBe('1');
          } else {
            expect(val.id).toBe('2');
          }
        },
        complete: () => {
          // assert
          expect(spy).toHaveBeenCalledTimes(5);
        }
      } as Observer<GameDto>);

      tick(1000);
    }));
  });
});
