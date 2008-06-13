package tests.org.ntropa.build;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.FileDescriptionList;
import org.ntropa.build.FileDescriptionList.FD;

public class FileDescriptionListTest extends TestCase {

    public FileDescriptionListTest(String testName) {
        super(testName);
    }

    public static Test suite() {

        TestSuite suite = new TestSuite(FileDescriptionListTest.class);
        return suite;
    }

    public void testIterationWithNoItems() {

        FileDescriptionList fdl = new FileDescriptionList();
        int i = 0;
        for (FD fd : fdl) {
            i++;
        }
        assertEquals("Zero items encountered in iteration over empty list", 0, i);
    }

    public void testIterationWithManyItems() {

        List<FD> fds = new LinkedList<FD>();
        fds.add(new FD("/home/projects/site-a/index.html", 1, 2, false));
        fds.add(new FD("/home/projects/site-a/_include", 10, 20, false));
        fds.add(new FD("/home/projects/site-a/_include/templates.html", 100, 200, true));

        FileDescriptionList fdl = new FileDescriptionList();
        for (FD fd : fds) {
            fdl.add("path-" + fd.getTimeStamp(), fd.getTimeStamp(), fd.getSize(), fd.isExists());
        }

        Set actualSizes = new HashSet();
        int i = 0;
        for (FD fd : fdl) {
            i++;
            actualSizes.add(fd.getSize());
        }

        /* L required to make equality test work. */
        Set expectedSizes = new HashSet();
        expectedSizes.add(2L);
        expectedSizes.add(20L);
        expectedSizes.add(200L);

        assertEquals(expectedSizes, actualSizes);
    }

    public void testFDGetter() {
        FileDescriptionList fdl = new FileDescriptionList();
        fdl.add("path-1", 7, 77, true);
        fdl.add("path-2", 88);

        assertEquals(7, fdl.getFD("path-1").getTimeStamp());
        assertEquals(88, fdl.getFD("path-2").getTimeStamp());
        assertNull(fdl.getFD("path-x"));
    }

}
