// CHECKSTYLE:OFF
package net.agilhard.jschutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will save the configuration into ~/.ssh/agilhard_jsch.properties file in property file format.
 *
 * @see net.agilhard.jschutil.JSchConfiguration
 * @see net.agilhard.jschutil.JSchConfigurationRepository
 */
public class JSchConfigurationRepositoryFS implements JSchConfigurationRepository {

    /** The Constant LOG. */
    private final Logger log = LoggerFactory.getLogger(JSchConfigurationRepositoryFS.class);

    /**
     * Property Key prefix.
     */
    private static final String AGILHARD_JSCH = "agilhard.jsch.";

    /** The ssh_home. */
    private final File ssh_home = new File(System.getProperty("user.home"), ".ssh");

    /** The agilhard_jsch_prop. */
    private final File agilhard_jsch_prop = new File(this.ssh_home, "agilhard_jsch.properties");

    /**
     * Load Configuration.
     *
     * @param name
     *            the name of the Configuration
     * @see net.agilhard.jschutil.JSchConfigurationRepository#load(java.lang.String)
     */
    @Override
    public JSchConfiguration load(final String name) {
        this.log.error("LOAD name=" + name);

        final JSchConfiguration conf = new JSchConfiguration();
        conf.name = name;

        final java.util.Properties prop = new java.util.Properties();
        InputStream in = null;

        try {
            in = new FileInputStream(this.agilhard_jsch_prop);
            prop.load(in);

            String key = AGILHARD_JSCH + name + ".font_size";
            if (prop.get(key) != null) {
                try {
                    conf.font_size = Integer.parseInt((String) prop.get(key));
                }
                catch (final Exception ee) {
                    // ignore it because of loading incompatible data.
                }
            }

            try {
                key = AGILHARD_JSCH + name + ".fg_bg";
                if (prop.get(key) != null) {
                    conf.fg_bg = ((String) prop.get(key)).split(",");
                }
            }
            catch (final Exception ee) {
                // ignore it because of loading incompatible data.
            }

            try {
                key = AGILHARD_JSCH + name + ".destination";
                if (prop.get(key) != null) {
                    conf.destinations = ((String) prop.get(key)).split(",");
                }
            }
            catch (final Exception ee) {
                // ignore it because of loading incompatible data.
            }

            try {
                key = AGILHARD_JSCH + name + ".use_fixed_host_mapping";
                if (prop.get(key) != null) {
                    conf.useFixedHostMapping =
                        (String) prop.get(key) != null ? ((String) prop.get(key)).equals("true") : false;
                    this.log.info("use_fixed_host_mapping=" + conf.useFixedHostMapping);
                }
            }
            catch (final Exception ee) {
                this.log.error("Exception", ee);
            }

        }
        catch (final IOException e) {
            this.log.error("IOException", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (final IOException e) {
                    this.log.warn("I/O Error", e);
                }
            }
        }

        return conf;
    }

    /**
     * Save Configuration.
     *
     * @see net.agilhard.jschutil.JSchConfigurationRepository#save(net.agilhard.jschutil.JSchConfiguration)
     */
    @Override
    public void save(final JSchConfiguration conf) {
        final java.util.Properties prop = new java.util.Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(this.agilhard_jsch_prop);
            prop.load(in);
            in.close();
        }
        catch (final IOException e) {
            this.log.warn("I/O Error", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (final IOException e) {
                    this.log.warn("I/O Error", e);
                }
            }
        }

        final String name = conf.name;

        prop.setProperty(AGILHARD_JSCH + name + ".destination", this.join(conf.destinations));

        prop.setProperty(AGILHARD_JSCH + name + ".font_size", Integer.toString(conf.font_size));

        prop.setProperty(AGILHARD_JSCH + name + ".fg_bg", this.join(conf.fg_bg));

        prop.setProperty(AGILHARD_JSCH + name + ".use_fixed_host_mapping",
            Boolean.valueOf(conf.useFixedHostMapping).toString());

        OutputStream out = null;
        try {
            out = new FileOutputStream(this.agilhard_jsch_prop);
            prop.store(out, "");
            out.close();
        }
        catch (final IOException e) {
            this.log.error("failed to save file");
        } finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (final IOException e) {
                    this.log.warn("I/O Error", e);
                }
            }
        }

    }

    /**
     * Join.
     *
     * @param array
     *            the array
     * @return the string
     */
    String join(final String[] array) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            builder.append(array[i]);
            if (i + 1 < array.length) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
}
// CHECKSTYLE:ON