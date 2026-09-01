import { HttpClient } from "@angular/common/http";
import { GameService } from "../service/game.service";
import { StateService } from "../service/state.service";
import { AppConfigService } from "../service/app-config.service";
import { LoadingService } from "../service/loading.service";

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
      'startGame',
      'getLobby',
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
      'setPlayerName',
      'getPlayerName',
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

export function getLoadingServiceMock(): jasmine.SpyObj<LoadingService> {
  return jasmine.createSpyObj(
    'LoadingService',
    [
      'startLoading',
      'stopLoading',
      'getIsLoadingObservable',
      'setWaitingForPlayers',
      'getIsWaitingForPlayersObservable'
    ]
  )
}
