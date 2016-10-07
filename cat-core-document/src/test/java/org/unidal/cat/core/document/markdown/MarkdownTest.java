package org.unidal.cat.core.document.markdown;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.markdown4j.Markdown4jProcessor;
import org.unidal.helper.Files;

public class MarkdownTest {
   @Test
   public void testReadme() throws IOException {
      Markdown4jProcessor p = new Markdown4jProcessor();
      InputStream md = getClass().getResourceAsStream("README.md");
      InputStream html = getClass().getResourceAsStream("README.html");
      String actual = p.process(md);
      String expected = Files.forIO().readFrom(html, "utf-8");

      Assert.assertEquals(expected, actual);
   }
}
