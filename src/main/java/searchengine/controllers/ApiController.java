package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StartIndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StartIndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final StartIndexingService startIndexingService;

    public ApiController(StatisticsService statisticsService, StartIndexingService startIndexingService) {
        this.statisticsService = statisticsService;
        this.startIndexingService = startIndexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<StartIndexingResponse> startIndexing() {
        return ResponseEntity.ok(startIndexingService.getStartIndexing());
    }
}
