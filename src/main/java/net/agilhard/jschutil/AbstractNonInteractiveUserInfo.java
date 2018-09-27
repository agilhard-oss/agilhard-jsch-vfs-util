package net.agilhard.jschutil;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import net.agilhard.jsch.UserInfo;

/**
 * The Class AbstractNonInteractiveUserInfo.
 */
public abstract class AbstractNonInteractiveUserInfo implements UserInfo {

    /** The print. */
    private PrintWriter print;

    /**
     * Instantiates a new abstract non interactive user info.
     * AbstractNonInteractiveUserInfo
     * 
     * @param out
     *            a OutputStream
     */
    protected AbstractNonInteractiveUserInfo(final OutputStream out) {
        try {
            this.print = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        }
        catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get Password.
     * 
     * @see net.agilhard.jsch.UserInfo#getPassword()
     * @return the password
     */
    @Override
    public abstract String getPassword();

    /**
     * Prompty yes/no.
     * 
     * @param str
     *            a message
     * @return always returns false
     * @see net.agilhard.jsch.UserInfo#promptYesNo(java.lang.String)
     */
    @Override
    public boolean promptYesNo(final String str) {
        this.print.println(str);
        this.print.println("[ssh-exec] promptYesNo in non interactive mode auto-answer=no");
        this.print.flush();

        return false;
    }

    /**
     * Get passphrase.
     *
     * @return always returns null
     * @see net.agilhard.jsch.UserInfo#getPassphrase()
     */
    @Override
    public String getPassphrase() {
        return null;
    }

    /**
     * Prompt passphrase.
     * 
     * @param message
     *            a message to show
     * @return always returns true
     * @see net.agilhard.jsch.UserInfo#promptPassphrase(java.lang.String)
     */
    @Override
    public boolean promptPassphrase(final String message) {
        this.print.println(message);
        this.print.println("[ssh-exec] promptPassphrase in non interactive mode returning true");
        return true;
    }

    /**
     * Prompt password.
     * 
     * @param message
     *            a message to show
     * @return always returns false
     * @see net.agilhard.jsch.UserInfo#promptPassword(java.lang.String)
     */
    @Override
    public boolean promptPassword(final String message) {
        this.print.println(message);
        this.print.println("[ssh-exec] promptPassword in non interactive mode returning false");
        this.print.flush();
        return false;
    }

    /**
     * Show message.
     * 
     * @param message
     *            a message to show
     * @see net.agilhard.jsch.UserInfo#showMessage(java.lang.String)
     */
    @Override
    public void showMessage(final String message) {
        this.print.println(message);
    }
}