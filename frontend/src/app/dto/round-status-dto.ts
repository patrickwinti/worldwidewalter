/**
 * Phase the current round is in, from a waiting player's point of view.
 */
export enum RoundPhase {
  WAITING_FOR_PLAYERS = 'WAITING_FOR_PLAYERS',
  PROPOSITIONS = 'PROPOSITIONS',
  SELECTIONS = 'SELECTIONS',
  FINISHED = 'FINISHED',
}

/**
 * What the current round is waiting for. Pushed on the /topic/games/{gameId}/round WebSocket
 * topic whenever the round advances, and readable via GET /games/{gameId}/rounds/status.
 */
export interface RoundStatusDto {
  phase: RoundPhase;
  /** Number of players the round waits for in this phase. */
  expected: number;
  /** Number of players that already did their part in this phase. */
  completed: number;
  /** Names of the players the round is still waiting for. */
  waitingFor: string[];
  /** True once the host ended the game. */
  gameEnded: boolean;
}
