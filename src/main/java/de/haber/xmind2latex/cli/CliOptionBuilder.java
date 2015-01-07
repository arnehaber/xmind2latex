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

import static de.haber.xmind2latex.cli.CliParameters.ENVIRONMENT;
import static de.haber.xmind2latex.cli.CliParameters.FORCE;
import static de.haber.xmind2latex.cli.CliParameters.HELP;
import static de.haber.xmind2latex.cli.CliParameters.INPUT;
import static de.haber.xmind2latex.cli.CliParameters.LEVEL;
import static de.haber.xmind2latex.cli.CliParameters.OUTPUT;
import static de.haber.xmind2latex.cli.CliParameters.TEMPLATE_LEVEL;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PatternOptionBuilder;

import de.haber.xmind2latex.XMindToLatexExporter;
import de.haber.xmind2latex.help.CoverageIgnore;

/**
 * Builds command line options for {@link XMindToLatexExporter}.
 *
 * @author  (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 * @since 1.1.0
 */
class CliOptionBuilder {
    
	/**
	 * Private default constructor to prevent utility class instantiation.
	 * @since 1.2.0
	 */
	@CoverageIgnore
	private CliOptionBuilder() {
		
	}
	
    @SuppressWarnings("static-access")
    /**
     * 
     * @return the CLI options for the XMindToLatex exporter.
     */
    protected static Options getOptions() {
        Options o = new Options();
        
        o.addOption(OptionBuilder.withArgName("input file")
                                .withLongOpt("input")
                                .withDescription("Required input file name.")
                                .hasArg(true)
                                .isRequired()
                                .withType(PatternOptionBuilder.FILE_VALUE)
                                .create(INPUT));
        o.addOption(OptionBuilder.withArgName("force")
                                .withLongOpt("force")
                                .withDescription("Force overwrite existing files (optional).")
                                .hasArg(false)
                                .isRequired(false)
                                .create(FORCE));
        o.addOption(OptionBuilder.withArgName("output file")
                                .withLongOpt("output")
                                .withDescription("Output file name (optional). Default output file is \"<input file>.tex.\"")
                                .hasArg()
                                .isRequired(false)
                                .withType(PatternOptionBuilder.FILE_VALUE)
                                .create(OUTPUT));
        o.addOption(OptionBuilder.withArgName("template level")
                                .withLongOpt("template-level")
                                .withDescription("Maximal level for template usage.")
                                .hasArg()
                                .isRequired(false)
                                .withType(PatternOptionBuilder.NUMBER_VALUE)
                                .create(TEMPLATE_LEVEL));
        o.addOption(OptionBuilder.withArgName("help")
                                 .withLongOpt("help")
                                 .withDescription("Prints this help message.")
                                 .hasArg(false)
                                 .isRequired(false)
                                 .create(HELP));
        o.addOption(OptionBuilder.withArgName("level> <start> <end")
                                 .withLongOpt("env")
                                 .hasArgs(3)
                                 .withDescription("Sets the start and end environment templates for the given level (optional). " +
                                        "Templates must be either loadable from the classpath with the given full qualified name (no file extension, " +
                                        "directories separated by a '.', or as a file (with '.ftl' extension, directories separated by a path separator).")
                                 .isRequired(false)
                                 .create(ENVIRONMENT));
        o.addOption(OptionBuilder.withArgName("level> <template")
                                 .withLongOpt("level-template")
                                 .withValueSeparator(' ')
                                 .withDescription("Sets the template that is to be used for the given level (optional). " +
                                         "Templates must be either loadable from the classpath with the given full qualified name (no file extension, " +
                                         "directories separated by a '.', or as a file (with '.ftl' extension, directories separated by a path separator).")
                                 .hasArgs(2)
                                 .isRequired(false)
                                 .create(LEVEL));
        return o;
    }
}
