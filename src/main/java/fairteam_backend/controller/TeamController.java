package fairteam_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.entity.Team;
import fairteam_backend.service.TeamAnalysisService;
import fairteam_backend.service.TeamService;
import fairteam_backend.strategy.BalancedLevelStrategy;
import fairteam_backend.strategy.PerformanceStrategy;
import fairteam_backend.strategy.SkillBalancedStrategy;
import fairteam_backend.strategy.TeamCandidate;
import fairteam_backend.strategy.TeamFormationStrategy;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamService service;
    private final TeamAnalysisService teamAnalysisService;
    private final BalancedLevelStrategy balancedLevelStrategy;
    private final PerformanceStrategy performanceStrategy;
    private final SkillBalancedStrategy skillBalancedStrategy;

    public TeamController(
            TeamService service,
            TeamAnalysisService teamAnalysisService,
            BalancedLevelStrategy balancedLevelStrategy,
            PerformanceStrategy performanceStrategy,
            SkillBalancedStrategy skillBalancedStrategy) {

        this.service = service;
        this.teamAnalysisService = teamAnalysisService;
        this.balancedLevelStrategy = balancedLevelStrategy;
        this.performanceStrategy = performanceStrategy;
        this.skillBalancedStrategy = skillBalancedStrategy;
    }

    @GetMapping
    public ResponseEntity<List<Team>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Team> create(@RequestBody Team team) {
        return ResponseEntity.ok(service.save(team));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> update(
            @PathVariable Long id,
            @RequestBody Team team) {
        return ResponseEntity.ok(service.update(id, team));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/analyze")
    public ResponseEntity<TeamAnalysisService.TeamAnalysisResult> analyzeTeam(
            @RequestBody List<TeamCandidate> candidates,
            @RequestParam(defaultValue = "balanced") String strategy) {

        TeamFormationStrategy selectedStrategy;

        switch (strategy.toLowerCase()) {
            case "performance":
                selectedStrategy = performanceStrategy;
                break;

            case "skill":
                selectedStrategy = skillBalancedStrategy;
                break;

            case "balanced":
            default:
                selectedStrategy = balancedLevelStrategy;
                break;
        }

        TeamAnalysisService.TeamAnalysisResult result =
                teamAnalysisService.analyzeTeam(
                        candidates,
                        selectedStrategy
                );

        return ResponseEntity.ok(result);
    }
}