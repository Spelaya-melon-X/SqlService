package com.codzilla.sqlservice.SqlService.Conroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UiController {

    @GetMapping("/")
    public String index() { return "index"; }

    // Все остальные неизвестные пути тоже на index (SPA-режим)
    // НО только GET и только не /sqlservice/**
    @GetMapping(value = "/{path:[^\\.]*}", produces = "text/html")
    public String spa(@PathVariable String path) {
        // не перехватываем API пути
        if (path.startsWith("sqlservice")) return null;
        return "index";
    }
}