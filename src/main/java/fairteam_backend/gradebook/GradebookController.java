package fairteam_backend.gradebook;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}")
@CrossOrigin(origins = "*")
public class GradebookController {
    private final GradeSheetService sheets;
    private final QuickRosterService roster;

    public GradebookController(GradeSheetService sheets, QuickRosterService roster) {
        this.sheets = sheets;
        this.roster = roster;
    }

    public record SaveRequest(String period, List<GradeSheet.Change> changes) { }

    public record QuickAdd(String text) { }

    @GetMapping("/grade-sheet")
    public GradeSheet sheet(@PathVariable long courseId, @RequestParam(required = false) String period) {
        return sheets.sheet(courseId, period);
    }

    @PutMapping("/grade-sheet")
    public GradeSheet.SaveResult save(@PathVariable long courseId, @RequestBody SaveRequest body) {
        return sheets.save(courseId, body.period(), body.changes());
    }

    @PostMapping("/students/quick")
    public QuickRosterService.Result quickAdd(@PathVariable long courseId, @RequestBody QuickAdd body) {
        return roster.add(courseId, body.text());
    }
}
