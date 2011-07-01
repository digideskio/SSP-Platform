/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.google.common.collect.ImmutableSet;

/**
 * FilenameFilter that uses a Set of regular expressions for testing. The file name
 * must match any one of the patterns
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AntPatternFileFilter implements FileFilter {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final boolean acceptDirectories;
    private final boolean ignoreHidden;
    private final Collection<String> includes;
    private final Collection<String> excludes;
    
    public AntPatternFileFilter(boolean acceptDirectories, boolean ignoreHidden, String include, Collection<String> excludes) {
        Validate.notNull(include);
        Validate.notNull(excludes);
        this.includes = ImmutableSet.of(include);
        this.excludes = ImmutableSet.copyOf(excludes);
        this.acceptDirectories = acceptDirectories;
        this.ignoreHidden = ignoreHidden;
    }
    
    public AntPatternFileFilter(boolean acceptDirectories, boolean ignoreHidden, Collection<String> includes, Collection<String> excludes) {
        Validate.notNull(includes);
        Validate.notNull(excludes);
        this.includes = ImmutableSet.copyOf(includes);
        this.excludes = ImmutableSet.copyOf(excludes);
        this.acceptDirectories = acceptDirectories;
        this.ignoreHidden = ignoreHidden;
    }

    @Override
    public boolean accept(File pathname) {
        if (ignoreHidden && pathname.isHidden()) {
            return false;
        }
        
        final String path;
        try {
            path = pathname.getCanonicalPath();
        }
        catch (IOException e) {
            throw new RuntimeException("Could not determine canonical path of: " + pathname, e);
        }
        
        logger.debug("checking path: {}", path);
        for (final String include : this.includes) {
            if ((acceptDirectories && pathname.isDirectory()) || pathMatcher.match(include, path)) {
                logger.debug("{} matches include {}", path, include);
                for (final String exclude : this.excludes) {
                    if (pathMatcher.match(exclude, path)) {
                        logger.debug("{} matches exclude {}", path, exclude);
                        logger.debug("denied path: {}", path);
                        return false;
                    }
                    else if (logger.isTraceEnabled()) {
                        logger.trace("{} doesn't match exclude {}", path, exclude);
                    }
                }
                
                logger.debug("acepted path: {}", path);
                return true;
            }
            else if (logger.isTraceEnabled()) {
                logger.trace("{} doesn't match include {}", path, include);
            }
        }
        
        logger.debug("denied path: {}", path);
        return false;
    }
}