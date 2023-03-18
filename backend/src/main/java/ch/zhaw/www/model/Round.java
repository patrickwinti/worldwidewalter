package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Model class with round information
 */
@AllArgsConstructor
@Data
public class Round {
    @NotNull
    String id;
    @NotNull
    String prompt;
}
