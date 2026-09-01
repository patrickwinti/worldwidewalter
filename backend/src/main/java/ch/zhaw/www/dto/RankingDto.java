package ch.zhaw.www.dto;

import lombok.Data;

/**
 * Represents a player's ranking information: the running total and what the round just played
 * added to it.
 */
@Data
public class RankingDto {
    private final String playerName;
    private final int points;
    private final int roundPoints;
}
