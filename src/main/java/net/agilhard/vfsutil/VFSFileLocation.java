package net.agilhard.vfsutil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * The Class VFSFileLocation.
 */
public class VFSFileLocation {

    /**
     * The slash char.
     */
    private static final String SLASH = "/";

    /** The Logger. */
    //private final Logger log = LoggerFactory.getLogger(VFSFileLocation.class);

    /**
     * The value returned by {@link #getActualLastModified()} for remote files.
     */
    public static final long LAST_MODIFIED_UNKNOWN = 0;

    /**
     * A prettied-up full path of the URL (password removed, etc.).
     */
    private final String fileFullPath;

    /**
     * A prettied-up filename (leading slash, and possibly "<code>%2F</code>",
     * removed).
     */
    private final String fileName;

    /**
     * FileObject for VFS file.
     */
    private final FileObject fileObject;

    /**
     * Constructor.
     *
     * @param fileObject
     *            a FileObject
     * @throws FileSystemException
     *             the file system exception
     */
    public VFSFileLocation(final FileObject fileObject) throws FileSystemException {
        this.fileObject = fileObject;
        this.fileFullPath = this.createFileFullPath();
        this.fileName = this.createFileName();
    }

    /**
     * Constructor.
     *
     * @param url
     *            The URL of the file.
     * @throws FileSystemException
     *             the file system exception
     */
    public VFSFileLocation(final String url) throws FileSystemException {
        final String url2 = url;
        /*
        if ( url != null && url.startsWith(SFTP_URL_START) ) {
        
            final JSchConfigurationRepository rep = JSchUtil.getCR();
            final JSchConfiguration config = rep.load("default");
            if (config != null && config.useFixedHostMapping) {
                String s = url.substring(8);
                final int i = s.indexOf(SLASH);
                String host = null;
                if (i < 0) {
                    host = s;
                    s = "";
                } else {
                    host = s.substring(0, i);
                    s = s.substring(i + 1);
                }
                final String ip = FixedHostMap.getFixedIpOf(host);
                if (ip != null) {
                    if ("".equals(s)) {
                        url2 = SFTP_URL_START + ip;
                    } else {
                        url2 = SFTP_URL_START + ip + SLASH + s;
                    }
                }
            }
        }
        */
        this.fileObject = VFSTools.getInstance().resolveFile(url2);

        this.fileFullPath = this.createFileFullPath();
        this.fileName = this.createFileName();
    }

    /**
     * Constructor.
     *
     * @param file
     *            The local file.
     * @throws FileSystemException
     *             the file system exception
     */
    public VFSFileLocation(final File file) throws FileSystemException {
        File file2 = file;
        try {
            // Useful on Windows and OS X.
            file2 = file.getCanonicalFile();
        }
        catch (final IOException ioe) {
            file2 = file;
        }
        this.fileObject = VFSTools.getInstance().createFileObject(file2.toURI().toString());
        this.fileFullPath = this.createFileFullPath();
        this.fileName = this.createFileName();
    }

    /**
     * Creates a "prettied-up" URL to use. This will be stripped of sensitive
     * information such as passwords.
     *
     * @return The full path to use.
     */
    private String createFileFullPath() {
        if (this.fileObject == null) {
            return null;
        }
        return VFSTools.getInstance().getFriendlyName(this.fileObject.toString());
    }

    /**
     * Creates the "prettied-up" filename to use.
     *
     * @return The base name of the file of this URL.
     * @throws FileSystemException
     *             the file system exception
     */
    private String createFileName() throws FileSystemException {
        if (this.fileObject == null) {
            return null;
        }
        String name = "";
        final java.net.URL url = this.fileObject.getURL();
        name = url.getPath();
        if (name.startsWith("/%2F/")) { // Absolute path
            name = name.substring(4);
        } else if (name.startsWith(SLASH)) { // All others
            name = name.substring(1);
        }
        return name;
    }

    /**
     * Returns the last time this file was modified, or.
     *
     * @return The last time this file was modified. This will always be
     *         {@link #LAST_MODIFIED_UNKNOWN} if this value cannot be computed
     *         (such as for a remote file). {@link LAST_MODIFIED_UNKNOWN} for
     *         URL's.
     */
    public long getActualLastModified() {
        // TODO: return something real for at least local files.
        return LAST_MODIFIED_UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    public String getFileFullPath() {
        return this.fileFullPath;
    }

    /**
     * {@inheritDoc}
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getInputStream() throws IOException {
        if (this.fileObject == null) {
            return null;
        }
        return VFSTools.getInstance().getInputStream(this.fileObject);
    }

    /**
     * {@inheritDoc}
     */
    public OutputStream getOutputStream() throws IOException {
        if (this.fileObject == null) {
            return null;
        }
        return VFSTools.getInstance().getOutputStream(this.fileObject);
    }

    /**
     * Returns whether this file location is a local file.
     *
     * @return Whether this is a local file.
     * @throws FileSystemException
     *             the file system exception
     * @see #isLocalAndExists()
     */
    public boolean isLocal() throws FileSystemException {

        if (this.fileObject == null) {
            return false;
        }
        final URL url = this.fileObject.getURL();
        if (url == null) {
            return false;
        }
        return "file".equals(url.getProtocol());
    }

    /**
     * Returns whether this file location is a local file and already exists.
     *
     * @return <code>true</code> or <code>false</code> always.
     * @throws FileSystemException
     *             the file system exception
     * @see #isLocal()
     */
    public boolean isLocalAndExists() throws FileSystemException {
        boolean b = false;

        b = this.isLocal() && this.fileObject != null && this.fileObject.exists();

        return b;
    }

    /**
     * Gets the file object.
     *
     * @return the file object
     */
    public FileObject getFileObject() {
        return this.fileObject;
    }

    /**
     * Creates the.
     *
     * @param fileFullPath
     *            the file full path
     * @return the vFS file location
     * @throws FileSystemException
     *             the file system exception
     */
    public static VFSFileLocation create(final String fileFullPath) throws FileSystemException {
        return new VFSFileLocation(new File(fileFullPath));
    }
}
