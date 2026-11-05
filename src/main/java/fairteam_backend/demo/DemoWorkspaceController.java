package fairteam_backend.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Loads fictional courses into the signed-in teacher's account so the whole app can be explored. */
@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoWorkspaceController {

    private final DemoWorkspaceService service;

    public DemoWorkspaceController(DemoWorkspaceService service) { this.service = service; }

    @PostMapping("/workspace")
    public DemoWorkspaceService.DemoSummary load() { return service.load(); }
}
