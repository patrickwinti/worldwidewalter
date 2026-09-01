package ch.zhaw.www.websocket;

import ch.zhaw.www.dto.LobbyDto;
import ch.zhaw.www.dto.RoundStatusDto;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.service.GameNotifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Broadcasts game state changes over STOMP to {@code /topic/games/{gameId}/lobby} and
 * {@code /topic/games/{gameId}/round}.
 */
@Component
public class GameNotifierImpl implements GameNotifier {

    private static final String LOBBY_TOPIC_TEMPLATE = "/topic/games/%s/lobby";
    private static final String ROUND_TOPIC_TEMPLATE = "/topic/games/%s/round";

    private final SimpMessagingTemplate messagingTemplate;

    GameNotifierImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyLobbyChanged(Game game) {
        messagingTemplate.convertAndSend(
                String.format(LOBBY_TOPIC_TEMPLATE, game.getId()),
                LobbyDto.from(game));
    }

    @Override
    public void notifyRoundChanged(Game game) {
        messagingTemplate.convertAndSend(
                String.format(ROUND_TOPIC_TEMPLATE, game.getId()),
                RoundStatusDto.from(game));
    }
}
