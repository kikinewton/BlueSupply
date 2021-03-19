package com.logistics.supply.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class MainController {
    @RequestMapping(value = {"/", "/index.html"})
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("../static/index");
    }
}
