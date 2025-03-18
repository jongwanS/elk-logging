package com.jw.study.elklogging.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ApiController {
    @RequestMapping("/")
    public String main() {
        log.info("123");
        return "Hello World!";
    }
}
