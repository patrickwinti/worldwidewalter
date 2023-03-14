package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Model class with turn information
 */
@AllArgsConstructor
@Data
public class Round {
    @NotNull
    String id;
}
