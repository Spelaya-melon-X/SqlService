package com.codzilla.sqlservice.SqlService.Conroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UiController {

    @GetMapping("/")
    public String index() { return "index"; }


    @GetMapping(value = "/{path:[^\\.]*}", produces = "text/html")
    public String spa(@PathVariable String path) {
        if (path.startsWith("sqlservice")) return null;
        return "index";
    }
}