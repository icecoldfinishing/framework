package com.example.controllers;

import etu.sprint.framework.annotation.Controller;
import etu.sprint.framework.annotation.Url;

@Controller
public class TestController {

    @Url("/hello")
    public void sayHello() {
        // In a future sprint, this method would return a ModelView
        System.out.println("The sayHello() method is called!");
    }

    @Url("/test-url")
    public void anotherTest() {
        System.out.println("The anotherTest() method is called!");
    }
}
