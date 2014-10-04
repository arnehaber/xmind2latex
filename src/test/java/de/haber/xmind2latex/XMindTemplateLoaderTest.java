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


import static org.junit.Assert.*;
import static de.haber.xmind2latex.XMindToLatexExporter.TEMPLATE_PACKAGE;

import java.net.URL;

import org.junit.Test;

/**
 * Unit test for the {@link XMindTemplateLoader}. <br>
 * <br>
 * 
 * @author (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 */
public class XMindTemplateLoaderTest {
	
	@Test
	public void testNullClassLoader() {
		try {
			new XMindTemplateLoader(null);
			fail("NullPointerException expected");
		}
		catch (Exception e) {
			assertTrue(e instanceof NullPointerException);
		}
	}
	
	@Test
	public void testQualifiedTemplateName() {
		XMindTemplateLoader testee = new XMindTemplateLoader(this.getClass().getClassLoader());
		URL result = testee.getURL(TEMPLATE_PACKAGE + "section");
		assertNotNull(result);
		assertEquals("file", result.getProtocol());
	}
	
	@Test
	public void testQualifiedTemplateNameWithFileExtension() {
		XMindTemplateLoader testee = new XMindTemplateLoader(this.getClass().getClassLoader());
		URL result = testee.getURL(TEMPLATE_PACKAGE + "section.ftl");
		assertNull(result);
	}
	
	@Test
	public void testQualifiedTemplateNameNotExists() {
		XMindTemplateLoader testee = new XMindTemplateLoader(this.getClass().getClassLoader());
		URL result = testee.getURL(TEMPLATE_PACKAGE + "does.not.exist");
		assertNull(result);
	}
	
	@Test
	public void testFileTemplateName() {
		XMindTemplateLoader testee = new XMindTemplateLoader(this.getClass().getClassLoader());
		URL result = testee.getURL("src/test/resources/someExternalTemplate.ftl");
		assertNotNull(result);
		assertEquals("file", result.getProtocol());
	}
	
	@Test
	public void testFileTemplateNameNotExists() {
		XMindTemplateLoader testee = new XMindTemplateLoader(this.getClass().getClassLoader());
		URL result = testee.getURL("src/test/resources/doesNotExist.ftl");
		assertNull(result);
	}
	
	@Test
	public void testFileTemplateNameWOFileExtension() {
		XMindTemplateLoader testee = new XMindTemplateLoader(this.getClass().getClassLoader());
		URL result = testee.getURL("src/test/resources/someExternalTemplate");
		assertNull(result);
	}
}
