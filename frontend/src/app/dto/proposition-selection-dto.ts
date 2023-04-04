import { PropositionDto } from "./proposition-dto";

export interface PropositionSelectionDto {
  roundId: string;
  propositions: PropositionDto[];
  selectionSubmissionEndInUtc: string;
}
