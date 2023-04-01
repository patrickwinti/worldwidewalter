export interface PropositionSelectionDto {
  roundId: string;
  propositions: Map<string, Array<string>>;
  selectionSubmissionEndInUtc: string;
}
