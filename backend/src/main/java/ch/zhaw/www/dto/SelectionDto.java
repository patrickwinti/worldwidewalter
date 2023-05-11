package ch.zhaw.www.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Represents a selection object containing lists of authors, gaps, and selectors.
 */
@Data
@AllArgsConstructor
public class SelectionDto {
    List<String> authors;
    List<String> gaps;
    List<String> selectors;
    boolean sphinxResponse;
}
