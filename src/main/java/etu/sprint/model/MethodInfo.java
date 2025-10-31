package etu.sprint.model;

import java.lang.reflect.Method;

public class MethodInfo {
    private final String signature;

    public MethodInfo(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getSimpleName()).append(" ");
        sb.append(method.getName()).append("(");
        Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getSimpleName());
            if (i < params.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        this.signature = sb.toString();
    }

    public String getSignature() {
        return signature;
    }
}
