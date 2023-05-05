import { PlayerDto } from "./player-dto";

export interface RoundDto {
  id: string;
  prompt: string;
  walters: string[];
  numberOfPlaceholders: number;
  sphinx: PlayerDto;
  endOfSubmissionsInUtc: string;
}
