package etu.sprint.controller;

import etu.sprint.annotation.AnnotationController;
import java.io.IOException;
import jakarta.servlet.http.*;

@AnnotationController("/")
public class HelloController {
    public void handle(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().println("<h1>Hello World from Annotation Controller!</h1>");
    }
}
