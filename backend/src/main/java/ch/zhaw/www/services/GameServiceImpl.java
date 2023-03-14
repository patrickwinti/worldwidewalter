package ch.zhaw.www.services;

import ch.zhaw.www.models.Game;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class GameServiceImpl implements GameService {
    @Override
    public Game createGame() {
        return new Game(UUID.randomUUID().toString());
    }
}
