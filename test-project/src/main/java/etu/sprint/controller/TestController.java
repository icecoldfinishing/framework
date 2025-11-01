package etu.sprint.controller;

import etu.sprint.annotation.AnnotationController;
import etu.sprint.annotation.GetMethode;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@AnnotationController("/test")
public class TestController {

    @GetMethode("/hello")
    public void sayHello(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().println("<h1>Hello from annotated method!</h1>");
    }

    @GetMethode("/bye")
    public void sayBye(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().println("<h1>Goodbye from annotated method!</h1>");
    }
}
