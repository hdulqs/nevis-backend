package dwfe.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/dwfe/v1", produces = "application/json; charset=utf-8")
public class DwfeControllerV1
{

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/test")
  public String test()
  {
    return "{\"success\":true}";
  }
}
