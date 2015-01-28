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
package de.haber.xmind2latex.cli;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import de.haber.xmind2latex.XMindToLatexExporter;
import de.haber.xmind2latex.XMindToLatexExporter.Builder;
import de.haber.xmind2latex.help.ConfigurationException;
import de.haber.xmind2latex.help.CoverageIgnore;

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
public class CliParameters {
    
    /**
     * Empty constructor to avoid utility class instantiation.
     */
    @CoverageIgnore
    private CliParameters() {
        
    }
    
    public static final char ENVIRONMENT = 'e';
    
    public static final char FORCE = 'f';
    
    public static final char HELP = 'h';
    
    public static final char INPUT = 'i';
    
    public static final char LEVEL = 'l';
    
    public static final char OUTPUT = 'o';
    
    public static final char TEMPLATE_LEVEL = 't';
    
    public static final char VERSION = 'v';
    
    private static final Options options = CliOptionBuilder.getOptions();
    
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
    
    /**
     * Creates a {@link XMindToLatexExporter} for the given arguments.
     * 
     * @param args Arguments to configure this {@link XMindToLatexExporter}.
     * 
     * @return A created {@link XMindToLatexExporter} or null, if no {@link CliParameters#INPUT} parameter is used.
     * 
     * @throws ParseException, NumberFormatException for invalid arguments
     * @throws ConfigurationException for invalid input files
     * @throws IllegalArgumentException if the given input file does not exist
     */
    public static XMindToLatexExporter build(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args, false);
        
        
        if (cmd.getOptions().length == 0) {
            throw new ParseException("Parameter -" + INPUT + " expected.");
        }
        
        if (cmd.hasOption(VERSION)) {
            printVersion();
        }
        
        CliParameters.validateNumberOfArguments(cmd, INPUT, options);
        if (cmd.hasOption(INPUT)) {
            File in = new File(cmd.getOptionValue(INPUT));
            Builder builder = new Builder(in);            
            if (cmd.hasOption(FORCE)) {
                CliParameters.validateNumberOfArguments(cmd, FORCE, options);
                builder.overwritesExistingFiles(true);
            }
            
            File out;
            if (cmd.hasOption(OUTPUT)) {
                CliParameters.validateNumberOfArguments(cmd, OUTPUT, options);
                out = new File(cmd.getOptionValue(OUTPUT));
                builder.withTargetFile(out);
            }
            
            if (cmd.hasOption(TEMPLATE_LEVEL)) {
                CliParameters.validateNumberOfArguments(cmd, TEMPLATE_LEVEL, options);
                
                String level = cmd.getOptionValue(TEMPLATE_LEVEL);
                try {
                    int levelAsInt = Integer.parseInt(level);
                    if (levelAsInt < 0) {
                        throw new NumberFormatException();
                    }
                    builder.withMaxLevel(levelAsInt);
                }
                catch (NumberFormatException e) {
                    ParseException ex = new ParseException("The level argument of option " + TEMPLATE_LEVEL + " has to be a positive integer.");
                    ex.addSuppressed(e);
                    throw ex;
                }
                
            }
            if (cmd.hasOption(HELP)) {
                CliParameters.validateNumberOfArguments(cmd, HELP, options);
                
                showHelp();
            }
            
            if (cmd.hasOption(ENVIRONMENT)) {
                CliParameters.validateNumberOfArguments(cmd, ENVIRONMENT, options);
                
                String[] env = cmd.getOptionValues(ENVIRONMENT);
                for (int i = 0; i + 2 < env.length; i = i + 3) {
                    String level = env[i];
                    String start = env[i + 1];
                    String end = env[i + 2];
                    try {
                        int levelAsInt = Integer.parseInt(level);      
                        builder.withEnvironmentTemplates(levelAsInt, start, end);
                    }
                    catch (NumberFormatException e) {
                        ParseException ex = new ParseException("The level argument of option " + ENVIRONMENT + " has to be an integer.");
                        ex.addSuppressed(e);
                        throw ex;
                    }
                }
            }
            if (cmd.hasOption(LEVEL)) {
                CliParameters.validateNumberOfArguments(cmd, LEVEL, options);
                
                String[] tmp = cmd.getOptionValues(LEVEL);
                
                for (int i = 0; i + 1 < tmp.length; i = i + 2) {
                    String level = tmp[i];
                    String template = tmp[i + 1];
                    try {
                        int levelAsInt = Integer.parseInt(level);
                        builder.withTemplate(levelAsInt, template);
                    }
                    catch (NumberFormatException e) {
                        ParseException ex = new ParseException("The level argument of option " + LEVEL + " has to be an integer.");
                        ex.addSuppressed(e);
                        throw ex;
                    }
                }
            }
            return builder.build();
        }
        else {
            return null;
        }
    }
    
    private static void printVersion() {
        List<Optional<String>> props = PropertyLoader.getProperties("xmind2latex-app.properties", "app.version", "app.name");
        String name = props.get(1).or("xmind2latex");
        String version = props.get(0).or("unknown");
        System.out.println(name + " version \"" + version + "\"");
    }

    /**
     * 
     */
    public static void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("xmind2latex", options);
    }
}
