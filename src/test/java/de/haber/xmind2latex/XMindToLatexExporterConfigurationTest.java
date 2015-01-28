/*
 * #%L
 * XMind to Latex
 * %%
 * Copyright (C) 2014 Arne Haber
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.haber.xmind2latex;

import static de.haber.xmind2latex.XMindToLatexExporter.TEMPLATE_PACKAGE;
import static de.haber.xmind2latex.cli.CliParameters.INPUT;
import static de.haber.xmind2latex.cli.CliParameters.LEVEL;
import static de.haber.xmind2latex.cli.CliParameters.TEMPLATE_LEVEL;
import static de.haber.xmind2latex.cli.CliParameters.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import net.lingala.zip4j.io.ZipInputStream;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.haber.xmind2latex.XMindToLatexExporter.Builder;
import de.haber.xmind2latex.cli.CliParameters;
import de.haber.xmind2latex.help.ConfigurationException;

/**
 * Configuration tests for the {@link XMindToLatexExporter}. <br>
 * <br>
 * 
 * @author (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 */
public class XMindToLatexExporterConfigurationTest {
    
    @Test
    public void testConfigureEnvironment() {
        File in = new File("src/test/resources/content.xml");
         
        String expectedStart5 = TEMPLATE_PACKAGE + "env.startEnumerate";
        String expectedEnd5 = TEMPLATE_PACKAGE + "env.endEnumerate";
        String expectedStart6 = TEMPLATE_PACKAGE + "env.startItemize";
        String expectedEnd6 = TEMPLATE_PACKAGE + "env.endItemize";
        
        String[] args = new String[] {
                "-i", in.getPath(),
                
                "-e", "5", expectedStart5, expectedEnd5,
                "-e", "6", expectedStart6, expectedEnd6,
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            assertEquals("\\begin{enumerate}\n", exporter.getStartEnvironment(5));
            assertEquals("\\end{enumerate}\n", exporter.getEndEnvironment(5));
            assertEquals("\\begin{itemize}\n", exporter.getStartEnvironment(6));
            assertEquals("\\end{itemize}\n", exporter.getEndEnvironment(6));
        }
        catch (ParseException e) {
          e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureFaultyEnvironment() {
        File in = new File("src/test/resources/content.xml");
         
        String expectedStart5 = TEMPLATE_PACKAGE + "env.startEnumerate";
        String expectedEnd5 = TEMPLATE_PACKAGE + "env.endEnumerate";
        String expectedStart6 = TEMPLATE_PACKAGE + "env.startItemize";
        String expectedEnd6 = TEMPLATE_PACKAGE + "env.endItemize";
        
        String[] args = new String[] {
                "-i", in.getPath(),
                
                "-e", "thisShouldBeAnInteger", expectedStart5, expectedEnd5,
                "-e", "6", expectedStart6, expectedEnd6,
        };
        try {
            CliParameters.build(args);
            fail("ParseException expected.");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testConfigureForce() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedOut = in.getAbsolutePath().concat(".tex");
        
        String[] args = new String[] {
                "-i", in.getPath(),
                "-f"
        
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            assertTrue(exporter.isOverwriteExistingFile());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureForce2() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedOut = in.getAbsolutePath().concat(".tex");
        
        String[] args = new String[] {
                "-i", in.getPath(),
                "--force"
        
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            assertTrue(exporter.isOverwriteExistingFile());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureLevel() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedTemplate = TEMPLATE_PACKAGE + "paragraph"; 
        String[] args = new String[] {
                "-i", in.getPath(),
                "-l", "2", expectedTemplate
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(5, exporter.getTemplates().size());
            assertEquals(expectedTemplate, exporter.getTemplates().get(2));
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureLevelAndMaxLevelInvalid() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedTemplate = TEMPLATE_PACKAGE + "paragraph"; 
        String[] args = new String[] {
                "-" + INPUT, in.getPath(),
                "-" + LEVEL, "3", expectedTemplate,
                "-" + TEMPLATE_LEVEL, "2"
        };
        try {
            CliParameters.build(args);
            
            fail("ConfigurationException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ConfigurationException);
        }
    }
    
    @Test
    public void testConfigureLevelAndMaxLevelValid() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedTemplate = TEMPLATE_PACKAGE + "paragraph"; 
        String[] args = new String[] {
                "-" + INPUT, in.getPath(),
                "-" + LEVEL, "2", expectedTemplate,
                "-" + TEMPLATE_LEVEL, "2"
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(5, exporter.getTemplates().size());
            assertEquals(expectedTemplate, exporter.getTemplates().get(2));
            assertEquals(2, exporter.getMaxLevel());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureMaxLevel() {
        File in = new File("src/test/resources/content.xml");
         
        String[] args = new String[] {
                "-" + INPUT, in.getPath(),
                "-" + TEMPLATE_LEVEL, "2"
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(2, exporter.getMaxLevel());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureNegativeMaxLevel() {
        File in = new File("src/test/resources/content.xml");
         
        String[] args = new String[] {
                "-" + INPUT, in.getPath(),
                "-" + TEMPLATE_LEVEL, "-2"
        };
        try {
            CliParameters.build(args);
            fail("ParseException expected due to negative max level.");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testConfigureMaxLevelDefault() {
        File in = new File("src/test/resources/content.xml");
         
        String[] args = new String[] {
                "-" + INPUT, in.getPath(),
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(-1, exporter.getMaxLevel());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureLevelAboveMax() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedTemplate = TEMPLATE_PACKAGE + "paragraph"; 
        String[] args = new String[] {
                "-i", in.getPath(),
                "-l", "10", expectedTemplate
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(11, exporter.getTemplates().size());
            assertEquals(expectedTemplate, exporter.getTemplates().get(10));
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureLevelFail2() {
        File in = new File("src/test/resources/content.xml");
        String[] args = new String[] {
                "-i", in.getPath(),
                "-l", "Hallo", "foo"
        };
        try {
            CliParameters.build(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testConfigureLevelFailMissingArgument() {
        File in = new File("src/test/resources/content.xml");
        String[] args = new String[] {
                "-i", in.getPath(),
                "-l", "2"
        };
        try {
            CliParameters.build(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testConfigureEnvFailMissingArgument() {
        File in = new File("src/test/resources/content.xml");
        String[] args = new String[] {
                "-i", in.getPath(),
                "-e", "2"
        };
        try {
            CliParameters.build(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testConfigureInputFailMissingArgument() {
        File in = new File("src/test/resources/content.xml");
        String[] args = new String[] {
                "-i", in.getPath(),
                "-e", "2"
        };
        try {
            CliParameters.build(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testConfigureOutputFailMissingArgument() {
        File in = new File("src/test/resources/content.xml");
        String[] args = new String[] {
                "-i", in.getPath(),
                "-o"
        };
        try {
            CliParameters.build(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testConfigureTemplateLevelFailMissingArgument() {
        File in = new File("src/test/resources/content.xml");
        String[] args = new String[] {
                "-i", in.getPath(),
                "-" + TEMPLATE_LEVEL
        };
        try {
            CliParameters.build(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testConfigureLevelMax() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedTemplate = TEMPLATE_PACKAGE + "paragraph"; 
        String[] args = new String[] {
                "-i", in.getPath(),
                "-l", "5", expectedTemplate
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            assertEquals(6, exporter.getTemplates().size());
            assertEquals(expectedTemplate, exporter.getTemplates().get(5));
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureOut() {
        File in = new File("src/test/resources/content.xml");
        File out = new File("target/testout/result.tex");
        
        String[] args = new String[] {
                "-i", in.getPath(),
                "-o", out.getAbsolutePath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(out.getAbsolutePath(), exporter.getTargetFile().getAbsolutePath());
            assertFalse(exporter.isOverwriteExistingFile());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureOut2() {
        File in = new File("src/test/resources/content.xml");
        File out = new File("target/testout/result.tex");
        
        String[] args = new String[] {
                "--input", in.getPath(),
                "--output", out.getAbsolutePath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(out.getAbsolutePath(), exporter.getTargetFile().getAbsolutePath());
            assertFalse(exporter.isOverwriteExistingFile());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureIn() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedOut = in.getAbsolutePath().concat(".tex");
        
        String[] args = new String[] {
                "-i", in.getPath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            assertTrue(exporter.getxMindSourceAsStream() instanceof FileInputStream);
            FileInputStream fis = (FileInputStream) exporter.getxMindSourceAsStream();
            assertTrue(fis.getFD().valid());
            assertFalse(exporter.isOverwriteExistingFile());
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testFailConfigureNotExistingInput() {
        File in = new File("src/test/resources/content_doesNotExist.xml");
        
        String[] args = new String[] {
                "-i", in.getPath()
        };
        try {
            CliParameters.build(args);
            
            fail("IllegalArgumentException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
    
    @Test
    public void testFailConfigureInvalidZipInput() {
        File in = new File("src/test/resources/jms.7zip.xmind");
        
        String[] args = new String[] {
                "-i", in.getPath()
        };
        try {
            CliParameters.build(args);
            
            fail("ConfigurationException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ConfigurationException);
        }
    }
    
    @Test
    public void testConfigureIn2() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedOut = in.getAbsolutePath().concat(".tex");
        String[] args = new String[] {
                "--input", in.getPath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            FileInputStream fis = (FileInputStream) exporter.getxMindSourceAsStream();
            assertTrue(fis.getFD().valid());
            assertFalse(exporter.isOverwriteExistingFile());
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureInXMindSource() {
        File in = new File("src/test/resources/example.xmind");
        
        String expectedOut = in.getAbsolutePath().concat(".tex");
        
        String[] args = new String[] {
                "-i", in.getPath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            assertTrue(exporter.getxMindSourceAsStream().getClass().getName(), exporter.getxMindSourceAsStream() instanceof ZipInputStream);
            ZipInputStream fis = (ZipInputStream) exporter.getxMindSourceAsStream();
            assertNotNull(fis);
            assertFalse(exporter.isOverwriteExistingFile());
        }
        catch (Exception e) {
            for (Throwable t : e.getSuppressed()) {
                t.printStackTrace();
            }
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testEmptyConfigure() {
        String[] args = new String[] {};
        try {
            CliParameters.build(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            // this is expected
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testVersion() {
        String[] args = new String[] {"-" + VERSION};
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        
        PrintStream out = new PrintStream(result);
        PrintStream old_out = System.out;
        System.setOut(out);
        try {
            XMindToLatexExporter exp = CliParameters.build(args);
            assertNull(exp);
            String resultContent = new String(result.toByteArray());
            assertFalse(resultContent.isEmpty());
            assertTrue(resultContent.contains("version"));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("ParseException expected.");
            // this is expected
        }
        finally {
            System.setOut(old_out);
            try {
                result.close();
            }
            catch (IOException e) {
                fail(e.getMessage());
            }
        }
    }
    
    @Test
    public void testConfigureHelp1() {
        File in = new File("src/test/resources/content.xml");
        File result = new File("target/testout/helpmsg.txt");
        result.delete();
        result.getParentFile().mkdirs();
        
        String[] args = new String[] {
                "-i", in.getPath(),
                "-h"
        };
        try {
            result.createNewFile();            
            FileOutputStream os = new FileOutputStream(result);
            PrintStream out = new PrintStream(os);
            PrintStream old_out = System.out;
            System.setOut(out);
            CliParameters.build(args);
            String resultContent = FileUtils.readFileToString(result);
            assertFalse(resultContent.isEmpty());
            assertTrue(resultContent.contains("usage: xmind2latex"));
            System.setOut(old_out);
            os.close();
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureHelp2() {
        File in = new File("src/test/resources/content.xml");
        File result = new File("target/testout/helpmsg.txt");
        result.delete();
        result.getParentFile().mkdirs();
        
        String[] args = new String[] {
                "-i", in.getPath(),
                "-help"
        };
        try {
            result.createNewFile();            
            FileOutputStream os = new FileOutputStream(result);
            PrintStream out = new PrintStream(os);
            PrintStream old_out = System.out;
            System.setOut(out);
            CliParameters.build(args);
            
            String resultContent = FileUtils.readFileToString(result);
            assertFalse(resultContent.isEmpty());
            assertTrue(resultContent.contains("usage: xmind2latex"));
            System.setOut(old_out);
            os.close();
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testBuilderWithTemplateExceptionNonExistingTemplate() {
        File in = new File("src/test/resources/content.xml");
        Builder builder = new Builder(in);
        try {
            builder.withTemplate(2, "doesnotexist");
            fail("Expected " + TemplateNotExistsException.class.getName());
        }
        catch (Exception e) {
            assertEquals(TemplateNotExistsException.class, e.getClass());
        }
    }
    
    @Test
    public void testBuilderWithTemplateExceptionInvalidLevel() {
        File in = new File("src/test/resources/content.xml");
        Builder builder = new Builder(in);
        try {
            builder.withTemplate(-1, TEMPLATE_PACKAGE + "undefined");
            fail("Expected " + IllegalArgumentException.class.getName());
        }
        catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }
    
    @Test
    public void testBuilderWithEnvironmentTemplatesExceptionNonExistingStartTemplate() {
        File in = new File("src/test/resources/content.xml");
        Builder builder = new Builder(in);
        try {
            builder.withEnvironmentTemplates(2, "doesnotexist", TEMPLATE_PACKAGE + "undefined");
            fail("Expected " + TemplateNotExistsException.class.getName());
        }
        catch (Exception e) {
            assertEquals(TemplateNotExistsException.class, e.getClass());
        }
    }
    
    @Test
    public void testBuilderWithEnvironmentTemplatesExceptionNonExistingEndTemplate() {
        File in = new File("src/test/resources/content.xml");
        Builder builder = new Builder(in);
        try {
            builder.withEnvironmentTemplates(2, TEMPLATE_PACKAGE + "undefined", "doesnotexist");
            fail("Expected " + TemplateNotExistsException.class.getName());
        }
        catch (Exception e) {
            assertEquals(TemplateNotExistsException.class, e.getClass());
        }
    }
    
    @Test
    public void testBuilderWithEnvironmentTemplatesExceptionInvalidLevel() {
        File in = new File("src/test/resources/content.xml");
        Builder builder = new Builder(in);
        try {
            builder.withEnvironmentTemplates(-1, TEMPLATE_PACKAGE + "undefined", TEMPLATE_PACKAGE + "undefined");
            fail("Expected " + IllegalArgumentException.class.getName());
        }
        catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }
    
    @Test
    public void testBuilderConstructorNotExistingInputException() {
        File in = new File("doesnotexist.xml");
        try {
            new Builder(in);
            fail("Expected " + IllegalArgumentException.class.getName());
        }
        catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }
    
    @Test
    public void testBuilderConstructorNullInputException() {
        try {
            new Builder(null);
            fail("Expected " + NullPointerException.class.getName());
        }
        catch (Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
        }
    }
    
    @Test
    public void testBuilderWithTargetFileNullOutputException() {
        File in = new File("src/test/resources/content.xml");
        Builder builder = new Builder(in);
        try {
            builder.withTargetFile(null);
            fail("Expected " + NullPointerException.class.getName());
        }
        catch (Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
        }
    }
    
    @Test
    public void testBuilderWithMaxLevelException() {
        File in = new File("src/test/resources/content.xml");
        Builder builder = new Builder(in);
        try {
            builder.withMaxLevel(-2);
            fail("Expected " + IllegalArgumentException.class.getName());
        }
        catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }
    
}
