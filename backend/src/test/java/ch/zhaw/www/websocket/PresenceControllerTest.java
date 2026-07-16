package ch.zhaw.www.websocket;

import ch.zhaw.www.service.GameService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PresenceControllerTest {

    private final PlayerSessionRegistry registry = mock(PlayerSessionRegistry.class);
    private final GameService gameService = mock(GameService.class);
    private final PresenceController controller = new PresenceController(registry, gameService);

    @Test
    void registerPresenceRegistersSessionAndMarksPlayerConnected() {
        controller.registerPresence("session-1", "WXYZ", "player-1");

        verify(registry).register("session-1", "WXYZ", "player-1");
        verify(gameService).connectPlayer("WXYZ", "player-1");
    }
}
