package com.example.controllers;

import etu.sprint.framework.annotation.Controller;
import etu.sprint.framework.annotation.Url;

@Controller
public class TestController {

    @Url("/hello")
    public String sayHello() {
        // In a future sprint, this method would return a ModelView
        System.out.println("The sayHello() method is called!");
        return "Hello from TestController!";
    }

    @Url("/test-url")
    public String anotherTest() {
        System.out.println("The anotherTest() method is called!");
        return "Another test from TestController!";
    }
}
