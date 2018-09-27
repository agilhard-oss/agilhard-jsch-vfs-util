/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.agilhard.vfs2.provider.sftp;

import net.agilhard.jsch.IdentityRepository;
import net.agilhard.jsch.JSch;

/**
 * Creates instances of JSch {@link IdentityRepository}.
 *
 * @version $Id: IdentityRepositoryFactory.java 1808381 2017-09-14 19:26:39Z ggregory $
 */
public interface IdentityRepositoryFactory {
    /**
     * Creates an Identity repository for a given JSch instance.
     *
     * @param jsch JSch context
     * @return a new IdentityRepository
     */
    IdentityRepository create(JSch jsch);
}
