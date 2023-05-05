import { PlayerDto } from "./player-dto";

export interface RoundDto {
  id: string;
  prompt: string;
  walters: string[];
  sphinx: PlayerDto;
  endOfSubmissionsInUtc: string;
}
