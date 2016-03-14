package resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
public class ResourceController {

    @RequestMapping("/")
    public Principal getPrincipal(Principal principal) {
        return principal;
    }

}
