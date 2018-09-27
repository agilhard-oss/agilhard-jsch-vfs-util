// CHECKSTYLE:OFF
package net.agilhard.jschutil;

import java.awt.Color;
import java.util.Vector;

/**
 * This class abstracts settings from JCTerm. - font size - pairs of foreground
 * and background color - list of destinations for the prompt.
 *
 * @see net.agilhard.jschutil.JSchConfigurationRepository.ConfigurationRepository
 */
public class JSchConfiguration {

    /** The font size. */
    public static final int FONT_SIZE = 14;

    /** The fg bg. */
    private static final String[] FG_BG = { "#000000:#ffffff", "#ffffff:#000000" };

    /** The destinations. */
    public static final String[] DESTINATIONS = new String[0];

    /** The name. */
    public String name = "default";

    /** The font_size. */
    public int font_size = FONT_SIZE;

    /** The fg_bg. */
    public String[] fg_bg = FG_BG.clone();

    /** The destinations. */
    public String[] destinations = DESTINATIONS;

    /**
     * The use fixed host mapping flag.
     */
    public boolean useFixedHostMapping;

    /**
     * Adds the destination.
     *
     * @param d
     *            the d
     */
    public synchronized void addDestination(final String d) {
        this.destinations = this.add(d, this.destinations);
    }

    /**
     * Adds the fg bg.
     *
     * @param d
     *            the d
     */
    public synchronized void addFgBg(final String d) {
        this.fg_bg = this.add(d, this.fg_bg);
    }

    /**
     * Adds the.
     *
     * @param d
     *            the d
     * @param array
     *            the array
     * @return the string[]
     */
    private String[] add(final String d, final String[] array) {
        int i = 0;
        while (i < array.length) {
            if (d.equals(array[i])) {
                if (i != 0) {
                    System.arraycopy(array, 0, array, 1, i);
                    array[0] = d;
                }
                return array;
            }
            i++;
        }
        final String[] foo = new String[array.length + 1];
        if (array.length > 0) {
            System.arraycopy(array, 0, foo, 1, array.length);
        }
        foo[0] = d;

        //array = foo;
        return foo;
    }

    /**
     * Parses the destinations.
     *
     * @param d
     *            the d
     * @return the string[]
     */
    static String[] parseDestinations(final String d) {
        String[] tmp = d.split(",");
        if (tmp.length == 1 && tmp[0].length() == 0) {
            tmp = new String[0];
        }
        return tmp;
    }

    /**
     * Parses the fg bg.
     *
     * @param fg_bg
     *            the fg_bg
     * @return the string[]
     */
    static String[] parseFgBg(final String fg_bg) {
        final Vector<String> v = new Vector<>();
        final String[] _fg_bg = fg_bg.split(",");
        for (int i = 0; i < _fg_bg.length; i++) {
            final String[] tmp = _fg_bg[i].split(":");
            if (tmp.length != 2) {
                continue;
            }
            final Color fg = toColor(tmp[0]);
            final Color bg = toColor(tmp[1]);
            if (fg != null && bg != null) {
                v.addElement(_fg_bg[i]);
            }
        }
        if (v.size() == 0) {
            return null;
        }
        return v.toArray(new String[0]);
    }

    /**
     * To color.
     *
     * @param o
     *            the o
     * @return the java.awt. color
     */
    static java.awt.Color toColor(final Object o) {
        if (o instanceof String) {
            try {
                return java.awt.Color.decode(((String) o).trim());
            }
            catch (final java.lang.NumberFormatException e) {
                // .
            }
            return java.awt.Color.getColor(((String) o).trim());
        }
        if (o instanceof java.awt.Color) {
            return (java.awt.Color) o;
        }
        return Color.white;
    }

}
// CHECKSTYLE:ON
