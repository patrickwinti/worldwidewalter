package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;

/**
 * Notifies lobby subscribers that the state of a game's lobby has changed (a player joined or
 * left, the host changed, or the game started). Implemented over WebSocket, but kept as an
 * interface here so the service layer stays decoupled from the transport.
 */
public interface LobbyNotifier {

    /**
     * Broadcasts the current lobby state of the given game to its subscribers.
     *
     * @param game the game whose lobby changed
     */
    void notifyLobbyChanged(Game game);
}
