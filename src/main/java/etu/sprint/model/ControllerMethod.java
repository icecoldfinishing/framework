package etu.sprint.model;

import java.lang.reflect.Method;

public class ControllerMethod {
    public final Class<?> controllerClass;
    public final Method method;

    public ControllerMethod(Class<?> controllerClass, Method method) {
        this.controllerClass = controllerClass;
        this.method = method;
    }
}
