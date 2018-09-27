package net.agilhard.vfsutil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import net.agilhard.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.agilhard.jschutil.JSchUtil;
import net.agilhard.jschutil.JSchIdentityRepositoryFactory;

/**
 * The Class VFSTools.
 */
public final class VFSTools {

    /** The Constant OS_NAME. */
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    /** The Constant PROTO_PREFIX. */
    private static final String PROTO_PREFIX = "://";

    /** The Constant FILE_PREFIX. */
    private static final String FILE_PREFIX = OS_NAME.startsWith("windows") ? "file:///" : "file://";

    /** The Constant FILE_PREFIX_LEN. */
    private static final int FILE_PREFIX_LEN = FILE_PREFIX.length();

    /** The file system manager. */
    private FileSystemManager fileSystemManager;

    /** The opts. */
    private FileSystemOptions opts = new FileSystemOptions();

    /** The a lock. */
    private final ReadWriteLock aLock = new ReentrantReadWriteLock(true);

    private static VFSTools theOne;

    private VFSTools() {
        this.init();
    }

    public static synchronized VFSTools getInstance() {
        if (theOne == null) {
            theOne = new VFSTools();
        }
        return theOne;
    }

    public FileSystemOptions getOpts() {
        return this.opts;
    }

    private void init() {
        final Logger log = LoggerFactory.getLogger(VFSTools.class);
        log.info("VFSTools init()");

        this.opts = new FileSystemOptions();
        // final String knownHosts = JSchUtil.getKnownHosts();
        try {
            final SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();

            // host key checking only makes problems disable for now
            builder.setStrictHostKeyChecking(this.opts, "no");

            builder.setCompression(this.opts, "zlib,none");
            builder.setUserDirIsRoot(this.opts, false);
            builder.setRootURI(this.opts, "/");
            builder.setIdentityRepositoryFactory(this.opts, JSchUtil.getIdentityRepositoryFactory());

            builder.setPreferredAuthentications(this.opts, "publickey");
            builder.setIdentityInfo(this.opts, JSchIdentityRepositoryFactory.getIdentityInfo());
        }
        catch (final FileSystemException e) {
            log.warn("error during VFSTools initialization", e);
        }
    }

    /**
     * Returns the global filesystem manager.
     *
     * @return the global filesystem manager
     * @throws FileSystemException
     *             the file system exception
     */
    public FileSystemManager getFileSystemManager() throws FileSystemException {
        this.aLock.readLock().lock();
        if (this.fileSystemManager == null) {
            this.fileSystemManager = AccessController.doPrivileged(new PrivilegedAction<FileSystemManager>() {

                @Override
                public FileSystemManager run() {
                    StandardFileSystemManager fm = null;
                    final Logger log = LoggerFactory.getLogger(VFSTools.class);
                    try {
                        fm = new StandardFileSystemManager();
                        fm.setClassLoader(fm.getClass().getClassLoader());

                        fm.setCacheStrategy(CacheStrategy.MANUAL);
                        fm.init();
                    }
                    catch (final Exception e) {

                        log.debug("Exception while creating FileSystemManager", e);
                    }

                    if (fm.hasProvider("smb")) {
                        log.info("VFS Provider for smb found");
                    } else {
                        log.error("VFS Provider for smb missing");
                    }
                    try {
                        final File f = new java.io.File(".").getCanonicalFile();
                        final FileObject fileObject = fm.resolveFile(f.toURI().toString(), VFSTools.this.opts);

                        fm.setBaseFile(fileObject);
                    }
                    catch (final IOException ioe) {
                        log.error("can not set baseFile");
                    }
                    return fm;
                }
            });
        }
        this.aLock.readLock().unlock();
        return this.fileSystemManager;
    }

    /**
     * Sets the global filesystem manager.
     *
     * @param aFileSystemManager
     *            the global filesystem manager
     */
    public void setFileSystemManager(final FileSystemManager aFileSystemManager) {
        this.aLock.writeLock().lock();
        try {
            this.fileSystemManager = aFileSystemManager;
        } finally {
            this.aLock.writeLock().unlock();
        }
    }

    /**
     * Remove user credentials information.
     *
     * @param fileName
     *            The file name
     * @return The "safe" display name without username and password information
     */
    public String getFriendlyName(final String fileName) {
        return this.getFriendlyName(fileName, true);
    }

    /**
     * Gets the friendly name.
     *
     * @param fileName
     *            the file name
     * @param excludeLocalFilePrefix
     *            the exclude local file prefix
     * @return the friendly name
     */
    public String getFriendlyName(final String fileName, final boolean excludeLocalFilePrefix) {
        final StringBuilder filePath = new StringBuilder();
        final int pos = fileName.lastIndexOf('@');
        if (pos == -1) {
            filePath.append(fileName);
        } else {
            final int pos2 = fileName.indexOf(PROTO_PREFIX);

            if (pos2 == -1) {
                filePath.append(fileName);
            } else {
                final String protocol = fileName.substring(0, pos2);

                filePath.append(protocol).append(PROTO_PREFIX).append(fileName.substring(pos + 1, fileName.length()));
            }
        }

        final String returnedString = filePath.toString();
        if (excludeLocalFilePrefix && returnedString.startsWith(FILE_PREFIX)) {
            return filePath.substring(FILE_PREFIX_LEN);
        }

        return returnedString;
    }

    /**
     * Returns a buffered input stream from a file.
     *
     * @param fileObject
     *            A file object
     * @return an InputStream from the file object
     * @throws FileSystemException
     *             An exception while getting the file
     */
    public InputStream getInputStream(final FileObject fileObject) throws FileSystemException {
        return new BufferedInputStream(fileObject.getContent().getInputStream());
    }

    /**
     * Returns a buffered output stream from a file.
     *
     * @param fileObject
     *            A file object
     * @return an OutputStream from the file object
     * @throws FileSystemException
     *             An exception while getting the file
     */
    public OutputStream getOutputStream(final FileObject fileObject) throws FileSystemException {
        return new BufferedOutputStream(fileObject.getContent().getOutputStream());
    }

    /**
     * Resolve file.
     *
     * @param url
     *            the url
     * @return the file object
     * @throws FileSystemException
     *             the file system exception
     */
    public FileObject resolveFile(final String url) throws FileSystemException {
        return this.getFileSystemManager() != null ? this.getFileSystemManager().resolveFile(url, this.opts) : null;
    }

    /**
     * Returns a file representation.
     *
     * @param filePath
     *            The file path
     * @return a file representation
     * @throws FileSystemException
     *             the file system exception
     */
    public FileObject createFileObject(final String filePath) throws FileSystemException {
        return this.resolveFile(filePath);
    }
}
