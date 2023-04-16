import { PlayerDto } from "./player-dto";

export interface RoundDto {
  id: string;
  prompt: string;
  numberOfPlaceholders: number;
  sphinx: PlayerDto;
  endOfSubmissionsInUtc: string;
}
