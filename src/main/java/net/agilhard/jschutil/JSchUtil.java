package net.agilhard.jschutil;

import java.io.File;

import net.agilhard.vfs2.provider.sftp.IdentityRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.agilhard.jsch.IdentityRepository;
import net.agilhard.jsch.JSch;

/**
 * The Class JSchUtil.
 */
public final class JSchUtil {

    /** The use agent. */
    public static boolean useAgent = true;

    /**
     * Path to chmod.
     */
    public static final String CHMOD_PATH = "/bin/chmod";

    /**
     * The Constant AGILHARD_CONFIG_SSH_USE_SSH_AGENT.
     */
    public static final String AGILHARD_CONFIG_SSH_USE_SSH_AGENT = "agilhard.config.ssh.use_ssh_agent";

    /** The Constant PROBLEM_READING_KNOWN_HOSTS. */
    private static final String PROBLEM_READING_KNOWN_HOSTS = "Problem reading known_hosts ";

    /** The jsch. */
    private static JSch jsch;

    /** The ir. */
    private static IdentityRepository ir;

    /** The irfactory. */
    private static IdentityRepositoryFactory irfactory;

    /** The known_hosts. */
    private static String knownHosts;

    /** The cr. */
    private static JSchConfigurationRepository cr = null;

    /**
     * Private Constructor for utility class.
     */
    private JSchUtil() {
        // .
    }

    /**
     * get chmod path.
     *
     * @return the chmod path
     */
    private static String getChmodPath() {
        return CHMOD_PATH;
    }

    /**
     * Gets the identity repository factory.
     *
     * @return the identity repository factory
     */
    public static synchronized IdentityRepositoryFactory getIdentityRepositoryFactory() {
        if (irfactory == null) {
            irfactory = new JSchIdentityRepositoryFactory();
        }
        return irfactory;
    }

    /**
     * Gets the cr.
     *
     * @return the cr
     */
    public static synchronized JSchConfigurationRepository getCR() {
        if (cr == null) {
            cr = new JSchConfigurationRepositoryFS();
        }
        return cr;
    }

    /**
     * Gets the Jsch.
     *
     * @return the j sch
     */
    public static synchronized JSch getJSch() {
        if (jsch == null) {
            jsch = new JSch();

            final JSchLogger logger = new JSchLogger();
            logger.setThreshold(LogLevel.WARN);
            JSch.setLogger(logger);

            final String myKnownHosts = JSchUtil.getKnownHosts();
            if (myKnownHosts != null) {
                try {
                    jsch.setKnownHosts(myKnownHosts);
                }
                catch (final Exception e) {
                    final Logger log = LoggerFactory.getLogger(JSchUtil.class);
                    log.warn(PROBLEM_READING_KNOWN_HOSTS + myKnownHosts, e);
                }
            }

            maybeUseSSHAgent();
        }
        return jsch;
    }

    /**
     * Gets the known hosts.
     *
     * @return the known hosts
     */
    public static synchronized String getKnownHosts() {
        final Logger log = LoggerFactory.getLogger(JSchUtil.class);

        if (knownHosts != null) {
            return knownHosts;
        }
        knownHosts = System.getProperty("agilhard.config.ssh.known_hosts");
        if (knownHosts == null) {
            knownHosts = System.getenv("SSH_KNOWN_HOSTS");
        }
        if (knownHosts != null) {
            final File f = new File(knownHosts);
            try {
                if (!f.exists()) {
                    if (f.createNewFile()) {
                        if (new File(getChmodPath()).exists()) {
                            final String[] cmd = { "chmod", "600", f.getAbsolutePath() };
                            final Process p = Runtime.getRuntime().exec(cmd);
                            p.waitFor();
                        }
                    }
                }
                if (f.exists()) {
                    //LOG.debug("found known host file "+f.getAbsolutePath());
                    return f.getAbsolutePath();
                }
            }
            catch (final Exception e) {
                log.warn(PROBLEM_READING_KNOWN_HOSTS + f.getAbsolutePath() + " : ", e);
            }

        } else {
            final String home = System.getenv("HOME");
            if (home != null) {
                final File f = new File(new File(new File(home), ".ssh"), "known_hosts");
                try {
                    if (f.exists()) {
                        //LOG.debug("found known host file "+f.getAbsolutePath());
                        return f.getAbsolutePath();
                    }
                }
                catch (final Exception e) {
                    log.warn(PROBLEM_READING_KNOWN_HOSTS + f.getAbsolutePath() + " : ", e);
                }
            }
        }
        return null;
    }

    /**
     * Gets the identity repository.
     *
     * @param use
     *            the use
     * @return the identity repository
     */
    public static synchronized IdentityRepository getIdentityRepository(final boolean use) {
        final Logger log = LoggerFactory.getLogger(JSchUtil.class);

        if (use) {
            if (JSchUtil.ir == null) {
                try {
                    JSchUtil.ir = new JSchIdentityRepository();
                }
                catch (final java.lang.NoClassDefFoundError e) {
                    log.debug("getIdentityRepository", e);
                }
                catch (final Exception e) {
                    log.debug("getIdentityRepository", e);
                }
                if (JSchUtil.ir == null) {
                    log.error("IdentityRepository is not available.");
                }
            }
        } else {
            return getJSch().getIdentityRepository();
        }
        return JSchUtil.ir;
    }

    /**
     * Maybe use ssh agent.
     */
    public static void maybeUseSSHAgent() {
        final String s = System.getProperty(AGILHARD_CONFIG_SSH_USE_SSH_AGENT);
        final Logger log = LoggerFactory.getLogger(JSchUtil.class);
        if (s != null && s.equals("true")) {
            JSchUtil.useSSHAgent(true);
            log.debug(AGILHARD_CONFIG_SSH_USE_SSH_AGENT + "=true");
        } else {
            JSchUtil.useSSHAgent(false);
            log.debug(AGILHARD_CONFIG_SSH_USE_SSH_AGENT + "=false");
        }
    }

    /**
     * Use ssh agent.
     *
     * @param use
     *            the use
     */
    public static void useSSHAgent(final boolean use) {
        useAgent = use;
        if (use) {
            System.setProperty(AGILHARD_CONFIG_SSH_USE_SSH_AGENT, "true");
        } else {
            System.clearProperty(AGILHARD_CONFIG_SSH_USE_SSH_AGENT);
        }
        final IdentityRepositoryFactory factory = getIdentityRepositoryFactory();

        JSchUtil.ir = factory.create(getJSch());

        if (jsch == null) {
            getJSch();
        }
    }

    /**
     * Checks if is use agent.
     *
     * @return true, if checks if is use agent
     */
    public static boolean isUseAgent() {
        return useAgent;
    }

}
