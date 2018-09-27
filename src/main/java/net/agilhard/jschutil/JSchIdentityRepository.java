/* -*-mode:java; c-basic-offset:2; -*- */
/*
 * IdentityRepository
 * Copyright (C) 2002,2007 ymnk, JCraft,Inc.
 * Written by: ymnk<ymnk@jcaft.com>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Library General Public License for more details.
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package net.agilhard.jschutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.agilhard.jsch.agentproxy.core.AgentProxyException;
import net.agilhard.jsch.agentproxy.core.Connector;
import net.agilhard.jsch.agentproxy.core.USocketFactory;
import net.agilhard.jsch.agentproxy.connector.pageant.PageantConnector;
import net.agilhard.jsch.agentproxy.usocket.jna.JNAUSocketFactory;
import net.agilhard.jsch.agentproxy.usocket.nc.NCUSocketFactory;

/**
 * The Class IdentityRepository.
 */
public class JSchIdentityRepository extends RemoteIdentityRepository {

    /** The con. */
    private static Connector con;

    static {

        final Logger log = LoggerFactory.getLogger(JSchIdentityRepository.class);

        con = null;
        final boolean hasSSHAgent = SSHAgentConnector.isConnectorAvailable();

        try {
            if (hasSSHAgent) {
                final USocketFactory usf = new JNAUSocketFactory();
                log.info("initializing SSHAgentConnector (jna)");
                con = new SSHAgentConnector(usf);
            } else {
                log.warn("SSHAgentConnector is not available.");
            }

        }
        catch (final AgentProxyException e) {
            log.warn("can not initialize SSHAgentConnector via JNAUSocketFactory.", e);
        }

        if (hasSSHAgent && con == null) {
            try {
                final USocketFactory usf = new NCUSocketFactory();
                log.debug("initializing SSHAgentConnector (ncu)");
                con = new SSHAgentConnector(usf);
            }
            catch (final AgentProxyException e) {
                log.warn("can not initialize SSHAgentConnector via NCUSocketFactory.", e);
            }
        }

        try {
            if (con == null && PageantConnector.isConnectorAvailable()) {
                log.info("initializing PageantConnector");
                con = new PageantConnector();
            } else {
                log.warn("PageantConnector is not available");
            }
        }
        catch (final AgentProxyException e) {
            log.warn("can not initialize PageantConnector", e);
        }

        if (con == null) {
            log.info("do not know how initialize AgentProxy for os.name=" + System.getProperty("os.name"));
        }
    }

    /**
     * Instantiates a new identity repository.
     */
    public JSchIdentityRepository() {
        super(con);
    }

    /**
     * Get the status.
     *
     * @return a status code
     * @see net.agilhard.jschutil.RemoteIdentityRepository#getStatus()
     */
    @Override
    public int getStatus() {
        if (con == null) {
            return UNAVAILABLE;
        }
        return super.getStatus();
    }
}
