import { PlayerDto } from "./player-dto";

export interface GameCreatedDto {
  gameId: string;
  host: PlayerDto;
}
