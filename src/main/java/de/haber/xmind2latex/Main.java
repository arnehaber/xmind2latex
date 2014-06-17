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

import org.apache.commons.cli.ParseException;

/**
 * Main class to execute a {@link XMindToLatexExporter}.
 *
 * <br>
 * <br>
 * Copyright (c) 2014 RWTH Aachen. All rights reserved.
 *
 * @author  (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 */
public class Main {
    
    public static void main(String[] args) {
        XMindToLatexExporter tool = new XMindToLatexExporter();
        try {
            tool.configure(args);
            tool.convert();
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
            tool.showHelp();
            System.exit(-1);
        } 
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
