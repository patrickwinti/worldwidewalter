import { PlayerDto } from "./player-dto";

export interface LobbyDto {
  players: PlayerDto[];
  hostId: string;
  started: boolean;
  minimumPlayers: number;
  /** True once the host ended the game; the lobby is then showing a finished match. */
  ended: boolean;
}
