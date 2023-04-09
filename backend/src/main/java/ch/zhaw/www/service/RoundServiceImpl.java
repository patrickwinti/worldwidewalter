package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

@Service
class RoundServiceImpl implements RoundService {
    private static final int ONE_ROUND_LEFT = 1;
    
    @Nullable
    @Override
    public Player selectSphinx(Game game) {
        var entries = game.getSphinxCandidates();
        if (entries.isEmpty()) return null;
        var player = Collections.min(entries, Comparator.comparingInt(Map.Entry::getValue));
        entries.remove(player);
        if (player.getValue() > ONE_ROUND_LEFT) {
            entries.add(Map.entry(player.getKey(), player.getValue() - 1));
        }
        game.setSphinxCandidates(entries);
        return player.getKey();
    }
    
}
