package com.lighthouse;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    @ApiOperation(
            value = "기본 홈 페이지"
    )
    public String home() {
        return "index";
    }
}
