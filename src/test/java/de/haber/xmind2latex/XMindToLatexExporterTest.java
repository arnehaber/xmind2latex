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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.haber.xmind2latex.cli.CliParameters;

/**
 * Further {@link XMindToLatexExporter} tests that do not concern configuration
 * or execution. 
 * 
 * <br>
 * <br>
 * Copyright (c) 2014 RWTH Aachen. All rights reserved.
 * 
 * @author (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 */
public class XMindToLatexExporterTest {
    
    /**
     * Tests the showHelp() method. Additionally exports the help message into file
     * target/app/doc/commands.txt that is used in the documentation.
     * 
     * @throws IOException
     */
    @Test
    public void testShowHelp() throws IOException {
        File result = new File("target/app/doc/commands.txt");
        result.delete();
        result.getParentFile().mkdirs();
        result.createNewFile();
        FileOutputStream os = new FileOutputStream(result);
        PrintStream out = new PrintStream(os);
        PrintStream old_out = System.out;
        System.setOut(out);
        CliParameters.showHelp();
        String resultContent = FileUtils.readFileToString(result);
        assertFalse(resultContent.isEmpty());
        System.setOut(old_out);
        os.close();
    }
    
    @Test
    public void testRemoveLineBreaksInComments() {
        File in = new File("src/test/resources/content.xml");
        String[] args = new String[] {
                "-i", in.getAbsolutePath()
        };
        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            Method getTextForLevel = exporter.getClass().getDeclaredMethod("getTextForLevel", int.class, String.class);
            assertNotNull(getTextForLevel);
            getTextForLevel.setAccessible(true);
            String txt = "a \nb \nc \n";
            String undef = (String) getTextForLevel.invoke(exporter, 7, txt);
            assertTrue(undef.contains("    % 2 - a \n"));
            assertTrue(undef.contains("\n    %   - b"));
            assertTrue(undef.contains("\n    %   - c"));
            
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testSave() {
        File in = new File("src/test/resources/content.xml");
        File out = new File("target/testout/result.tex");
        if (out.exists()) {
            out.delete();
        }
        String[] args = new String[] {
                "-i", in.getAbsolutePath(),
                "-o", out.getAbsolutePath()
        };

        XMindToLatexExporter exporter;
        try {
            exporter = CliParameters.build(args);
            
            Method save = exporter.getClass().getDeclaredMethod("save", String.class);
            assertNotNull(save);
            save.setAccessible(true);
            
            String txt = "ä ü ö ? ß Ü Ä Ö ";
            save.invoke(exporter, txt);
            String content = FileUtils.readFileToString(out, "UTF-8");
            assertEquals(txt, content);
            
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
}
