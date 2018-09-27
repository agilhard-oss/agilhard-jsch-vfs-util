// CHECKSTYLE:OFF
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

package net.agilhard.jschutil;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import net.agilhard.jsch.IdentityRepository;
import net.agilhard.jsch.JSchException;
import net.agilhard.jsch.agentproxy.core.AgentProxy;
import net.agilhard.jsch.agentproxy.core.Buffer;
import net.agilhard.jsch.agentproxy.core.Connector;
import net.agilhard.jsch.agentproxy.core.Identity;

/**
 * The Class RemoteIdentityRepository.
 */
public class RemoteIdentityRepository implements IdentityRepository {

    /** The agent. */
    private final AgentProxy agent;

    /**
     * Instantiates a new remote identity repository.
     *
     * @param connector
     *            the connector
     */
    public RemoteIdentityRepository(final Connector connector) {
        this.agent = new AgentProxy(connector);
    }

    /* (non-Javadoc)
     * @see net.agilhard.jsch.IdentityRepository#getIdentities()
     */
    @Override
    public Vector<net.agilhard.jsch.Identity> getIdentities() {

        final Vector<net.agilhard.jsch.Identity> result = new Vector<>();

        final Identity[] identities = this.agent.getIdentities();

        for (int i = 0; i < identities.length; i++) {

            final Identity _identity = identities[i];

            final class SomeIdentity implements net.agilhard.jsch.Identity {

                byte[] blob = _identity.getBlob();

                String algname;

                public SomeIdentity() {
                    try {
                        this.algname = this.blob != null ? new String(new Buffer(this.blob).getString(), "UTF-8") : "";
                    }
                    catch (final UnsupportedEncodingException e) {
                        this.algname = "";
                    }
                }

                @SuppressWarnings("unused")
                @Override
                public boolean setPassphrase(final byte[] passphrase) throws JSchException {
                    return true;
                }

                @Override
                public byte[] getPublicKeyBlob() {
                    return this.blob;
                }

                @SuppressWarnings("synthetic-access")
                @Override
                public byte[] getSignature(final byte[] data) {
                    return this.blob != null && data != null ? RemoteIdentityRepository.this.agent.sign(this.blob, data)
                        : null;
                }

                @Override
                public boolean decrypt() {
                    return true;
                }

                @Override
                public String getAlgName() {
                    return this.algname;
                }

                @Override
                public String getName() {
                    String s = null;
                    try {
                        s = new String(_identity.getComment(), "UTF-8");
                    }
                    catch (final UnsupportedEncodingException e) {
                        // ignored
                    }
                    return s;
                }

                @Override
                public boolean isEncrypted() {
                    return false;
                }

                @Override
                public void clear() {
                    //.
                }
            }


            final net.agilhard.jsch.Identity id = new SomeIdentity();

            if (id.getPublicKeyBlob() != null && id.getPublicKeyBlob().length > 0) {
                result.addElement(id);
            }
        }

        return result;
    }

    /* (non-Javadoc)
     * @see net.agilhard.jsch.IdentityRepository#add(byte[])
     */
    @Override
    public boolean add(final byte[] identity) {
        return this.agent.addIdentity(identity);
    }

    /* (non-Javadoc)
     * @see net.agilhard.jsch.IdentityRepository#remove(byte[])
     */
    @Override
    public boolean remove(final byte[] blob) {
        return this.agent.removeIdentity(blob);
    }

    /* (non-Javadoc)
     * @see net.agilhard.jsch.IdentityRepository#removeAll()
     */
    @Override
    public void removeAll() {
        this.agent.removeAllIdentities();
    }

    /* (non-Javadoc)
     * @see net.agilhard.jsch.IdentityRepository#getName()
     */
    @Override
    public String getName() {
        return this.agent.getConnector().getName();
    }

    /* (non-Javadoc)
     * @see net.agilhard.jsch.IdentityRepository#getStatus()
     */
    @Override
    public int getStatus() {
        if (this.agent.getConnector().isAvailable()) {
            return RUNNING;
        }

        return NOTRUNNING;

    }
}
// CHECKSTYLE:ON
