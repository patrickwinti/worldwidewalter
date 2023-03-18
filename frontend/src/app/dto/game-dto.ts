import { GameState } from "../model/game-state";

export interface GameDto {
  id: string;
  state: GameState;
}
