package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;

/**
 * Notifies subscribers of a game that its state changed: the lobby (a player joined or left,
 * the host changed, the game started) or the running round (a player continued, answered or
 * selected, the host ended the game). Implemented over WebSocket, but kept as an interface here
 * so the service layer stays decoupled from the transport.
 */
public interface GameNotifier {

    /**
     * Broadcasts the current lobby state of the given game to its subscribers.
     *
     * @param game the game whose lobby changed
     */
    void notifyLobbyChanged(Game game);

    /**
     * Broadcasts what the current round is waiting for, so clients can tell players who is
     * still missing instead of showing an unexplained spinner.
     *
     * @param game the game whose round changed
     */
    void notifyRoundChanged(Game game);
}
