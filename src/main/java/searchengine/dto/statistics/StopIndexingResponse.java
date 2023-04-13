package searchengine.dto.statistics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class StopIndexingResponse {
    private boolean result;
    private String error;
}
