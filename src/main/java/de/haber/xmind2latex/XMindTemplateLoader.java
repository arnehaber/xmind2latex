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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import freemarker.cache.URLTemplateLoader;

/**
 * 
 * Is used to load templates with a given {@link ClassLoader} or from a file reference.
 *
 * <br>
 * <br>
 *
 * @author  (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 */
public class XMindTemplateLoader extends URLTemplateLoader {
    
    /** The file extension from FreeMarker templates is ".ftl". */
    public static final String FM_FILE_EXTENSION = ".ftl";
    
    /** the used class loader */
    private final ClassLoader classLoader;
    
    /**
     * Creates a new {@link XMindTemplateLoader} that uses the given class
     * loader to load FreeMarker templates from the class path
     * 
     * @param classLoader used class loader
     */
    public XMindTemplateLoader(final ClassLoader classLoader) {
        checkNotNull(classLoader);
        this.classLoader = classLoader;
    }
    
    /**
     * Resolves the location of a template from classpath or file.
     * 
     * @see freemarker.cache.URLTemplateLoader#getURL(java.lang.String)
     * @param templateName The qualified template name, if template is to be
     *            loaded from classpath. Or the path to the template, if the
     *            template exists as a file.
     * @return URL of the template loaded from classpath or file.
     */
    @Override
    protected URL getURL(final String templateName) {
        // Copying the parameter so the else-Block later in the method can
        // reuse the original String.
        String newName = templateName;
        // Since the input is almost always dot separated, this method just
        // goes ahead and converts it
        // without checking, only in the rare case that this procedure is
        // unsuccessful are
        // alternatives considered
        newName = newName.replace(".", "/").concat(FM_FILE_EXTENSION);
        
        URL result = classLoader.getResource(newName);

        // this is the case, if the original name has not been full qualified
        // but a reference to a file. Thus we load the URL as a file and do not
        // load it from classpath.
        if (result == null && templateName.endsWith(FM_FILE_EXTENSION)) {
            result = classLoader.getResource(templateName);
            File f = new File(templateName);
            if (f.exists()) {
                try {
                    result = f.toURI().toURL();
                }
                catch (MalformedURLException e) {
                    result = null;
                }
            }
        }
        return result;
    }
}
