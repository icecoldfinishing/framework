package etu.sprint.handler;

import etu.sprint.model.ControllerMethod;
import etu.sprint.model.ModelView;
import etu.sprint.util.TypeConverter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import etu.sprint.model.FileUpload;
import etu.sprint.model.Session;

import etu.sprint.annotation.RestAPI;
import etu.sprint.annotation.Authorized;
import etu.sprint.model.JsonResponse;
import etu.sprint.util.JsonConverter;
import etu.sprint.util.AuthorizationManager;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HandlerAdapter {
    private AuthorizationManager authorizationManager;
    public HandlerAdapter() {
        // AuthorizationManager sera initialisé avec ServletContext si nécessaire
    }

    public HandlerAdapter(ServletContext servletContext) {
        if (servletContext != null) {
            this.authorizationManager = new AuthorizationManager(servletContext);
        }
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, ControllerMethod controllerMethod,
                        Map<String, String> pathVariables) throws ServletException, IOException {
        try {
            Method method = controllerMethod.method;
            // Vérifier l'autorisation avant d'exécuter la méthode
            if (method.isAnnotationPresent(Authorized.class)) {
                if (authorizationManager == null) {
                    // Initialiser avec le ServletContext de la requête si pas encore fait
                    authorizationManager = new AuthorizationManager(request.getServletContext());
                }
                Authorized authorizedAnnotation = method.getAnnotation(Authorized.class);
                if (!authorizationManager.isAuthorized(request, response, authorizedAnnotation)) {
                    // La réponse d'erreur a déjà été envoyée par AuthorizationManager
                    return;
                }
            }
            Object controllerInstance = controllerMethod.controllerClass.getDeclaredConstructor().newInstance();
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];
            Map<String, String[]> requestParams = request.getParameterMap();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.getType() == Session.class || parameter.isAnnotationPresent(etu.sprint.annotation.Session.class)) {
                    handleSessionParameter(i, args, request);
                } else if (parameter.isAnnotationPresent(etu.sprint.annotation.RequestParameter.class)) {
                    handleRequestParameter(parameter, i, args, pathVariables, requestParams);
                } else if (parameter.getType() == Map.class) {
                    handleMapParameter(i, args, pathVariables, requestParams);
                } else if (parameter.getType() == FileUpload.class) {
                     handleFileUploadParameter(parameter, i, args, request);
                } else if (parameter.getType() == HttpServletRequest.class) {
                     args[i] = request;
                } else if (isComplexType(parameter.getType())) {
                    args[i] = handleComplexType(parameter.getType(), requestParams);
                } else {
                    handleDefaultParameter(parameter, i, args, pathVariables, requestParams);
                }
            }



            Object returnValue = method.invoke(controllerInstance, args);

            if (method.isAnnotationPresent(RestAPI.class)) {
                JsonResponse jsonResponse = new JsonResponse("success", 200, "OK", returnValue);
                String json = JsonConverter.toJson(jsonResponse);

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                out.print(json);
                out.flush();
            } else {
                if (returnValue instanceof String) {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println(returnValue);
                } else if (returnValue instanceof ModelView) {
                    ModelView mv = (ModelView) returnValue;
                    mv.getData().forEach(request::setAttribute);
                    RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getView());
                    dispatcher.forward(request, response);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ServletException("Erreur lors de l'execution de la methode du controleur", e);
        }
    }



    private void handleRequestParameter(Parameter parameter, int index, Object[] args,
                                        Map<String, String> pathVariables, Map<String, String[]> requestParams) {
        etu.sprint.annotation.RequestParameter rp = parameter.getAnnotation(etu.sprint.annotation.RequestParameter.class);
        String paramName = rp.value();
        String paramValue = pathVariables.getOrDefault(paramName, requestParams.containsKey(paramName) ? requestParams.get(paramName)[0] : null);
        args[index] = TypeConverter.convertStringValue(paramValue, parameter.getType());
    }


    private void handleMapParameter(int index, Object[] args,
                                    Map<String, String> pathVariables, Map<String, String[]> requestParams) {
        Map<String, Object> dataMap = new HashMap<>();
        pathVariables.forEach(dataMap::put);
        requestParams.forEach((key, values) -> {
            if (values.length == 1) {
                dataMap.put(key, values[0]);
            } else {
                dataMap.put(key, Arrays.asList(values));
            }
        });
        args[index] = dataMap;
    }



    private Object handleComplexType(Class<?> type, Map<String, String[]> requestParams)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object instance = type.getDeclaredConstructor().newInstance();
        for (Field field : type.getDeclaredFields()) {
            String fieldName = field.getName();
            String[] paramValues = requestParams.get(fieldName);
            if (paramValues != null) {
                try {
                    Method setter = type.getMethod("set" + capitalize(fieldName), field.getType());
                    if (field.getType() == List.class) {
                        setter.invoke(instance, Arrays.asList(paramValues));
                    } else if (paramValues.length > 0) {
                        Object convertedValue = TypeConverter.convertStringValue(paramValues[0], field.getType());
                        setter.invoke(instance, convertedValue);
                    }
                } catch (NoSuchMethodException e) {
                    // Ignorer si le setter n'existe pas
                }
            }
        }
        return instance;
    }



    private void handleDefaultParameter(Parameter parameter, int index, Object[] args,
                                        Map<String, String> pathVariables, Map<String, String[]> requestParams) {
        String paramName = parameter.getName();
        String paramValue = pathVariables.getOrDefault(paramName, requestParams.containsKey(paramName) ? requestParams.get(paramName)[0] : null);
        args[index] = TypeConverter.convertStringValue(paramValue, parameter.getType());
    }



    private boolean isComplexType(Class<?> type) {
        return !type.isPrimitive() && type != String.class && !Number.class.isAssignableFrom(type) && type != Boolean.class && !type.getPackage().getName().startsWith("java.lang");
    }



    private void handleFileUploadParameter(Parameter parameter, int index, Object[] args, HttpServletRequest request) throws ServletException, IOException {
        String paramName = parameter.getName();
        etu.sprint.annotation.RequestParameter rp = parameter.getAnnotation(etu.sprint.annotation.RequestParameter.class);
        if (rp != null) {
            paramName = rp.value();
        }
        try {
            Part part = request.getPart(paramName);
            if (part != null) {
                FileUpload fileUpload = new FileUpload();
                fileUpload.setFileName(part.getSubmittedFileName());
                fileUpload.setBytes(part.getInputStream().readAllBytes());
                fileUpload.setContentType(part.getContentType());
                args[index] = fileUpload;
            }
        } catch (Exception e) {
             // Handle exceptions or ignore if part is missing/invalid
             // e.printStackTrace();
        }
    }

    private void handleSessionParameter(int index, Object[] args, HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        Session session = new Session(httpSession);
        args[index] = session;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

}
