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
 * FileUtilities.java
 *
 * Created on September 4, 2001, 2:41 PM
 */

package org.ntropa.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * General purpose file handling utilities.
 * 
 * @author jdb
 * @version $Id: FileUtilities.java,v 1.21 2005/02/23 16:20:35 jdb Exp $
 */
public class FileUtilities {

    private static final int BUFF_SZ = 32768;

    /**
     * Recursively delete all contents of directory. The directory doesn't have
     * to exist.
     * 
     * @param topDirectory
     *            the directory to kill
     */
    static public void killDirectory(File topDirectory) {

        if (!topDirectory.exists())
            return;

        List contents = Arrays.asList(topDirectory.listFiles());

        for (Iterator it = contents.iterator(); it.hasNext();) {
            File f = (File) it.next();

            if (f.isDirectory())
                killDirectory(f);
            else
                f.delete();
        }

        topDirectory.delete();
    }

    /**
     * Recursively delete all contents of directory. The directory doesn't have
     * to exist.
     * 
     * @param topDirectory
     *            the directory to kill
     */
    static public void killDirectory(String topDirectory) {
        killDirectory(new File(topDirectory));
    }

    /**
     * Count file path elements
     * 
     * Examples pathElementCount ( "/" ) = 0 pathElementCount ( "temp" ) = 1
     * pathElementCount ( "/temp/t.txt" ) = 2 pathElementCount ( "about/other/" ) =
     * 2
     * 
     */
    static public int pathElementCount(File file) {
        return pathElementCount(file.getAbsolutePath());
    }

    static public int pathElementCount(String path) {

        return pathElements(path).size();
        /*
         * int cnt = 0 ; StringTokenizer s = new StringTokenizer ( path,
         * File.separator ) ; while ( s.hasMoreTokens () ) { s.nextToken () ;
         * cnt++ ; }
         * 
         * return cnt ;
         */
    }

    /**
     * Return a <code>List</code> of file path elements.
     * 
     * (Does not convert to absolute path).
     * 
     * @param file
     *            <code>File</code>
     */
    static public List pathElements(File file) {
        return pathElements(file.getPath());
    }

    static public List pathElements(String path) {
        List l = new LinkedList();

        StringTokenizer s = new StringTokenizer(path, File.separator);
        while (s.hasMoreTokens())
            l.add(s.nextToken());

        return l;
    }

    /**
     * Make a soft link from sourcename to destname. (Makes the same assumptions
     * as the 'ln' command.)
     * 
     * FIXME: What happens of the dest file name is "; rm -rf /" ?
     * 
     * @param sourcename
     *            a <code>File</code> object of the file to make the link from
     * @param destname
     *            a <code>File</code> object of the file to make the link to
     * @return true indicating success, false if there was a fault.
     */
    static public boolean makeSymbolicLink(File sourcename, File destname) {

        /*
         * ln options. -s: make symbolic link -f: force removal of existing
         * links
         */
        String[] progarray = new String[4];

        progarray[0] = "ln";
        progarray[1] = "-sf";
        progarray[2] = sourcename.getAbsolutePath();
        progarray[3] = destname.getAbsolutePath();

        return exec(progarray);

    }

    /**
     * 
     * @param f
     *            A non-null <code>File</code> object to test
     * @return True if the file is a symbolic link where the target file is
     *         missing.
     */
    public static boolean isSymbolicLinkWithMissingTarget(File f) {
        if (f == null)
            throw new IllegalArgumentException("File was null");
        /*
         * When the last modification time is 0 and the file corresponding to
         * the path does not exist we are dealing with a dangling symlink.
         * Checking the file's existence is better than confirming
         * f.getAbsolutePath != f.getCanonicalPath because if the parent
         * directory is a symlink that these values will always be different.
         */
        if (f.lastModified() == 0 && !f.exists())
            return true;

        return false;
    }

    /**
     * Make a copy of a file or directory link. Will overwrite existing files.
     * (Makes the same assumptions as the 'cp' command.)
     * 
     * FIXME: What happens of the dest file name is "; rm -rf /" ?
     * 
     * @param sourcename
     *            a <code>File</code> object of the file to copy
     * @param destname
     *            a <code>File</code> object of the place to copy to
     * @return true indicating success, false if there was a fault.
     */
    static public boolean copy(File sourcename, File destname) {

        if (!sourcename.exists())
            return false;

        /*
         * ln options. -R: Recursive copy of directory and contents, no effect
         * when source is a file
         */
        String[] progarray = new String[4];

        progarray[0] = "cp";
        progarray[1] = "-R";
        progarray[2] = sourcename.getAbsolutePath();
        progarray[3] = destname.getAbsolutePath();

        return exec(progarray);

    }

    /**
     * Update the timestamp of a file or directory. (Makes the same assumptions
     * as the 'touch' command.)
     * 
     * FIXME: What happens of the file name is "; rm -rf /" ?
     * 
     * @param file
     *            A <code>File</code> object of the file to 'touch'
     * @return true indicating success, false if there was a fault.
     */
    static public boolean touch(File file) {

        String[] progarray = new String[2];

        progarray[0] = "touch";
        progarray[1] = file.getAbsolutePath();

        return exec(progarray);

    }

    /**
     * Use the chmod command on a file or directory. The command is
     * non-recursive.
     * 
     * @param file
     *            The non-null file or directory to operate on.
     * @param modes
     *            The modes in the same form accepted by the Unix command chmod:
     *            660, a+x, etc.
     * @return
     */
    public static boolean chmod(File file, String modes) {
        if (file == null)
            throw new IllegalArgumentException("file was null");
        if (modes == null)
            throw new IllegalArgumentException("modes was null");

        String[] progarray = new String[3];
        progarray[0] = "chmod";
        progarray[1] = modes;
        progarray[2] = file.getAbsolutePath();

        return exec(progarray);
    }

    /**
     * Make a set of <code>File</code> objects in the given directory which
     * match the given FilePredicate.
     * 
     * @param rootDir
     *            The directory to start the find in.
     * @return filePredicate A <code>FilePredicate</code> used to test each
     *         file for inclusion in the result set.
     */
    static public Set find(File rootDir, FilePredicate filePredicate) {

        if (!rootDir.isDirectory())
            return Collections.EMPTY_SET;

        Set resultSet = new TreeSet();
        find(rootDir, filePredicate, resultSet);

        return resultSet;
    }

    /** Helper method */
    static protected void find(File rootDir, FilePredicate filePredicate, Set resultSet) {

        File fileList[] = rootDir.listFiles();

        if (fileList.length == 0)
            return;

        for (int i = 0; i < fileList.length; i++) {
            File f = fileList[i];
            if (filePredicate.accept(f))
                resultSet.add(f);
            if (f.isDirectory())
                find(f, filePredicate, resultSet);
        }
    }

    /**
     * Extracts a zip file into a given directory.
     * <p>
     * Invokes the unzip command
     * </p>
     * <p>
     * unzip is invoked with the -q option to prevent output to stdout. Without
     * this the call to #exec would never return, presumably because the buffer
     * for the input stream from the Process object had filled and the unzip
     * command had blocked. This failure was not present in Java 1.3 and was
     * present in Java 1.4.2 and 1.5.0
     * </p>
     * 
     * @return true indicating success, false if there was a fault.
     */
    static public boolean extractZip(File zipFile, File destinationDirectory) {

        String[] progarray = new String[5];

        progarray[0] = "unzip";
        progarray[1] = "-q";
        progarray[2] = zipFile.getAbsolutePath();
        progarray[3] = "-d";
        progarray[4] = destinationDirectory.getAbsolutePath();

        return exec(progarray);
    }

    /**
     * Hide messy details of system call here.
     */
    static protected boolean exec(String[] progarray) {

        Process p;
        try {
            p = Runtime.getRuntime().exec(progarray);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        int status;
        try {
            status = p.waitFor();
        } catch (InterruptedException e) {
            status = -1;
        }

        // See testMakeSymbolicLink for further comments.
        // Closing these streams solved the problem but
        // a test case exposing the problem was not found.
        // See: http://www.vnoel.com/content/view/51/49/
        // java.io.IOException: Too many open files
        try {
            p.getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            p.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            p.getErrorStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return status == 0;

    }

    /**
     * Read a file and return the content as a String.
     * 
     * @param file
     *            a <tt>File</tt> object representing the file to read
     * @return A <tt>String</tt> with the content of the file
     */
    static public String readFile(File file) throws IOException {
        StringBuffer sb = new StringBuffer();
        readFile(file, sb);
        return sb.toString();
    }

    /**
     * Read a file and return the content as a String.
     * 
     * @param file
     *            a <tt>File</tt> object representing the file to read
     * @param encoding
     *            A non-null encoding, such as "ISO-8859-1" or "UTF-8"
     * @return A <tt>String</tt> with the content of the file
     */
    static public String readFile(File file, Charset encoding) throws IOException {
        StringBuffer sb = new StringBuffer();
        readFile(file, sb, encoding);
        return sb.toString();
    }

    /**
     * Read a file into a StringBuffer. This method does not check the
     * StringBuffer is empty thus allowing the file to be appended.
     * 
     * @param file
     *            a <tt>File</tt> object representing the file to read
     * @param sb
     *            a <tt>StringBuffer</tt> object to append the data to
     */
    static public void readFile(File file, StringBuffer sb) throws IOException {

        Reader in = new BufferedReader(new FileReader(file));
        read(in, sb);

    }

    /**
     * Read a file into a StringBuffer. This method does not check the
     * StringBuffer is empty thus allowing the file to be appended.
     * 
     * @param file
     *            a <tt>File</tt> object representing the file to read
     * @param encoding
     *            A non-null encoding, such as "ISO-8859-1" or "UTF-8"
     * @param sb
     *            a <tt>StringBuffer</tt> object to append the data to
     */
    static public void readFile(File file, StringBuffer sb, Charset encoding) throws IOException {

        Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        read(in, sb);

    }

    static private void read(Reader in, StringBuffer sb) throws IOException {

        try {
            /* Read the whole file */
            int count;
            char[] cbuf = new char[BUFF_SZ];
            while ((count = in.read(cbuf, 0, cbuf.length)) > 0)
                sb.append(cbuf, 0, count);
        } finally {
            /* Is this really neccessary? */
            in.close();
        }
    }

    /**
     * (Over)Write a file from a String.
     * 
     * @param file
     *            a <code>File</code> object representing the file to write to
     * @param sb
     *            a <code>String</code> object to write to the file
     */
    static public void writeString(File file, String s) throws IOException {

        Writer out = new BufferedWriter(new FileWriter(file));
        write(out, s);

    }

    /**
     * (Over)Write a file from a String.
     * 
     * @param file
     *            a <code>File</code> object representing the file to write to
     * @param sb
     *            a <code>String</code> object to write to the file
     * @param encoding
     *            A non-null encoding, such as "ISO-8859-1" or "UTF-8"
     */
    static public void writeString(File file, String s, Charset encoding) throws IOException {

        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
        write(out, s);

    }

    static private void write(Writer out, String s) throws IOException {

        try {
            /* Write the whole string */
            out.write(s);
            out.close();
        } finally {
            /* Is this really neccessary? */
            out.close();
        }
    }

    /**
     * Create and return an implementation of FileFilter.
     * <p>
     * The returned object will return true when the file name ends with
     * .extension and has at least one character before the dot.
     * 
     * @param extension
     *            The extension to check for.
     * @return An implementation of FilenameFilter
     */
    public static FilenameFilter getExtensionFilenameFilter(String extension) {
        return new ExtensionFilenameFilter(extension);
    }

    private static class ExtensionFilenameFilter implements FilenameFilter {

        private String extension;

        private ExtensionFilenameFilter(String extension) {
            this.extension = extension;
        }

        public boolean accept(File dir, String name) {
            return (name.length() >= (2 + extension.length()) && name.endsWith("." + extension));
        }
    }

    /**
     * @param file
     *            a <code>File</code> to test
     * @throws FileNotFoundException
     *             if the <code>File</code> does not exist or is not a file.
     * @throws IOException
     *             if the <code>File</code> exists and is a file but can not
     *             be read
     */
    public static void ensureFileIsReadable(File file) throws FileNotFoundException, IOException {
        if (!file.exists())
            throw new FileNotFoundException("The file was missing: " + file);
        if (!file.isFile())
            throw new FileNotFoundException("The file was not a file: " + file);
        if (!file.canRead())
            throw new IOException("The file can not be read: " + file);
    }

    /**
     * @param file
     *            a <code>String</code> representing a file to test
     * @throws FileNotFoundException
     *             if the file does not exist or is not a file.
     * @throws IOException
     *             if the file exists and is a file but can not be read
     */
    public static void ensureFileIsReadable(String file) throws FileNotFoundException, IOException {
        ensureFileIsReadable(new File(file));
    }

    /**
     * @param directory
     *            a <code>File</code> to test
     * @throws FileNotFoundException
     *             if the <code>File</code> does not exist or is not a
     *             directory.
     * @throws IOException
     *             if the <code>File</code> exists and is a directory but can
     *             not be read
     */
    public static void ensureDirectoryIsReadable(File directory) throws FileNotFoundException, IOException {
        if (!directory.exists())
            throw new FileNotFoundException("The directory was missing: " + directory);
        if (!directory.isDirectory())
            throw new FileNotFoundException("The directory was not a directory: " + directory);
        if (!directory.canRead())
            throw new IOException("The directory can not be read: " + directory);
    }

    /**
     * @param directory
     *            a <code>String</code> representing a directory to test
     * @throws FileNotFoundException
     *             if the <code>File</code> does not exist or is not a
     *             directory.
     * @throws IOException
     *             if the <code>File</code> exists and is a directory but can
     *             not be read
     */
    public static void ensureDirectoryIsReadable(String directory) throws FileNotFoundException, IOException {
        ensureDirectoryIsReadable(new File(directory));
    }

    /**
     * @param directory
     *            a <code>File</code> to test
     * @throws FileNotFoundException
     *             if the <code>File</code> does not exist or is not a
     *             directory.
     * @throws IOException
     *             if the <code>File</code> exists and is a directory but can
     *             not be written
     */
    public static void ensureDirectoryIsWritable(File directory) throws FileNotFoundException, IOException {
        if (!directory.exists())
            throw new FileNotFoundException("The directory was missing: " + directory);
        if (!directory.isDirectory())
            throw new FileNotFoundException("The directory was not a directory: " + directory);
        if (!directory.canWrite())
            throw new IOException("The directory can not be written: " + directory);
    }

    /**
     * @param directory
     *            a <code>String</code> representing a directory to test
     * @throws FileNotFoundException
     *             if the <code>File</code> does not exist or is not a
     *             directory.
     * @throws IOException
     *             if the <code>File</code> exists and is a directory but can
     *             not be written
     */
    public static void ensureDirectoryIsWritable(String directory) throws FileNotFoundException, IOException {
        ensureDirectoryIsWritable(new File(directory));
    }

    /**
     * @param dir
     *            The path to a directory to get a list of files for
     * @return A String array of file paths
     * @throws IllegalArgumentException
     *             if dir is null
     * @throws FileNotFoundException
     *             if dir is not a directory
     * 
     * FIXME: should this use a FileFilter to return just files? At present it
     * will return directories as well.
     */
    public static String[] listFilesInDirectory(String dir) throws FileNotFoundException, IllegalArgumentException {
        if (dir == null)
            throw new IllegalArgumentException("dir was null");
        File f = new File(dir);
        if (!f.isDirectory())
            throw new FileNotFoundException("The dir string entered was not a valid directory path: " + dir);

        String[] files = f.list();
        return files;
    }
}
