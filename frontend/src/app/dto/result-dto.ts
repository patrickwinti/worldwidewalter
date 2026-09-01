export interface ResultDto {
  ranking: RankingDto[];
  selections: SelectionDto[];
}

export interface SelectionDto {
  authors: string[];
  gaps: string[];
  selectors: string[];
  sphinxResponse: boolean;
}

export interface RankingDto {
  playerName: string;
  /** Running total across the whole game. */
  points: number;
  /** What the round just played added to the total. */
  roundPoints: number;
}
