package com.gempukku.libgdx.graph;

public class SimpleNumberFormatter {
    public static String format(float value) {
        StringBuilder result = new StringBuilder();
        result.append((int) value);
        value -= ((int) value);
        result.append(".");
        if (value < 0)
            value = -value;
        for (int i = 0; i < 7; i++) {
            if (i > 0 && value == 0f)
                break;
            value *= 10f;
            result.append((int) value);
            value -= ((int) value);
        }
        return result.toString();
    }
}
