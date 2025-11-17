package etu.sprint.util;

import java.util.HashMap;
import java.util.Map;

public class UrlUtil {

    public static Map<String, String> extractPathVariables(String requestPath, String routePattern) {
        Map<String, String> pathVariables = new HashMap<>();
        String[] requestParts = requestPath.split("/");
        String[] routeParts = routePattern.split("/");

        if (requestParts.length != routeParts.length) {
            return null;
        }

        for (int i = 0; i < routeParts.length; i++) {
            String routePart = routeParts[i];
            String requestPart = requestParts[i];

            if (routePart.startsWith("{") && routePart.endsWith("}")) {
                String varName = routePart.substring(1, routePart.length() - 1);
                pathVariables.put(varName, requestPart);
            } else if (!routePart.equals(requestPart)) {
                return null;
            }
        }
        return pathVariables;
    }
}
