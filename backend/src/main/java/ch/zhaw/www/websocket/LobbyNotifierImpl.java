package ch.zhaw.www.websocket;

import ch.zhaw.www.dto.LobbyDto;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.service.LobbyNotifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Broadcasts lobby state changes over STOMP to {@code /topic/games/{gameId}/lobby}.
 */
@Component
public class LobbyNotifierImpl implements LobbyNotifier {

    private static final String LOBBY_TOPIC_TEMPLATE = "/topic/games/%s/lobby";

    private final SimpMessagingTemplate messagingTemplate;

    LobbyNotifierImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyLobbyChanged(Game game) {
        messagingTemplate.convertAndSend(
                String.format(LOBBY_TOPIC_TEMPLATE, game.getId()),
                LobbyDto.from(game));
    }
}
