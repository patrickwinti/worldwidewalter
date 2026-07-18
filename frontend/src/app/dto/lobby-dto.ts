import { PlayerDto } from "./player-dto";

export interface LobbyDto {
  players: PlayerDto[];
  hostId: string;
  started: boolean;
  minimumPlayers: number;
}
