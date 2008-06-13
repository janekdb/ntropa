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
 * Assembler.java
 *
 * Created on 22 October 2001, 12:36
 */

package org.ntropa.build.channel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.ntropa.build.Constants;
import org.ntropa.build.ContextPath;
import org.ntropa.build.DirectoryMirror;
import org.ntropa.build.DirectoryMonitor;
import org.ntropa.build.DirectoryMonitorException;
import org.ntropa.build.DirectoryPair;
import org.ntropa.build.DirectoryPairException;
import org.ntropa.build.FileChangeSet;
import org.ntropa.build.FileIgnorer;
import org.ntropa.build.FileListener;
import org.ntropa.build.FileMirror;
import org.ntropa.build.jsp.JSPBuilder;
import org.ntropa.build.mapper.Mapper;
import org.ntropa.build.mapper.MapperException;
import org.ntropa.build.mapper.Resolver;
import org.ntropa.utility.FilePredicate;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.StandardFilePredicate;

/**
 * A <code>StandardChannelMonitor</code> is reponsible for creating and
 * linking the objects that perform JSP updation on a channel.
 * 
 * @author jdb
 * @version $Id: StandardChannelMonitor.java,v 1.12 2003/10/02 10:51:50 jdb Exp $
 */
public class StandardChannelMonitor implements ChannelMonitor {

    /* This is the WebDAV upload folder */
    private File _inputFolder;

    /* This is the symbolic file system folder */
    private File _intermediateFolder;

    /* This is the output folder */
    private File _outputFolder;

    private DirectoryMonitor _inputMonitor;

    private DirectoryMonitor _outputMonitor;

    private ContextPath _cp;

    private final Charset _encoding;

    private Set _nonHtmlDirectoryNames;

    private Set _mirroredFiles;

    private Resolver _resolver;

    private Mapper _mapper;

    private static final String BUILD_STATUS = "build-status.properties";

    private static final String WEB_INF = "WEB-INF";

    private final File _buildStatusFile;

    /**
     * Creates new StandardChannelMonitor.
     * 
     * A <code>StandardChannelMonitor</code> is reponsible for creating and
     * linking the objects that perform JSP updation on a channel.
     * 
     * Create and connect the objects responsible for managing the files for a
     * channel.
     * 
     * For each channel the main objects are 1 x DirectoryMonitor 1 x JSPBuilder
     * 1 x DirectoryMirror 1 x FileIgnorer 1 x FileMirror
     * 
     * FIXME (sometime, low priority): If a file with an unknown type (eg .snd)
     * is added or modified, the FileListenerEvent is correctly handled by a
     * <code>FileIgnorer</code>. However when the file is deleted, the
     * predicate for FileIgnorer does not test true and the DirectoryMirror
     * receives the event. This is because the type of the object represented by
     * the path can not be determined after the file system object has been
     * deleted.
     * 
     * The DirectoryMirror object will survive being notified of the deletion of
     * a non-existant directory so it mainly a purist's concern.
     * 
     * 
     * Note: the file paths should be the paths to the actual end folders so the
     * channel id does not need to be suffixed. ie
     * 
     * input: /opt/xl/web/webdav/pg-ca intermediate: /opt/xl/web/sym/pg-ca
     * putput: /opt/xl/web/jsp/pg-ca
     * 
     * @param cp
     *            The ContextPath
     * @param encoding
     *            The file encoding to read and write files with.
     * @param inputFolder
     *            The directory to look for external changes to.
     * @param intermediateFolder
     *            The directory managed by the object to look for changes to.
     * @param outputFolder
     *            The destination directory for JSPs etc.
     * @param nonHtmlDirectoryNames
     *            The names of directories containinf files which are not
     *            directly converted into JSPs or other files. Typically
     *            paramater files.
     * @param mirroredFiles
     *            The set of files extensions for files which should be copied
     *            without change into the destination directory.
     * @param resolver
     *            The <code>Resolver</code> this
     *            <code>StandardChannelMonitor</code> will use when asking
     *            <code>Mapper</code> objects to handle link files
     *            (advice.html.link). This object is able to map inter-channel
     *            URIs such as wps://mba/advice.html, to physical file locations
     *            such as /opt/xl/web/webdav/mba/advice.html. This param is
     *            allowed to be null.
     */
    public StandardChannelMonitor(ContextPath cp, Charset encoding, File inputFolder, File intermediateFolder,
            File outputFolder, Set nonHtmlDirectoryNames, Set mirroredFiles, Resolver resolver) {
        _cp = cp;

        _encoding = encoding;

        /* take copies */
        _inputFolder = inputFolder.getAbsoluteFile();
        _intermediateFolder = intermediateFolder.getAbsoluteFile();
        _outputFolder = outputFolder.getAbsoluteFile();

        /*
         * The location of the build status File, which may not exist if no
         * changed have ever been made to this channel.
         */
        _buildStatusFile = new File(_outputFolder, BUILD_STATUS);

        /* take copies */
        _nonHtmlDirectoryNames = new TreeSet(nonHtmlDirectoryNames);
        _mirroredFiles = new TreeSet(mirroredFiles);

        _resolver = resolver;

    }

    public static interface ExceptionListener {

        void exception(Exception e) throws RuntimeException;

    }

    private ExceptionListener exceptionListener = new ExceptionListener() {

        public void exception(Exception e) {
            // TODO: Use a Logger
            e.printStackTrace(System.out);
        }
    };

    /**
     * 
     * @param listener
     *            A non-null <code>ExceptionListener</code>.
     */
    public void setExceptionListener(ExceptionListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener was null");

        this.exceptionListener = listener;
    }

    /**
     * Perform an update on a channel and return the number of changed input
     * files.
     * <p>
     * This method catches all Exceptions so the process does not stop.
     */
    public FileChangeSet update() {
        try {
            /* The side-effects of these two method invocations do the updates. */
            int inputFolderChangeCount = _inputMonitor.monitorFolder().size();
            int intermediateFolderChangeCount = _mapper.monitorLinkFolder();

            FileChangeSet ofcs = _outputMonitor.monitorFolder();
            if (ofcs.size() > 0)
                recordChangeSummary(ofcs);
            return ofcs;
            // return inputFolderChangeCount + intermediateFolderChangeCount;
        } catch (Exception e) {
            this.exceptionListener.exception(e);
        }
        return new FileChangeSet();
    }

    private void recordChangeSummary(FileChangeSet fcs) throws IOException {

        Date now = new Date();

        BuildStatus buildStatus = null;
        if (_buildStatusFile.exists())
            buildStatus = new BuildStatus(FileUtilities.readFile(_buildStatusFile));
        else
            buildStatus = new BuildStatus(now);

        if (fcs.getAdded().size() > 0)
            buildStatus.setAdditionDate(now);

        if (fcs.getModified().size() > 0)
            buildStatus.setModificationDate(now);

        if (fcs.getDeleted().size() > 0)
            buildStatus.setDeletionDate(now);

        FileUtilities.writeString(_buildStatusFile, buildStatus.toExternalForm());

    }

    public void init() throws ChannelMonitorException {

        /*
         * Construct a DirectoryMonitor for the WebDAV upload directory.
         * 
         * (Ignore certain files via a mask.)
         * 
         */
        File upload = _inputFolder.getAbsoluteFile();
        try {
            _inputMonitor = new DirectoryMonitor(upload, getInputFilter());
        } catch (DirectoryMonitorException e) {
            throw new ChannelMonitorException("Problem with input folder: " + e);
        }

        File output = _outputFolder.getAbsoluteFile();
        try {
            /* Do not specify a file filter to get all changes. */
            _outputMonitor = new DirectoryMonitor(output, getOutputFilter());
        } catch (DirectoryMonitorException e) {
            throw new ChannelMonitorException("Problem with output folder: " + e);
        }

        /*
         * Construct a mapper object to handle the creation of the symbolic
         * filesystem based on: 1. The WebDav upload directory 2. The link files
         * in the WebDAV directory
         * 
         * This mapper also handles changes to any files or directories linked
         * in from other channels.
         * 
         * IGNORE: (Note: Consider a multipass approach for IGNORE: updating the
         * symbolic filesystems to resolve rependencies. IGNORE: Watch out for
         * cycles.
         * 
         * IGNORE: Update channel A IGNORE: Channel A uses channel B -> update
         * channel B first IGNORE: etc
         * 
         * IGNORE: Consider using a 'ChannelChangeListener' to handle changes to
         * IGNORE: dependent channels. IGNORE: )
         * 
         */

        /*
         * Create a Mapper object.
         */
        try {
            DirectoryPair dp = new DirectoryPair(_inputFolder, _intermediateFolder);
            _mapper = new Mapper(_cp, dp);
            _mapper.setDebugLevel(0);
            if (_resolver != null)
                _mapper.setResolver(_resolver);
        } catch (DirectoryPairException e) {
            throw new ChannelMonitorException("Problem with DirectoryPair : " + e);
        } catch (MapperException e) {
            throw new ChannelMonitorException("Problem with Mapper : " + e);
        }

        _inputMonitor.addFileChangeSetListener(_mapper);

        /*
         * Construct a DirectoryPair with the source equal to the directory the
         * JSPBuilder should read files from, and the destination equal to the
         * directory the JSPBuilder should write and delete files to.
         * 
         * (This object is also used by the other FileListeners.)
         */

        DirectoryPair dp = null;
        try {
            dp = new DirectoryPair(_intermediateFolder, _outputFolder);
        } catch (DirectoryPairException e) {
            throw new ChannelMonitorException("Problem with DirectoryPair : " + e);
        }

        /* - - - - JSPBuilder - - - - */

        /*
         * Create a JSPBuilder with access to the channel id (and therefore the
         * channel data) and knowledge of the source and destination
         * directories.
         * 
         * Create an object which decides if a file is handled by the
         * JSPBuilder. The only files currently handled are *.html.
         * 
         * Add the JSPBuilder as a listener to the Mapper
         */
        FileListener jspbuilder = new JSPBuilder(_cp, dp, _encoding);

        StandardFilePredicate htmlFilePredicate = new StandardFilePredicate();

        /*
         * Initially the predicate required the file existed. However deleted
         * files were then excluded. We could add a setOmitDirectories () method
         * to FilePredicate. For now we rely on JSPBuilder not failing when a
         * directory ending .html is added/modifed/deleted. Archive code for
         * 'file must exist': htmlFilePredicate.setWantFiles ( true ) ;
         * htmlFilePredicate.setRoot ( symbolicFilesysChannelLocation ) ;
         */

        /* The file must have .html suffix ... */
        htmlFilePredicate.setIncludeFileSuffixes(Collections.singleton(".html"));
        /* ... and must not be in any of these directories */
        htmlFilePredicate.setExcludeDirectoryNames(_nonHtmlDirectoryNames);

        _mapper.addFileListener(jspbuilder, htmlFilePredicate);

        /* - - - - FileIgnorer - - - - */

        /*
         * Create a FileIgnorer.
         * 
         * Create an FilePredicate which decides if a file or directory is
         * handled by a FileIgnorer. The currently handled files are all files
         * and directories in a directory with one of these names and the
         * directories themselves.
         * 
         * _presentation _application _data
         * 
         * This prevents these directories and their content from being handled
         * by any other FileListener. If later a new file type needs a special
         * handler (such as an XML file in a _data directory) then the
         * FileListener should be added before this FileListener.
         * 
         * Add the FileIgnorer as a listener to the Mapper
         */
        FileListener fileIgnorer = new FileIgnorer();
        StandardFilePredicate ignorerPredicate = new StandardFilePredicate();
        /* Ignore any of these directories and any of the files within */
        ignorerPredicate.setIncludeDirectoryNames(_nonHtmlDirectoryNames);

        _mapper.addFileListener(fileIgnorer, ignorerPredicate);

        /* - - - - FileMirror - - - - */

        /*
         * Create a FileMirror with knowledge of the source and destination
         * directories.
         * 
         * Create an FilePredicate which decides if a file is handled by a
         * FileMirror. The files that should be handled includes
         * 
         * .bmp .css .gif .ico .jpg .jpeg .js .pdf .swf .zip
         * 
         * Add the FileMirror as a listener to the Mapper
         */
        FileListener fileMirror = new FileMirror(dp);
        StandardFilePredicate mirroredFilePredicate = new StandardFilePredicate();
        mirroredFilePredicate.setIncludeFileSuffixes(_mirroredFiles);
        _mapper.addFileListener(fileMirror, mirroredFilePredicate);

        /* - - - - FileIgnorer - - - - */

        /*
         * Ignore all other file types if the file exists.
         * 
         * This stops FileAdded and FileModified events being passed to the
         * DirectoryMirror
         * 
         * The type of the object can not be tested by DirectoryMirror. If we
         * tested for the path being a directory no events would ever be
         * received for deleted directories as the directory would not exist at
         * the time we needed to determine the type.
         */
        StandardFilePredicate fileExistsPredicate = new StandardFilePredicate();
        fileExistsPredicate.setWantFiles(true);
        fileExistsPredicate.setRoot(_intermediateFolder);
        /* reuse the earlier FileIgnorer */
        _mapper.addFileListener(fileIgnorer, fileExistsPredicate);

        /* - - - - DirectoryMirror - - - - */

        /*
         * Create a DirectoryMirror with knowledge of the source and destination
         * directories.
         * 
         * Create an FilePredicate which decides if a file is handled by a
         * DirectoryMirror. The only files currently handled are directories.
         * 
         * Add the DirectoryMirror as a listener to the Mapper
         */
        FileListener directoryMirror = new DirectoryMirror(_outputFolder);
        StandardFilePredicate directoryPredicate = new StandardFilePredicate();

        directoryPredicate.setExcludeFileSuffixes(_mirroredFiles);
        directoryPredicate.setRoot(_intermediateFolder);

        _mapper.addFileListener(directoryMirror, directoryPredicate);

    }

    /*
     * Return an input filter for the WebDAV upload directory.
     * 
     * 
     */
    private FilePredicate getInputFilter() {

        return new StandardInputFilter();

    }

    /**
     * 
     * @return A <code>FilePredicate</code> that returns true for everything
     *         other than the build status file.
     */
    private FilePredicate getOutputFilter() {
        return new FilePredicate() {

            public boolean accept(File f) {
                if (BUILD_STATUS.equals(f.getName()))
                    return false;
                /* WEB-INF and it's content will be ignored. */
                if (WEB_INF.equals(f.getName()))
                    return false;
                /* _include and it's content will be ignored. */
                if (Constants.getIncludeDirectoryName().equals(f.getName()))
                    return false;
                return true;
            }

            public boolean accept(String file) {
                return accept(new File(file));
            }
        };
    }
}
