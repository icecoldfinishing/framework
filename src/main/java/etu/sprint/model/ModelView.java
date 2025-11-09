package etu.sprint.model;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    private String view;
    private final Map<String, Object> data = new HashMap<>();

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void addItem(String key, Object value) {
        this.data.put(key, value);
    }
}
