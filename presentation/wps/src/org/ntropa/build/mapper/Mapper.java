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
 * Mapper.java
 *
 * Created on September 10, 2001, 6:18 PM
 */

package org.ntropa.build.mapper;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ntropa.build.Constants;
import org.ntropa.build.ContextPath;
import org.ntropa.build.DirectoryMonitor;
import org.ntropa.build.DirectoryMonitorException;
import org.ntropa.build.DirectoryPair;
import org.ntropa.build.FileChangeSet;
import org.ntropa.build.FileChangeSetListener;
import org.ntropa.build.FileListener;
import org.ntropa.build.FileListenerEvent;
import org.ntropa.build.channel.FileLocation;
import org.ntropa.build.channel.FileLocationException;
import org.ntropa.utility.FilePredicate;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.PathWalker;
import org.ntropa.utility.StandardFilePredicate;

/**
 * FIXME: Fails to calculate dependencies for affected files.
 * 
 * A <code>Mapper</code> is responsible for responding to changes in the input
 * directory (= upload directory, webdav directory) for a channel .
 * 
 * @author jdb
 * @version $Id: Mapper.java,v 1.32 2005/06/24 13:28:14 jdb Exp $
 */
public class Mapper implements FileChangeSetListener {

    /* The context path the object is responsible for */
    protected ContextPath m_contextPath;

    /* The folder for making links to to and reading folder link files from */
    protected File m_SourceFilesystemLocation;

    // protected int m_SourcePathLength ;

    /* The folder for writing symlinks to and deleting symlinks from */
    protected File m_SymbolicFilesystemLocation;

    Vector listeners = new Vector();

    /*
     * This is a nasty way of adding a changeable default log level to the
     * class. It was used to reduce the amount of info spewed while running the
     * unit tests and to retain the output while in production.
     */
    private static int _classLevelDebug = 1;

    /* The debug level */
    private int _debug = 0;

    /*
     * A file predicate to test for ADDED, MODIFIED, or DELETED file being link
     * files.
     */
    private static FilePredicate linkFilePredicate;

    static {

        StandardFilePredicate pred = new StandardFilePredicate();

        pred.setIncludeFileSuffixes(Collections.singleton(Constants.getLinkFileSuffix()));

        linkFilePredicate = pred;
    }

    /**
     * The <code>DirectoryMonitor</code> which reports on changes to the
     * symbolic filesystem
     */
    private DirectoryMonitor symbolicFilesystemMonitor;

    /**
     * This constructor supplies all the information the Mapper object will need
     * to respond to FileChangeSet events.
     * 
     * @param contextPath
     *            The context path of the channel this <code>Mapper</code> is
     *            responsible for.
     * @param dp
     *            A directory pair of which the source directory is the input
     *            folder (upload, webdav) and the destination directory is the
     *            root directory of the filesystem containing the symbolic links
     *            for this channel.
     * 
     */
    public Mapper(ContextPath contextPath, DirectoryPair dp) throws MapperException {

        if (contextPath == null)
            throw new MapperException("Mapper:Attempt to create Mapper from null ContextPath");

        if (dp == null)
            throw new MapperException("Mapper:Attempt to create Mapper from null DirectoryPair");

        /* Save new objects */
        m_SourceFilesystemLocation = dp.getSource().getAbsoluteFile();
        // m_SourcePathLength =
        // m_SourceFilesystemLocation.getAbsolutePath().length() ;

        m_SymbolicFilesystemLocation = dp.getDestination().getAbsoluteFile();

        /* ContextPaths are immutable */
        m_contextPath = contextPath;

        /* Nasty hack continued from above ... */
        _debug = _classLevelDebug;

        try {
            symbolicFilesystemMonitor = new DirectoryMonitor(m_SymbolicFilesystemLocation);
        } catch (DirectoryMonitorException e) {
            throw new MapperException("Mapper:DirectoryMonitor threw an exception: " + e);
        }
    }

    protected File getSymFSFile(FileLocation relativeFileLoc) {

        return new File(m_SymbolicFilesystemLocation.getAbsolutePath(), relativeFileLoc.getLocation());

    }

    /**
     * Return the absolute File to the parent directory of the parent directory
     * of the given relative file location.
     * 
     * @param relativeFileLoc
     *            A relative file path such as '_include/templates.html' or
     *            'about/_include/headers-poi.html'.
     */
    protected File getSymFSGrandParentDir(FileLocation relativeFileLoc) {

        /*
         * use a PathWalker as this encapsulates the details of path element
         * manipulation
         */
        PathWalker pw = new PathWalker(new File(m_SymbolicFilesystemLocation.getAbsolutePath(), relativeFileLoc
                .getLocation()));

        Iterator it = pw.iterator();

        /* '/PATH/about/_include/headers-poi.html' */
        it.next();
        /* '/PATH/about/_include' */
        it.next();

        /* '/PATH/about' */
        return new File((String) it.next());

    }

    /**
     * Act as a FileChangeSetListener
     * 
     * Maintain the symbolic file system. No event forwarding.
     */
    public void changed(FileChangeSet fcs) {

        if (fcs.size() == 0)
            return;

        dispatchEvents(fcs, m_SourceFilesystemLocation, null /* apexList */);

    }

    /**
     * Respond to changes in the symbolic file system by notifying listeners.
     * <p>
     * Maintain any dependencies by issuing MODIFIED events to any file that
     * could be dependent on a file which has been added. modified, or deleted.
     * 
     * @return the number of unique messages set to listeners. Note: the return
     *         value is independent of the number of listeners.
     */
    public int monitorLinkFolder() {

        FileChangeSet fcs = symbolicFilesystemMonitor.monitorFolder();

        if (fcs.size() == 0)
            return 0;

        ApexList apexList = new ApexList();

        /* Dispatch messages with dependency tracking */
        notifyListeners(fcs, apexList);

        FileChangeSet maybeModifiedDependents = getDependencyEvents(apexList);

        /* Dispatch messages without dependency tracking */
        notifyListeners(maybeModifiedDependents, null);

        return fcs.size();
    }

    /**
     * Break the FileChangeSet object into multiple messages
     * 
     * Handle dependencies. Whenever a file in a special directory (_include et
     * al) is added, modified or deleted note the directory the special
     * directory is in and issue fileModified events for all dependent files.
     * 
     * @param fcs
     *            The set of files changes.
     * @param rootDirForRelativePaths
     *            A <code>File</code> representing the directory to calculate
     *            relative paths from. The root is the webdav root when the
     *            source of the changes is a change in that directory. The root
     *            is the symbolic fs root when the source of the changes is a
     *            set of dependencies generated in response to a change in a
     *            file others are dependent on.
     * @param apexList
     *            The <code>ApexList</code> to record directories which may
     *            have dependent files in. If null then no record is made.
     */
    protected void dispatchEvents(FileChangeSet fcs, File rootDirForRelativePaths, ApexList apexList) {

        int changeCnt = fcs.size();

        if (_debug >= 1)
            log("Mapper:dispatchEvents:\n" + fcs);
        for (int changeIDX = 0; changeIDX < changeCnt; changeIDX++) {

            int event = fcs.getEvent(changeIDX);
            String path = fcs.getPath(changeIDX);

            /* A file or directory */
            File sourceFile = new File(path);

            try {
                /*
                 * Convert to path relative to upload directory.
                 * 
                 * /opt/xl/web/webdav/pg-au/about/contact.html ->
                 * about/contact.html or
                 * /opt/xl/web/sym/pg-au/about/contact.html ->
                 * about/contact.html or
                 * /opt/xl/web/webdav/pg-au/mba-contact.html.link ->
                 * mba-contact.html
                 * 
                 */

                FileLocation relativeFileLoc = new FileLocation(path.substring(rootDirForRelativePaths
                        .getAbsolutePath().length() + 1));
                if (_debug >= 2)
                    log("relativeFileLoc.getLocation (): " + relativeFileLoc.getLocation());

                /*
                 * Is the file in a directory which contains files depended on
                 * by other files, for example is it in _include or
                 * _application?
                 */
                if ((apexList != null) && (isDependencyFile(relativeFileLoc))) {
                    apexList.add(getSymFSGrandParentDir(relativeFileLoc));
                }

                if (linkFilePredicate.accept(sourceFile))
                    processLinkFileEvent(event, sourceFile, relativeFileLoc);
                else
                    /* A normal file or directory */
                    processEvent(event, sourceFile, relativeFileLoc);

            } catch (FileLocationException e) {
                log("Mapper:dispatchEvents, event was skipped: " + e);
            }
        }
    }

    /**
     * Send events based on changes to the symbolic filesystem to all listeners
     * <p>
     * Build a list of files which could be dependent on files which have been
     * added, modified, or deleted in any of the special directories and send
     * MODIFIED events for those maybe dependent files.
     * 
     * @param fcs
     *            The set of changes in the symbolic filesystem.
     */
    private void notifyListeners(FileChangeSet fcs, ApexList apexList) {

        String LOG_PREFIX = "[Mapper.notifyListeners] ";

        int changeCnt = fcs.size();

        if (_debug >= 1)
            log("Mapper:notifyListeners:\n" + fcs);

        for (int changeIDX = 0; changeIDX < changeCnt; changeIDX++) {

            int event = fcs.getEvent(changeIDX);
            String path = fcs.getPath(changeIDX);

            /* A file or directory */
            File sourceFile = new File(path);

            try {
                /*
                 * Convert to path relative to the symbolic filesystem
                 * directory.
                 * 
                 * /opt/xl/web/sym/pg-au/about/contact.html ->
                 * about/contact.html
                 */

                FileLocation relativeFileLoc = new FileLocation(path.substring(m_SymbolicFilesystemLocation
                        .getAbsolutePath().length() + 1));
                if (_debug >= 2)
                    log(LOG_PREFIX + "relativeFileLoc.getLocation (): " + relativeFileLoc.getLocation());

                /*
                 * Is the file in a directory which contains files depended on
                 * by other files, for example is it in _include or
                 * _application?
                 */
                if ((apexList != null) && isDependencyFile(relativeFileLoc))
                    apexList.add(getSymFSGrandParentDir(relativeFileLoc));

                try {
                    FileListenerEvent fle = new FileListenerEvent(relativeFileLoc);
                    notifyListeners(event, fle);
                } catch (Exception e) {
                    log(LOG_PREFIX + "Exception while notifying listeners with event '" + event
                            + "' for FileLocation '" + relativeFileLoc.getLocation() + "': " + e);
                }

            } catch (FileLocationException e) {
                log(LOG_PREFIX + "event was skipped: " + e);
            }
        }
    }

    /**
     * The location of the symbolic file and it's name are implied by the
     * location of the source file and the name of the source file. This is a
     * design decision that allows easy handling of deleted files, which can not
     * be read (because they have been deleted).
     * 
     * The addition of a link file which links to a file creates a link to the
     * file in the symbolic filesystem. This allow links to links to work but
     * more importantly it allows the Mapper to not have to do file name
     * filtering to remove *.html~ etc, which it would be obliged to do it
     * making links to directories in the webdav filesystem directly.
     */
    private void processLinkFileEvent(int event, File sourceFile, FileLocation relativeFileLoc) {

        String LOG_PREFIX = "[Mapper.processLinkFileEvent] ";

        String loc = relativeFileLoc.getLocation();

        /* Drop the trailing .link */
        FileLocation impliedRelativeLoc;
        try {
            impliedRelativeLoc = new FileLocation(loc.substring(0, loc.length()
                    - Constants.getLinkFileSuffix().length()));
        } catch (FileLocationException e) {
            log(LOG_PREFIX + "event was skipped: " + e);
            return;
        }

        /* path for either a symbolic link or a normal directory */
        File symFSFile = getSymFSFile(impliedRelativeLoc);

        File resolvedFile = null;
        if (FileChangeSet.ADDED == event || FileChangeSet.MODIFIED == event) {

            LinkFile lf;
            try {
                lf = new LinkFile(sourceFile);
                if (!LinkFile.WPS_PROTOCOL.equals(lf.getProtocol())) {
                    log(LOG_PREFIX + "unhandled protocol: " + lf.toString());
                    return;
                }
            } catch (Exception e) {
                log(LOG_PREFIX + "event was skipped: " + e);
                return;
            }

            try {
                /* resolve to somewhere in some symbolic filesystem */
                resolvedFile = resolver.resolve(lf);
            } catch (Exception e) {
                log(LOG_PREFIX + "exception while resolving LinkFile: " + e);
                return;
            }
        }

        switch (event) {

        case FileChangeSet.ADDED:
            // System.out.println(LOG_PREFIX + "FileUtilities.makeSymbolicLink (
            // " + resolvedFile + ", " + symFSFile
            // + " )");

            if (!symbolicLinkWouldBeSafe(resolvedFile, symFSFile)) {
                log(LOG_PREFIX + "LinkFile ADDED event was skipped because it was not safe, " + "resolvedFile: '"
                        + resolvedFile.getAbsolutePath() + "', symFSFile: '" + symFSFile.getAbsolutePath() + "'");
            } else {
                if (!FileUtilities.makeSymbolicLink(resolvedFile, symFSFile))
                    log(LOG_PREFIX + "failed to make symbolic link: " + symFSFile);
            }

            break;

        case FileChangeSet.MODIFIED:
            /*
             * The existing symlink must be removed in the case of a directory
             * to prevent the new symlink from being created inside the linked
             * directory instead of replacing the symlink.
             * 
             * When the link is to a file we follow the same tactic because
             * there is no reason not to although it is not neccessary to delete
             * the link first.
             * 
             * Note: When a link changes target the modification time of the
             * symlink is reported to be the same as the modification time of
             * the target file. This would mean a link file changed to target a
             * different file with the same length and same modification time
             * would not be detected by a DirectoryMonitor.
             * 
             * Note: The deletion is inside the existence test because a link
             * file will generate a MODIFIED event when it is corrected. The
             * addition of an incorrect link file will not have created a
             * symlink to replace.
             */
            if (symFSFile.exists()) {
                if (!symFSFile.delete()) {
                    log(LOG_PREFIX + "failed to delete symbolic link before adding it: " + symFSFile);
                    return;
                }
            }

            if (!symbolicLinkWouldBeSafe(resolvedFile, symFSFile)) {
                log(LOG_PREFIX + "LinkFile MODIFIED event was skipped because it was not safe, " + "resolvedFile: '"
                        + resolvedFile.getAbsolutePath() + "', symFSFile: '" + symFSFile.getAbsolutePath() + "'");
            } else {
                if (!FileUtilities.makeSymbolicLink(resolvedFile, symFSFile))
                    log(LOG_PREFIX + "failed to replace symbolic link: " + symFSFile);
            }

            break;

        case FileChangeSet.DELETED:
            if (!symFSFile.delete())
                log(LOG_PREFIX + "failed to delete symbolic link: " + symFSFile);

            break;

        default:
            log(LOG_PREFIX + "invalid FileChangeSet event value: " + event);
        }
    }

    /**
     * 
     * @return true if creating a symbolic link pointing to
     *         <code>existingFile</code> as <code>proposedSymbolicLink</code>
     *         or in <code>proposedSymbolicLink</code> if
     *         <code>proposedSymbolicLink</code> is a directory would not
     *         result in a directory being contained within itself or replacing
     *         itself.
     * 
     * The cases below shown sample outcomes and explain the algorithm.
     * 
     * Assume we have this system of directories where each letter (A, B, C, D,
     * G) is a directory. The path separators are not shown
     * 
     * <pre>
     *   
     *    
     *                A
     *                AB
     *                ABC
     *                ABCD
     *                AG
     *     
     *    
     * </pre>
     * 
     * This table shows the the symbolic links that would be created. Read it as
     * "what directory results from making a link with the name 'proposed' that
     * links to 'existing'". 'H' is always a name that does not already exist.
     * 
     * <pre>
     *   
     *    
     *                        |proposed
     *                        |
     *               existing | A       AB      ABC     ABCD    AG      AH      ABH
     *               ---------|---------------------------------------------------------
     *               A        | AA      ABA     ABCA    ABCDA   AGA     AH      ABH 
     *               AB       | AB      ABB     ABCB    ABCDB   AGB     AH      ABH
     *               ABC      | AC      ABC     ABCC    ABCDC   AGC     AH      ABH
     *               ABCD     | AD      ABD     ABCD    ABCDD   AGD     AH      ABH
     *               AG       | AGA     ABG     ABCG    ABCDG   AGG     AH      ABH
     *               
     *     This table shows the combinations that are not allowed and why
     *     
     *       . : the link is allowed
     *       S : directory is either directly or indirectly contained within itself
     *       R : directory replaced itself in the same parent directory
     *       
     *                        |proposed
     *                        |
     *               existing | A       AB      ABC     ABCD    AG      AH      ABH
     *               ---------|---------------------------------------------------------
     *               A        | S       S       S       S       S       S       S 
     *               AB       | R       S       S       S       .       .       S
     *               ABC      | .       R       S       S       .       .       .
     *               ABCD     | .       .       R       S       .       .       .
     *               AG       | R       .       .       .       S       .       .
     *               
     *     
     *    
     * </pre>
     * 
     */
    public boolean symbolicLinkWouldBeSafe(File existingFile, File proposedSymbolicLink) {

        File maybeAdjustedProposedSymLink = proposedSymbolicLink;
        /*
         * correct the proposed name because the link will be created within the
         * directory
         */
        if (proposedSymbolicLink.isDirectory())
            maybeAdjustedProposedSymLink = new File(proposedSymbolicLink, existingFile.getName());

        // System.out.println("Mapper: existingFile: " + existingFile);
        // System.out.println("Mapper; proposedSymbolicLink: " +
        // proposedSymbolicLink);
        // System.out.println("Mapper; maybeAdjustedProposedSymLink: " +
        // maybeAdjustedProposedSymLink);

        // List existingElements =
        // FileUtilities.pathElements(existingFile.getAbsolutePath());
        // List proposedElements =
        // FileUtilities.pathElements(maybeAdjustedProposedSymLink.getAbsolutePath());

        // when existingFile is a file then it can go anywhere but not in the
        // same directory with the same name
        if (maybeAdjustedProposedSymLink.equals(existingFile))
            return false;

        /*
         * Adding a link to a file can not cause a directory to be contained
         * within itself. The case of a link to a file and the file being the
         * same is handled above.
         */
        if (existingFile.isFile())
            return true;

        /*
         * If the existingFile (which must be a directory at this point) is a
         * direct or indirect parent return false. Walk up the path to find out.
         */
        File curDir = maybeAdjustedProposedSymLink;

        int possibleParentPathLength = existingFile.getAbsolutePath().length();
        while (curDir.getAbsolutePath().length() > possibleParentPathLength) {
            File parent = curDir.getParentFile();
            if (parent.equals(existingFile))
                return false;
            curDir = parent;
        }

        return true;
    }

    private void processEvent(int event, File sourceFile, FileLocation relativeFileLoc) {

        /* path for either a symbolic link or a normal directory */
        File symFSFile = getSymFSFile(relativeFileLoc);

        String LOG_PREFIX = "[Mapper.processEvent] ";

        switch (event) {

        case FileChangeSet.ADDED:
            // System.out.println("Mapper:ADDED:" + sourceFile );
            if (sourceFile.isFile()) {
                /*
                 * I did not add a use of #symbolicLinkWouldBeSafe here because
                 * the source file is not a directory and despite the fact that
                 * this method detects an attempt at a file being linked to
                 * itself this is not the problem that was being experienced.
                 * The problem was directories within themselves. Even if an
                 * attempt to link a file to itself is made it will fail > ln
                 * -sf file.x file.x ln: `file.x' and `file.x' are the same file
                 * 
                 */
                if (!FileUtilities.makeSymbolicLink(sourceFile, symFSFile))
                    log(LOG_PREFIX + "failed to make symbolic link: " + symFSFile);
            }

            if (sourceFile.isDirectory()) {
                if (!symFSFile.mkdir())
                    log(LOG_PREFIX + "failed to make directory: " + symFSFile);
            }

            break;

        case FileChangeSet.MODIFIED:
            // System.out.println("Mapper:MODIFIED:" + sourceFile );
            break;

        case FileChangeSet.DELETED:
            // System.out.println("Mapper:DELETED:" + sourceFile );
            if (!symFSFile.delete())
                log(LOG_PREFIX + "failed to delete symbolic link: " + symFSFile);

            break;

        default:
            log(LOG_PREFIX + "invalid FileChangeSet event value: " + event);
        }

    }

    /**
     * The <code>Resolver</code> to use to get a File from a LinkFile with.
     * This allows Mapper objects to remain unaware of the physical arragement
     * of files in other channels.
     */
    private Resolver resolver = null;

    public void setResolver(Resolver resolver) {

        if (resolver == null)
            throw new IllegalArgumentException("resolver was null");

        this.resolver = resolver;
    }

    /**
     * Build a list of files within each directory in the list of directories
     * which need to be recreated due to possible dependencies on other files
     * which have changed.
     * 
     * At the time of writing (02-Dec-4) the only dependent files were JSPs
     * which depend on files in _include, _application and _presentation
     * directories.
     * 
     * @param apexList
     *            An <code>ApexList</code> containing the directories to issue
     *            MODIFIED events for.
     * @return A <code>FileChangeSet</code> suitable for invoking
     *         {@link #dispatchEvents} with. If there are no possible changes to
     *         dependent files then a <code>FileChangeSet</code> with zero
     *         entries is returned.
     */
    protected FileChangeSet getDependencyEvents(ApexList apexList) {

        Set dirs = apexList.getDirectories();

        FileChangeSet fcs = new FileChangeSet();

        if (dirs.size() == 0)
            return fcs;

        /*
         * We need to issue MODIFIED events for all .html files in the symbolic
         * file system which can be the source of a directly servable JSP.
         */
        StandardFilePredicate jspPredicate = new StandardFilePredicate();
        /* files which exist which end in .html... */
        jspPredicate.setWantFiles(true);
        jspPredicate.setIncludeFileSuffixes(Collections.singleton(".html"));
        /*
         * ... which are not inside any of the reserved directories and not in
         * the _include directory. The update was triggered by a change to one
         * or more files in these directories.
         */
        Set excludeDirs = Constants.getNonHtmlDirectoryNames();
        excludeDirs.add(Constants.getIncludeDirectoryName());
        jspPredicate.setExcludeDirectoryNames(excludeDirs);

        for (Iterator it = dirs.iterator(); it.hasNext();) {
            File dir = new File((String) it.next());
            Set results = FileUtilities.find(dir, (FilePredicate) jspPredicate);
            for (Iterator rIt = results.iterator(); rIt.hasNext();)
                fcs.fileModified((File) rIt.next());
        }
        // System.out.println("dispatchDependencyEvents: FCS: " + fcs );
        return fcs;
    }

    /**
     * Return true if the relative path includes any directory which contains
     * files which other files depend on as a parent somewhere.
     * 
     * These are _include _application _presentation _data
     * 
     * @param floc
     *            A <code>FileLocation</code> object representing the path to
     *            test.
     */
    protected boolean isDependencyFile(FileLocation floc) {
        String path = floc.getLocation();
        /* not interested in the directory itself */
        if (path.endsWith(Constants.getIncludeDirectoryName()))
            return false;
        if (path.endsWith(Constants.getApplicationDirectoryName()))
            return false;
        if (path.endsWith(Constants.getPresentationDirectoryName()))
            return false;
        if (path.endsWith(Constants.getDataDirectoryName()))
            return false;

        // FIXME: use a FilePredicate. A directory '__include' will give a false
        // positive here.
        /*
         * If the path includes one of these directories it will include the
         * trailing slash due to the test above
         */
        return (path.indexOf(Constants.getIncludeDirectoryName() + File.separator) != -1)
                || (path.indexOf(Constants.getApplicationDirectoryName() + File.separator) != -1)
                || (path.indexOf(Constants.getPresentationDirectoryName() + File.separator) != -1)
                || (path.indexOf(Constants.getDataDirectoryName() + File.separator) != -1);

    }

    /**
     * Act as a FileListenerEvent source.
     * 
     * 
     */

    public void addFileListener(FileListener listener, FilePredicate fp) {
        PredicatedListener pl = new PredicatedListener(listener, fp);
        listeners.addElement(pl);
    }

    /*
     * public void removeFileListener (FileListener listener) {
     * listeners.removeElement ( listener ) ; }
     */

    public void notifyListeners(int event, FileListenerEvent fle) throws FileLocationException {
        /*
         * This avoids a race condition
         * 
         * (I kept this comment in from the Sun example I copied this from
         * although it won't affect xl as the construction of this list of
         * listeners is performed once at start up- jdb.)
         */
        Vector v = (Vector) listeners.clone();

        /*
         * Deliver a FileListenerEvent to the first listener able to handle it.
         */
        for (int i = 0; i < v.size(); i++) {
            PredicatedListener pl = (PredicatedListener) v.elementAt(i);
            FileListener f = pl.getListener();
            FilePredicate fp = pl.getPredicate();

            if (fp.accept(fle.getLocation())) {
                /*
                 * System.out.println ("FilePredicate true for file: " +
                 * fle.getLocation () ); System.out.println ("FilePredicate:\n" +
                 * fp); System.out.println ("FileListener:\n" + f );
                 */
                notifyListener(event, f, fle);
                break;
            }
        }
    }

    public void notifyListener(int event, FileListener fl, FileListenerEvent fle) {

        switch (event) {

        case FileChangeSet.ADDED:
            fl.fileAdded(fle);
            break;

        case FileChangeSet.MODIFIED:
            fl.fileModified(fle);
            break;

        case FileChangeSet.DELETED:
            fl.fileDeleted(fle);
            break;

        case FileChangeSet.TARGET_ADDED:
            fl.targetFileAdded(fle);
            break;

        case FileChangeSet.TARGET_DELETED:
            fl.targetFileDeleted(fle);
            break;

        default:
            log("Mapper:Invalid FileChangeSet event value: " + event);
        }

    }

    /**
     * Set the default debug level 0 - 99
     * 
     * FIXME: this is a crappy little hack, try to remove. See comment above.
     * 
     * @param debug
     *            The required default debug level in the range 0 - 99.
     */
    public static void setDefaultDebugLevel(int debug) {
        /* A negative arg is a mistake; go large in response */
        _classLevelDebug = debug >= 0 ? debug : 99;
    }

    /**
     * Set the debug level 0 - 99
     * 
     * @param debug
     *            The required debug level in the range 0 - 99.
     */
    public void setDebugLevel(int debug) {
        /* A negative arg is a mistake; go large in response */
        _debug = debug >= 0 ? debug : 99;
    }

    /**
     * FIXME: use proper logger passed in at construction.
     */
    private void log(String msg) {
        System.out.println( /* this.toString () + "\n" + */msg);
    }

    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("m_contextPath", m_contextPath).append(
                "m_SourceFilesystemLocation", m_SourceFilesystemLocation).append("m_SymbolicFilesystemLocation",
                m_SymbolicFilesystemLocation).append("listeners", listeners).append("_debug", _debug).toString();

    }

    private class PredicatedListener {

        private FileListener _fileListener;

        private FilePredicate _filePredicate;

        protected PredicatedListener(FileListener listener, FilePredicate fp) {
            _fileListener = listener;
            _filePredicate = fp;
        }

        protected FileListener getListener() {
            return _fileListener;
        }

        protected FilePredicate getPredicate() {
            return _filePredicate;
        }
    }

    /**
     * This class builds a list of deepest top-most directories from added
     * directories.
     * 
     * eg. If these directories are added
     * 
     * /a /a/b /a/c /foo/bar/ /foo/bar/d /foo/bar/e
     * 
     * the list of deepest top-most directories is
     * 
     * /a /foo/bar
     * 
     * as all directories encountered are either one of these directories or
     * contained within one of this directories.
     * 
     * FIXME: This will not work with these directories
     * 
     * /foo /foobar
     * 
     * 'foobar' will not be collected.
     * 
     * FIXME: This will not work when these directories are added in this order
     * 
     * /foo/bar /foo
     * 
     * because both will be added to the set of apexes. /foo/bar should be
     * removed on encountering /foo. I have a suspision that add ( File ) is
     * always invoked with the shortest paths first. Have a look at the
     * invocations of add ( File ) and create a test case.
     */
    private class ApexList {

        private Set _apexes;

        protected Iterator _iterator;

        public ApexList() {
            _apexes = new TreeSet();
        }

        public void add(File file) {

            String path = file.getAbsolutePath();

            Iterator it = _apexes.iterator();
            while (it.hasNext()) {
                String apex = (String) it.next();
                if (path.startsWith(apex))
                    return;
            }
            _apexes.add(path);
        }

        public Set getDirectories() {
            return _apexes;
        }

        public Iterator iterator() {
            _iterator = _apexes.iterator();
            return _iterator;
        }

        public boolean hasNext() {
            return _iterator.hasNext();
        }

        public Object next() throws NoSuchElementException {
            return _iterator.next();
        }

    }
}
