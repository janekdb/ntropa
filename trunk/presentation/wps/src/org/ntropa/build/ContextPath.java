/*
 * Copyright 2001-2006 LEARNING INFORMATION SYSTEMS PTY LIMITED
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * ContextPath.java
 *
 * Created on 20 June 2002, 16:10
 */

package org.ntropa.build;

import org.ntropa.utility.StringUtilities;

/**
 * Represent a directory name which is used as the deepest directory in the path
 * to a channel's HTML documents and other files.
 * <p>
 * The object is immutable.
 * <p>
 * This class was originally added to break the dependency on ChannelID.
 * 
 * @author jdb
 * @version $Id: ContextPath.java,v 1.1 2002/06/20 19:20:46 jdb Exp $
 */
public class ContextPath implements Comparable<ContextPath>{

    private final String path;

    /**
     * Creates a new <code>ContextPath</code> from a <code>String</code>
     * object.
     * 
     * @param contextPath
     *            the context path. Can not be null or zero-length
     * @throws A
     *             runtime exception ContextPathException if contextPath is null
     *             or zero-length or if the contextPath has one or more
     *             characters which is not a digit, an underscore, a hyphen or
     *             an upper or lower case letter (A-Za-z).
     */
    public ContextPath(String contextPath) throws ContextPathException {

        if (contextPath == null)
            throw new ContextPathException("contextPath was null.");

        if (contextPath.equals(""))
            throw new ContextPathException("contextPath was empty String");

        if (!allCharactersAllowable(contextPath))
            throw new ContextPathException("contextPath had disallowed characters: " + contextPath);

        path = contextPath;
    }

    private static String allowableCharacters = "0123456789-_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private boolean allCharactersAllowable(String contextPath) {
        return StringUtilities.isSubset(contextPath, allowableCharacters);
    }

    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!(obj instanceof ContextPath))
            return false;

        ContextPath c = (ContextPath) obj;

        return path.equals(c.getPath());
    }

    public String toString() {
        return "ContextPath: " + getPath();
    }

    public int hashCode() {
        return path.hashCode();
    }

    public String getPath() {
        return path;
    }

    public int compareTo(ContextPath other) {
        return this.path.compareTo(other.path);
    }

}
