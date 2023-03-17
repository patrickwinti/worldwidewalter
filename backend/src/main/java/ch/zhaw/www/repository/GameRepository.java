package ch.zhaw.www.repository;

import ch.zhaw.www.model.Game;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository {
    Game getGame(@NotNull String gameId);
}
