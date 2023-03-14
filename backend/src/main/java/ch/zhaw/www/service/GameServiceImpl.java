package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class GameServiceImpl implements GameService {
    @Override
    public Game createGame() {
        return new Game(UUID.randomUUID().toString());
    }
}
