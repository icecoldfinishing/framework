package etu.sprint.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonConverter {

    public static String toJson(Object object) {
        if (object == null) {
            return "null";
        }
        if (object instanceof String) {
            return "\"" + escapeString((String) object) + "\"";
        }
        if (object instanceof Number || object instanceof Boolean) {
            return object.toString();
        }
        if (object.getClass().isArray() || object instanceof Collection) {
            return collectionToJson(object);
        }
        if (object instanceof Map) {
            return mapToJson((Map<?, ?>) object);
        }
        return objectToJson(object);
    }

    private static String objectToJson(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                sb.append("\"").append(field.getName()).append("\":").append(toJson(value));
                if (i < fields.length - 1) {
                    sb.append(",");
                }
            } catch (IllegalAccessException e) {
                // Ignore field if not accessible
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String collectionToJson(Object object) {
        Collection<?> collection = (object.getClass().isArray())
                ? java.util.Arrays.asList((Object[]) object)
                : (Collection<?>) object;
        return "[" + collection.stream().map(JsonConverter::toJson).collect(Collectors.joining(",")) + "]";
    }

    private static String mapToJson(Map<?, ?> map) {
        return "{"
                + map.entrySet().stream()
                .map(entry -> "\"" + escapeString(entry.getKey().toString()) + "\":" + toJson(entry.getValue()))
                .collect(Collectors.joining(","))
                + "}";
    }

    private static String escapeString(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
