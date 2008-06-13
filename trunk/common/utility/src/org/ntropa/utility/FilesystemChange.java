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
 * FilesystemChange.java
 *
 * Created on September 13, 2001, 12:53 PM
 */

package org.ntropa.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author  jdb
 * @version $Id: FilesystemChange.java,v 1.2 2001/09/13 16:38:49 jdb Exp $
 */
public class FilesystemChange {
    
    /* Intentionally different to FileChangeSet to avoid unintended assumptions */
    public static final int ADDED = 10 ;
    public static final int DELETED = 20 ;
    public static final int MODIFIED = 40 ;
    
    private long m_LastModificationTime ;
    
    private String m_fsRoot ;
    private UniqueFilenameSequence m_ufs ;
    private Random m_random ;
    
    /** Creates new FilesystemChange */
    public FilesystemChange ( String fsRoot, UniqueFilenameSequence ufs, Random r ) {
        
        m_fsRoot = fsRoot ;
        m_ufs = ufs ;
        m_LastModificationTime = 0 ;
        
        m_random = r ;
    }
    
    /** Creates new FilesystemChange */
    public FilesystemChange ( File fsRoot, UniqueFilenameSequence ufs, Random r ) {
        
        m_fsRoot = fsRoot.getAbsolutePath () ;
        m_ufs = ufs ;
        m_LastModificationTime = 0 ;
        
        m_random = r ;
    }
    
    
    public void changeFilesystem () throws IOException {
        SortedSet addedSet = new TreeSet () ;
        SortedSet modifiedSet = new TreeSet () ;
        SortedSet deletedSet = new TreeSet () ;
        
        changeFilesystem ( addedSet, modifiedSet, deletedSet ) ;
    }
    
    public void changeFilesystem ( SortedSet addedSet, SortedSet modifiedSet, SortedSet deletedSet ) throws IOException {
        
        boolean verbose = false ;
        // Make 1 - 100 changes.
        int changeCnt = Math.abs ( m_random.nextInt () ) % 100 + 1 ;
        if ( verbose )
            System.out.print ("Making changes:" );
        for ( int changeIDX = 1 ; changeIDX <= changeCnt ; changeIDX++ ) {
            
            int changeType = getChangeType () ;
            
            switch ( changeType ) {
                case ADDED :
                    addFileOrDirectory ( addedSet ) ;
                    break ;
                case MODIFIED :
                    modifyFile ( modifiedSet, addedSet ) ;
                    break ;
                case DELETED :
                    deleteFileOrDirectory ( deletedSet, addedSet, modifiedSet ) ;
                    break ;
            }
        } // changeIDX
        
        if ( verbose )
            System.out.println ("\tADDED: " + addedSet.size ()
            + "\tMODIFIED: " + modifiedSet.size ()
            + "\tDELETED: " + deletedSet.size () );
        
        
    }
    
    /**
     * Naviagte down the filesytem and add a file or directory.
     */
    protected void addFileOrDirectory ( Set addedSet ) throws IOException {
        
        String directory = navigateDown () ;
        
        String newOne = directory + File.separator + m_ufs.next () ;
        
        File f = new File ( newOne ) ;
        // We want more files than directories to allow for the reduction in file count
        // due to directory deletion.
        if ( Math.abs ( m_random.nextInt () % 20 ) == 0 ) {
            // Make a directory
            if ( ! f.mkdir () )
                throw new IOException ( "An exception was encountered by the test while changing the filesystem." ) ;
        }
        else {
            if ( ! f.createNewFile () )
                // Make file
                throw new IOException  ( "An exception was encountered by the test while changing the filesystem." ) ;
        }
        
        addedSet.add ( newOne ) ;
        
    }
    
    /**
     * Modify a file.
     * Might not do anything if which case modifiedSet will not be added to.
     */
    protected void modifyFile ( Set modifiedSet, Set addedSet ) throws IOException {
        
        String fn = selectFile () ;
        
        if ( fn == null )
            return ;
        
        // An ADD followed by a MODIFY will be reported as an ADDED.
        if ( addedSet.contains ( fn ) )
            return ;
        
        File f = new File ( fn ) ;
        
        if ( f.isDirectory () )
            return ;
        
        modifyFile ( f ) ;
        
        modifiedSet.add ( fn ) ;
        
    }
    
    /**
     * Select a file import directory to delete.
     * Call other method to do the deletion.
     */
    protected void deleteFileOrDirectory ( Set deletedSet, Set addedSet, Set modifiedSet ) throws IOException {
        
        String fn = selectFile () ;
        
        if ( fn == null )
            return ;
        
        File f = new File ( fn ) ;
        
        deleteFileOrDirectory ( f, deletedSet, addedSet, modifiedSet ) ;
        
    }
    
    protected void deleteFileOrDirectory ( File f, Set deletedSet, Set addedSet, Set modifiedSet ) throws IOException {
        
        String fn = f.getAbsolutePath () ;
        
        if ( f.isDirectory () ) {
            // Recurse
            File children [] = f.listFiles () ;
            for ( int childIDX = 0 ; childIDX < children.length ; childIDX++ ) {
                deleteFileOrDirectory ( children [ childIDX ], deletedSet, addedSet, modifiedSet ) ;
            }
        }
        
        boolean wasFile = f.isFile () ;
        
        if ( ! f.delete () ) {
            throw new IOException ( "Failed to create fixture: File deletion failed." ) ;
        }
        
        //System.out.println ( "DirectoryMonitorTest: deleted: " + fn ) ;
        
        // We only add the record of deletion if the file or directory had not
        // been added within the current group.
        if ( ! addedSet.contains ( fn ) )
            deletedSet.add ( fn ) ;
        
        // A delete on a file or directory which was added within the current group
        // will not be reported as added so we remove the record of the addition.
        if ( addedSet.contains ( fn ) )
            addedSet.remove ( fn ) ;
        
        // A delete on a file or directory which was modified within the current group
        // will not be reported as modified so we remove the record of the modification.
        if ( modifiedSet.contains ( fn ) )
            // In this test modification of directories is ignored.
            if ( wasFile )
                modifiedSet.remove ( fn ) ;
    }
    
    /**
     * Modify a file
     */
    protected void modifyFile ( File f ) throws IOException {
        try {
            FileWriter w = new FileWriter ( f ) ;
            w.write ( "modified" ) ;
            w.close () ;
            // Although Java reports the modification time in milliseconds
            // Linux only records to the second so we bump the modification time forward.
            
            f.setLastModified ( nextModificationTime () ) ;
            //System.out.println ("modifyFile:" + f + ":" + f.lastModified () );
            
        }
        catch ( Exception e ) {
            throw new IOException ( "Failed to create fixture: File modification failed," ) ;
        }
    }
    
    /**
     * Return a time guaranteed to be greater than the last retuned
     * value by at least 2 seconds and greater than the current time
     * by at least 2 seconds.
     *
     * Without this trickery two calls to modifyFile () could result in
     * the file having the same timestamp. Under Linux/ext2 timestamp
     * are recorded to a resolution of 1 second.
     */
    protected long nextModificationTime () {
        
        long mt ;
        
        synchronized ( this ) {
            long curTime = System.currentTimeMillis () ;
            m_LastModificationTime += 2000 ;
            if ( m_LastModificationTime <= curTime + 2000 )
                m_LastModificationTime = curTime + 2000 ;
            mt = m_LastModificationTime ;
        }
        
        return mt ;
        
    }
    
    /**
     * Return a file or directory
     * Can return null if there were no files or directories in the navigated directory.
     */
    protected String selectFile () throws IOException {
        
        String directory = navigateDown () ;
        
        File f = new File ( directory ) ;
        
        List childList = new ArrayList ( Arrays.asList ( f.list () ) ) ;
        
        if ( childList.size () == 0 )
            return null ;
        
        // ensure this is repeatable.
        Collections.sort ( childList ) ;
        int randomIDX = Math.abs ( m_random.nextInt () % childList.size () ) ;
        return directory + File.separator + childList.get ( randomIDX ) ;
        
    }
    
    /**
     * Naviagte down the filesytem.
     */
    protected String navigateDown () throws IOException {
        
        StringBuffer currentDirectory = new StringBuffer ( m_fsRoot ) ;
        
        boolean canGoFurther = true ;
        
        // 9/10 chance
        boolean wantToGoFurther = Math.abs ( m_random.nextInt () % 10 ) > 0 ;
        
        while ( canGoFurther && wantToGoFurther ) {
            File f = new File ( currentDirectory.toString () ) ;
            
            // Use the Arrays static factory to make a collection; use the new operator to make a modifiable collection.
            List childList = new ArrayList ( Arrays.asList ( f.list () ) ) ;
            
            // Remove non-directories.
            Iterator childIt = childList.iterator () ;
            while ( childIt.hasNext () ) {
                String child = (String) childIt.next () ;
                File c = new File ( currentDirectory + File.separator + child ) ;
                if ( ! c.isDirectory () )
                    childIt.remove () ;
            }
            
            if ( childList.size () == 0 ) {
                canGoFurther = false ;
                break ;
            }
            
            // ensure this is repeatable.
            Collections.sort ( childList ) ;
            int randomIDX = Math.abs ( m_random.nextInt () % childList.size () ) ;
            currentDirectory.append ( File.separator + childList.get ( randomIDX ) ) ;
            
            // 9/10 chance
            wantToGoFurther = Math.abs ( m_random.nextInt () % 10 ) > 0 ;
            
        } //while
        
        return currentDirectory.toString () ;
    }
    
    protected int getChangeType () {
        
        int changeType = ADDED ;
        
        switch ( Math.abs ( m_random.nextInt () % 3 ) ) {
            case 0:
                changeType = ADDED ;
                break ;
            case 1:
                changeType = MODIFIED ;
                break ;
            case 2:
                changeType = DELETED ;
                break ;
        }
        
        return changeType ;
    }
    
}
