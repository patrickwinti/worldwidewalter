package ch.zhaw.www.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SelectionDto {
    List<String> authors;
    List<String> gaps;
    List<String> selectors;
    boolean sphinxResponse;
}
