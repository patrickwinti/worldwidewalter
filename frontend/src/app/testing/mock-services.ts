import { HttpClient } from "@angular/common/http";
import { GameService } from "../service/game.service";
import { StateService } from "../service/state.service";
import { AppConfigService } from "../service/app-config.service";

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
      'getGameAsSoonAsInGivenState',
      'submitProposition'
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
      'getPlayerId',
      'setRound',
      'getRound'
    ]
  )
}

export function getAppConfigServiceMock(): jasmine.SpyObj<AppConfigService> {
  return jasmine.createSpyObj(
    'AppConfigService',
    [
      'getBaseUrl'
    ]
  )
}
