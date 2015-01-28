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

import org.apache.commons.cli.ParseException;

import de.haber.xmind2latex.cli.CliParameters;

/**
 * Main class to execute a {@link XMindToLatexExporter}.
 *
 * <br>
 * <br>
 *
 * @author  (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 */
public class Main {
	
	/**
	 * Private default constructor to prevent utility class instantiation.
	 * 
	 * @since 1.2.0
	 */
	private Main() {
		
	}
    
	/**
	 * Executes a {@link XMindToLatexExporter} with the given arguments.
	 * 
	 * @param args configuration arguments.
	 */
    public static void main(String[] args) {
        try {
            XMindToLatexExporter tool = CliParameters.build(args);
            if (tool != null) {
                tool.convert();                
            }
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
            CliParameters.showHelp();
            System.exit(-1);
        } 
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
