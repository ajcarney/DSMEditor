package Util;

import javafx.scene.paint.Color;

/**
 * A class with method to generate random colors that are unique
 * enough to be distinguished
 *
 * @author Aiden Carney
 */
public class RandomColorGenerator {
    private double seed;

    /**
     * Creates a new random color generator with a given starting value
     * @param seed the starting value to generate colors from (the hue)
     */
    public RandomColorGenerator(double seed) {
        this.seed = seed;
    }

    /**
     * generates the next random color based on the golden ratio method. These are usually evenly spaced enough
     * to provide enough contrast to viewers
     * @return the next random color as a javafx color object
     */
    public Color next() {
        seed += 0.618033988749895;  // golden_ratio_conjugate, this is a part of the golden ratio method for generating unique colors
        seed %= 1;
        java.awt.Color hsvColor = java.awt.Color.getHSBColor((float)seed, (float)0.5, (float)0.95);

        double r = hsvColor.getRed() / 255.0;
        double g = hsvColor.getGreen() / 255.0;
        double b = hsvColor.getBlue() / 255.0;

        return Color.color(r, g, b);
    }

}
