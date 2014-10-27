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

import com.google.common.base.Preconditions;

/**
 * Thrown, if a template may not be loaded from file or classpath.
 *
 * <br>
 * <br>
 *
 * @author  (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 */
public class TemplateNotExistsException extends RuntimeException {

    private final String template;
    /**
     * 
     */
    private static final long serialVersionUID = -7022763682540866022L;
    
    /**
     * @param template the name of the template that could not be loaded
     */
    public TemplateNotExistsException(final String template) {
        super("Unable to load template " + template + ".");
        Preconditions.checkNotNull(template);
        this.template = template;
    }

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }
    
}
