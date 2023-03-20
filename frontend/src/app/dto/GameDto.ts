export enum State {
  WAITING_FOR_PLAYERS,
  READY
}

export class GameDto {
  id: string;
  state: State;
}
