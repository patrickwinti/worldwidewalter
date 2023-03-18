package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * Model class with round information
 */
@AllArgsConstructor
@Data
public class Round {
    Map<Player, String> propositions;
    Map<Player, String> selections;

    @NotNull
    final String id;
    @NotNull
    String prompt;


}
