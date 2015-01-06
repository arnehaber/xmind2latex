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
package de.haber.xmind2latex.help;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.collect.Maps;

/**
 * Contains parameter constants.
 *
 * <br>
 * <br>
 *
 * @author  (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 */
public class Parameters {
    
    /**
     * Empty constructor to avoid utility class instantiation.
     */
    private Parameters() {
        
    }
    
    public static final char ENVIRONMENT = 'e';
    
    public static final char FORCE = 'f';
    
    public static final char HELP = 'h';
    
    public static final char INPUT = 'i';
    
    public static final char LEVEL = 'l';
    
    public static final char OUTPUT = 'o';
    
    public static final char TEMPLATE_LEVEL = 't';
    
    /**
     * Contains the valid number of arguments for parameters with arguments.
     */
    private static final Map<Character, Integer> param2arguments = Maps.newHashMap();
    
    static {
      param2arguments.put(ENVIRONMENT, 3);
      param2arguments.put(LEVEL, 2);
    }
    
    /**
     * Helper method that throws a {@link ParseException} if parameter arguments are missing.
     * 
     * @param cmd the concrete {@link CommandLine}
     * @param param name of the parameter that is to be validated
     * @param opts configured {@link Options}
     * @throws ParseException if the number of arguments does not correspond to the expected number of arguments for the given parameter
     */
    public static void validateNumberOfArguments(CommandLine cmd, char param, Options opts) throws ParseException {
        Integer numberOfArgs = param2arguments.get(param);
        String[] tmp = cmd.getOptionValues(param);
        if (numberOfArgs != null && tmp != null && tmp.length % numberOfArgs != 0) {
            Option o = opts.getOption(Character.toString(param));
            String name = o.getLongOpt() != null ? o.getLongOpt() : Character.toString(param);
            throw new ParseException("Invalid amount of arguments for parameter " + name + ": <" + o.getArgName() + ">. Description: " + o.getDescription());
        }
    }
}
