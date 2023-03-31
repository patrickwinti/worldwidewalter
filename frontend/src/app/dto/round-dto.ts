import { PlayerDto } from "./player-dto";

export interface RoundDto {
  id: string;
  prompt: string;
  numberOfGaps: number;
  sphinx: PlayerDto;
}
