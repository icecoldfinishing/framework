package etu.sprint.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DataObject {
    private final Class<?> type;
    private final Map<String, Method> setters = new HashMap<>();

    public DataObject(Class<?> type) {
        this.type = type;
        this.cacheSetters();
    }

    private void cacheSetters() {
        for (Field field : type.getDeclaredFields()) {
            String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            try {
                Method setter = type.getMethod(setterName, field.getType());
                setters.put(field.getName(), setter);
            } catch (NoSuchMethodException e) {
                // Ignorer si le setter n'est pas trouvé
            }
        }
    }

    public Class<?> getType() {
        return type;
    }

    public Map<String, Method> getSetters() {
        return setters;
    }
}
