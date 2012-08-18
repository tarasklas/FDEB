package org.gephi.preview.types;

/**
 *
 */
public enum RendererModes {

    SIMPLE("Simple renderer"),
    GRADIENT("Gradient renderer"),
    GRADIENT_COMPLEX("Gradient complex renderer");
    private String name;

    private RendererModes(String name) {
        this.name = name;
    }

    public static RendererModes fromString(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        for (RendererModes mode : RendererModes.values()) {
            if (mode.name.equals(string)) {
                return mode;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}