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
  points: number;
}
