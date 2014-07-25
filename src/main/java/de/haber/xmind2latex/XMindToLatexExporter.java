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

import static de.haber.xmind2latex.Parameters.ENVIRONMENT;
import static de.haber.xmind2latex.Parameters.FORCE;
import static de.haber.xmind2latex.Parameters.HELP;
import static de.haber.xmind2latex.Parameters.INPUT;
import static de.haber.xmind2latex.Parameters.LEVEL;
import static de.haber.xmind2latex.Parameters.OUTPUT;
import static de.haber.xmind2latex.Parameters.TEMPLATE_LEVEL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Reads an xMind XML file and produces a latex output from it. 
 * 
 * <br>
 * <br>
 * 
 * @author (last commit) $Author$
 * @version $Date$<br>
 *          $Revision$
 */
public class XMindToLatexExporter {
        
    public static final String NEW_LINE = "\n";
    
    protected static final String TEMPLATE_FOLDER = "de.haber.xmind2latex.templates.";
    public static final String TEXT = "#text";
    
    public static final String TITLE = "title";
    public static final String TOPIC = "topic";
    
    private int depthCounter = 0;
    private Map<Integer, String> level2endTemplate = Maps.newHashMap();
    
    private Map<Integer, String> level2startTemplate = Maps.newHashMap();
    
    private final Options options;
    private boolean overwriteExistingFile = false;
    
    /**
     * The maximal level used for template processing. -1 corresponds to
     * 'process all available templates'.
     */
    private int maxLevel = -1;
    
    /**
     * Target file.
     */
    private File targetFile;
    
    private List<String> templates = Lists.newArrayList(
            TEMPLATE_FOLDER + "undefined",
            TEMPLATE_FOLDER + "chapter",
            TEMPLATE_FOLDER + "section",
            TEMPLATE_FOLDER + "subsection",
            TEMPLATE_FOLDER + "subsubsection"
    );
    
    private final Configuration templateConfig;

    /**
     * Creates a new {@link XMindToLatexExporter}.
     */
    public XMindToLatexExporter() {
        options = CliOptionBuilder.getOptions();
        templateConfig = new Configuration();
        templateConfig.setClassForTemplateLoading(getClass(), "");
        templateConfig.setTemplateLoader(new XMindTemplateLoader(getClass().getClassLoader()));
        templateConfig.setLocalizedLookup(false);
    }
    
    /**
     * @param args Arguments to configure this {@link XMindToLatexExporter}.
     * @throws ParseException for invalid arguments
     */
    public void configure(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        File in = new File(cmd.getOptionValue(INPUT));
        try {
            setxMindSourceInputStream(in);
        }
        catch (Exception e) {
            ParseException e1 = new ParseException(e.getMessage());
            e1.addSuppressed(e);
            throw e1;
        }
        if (cmd.hasOption(FORCE)) {
            this.setOverwriteExistingFile(true);
        }
        
        File out;
        if (cmd.hasOption(OUTPUT)) {
            out = new File(cmd.getOptionValue(OUTPUT));
        }
        else {            
            String outDerived = in.getAbsolutePath().concat(".tex");
            out = new File(outDerived);
        }
        this.setTargetFile(out);

        if (cmd.hasOption(TEMPLATE_LEVEL)) {
            String level = cmd.getOptionValue(TEMPLATE_LEVEL);
            try {
                int levelAsInt = Integer.parseInt(level);
                if (levelAsInt < 0) {
                    throw new NumberFormatException();
                }
                setMaxLevel(levelAsInt);
            }
            catch (NumberFormatException e) {
                ParseException ex = new ParseException("The level argument of option " + TEMPLATE_LEVEL + " has to be a positive integer.");
                ex.addSuppressed(e);
                throw ex;
            }
            
        }
        if (cmd.hasOption(HELP)) {
            showHelp();
        }
        
        if (cmd.hasOption(ENVIRONMENT)) {
            String[] env = cmd.getOptionValues(ENVIRONMENT);
            for (int i = 0; i + 2 < env.length; i = i + 3) {
                String level = env[i];
                String start = env[i + 1];
                String end = env[i + 2];
                try {
                    int levelAsInt = Integer.parseInt(level);      
                    setEnvironmentTemplates(levelAsInt, start, end);
                }
                catch (NumberFormatException e) {
                    ParseException ex = new ParseException("The level argument of option " + ENVIRONMENT + " has to be an integer.");
                    ex.addSuppressed(e);
                    throw ex;
                }
            }
        }
        if (cmd.hasOption(LEVEL)) {
            String[] tmp = cmd.getOptionValues(LEVEL);
            for (int i = 0; i + 1 < tmp.length; i = i + 2) {
                String level = tmp[i];
                String template = tmp[i + 1];
                try {
                    int levelAsInt = Integer.parseInt(level);      
                    setLevelTemplate(levelAsInt, template);
                    // warn, if added templates will not be used, because max level is set
                    int maxLvl = getMaxLevel();
                    if (maxLvl != -1 && levelAsInt > maxLvl) {
                        throw new ParseException("The added template for level " 
                                + levelAsInt + 
                                " will not be used because max template level has been configured to level " 
                                + maxLvl);
                    }
                }
                catch (NumberFormatException e) {
                    ParseException ex = new ParseException("The level argument of option " + LEVEL + " has to be an integer.");
                    ex.addSuppressed(e);
                    throw ex;
                }
            }
        }
    }
    
    public void convert() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = getxMindSourceAsStream();
        if (is == null) {
            throw new ParserConfigurationException("Call configure() before convert()."); 
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);
        is.close();
        StringBuilder sb = new StringBuilder();
        
        sb.append(convert(document.getChildNodes()));
        
        String text = sb.toString();
        save(text);
    }
    
    /**
     * @param childNodes
     * @return
     */
    private StringBuilder convert(NodeList childNodes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node n = childNodes.item(i);
            if (n.getNodeName().equals(TEXT)) {
                String text = n.getNodeValue().replace(NEW_LINE, " ").replace("\t", "").trim();
                if (!text.isEmpty()) {
                    sb.append(getTextForLevel(depthCounter, text));
                    sb.append(NEW_LINE);
                }
            }
            
            if (n.getNodeName().equals(TOPIC)) {
                depthCounter++;
                sb.append(getStartEnvironment(depthCounter));
            }
            sb.append(convert(n.getChildNodes()));
            if (n.getNodeName().equals(TOPIC)) {
                sb.append(getEndEnvironment(depthCounter));
                depthCounter--;
            }
        }
        return sb;
    }
    
    /**
     * @param depthCounter2
     * @return
     */
    public String getEndEnvironment(int level) {
        String templ = level2endTemplate.get(level);
        if (templ != null) {
            return processTemplate(templ, level, "");
        }
        else {
            return "";            
        }
    }
    
    /**
     * @param depthCounter2
     * @return
     */
    public String getStartEnvironment(int level) {
        String templ = level2startTemplate.get(level);
        if (templ != null) {
            return processTemplate(templ, level, "");
        }
        else {
            return "";            
        }
    }
    
    /**
     * @return the targetFile
     */
    public File getTargetFile() {
        return targetFile;
    }

    /**
     * @return the templates
     */
    public List<String> getTemplates() {
        return templates;
    }

    private String getTextForLevel(int level, String text) {
        String template;
        // we are using the undefined template, if the current level is higher
        // then the amount of registered templates
        if (level >= templates.size()) {
            template = templates.get(0);
        }
        else if (level <= 0) {
            return "";
        }
        // we are using the undefined template if the current level is higher
        // then the max level and the max level is set
        else if (level > getMaxLevel() && getMaxLevel() != -1) {
            template = templates.get(0);
        }
        else {
            template = templates.get(level);
        }
        return processTemplate(template, level, text);
    }
    
    public InputStream getxMindSourceAsStream() {
        return this.xMindSourceStream;
    }
    
    /**
     * @return the overwriteExistingFile
     */
    public boolean isOverwriteExistingFile() {
        return overwriteExistingFile;
    }
    
    
    
    private String processTemplate(String template, int level, String text) {
        Map<String, String> data = new HashMap<String, String>();
        
        int inner = level - templates.size() + 1;
        data.put("text", text);
        data.put("level", "" + level);
        data.put("innerLevel", "" + inner);

        StringWriter writer = new StringWriter();
        try {
            String concreteTempl = template;
            Template t = templateConfig.getTemplate(concreteTempl);
            t.process(data, writer);
        }
        catch (Exception e) {
            throw new TemplateNotExistsException(template);
        }
        return writer.getBuffer().toString();
    }
    
    /**
     * @param text2
     * @throws IOException
     */
    private void save(String text2) throws IOException {
        File tf = getTargetFile();
        if (tf.getParentFile() != null && !tf.getParentFile().exists()) {
            tf.getParentFile().mkdirs();
        }
        if (!tf.exists() || isOverwriteExistingFile()) {
            
            PrintWriter pw = new PrintWriter(tf);
            pw.write(text2);
            pw.close();
        }
        else {
            throw new FileAlreadyExistsException(tf.getAbsolutePath(), "", "If you want to overwrite existing files use param " + options.getOption("f").getOpt());
        }
    }
    
    
    public void setEnvironmentTemplates(int level, String startTemplate, String endTemplate) {
        level2startTemplate.put(level, startTemplate);
        level2endTemplate.put(level, endTemplate);
    }
    
    public void setLevelTemplate(int level, String template) {
        int prevSize = templates.size();
        
        if (level >= 0 && level < prevSize) {
            templates.set(level, template);
        }
        else if (level == prevSize) {
            templates.add(template);
        }
        // fill with default templates, if larger
        else {
            for (int i = 0; i < level - prevSize; i++) {
                templates.add(templates.get(0));
            }
            templates.add(template);
        }
    }
    
    /**
     * @param overwriteExistingFile the overwriteExistingFile to set
     */
    public void setOverwriteExistingFile(boolean overwriteExistingFile) {
        this.overwriteExistingFile = overwriteExistingFile;
    }
    
    /**
     * @param targetFile the targetFile to set
     */
    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }
    
    /**
     * @param xMindSource the xMindSource to set, must not be null
     * 
     * @throws ZipException if a given XMind file may not be extracted.
     * @throws IOException 
     */
    public void setxMindSourceInputStream(File xMindSource) throws ZipException, IOException {
        Preconditions.checkNotNull(xMindSource);
        File usedFile = xMindSource;
        if (!usedFile.exists()) {
            throw new FileNotFoundException("The given input file " + xMindSource+ " does not exist!");
        }
        if (usedFile.getName().endsWith(".xmind")) {
            ZipFile zip = new ZipFile(usedFile);
            FileHeader fh = zip.getFileHeader("content.xml");
            xMindSourceStream = zip.getInputStream(fh);
        }
        else {
            xMindSourceStream = FileUtils.openInputStream(usedFile);
        }
    }
    
    private InputStream xMindSourceStream;
    
    /**
     * 
     */
    protected void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("xmind2latex", this.options);
    }

    /**
     * @return The maximal level used for template processing. -1 corresponds to
     * 'process all available templates'.
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Sets the maximal level used for template processing. -1 corresponds to
     * 'process all available templates'.
     * @param maxLevel the maximal level to set
     */
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
}
