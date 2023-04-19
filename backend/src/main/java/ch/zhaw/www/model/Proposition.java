package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for the proposition sent by players
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Proposition {
    @NotNull
    private final String id;
    private final List<String> gaps;
    
    private final List<String> playerIds = new ArrayList<>();
    
    public void submittedBy(final String playerId) {
        playerIds.add(playerId);
    }
}

