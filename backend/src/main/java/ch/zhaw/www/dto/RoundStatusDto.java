package ch.zhaw.www.dto;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;

import java.util.List;

/**
 * What the current round is waiting for. Lets clients tell players how far the round has got and
 * who is still missing, instead of showing an unexplained spinner.
 *
 * @param phase       phase the round is currently in
 * @param expected    number of players the round waits for in this phase
 * @param completed   number of players that already did their part in this phase
 * @param waitingFor  names of the present players the round is still waiting for
 * @param gameEnded   whether the host ended the game
 */
public record RoundStatusDto(Phase phase,
                             int expected,
                             int completed,
                             List<String> waitingFor,
                             boolean gameEnded) {

    /**
     * Phase a round can be in from a waiting player's point of view.
     */
    public enum Phase {
        /** Waiting for the players of the previous round to continue into this one. */
        WAITING_FOR_PLAYERS,
        /** Players are writing their propositions. */
        PROPOSITIONS,
        /** Players are picking the proposition they believe is the sphinx's. */
        SELECTIONS,
        /** Every selection is in; the round result is available. */
        FINISHED
    }

    /**
     * Builds the status of the game's current round.
     *
     * @param game the game to describe
     * @return the current round status
     */
    public static RoundStatusDto from(Game game) {
        Round round = game.getCurrentRound();
        if (round == null || round.getSphinx() == null) {
            List<String> waitingFor = names(game.getPlayersNotEnteredRound());
            int entered = game.getPresentActivePlayers().size();
            return new RoundStatusDto(Phase.WAITING_FOR_PLAYERS,
                    entered + waitingFor.size(),
                    entered,
                    waitingFor,
                    game.isEnded());
        }
        int expected = (int) game.getExpectedResponderCount();
        if (!game.hasAllPropositions(round)) {
            List<String> waitingFor = names(game.getPlayersMissingProposition(round));
            return new RoundStatusDto(Phase.PROPOSITIONS,
                    expected,
                    expected - waitingFor.size(),
                    waitingFor,
                    game.isEnded());
        }
        if (!game.hasAllSelections(round)) {
            // Everybody but the sphinx selects.
            int expectedSelections = expected - 1;
            List<String> waitingFor = names(game.getPlayersMissingSelection(round));
            return new RoundStatusDto(Phase.SELECTIONS,
                    expectedSelections,
                    expectedSelections - waitingFor.size(),
                    waitingFor,
                    game.isEnded());
        }
        return new RoundStatusDto(Phase.FINISHED, expected, expected, List.of(), game.isEnded());
    }

    private static List<String> names(List<Player> players) {
        return players.stream().map(Player::getName).toList();
    }
}
