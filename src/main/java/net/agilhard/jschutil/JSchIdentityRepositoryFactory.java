package net.agilhard.jschutil;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.agilhard.jsch.IdentityRepository;
import net.agilhard.jsch.JSch;
import net.agilhard.jsch.JSchException;
import net.agilhard.vfs2.provider.sftp.IdentityInfo;
import net.agilhard.vfs2.provider.sftp.IdentityRepositoryFactory;

/**
 * A factory for creating IdentityRepository objects.
 */
public class JSchIdentityRepositoryFactory implements IdentityRepositoryFactory {


    /** The Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(IdentityRepositoryFactory.class);

    private static HashMap<String, String> idFiles = new HashMap<>();

    public static void addIdFile(final String name, final String pass) {
        idFiles.put(name, pass);
    }

    static byte[] str2byte(final String str, final String encoding) {
        if (str == null) {
            return null;
        }
        try {
            return str.getBytes(encoding);
        }
        catch (final java.io.UnsupportedEncodingException e) {
            return str.getBytes();
        }
    }

    static byte[] str2byte(final String str) {
        return str2byte(str, "UTF-8");
    }

    public static IdentityInfo[] getIdentityInfo() {

        LOG.info("getIdentityInfo()");

        final Set<String> s = idFiles.keySet();
        if (s.isEmpty()) {
            return null;
        }
        final IdentityInfo[] info = new IdentityInfo[s.size()];

        final int i = 0;
        for (final String f : s) {

            final File idFile = new File(new File(new File(System.getProperty("user.home")), ".ssh"), f);
            final String pass = JSchIdentityRepositoryFactory.idFiles.get(f);
            info[i] = new IdentityInfo(idFile, str2byte(pass));
            LOG.info("getIdentityInfo() file=" + idFile.getAbsolutePath() + " pass=" + pass);
        }

        return info;
    }

    /**
     * Create IdentityRepository.
     *
     * @param jsch
     *            a Jsch
     * @see net.agilhard.jsch.IdentityRepository#create(net.agilhard.jsch.JSch)
     * @return a IdentityRepository
     */
    @Override
    public IdentityRepository create(final JSch jsch) {
        IdentityRepository ir = null;

        if (JSchUtil.isUseAgent()) {
            LOG.info("IdentityRepository.create useAgent=true");
            ir = JSchUtil.getIdentityRepository(true);

        } else {
            LOG.info("IdentityRepository.create useAgent=false");
            if (JSchIdentityRepositoryFactory.idFiles.keySet().isEmpty()) {
                ir = JSchUtil.getIdentityRepository(false);
                ir = jsch.getIdentityRepository();

                final File idFile = new File(new File(new File(System.getProperty("user.home")), ".ssh"), "id_rsa");
                final String nopass = null;
                try {
                    jsch.addIdentity(idFile.getAbsolutePath(), nopass);
                }
                catch (final JSchException e) {
                    LOG.error("can not add identity");
                }
            }
        }

        for (final String f : JSchIdentityRepositoryFactory.idFiles.keySet()) {
            LOG.info("IdentityRepository.create add idFile=" + f);

            final File idFile = new File(new File(new File(System.getProperty("user.home")), ".ssh"), f);
            final String pass = JSchIdentityRepositoryFactory.idFiles.get(f);
            try {
                jsch.addIdentity(idFile.getAbsolutePath(), pass);
            }
            catch (final JSchException e) {
                LOG.error("can not add identity");
            }
        }

        jsch.setIdentityRepository(ir);
        ir = jsch.getIdentityRepository();

        if (ir != null) {
            LOG.info("got IdentityRepository class=" + ir.getClass().getName() + " status=" + ir.getStatus()
                + " (0=UNAVAILABLE, 1=NOTRUNNING, 2=RUNNING)");
            JSch.setConfig("PreferredAuthentications", "publickey");
        } else {
            LOG.info("could not get IdentityRepository");
        }

        return ir;
    }
}
