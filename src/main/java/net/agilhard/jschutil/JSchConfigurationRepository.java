package net.agilhard.jschutil;

/**
 * This interface abstracts where the configuration is stored to and retrived
 * from.
 *
 * @see net.agilhard.jschutil.JSchConfiguration.Configuration
 * @see net.agilhard.jschutil.jsch.JSchConfigurationRepositoryFS.ConfigurationRepositoryFS
 */
public interface JSchConfigurationRepository {

    /**
     * Load.
     *
     * @param name
     *            the name
     * @return the j sch configuration
     */
    JSchConfiguration load(String name);

    /**
     * Save.
     *
     * @param conf
     *            the conf
     */
    void save(JSchConfiguration conf);
}
