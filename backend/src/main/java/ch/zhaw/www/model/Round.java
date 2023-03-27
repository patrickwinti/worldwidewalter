package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Model class with round information
 */
@RequiredArgsConstructor
@Data
public class Round {
    Map<Player, Proposition> propositions;
    Map<Player, String> selections;

    @NotNull
    final String id;
    @NotNull
    final Prompt prompt;


}
