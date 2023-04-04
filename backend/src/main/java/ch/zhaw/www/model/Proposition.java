package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;

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
    @Id
    @NotNull
    private final String id;
    private final List<String> gaps;
    private final List<Proposition> duplicates = new ArrayList<>();
}

