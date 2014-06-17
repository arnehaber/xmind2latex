/*
 * Copyright 2014 Arne Haber
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package de.haber.xmind2latex;

import static de.haber.xmind2latex.XMindToLatexExporter.TEMPLATE_FOLDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

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
         
        String expectedStart5 = TEMPLATE_FOLDER + "env.startEnumerate";
        String expectedEnd5 = TEMPLATE_FOLDER + "env.endEnumerate";
        String expectedStart6 = TEMPLATE_FOLDER + "env.startItemize";
        String expectedEnd6 = TEMPLATE_FOLDER + "env.endItemize";
        
        String[] args = new String[] {
                "-i", in.getPath(),
                
                "-e", "5", expectedStart5, expectedEnd5,
                "-e", "6", expectedStart6, expectedEnd6,
        };
        XMindToLatexExporter exporter;
        try {
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            assertEquals("\\begin{enumerate}\n", exporter.getStartEnvironment(5));
            assertEquals("\\end{enumerate}\n", exporter.getEndEnvironment(5));
            assertEquals("\\begin{itemize}\n", exporter.getStartEnvironment(6));
            assertEquals("\\end{itemize}\n", exporter.getEndEnvironment(6));
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureFaultyEnvironment() {
        File in = new File("src/test/resources/content.xml");
         
        String expectedStart5 = TEMPLATE_FOLDER + "env.startEnumerate";
        String expectedEnd5 = TEMPLATE_FOLDER + "env.endEnumerate";
        String expectedStart6 = TEMPLATE_FOLDER + "env.startItemize";
        String expectedEnd6 = TEMPLATE_FOLDER + "env.endItemize";
        
        String[] args = new String[] {
                "-i", in.getPath(),
                
                "-e", "thisShouldBeAnInteger", expectedStart5, expectedEnd5,
                "-e", "6", expectedStart6, expectedEnd6,
        };
        XMindToLatexExporter exporter;
        try {
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
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
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            assertEquals(in.getAbsolutePath(), exporter.getxMindSource().getAbsolutePath());
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
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            assertEquals(in.getAbsolutePath(), exporter.getxMindSource().getAbsolutePath());
            assertTrue(exporter.isOverwriteExistingFile());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureLevel() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedTemplate = TEMPLATE_FOLDER + "paragraph"; 
        String[] args = new String[] {
                "-i", in.getPath(),
                "-l", "2", expectedTemplate
        };
        XMindToLatexExporter exporter;
        try {
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            assertEquals(5, exporter.getTemplates().size());
            assertEquals(expectedTemplate, exporter.getTemplates().get(2));
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testConfigureLevelAboveMax() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedTemplate = TEMPLATE_FOLDER + "paragraph"; 
        String[] args = new String[] {
                "-i", in.getPath(),
                "-l", "10", expectedTemplate
        };
        XMindToLatexExporter exporter;
        try {
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
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
            new XMindToLatexExporter().configure(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testConfigureLevelMax() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedTemplate = TEMPLATE_FOLDER + "paragraph"; 
        String[] args = new String[] {
                "-i", in.getPath(),
                "-l", "5", expectedTemplate
        };
        XMindToLatexExporter exporter;
        try {
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
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
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            
            assertEquals(out.getAbsolutePath(), exporter.getTargetFile().getAbsolutePath());
            assertEquals(in.getAbsolutePath(), exporter.getxMindSource().getAbsolutePath());
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
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            
            assertEquals(out.getAbsolutePath(), exporter.getTargetFile().getAbsolutePath());
            assertEquals(in.getAbsolutePath(), exporter.getxMindSource().getAbsolutePath());
            assertFalse(exporter.isOverwriteExistingFile());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testDefaultConfigure() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedOut = in.getAbsolutePath().concat(".tex");
        
        String[] args = new String[] {
                "-i", in.getPath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            assertEquals(in.getAbsolutePath(), exporter.getxMindSource().getAbsolutePath());
            assertFalse(exporter.isOverwriteExistingFile());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testFailConfigureNotExistingInput() {
        File in = new File("src/test/resources/content_doesNotExist.xml");
        
        String[] args = new String[] {
                "-i", in.getPath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testFailConfigureInvalidZipInput() {
        File in = new File("src/test/resources/jms.7zip.xmind");
        
        String[] args = new String[] {
                "-i", in.getPath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            assertTrue(e instanceof ParseException);
        }
    }
    
    @Test
    public void testDefaultConfigure2() {
        File in = new File("src/test/resources/content.xml");
        
        String expectedOut = in.getAbsolutePath().concat(".tex");
        String[] args = new String[] {
                "--input", in.getPath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            assertEquals(in.getAbsolutePath(), exporter.getxMindSource().getAbsolutePath());
            assertFalse(exporter.isOverwriteExistingFile());
        }
        catch (ParseException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testDefaultConfigureXMindSource() {
        File in = new File("src/test/resources/example.xmind");
        File tmp = new File("target/testout/tmp");
        
        String expectedOut = in.getAbsolutePath().concat(".tex");
        File expectedTmpFile = new File(tmp, "content.xml");
        
        String[] args = new String[] {
                "-i", in.getPath()
        };
        XMindToLatexExporter exporter = new XMindToLatexExporter();
        XMindToLatexExporter.TMP_DIRECTORY = tmp.getAbsolutePath();
        try {
            exporter.configure(args);
            assertEquals(expectedOut, exporter.getTargetFile().getAbsolutePath());
            assertEquals(expectedTmpFile.getAbsolutePath(), exporter.getxMindSource().getAbsolutePath());
            assertFalse(exporter.isOverwriteExistingFile());
            assertTrue(tmp.exists());
            assertTrue(expectedTmpFile.exists());
        }
        catch (ParseException e) {
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
            XMindToLatexExporter exporter = new XMindToLatexExporter();
            exporter.configure(args);
            fail("ParseException expected");
        }
        catch (Exception e) {
            // this is expected
            assertTrue(e instanceof ParseException);
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
        XMindToLatexExporter exporter;
        try {
            result.createNewFile();            
            FileOutputStream os = new FileOutputStream(result);
            PrintStream out = new PrintStream(os);
            PrintStream old_out = System.out;
            System.setOut(out);
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
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
        XMindToLatexExporter exporter;
        try {
            result.createNewFile();            
            FileOutputStream os = new FileOutputStream(result);
            PrintStream out = new PrintStream(os);
            PrintStream old_out = System.out;
            System.setOut(out);
            exporter = new XMindToLatexExporter();
            exporter.configure(args);
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
}
