package ch.zhaw.www.dto;

import lombok.Data;

/**
 * Represents a player's ranking information.
 */
@Data
public class RankingDto {
    private final String playerName;
    private final int points;
}
