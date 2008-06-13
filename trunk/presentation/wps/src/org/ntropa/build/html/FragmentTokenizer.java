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
 * FragmentTokenizer.java
 *
 * Created on 07 November 2001, 17:26
 */

package org.ntropa.build.html;

/**
 * An interface defining the operations required to obtain the parts
 * of a markedup html fragment.
 *
 * Typically a <code>Tokenizer</code> will be used in a similar way to a
 * <code>java.util.Iterator</code>. A <code>Tokenizer</code> supports the
 * additinal <code>pushbackFragment</code> method which allows html to be
 * put at the beginning of the html awaiting tokenization.
 *
 * The interface does not extend <code>java.util.Iterator</code> to avoid
 * having to provide an implementation of <code>remove</code>.
 *
 * @author  jdb
 * @version $Id: FragmentTokenizer.java,v 1.2 2001/11/20 22:20:51 jdb Exp $
 */
public interface FragmentTokenizer {

    public boolean hasNext ();
    
    Object next ();
    
    //void pushbackFragment ( Fragment fragment );
    
}

