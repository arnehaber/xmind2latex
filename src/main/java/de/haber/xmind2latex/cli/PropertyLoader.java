/*
 * #%L
 * XMind to Latex
 * %%
 * Copyright (C) 2014 - 2015 Arne Haber
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
package de.haber.xmind2latex.cli;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;

/**
 * Helper, to load information from a property file.

 * @since 1.2.0
 *
 */
final class PropertyLoader {
    
    /**
     * @param fileName file name of the property file
     * @param propName name of the property to read out
     * 
     * @return reads out property 'propName' from the given property file 'fileName'. If the file or
     * the property does not exist, the returned {@link Optional} is absent.
     */
    public static Optional<String> getProperty(String fileName, String propName) {
        return getProperties(fileName, propName).iterator().next();
    }
    
    /**
     * @param fileName file name of the property file
     * @param propNames names of the properties to read out
     *
     * @return reads out properties 'propName' from the given property file 'fileName' and stored
     * them in the result list in the same order. If a property is not available or the property
     * file cannot be loaded, the corresponding {@link Optional} is absent.
     */
    public static List<Optional<String>> getProperties(String fileName, String... propNames) {
        checkNotNull(fileName);
        checkNotNull(propNames);
        
        List<Optional<String>> result = new ArrayList<Optional<String>>(propNames.length);
        
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(fileName);
        try {
            prop.load(stream);
            for (String p : propNames) {
                String versionNumber = prop.getProperty(p);
                result.add(Optional.fromNullable(versionNumber));
            }
        }
        catch (Exception e) {
            for (int i = 0; i < propNames.length; i++) {
                result.add(Optional.<String> absent());
            }
        }
        finally {
            try {
                Closeables.close(stream, true);
            }
            catch (IOException e) {
                Throwables.propagate(e);
            }
        }
        return ImmutableList.copyOf(result);
    }
}
