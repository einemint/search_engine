package searchengine.dto.statistics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class StartIndexingResponse {
    private boolean result;
    private String error;
}
