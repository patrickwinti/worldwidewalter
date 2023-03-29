import { HttpClient } from "@angular/common/http";
import { GameService } from "../service/game.service";
import { StateService } from "../service/state.service";

export function getHttpClientMock(): jasmine.SpyObj<HttpClient> {
  return jasmine.createSpyObj(
    'HttpClient',
    [
      'get',
      'post',
      'put'
    ]
  )
}

export function getGameServiceMock(): jasmine.SpyObj<GameService> {
  return jasmine.createSpyObj(
    'GameService',
    [
      'requestNewGame',
      'getGame',
      'getGameAsSoonAsInGivenState'
    ]
  )
}

export function getStateServiceMock(): jasmine.SpyObj<StateService> {
  return jasmine.createSpyObj(
    'StateService',
    [
      'goToNextState',
      'getStateObservable',
      'getCurrentState',
      'setGameId',
      'getGameId',
      'setPlayerId',
      'getPlayerId'
    ]
  )

}
