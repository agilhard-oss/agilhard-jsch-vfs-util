/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
 * Copyright (c) 2011 ymnk, JCraft,Inc. All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution.
 * 3. The names of the authors may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 * INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// CHECKSTYLE:OFF
package net.agilhard.jschutil;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.agilhard.jsch.agentproxy.core.AgentProxyException;
import net.agilhard.jsch.agentproxy.core.Buffer;
import net.agilhard.jsch.agentproxy.core.Connector;
import net.agilhard.jsch.agentproxy.core.USocketFactory;

/**
 * The Class SSHAgentConnector.
 */
public class SSHAgentConnector implements Connector {

    /** The factory. */
    private final USocketFactory factory;

    /** The Constant LOG. */
    private final Logger LOG = LoggerFactory.getLogger(SSHAgentConnector.class);

    /**
     * Instantiates a new sSH agent connector.
     *
     * @param factory
     *            the factory
     * @throws AgentProxyException
     *             the agent proxy exception
     */
    public SSHAgentConnector(final USocketFactory factory) throws AgentProxyException {
        this.factory = factory;

        // checking if factory is really functional.
        USocketFactory.Socket sock = null;
        try {
            sock = this.open();
        }
        catch (final IOException e) {
            this.LOG.warn("I/O error opening socket", e);
            throw new AgentProxyException(e.toString());
        }
        catch (final Exception e) {
            this.LOG.warn("unkonwn error", e);
            throw new AgentProxyException(e.toString());
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            }
            catch (final IOException e) {
                this.LOG.warn("I/O error closing socket", e);
                throw new AgentProxyException(e.toString());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see net.agilhard.jsch.agentproxy.Connector#getName()
     */
    @Override
    public String getName() {
        return "ssh-agent";
    }

    /**
     * Checks if is connector available.
     *
     * @return true, if is connector available
     */
    public static boolean isConnectorAvailable() {
        return System.getenv("SSH_AUTH_SOCK") != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.agilhard.jsch.agentproxy.Connector#isAvailable()
     */
    @Override
    public boolean isAvailable() {
        return isConnectorAvailable();
    }

    /**
     * Open.
     *
     * @return the u socket factory. socket
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private USocketFactory.Socket open() throws IOException {
        final String ssh_auth_sock = System.getenv("SSH_AUTH_SOCK");
        if (ssh_auth_sock == null) {
            this.LOG.warn("SSH_AUTH_SOCK is not defined.");
            throw new IOException("SSH_AUTH_SOCK is not defined.");
        }

        return this.factory.open(ssh_auth_sock);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.agilhard.jsch.agentproxy.Connector#query(net.agilhard.jsch.agentproxy
     * .Buffer)
     */
    @Override
    public void query(final Buffer buffer) throws AgentProxyException {
        USocketFactory.Socket sock = null;
        try {
            sock = this.open();
            sock.write(buffer.buffer, 0, buffer.getLength());
            buffer.rewind();
            int i = sock.readFull(buffer.buffer, 0, 4); // length
            i = buffer.getInt();
            buffer.rewind();
            buffer.checkFreeSize(i);
            i = sock.readFull(buffer.buffer, 0, i);
        }
        catch (final IOException e) {
            this.LOG.warn("I/O error on query", e);
            throw new AgentProxyException(e.toString());
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            }
            catch (final IOException e) {
                this.LOG.warn("I/O error closing socket", e);
                throw new AgentProxyException(e.toString());
            }
        }
    }
}
//CHECKSTYLE:ON
