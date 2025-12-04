package etu.sprint.handler;

import etu.sprint.model.ControllerMethod;
import etu.sprint.model.ModelView;
import etu.sprint.util.TypeConverter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;


public class HandlerAdapter {

    public void handle(HttpServletRequest request, HttpServletResponse response, ControllerMethod controllerMethod,
                        Map<String, String> pathVariables) throws ServletException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        try {
            Object controllerInstance = controllerMethod.controllerClass.getDeclaredConstructor().newInstance();
            Method method = controllerMethod.method;
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];

            // Combine path variables and request parameters (from query string or form body)
            Map<String, String> allParameters = new HashMap<>(pathVariables);
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) {
                    allParameters.putIfAbsent(key, values[0]); // Prioritize path variables if name collision
                }
            });

            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                String paramName;
                String paramValue = null;

                // Check for @RequestParameter annotation
                if (parameter.isAnnotationPresent(etu.sprint.annotation.RequestParameter.class)) {
                    etu.sprint.annotation.RequestParameter rp = parameter.getAnnotation(etu.sprint.annotation.RequestParameter.class);
                    paramName = rp.value();
                    paramValue = pathVariables.get(paramName); // Try to get from path variables first
                    if (paramValue == null) { // If not found in path variables, try request parameters
                        paramValue = allParameters.get(paramName);
                    }
                    args[i] = TypeConverter.convertStringValue(paramValue, parameter.getType());
                } else if (parameter.getType() == Map.class) {
                    // Handle Map<String, Object> parameter
                    Map<String, Object> dataMap = new HashMap<>();
                    // Add path variables
                    pathVariables.forEach(dataMap::put);
                    // Add request parameters
                    request.getParameterMap().forEach((key, values) -> {
                        if (values.length == 1) {
                            dataMap.put(key, values[0]);
                        } else {
                            dataMap.put(key, Arrays.asList(values)); // Handle multiple values for checkboxes
                        }
                    });
                    args[i] = dataMap;
                } else {
                    paramName = parameter.getName();
                    paramValue = pathVariables.get(paramName); // Try to get from path variables first by name
                    if (paramValue == null) { // If not found in path variables, try request parameters
                        paramValue = allParameters.get(paramName);
                    }
                    args[i] = TypeConverter.convertStringValue(paramValue, parameter.getType());
                }
            }

            Object returnValue = method.invoke(controllerInstance, args);

            if (returnValue instanceof String) {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println(returnValue);
            } else if (returnValue instanceof ModelView) {
                ModelView mv = (ModelView) returnValue;

                for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }

                RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getView());
                dispatcher.forward(request, response);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ServletException("Erreur lors de l'execution de la methode du controleur", e);
        }
    }
}