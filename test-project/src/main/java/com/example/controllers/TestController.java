package com.example.controllers;

import etu.sprint.framework.ModelView;
import etu.sprint.framework.annotation.Controller;
import etu.sprint.framework.annotation.Url;

@Controller
public class TestController {

    @Url("/hello")
    public ModelView sayHello() {
        ModelView modelView = new ModelView("hello.jsp");
        modelView.addObject("message", "Hello from TestController!");
        return modelView;
    }

    @Url("/test-url")
    public ModelView anotherTest() {
        ModelView modelView = new ModelView("test.jsp");
        modelView.addObject("message", "Another test from TestController!");
        return modelView;
    }

    @Url("/")
    public ModelView home() {
        ModelView modelView = new ModelView("home.jsp");
        modelView.addObject("message", "This is the home page.");
        return modelView;
    }
}
