package edu.kit.scc.test;

import edu.kit.scc.cdmi.filesystem.CapabilityFilesystemTest;
import edu.kit.scc.cdmi.filesystem.CdmiObjectFilesystemTest;
import edu.kit.scc.cdmi.filesystem.ContainerFilesystemTest;
import edu.kit.scc.cdmi.filesystem.DataObjectFilesystemTest;
import edu.kit.scc.cdmi.redis.CapabilityRedisTest;
import edu.kit.scc.cdmi.redis.CdmiObjectRedisTest;
import edu.kit.scc.cdmi.redis.ContainerRedisTest;
import edu.kit.scc.cdmi.redis.DataObjectRedisTest;
import edu.kit.scc.cdmi.rest.AuthorizationTest;
import edu.kit.scc.cdmi.rest.CapabilitiesTest;
import edu.kit.scc.cdmi.rest.CdmiObjectTest;
import edu.kit.scc.cdmi.rest.ContainerTest;
import edu.kit.scc.cdmi.rest.DataObjectTest;
import edu.kit.scc.cdmi.rest.DomainTest;
import edu.kit.scc.cdmi.rest.FilterJsonTest;
import edu.kit.scc.http.client.HttpClientTest;
import edu.kit.scc.utils.UtilsTest;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

@RunWith(Suite.class)
@Suite.SuiteClasses({CapabilityFilesystemTest.class, CdmiObjectFilesystemTest.class,
    ContainerFilesystemTest.class, DataObjectFilesystemTest.class, AuthorizationTest.class,
    CapabilitiesTest.class, CdmiObjectTest.class, ContainerTest.class, DataObjectTest.class,
    DomainTest.class, FilterJsonTest.class, HttpClientTest.class, UtilsTest.class,
    CapabilityRedisTest.class, CdmiObjectRedisTest.class, ContainerRedisTest.class,
    DataObjectRedisTest.class})
public class TestSuite {

  @AfterClass
  public static void destroy() throws IOException {
    Properties props = new Properties();
    InputStream is =
        ClassLoader.getSystemResourceAsStream("application-filesystem-test.properties");
    try {
      props.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    }

    String baseDirectoryName = props.getProperty("cdmi.data.baseDirectory");

    Path start = Paths.get(baseDirectoryName);
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
        if (ex == null) {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        } else {
          // directory iteration failed
          throw ex;
        }
      }
    });
  }
}
