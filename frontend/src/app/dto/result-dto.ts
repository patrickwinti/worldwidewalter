export interface ResultDto {
  ranking: RankingDto[];
  selections: SelectionDto[];
}

export interface SelectionDto {
  authors: string[];
  gaps: string[];
  selectors: string[];
}

export interface RankingDto {
  playerName: string;
  points: number;
}
