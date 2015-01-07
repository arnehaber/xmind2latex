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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.haber.xmind2latex.cli.CliParameters;
import de.haber.xmind2latex.help.ConfigurationException;
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
        
    /**
     * {@link XMindToLatexExporter} {@link Builder}.
     * 
     * @since 1.2.0
     */
    public static class Builder {
        // required fields
        private final File in;
        
        private Map<Integer, String> level2endTemplate = Maps.newHashMap();
        private Map<Integer, String> level2startTemplate = Maps.newHashMap();
        private int maxLevel = -1;
        // optional fields - initialized to default values
        private boolean overwriteExistingFile = false;
        // optional fields - defaults initialized in constructor
        private File targetFile;
        
        private List<String> templates = Lists.newArrayList(
                        TEMPLATE_PACKAGE + "undefined",
                        TEMPLATE_PACKAGE + "chapter",
                        TEMPLATE_PACKAGE + "section",
                        TEMPLATE_PACKAGE + "subsection",
                        TEMPLATE_PACKAGE + "subsubsection");
        
        /**
         * Creates a new {@link XMindToLatexExporter} builder for the given input file.
         * 
         * @param in input file, must not be null and has to exist
         */
        public Builder(File in) {
            checkNotNull(in);
            checkArgument(in.exists(), "Input file has to exist!");
            this.in = in;
            
            String outDerived = in.getAbsolutePath().concat(".tex");
            targetFile = new File(outDerived);
        }
        
        /**
         * 
         * @return a configured {@link XMindToLatexExporter}.
         * 
         * @throws ConfigurationException for invalid input files.
         */
        public XMindToLatexExporter build() {
          return new XMindToLatexExporter(this);
        }
        
        /***
         * @param overwriteExistingFile true, if existing files shall be overridden.
         * 
         * @return the builder.
         */
        public Builder overwritesExistingFiles(boolean overwriteExistingFile) {
            this.overwriteExistingFile = overwriteExistingFile;
            return this;
        }
        
        /**
         * Sets environment start and end templates for the given level.
         * 
         * @param level level to configure, has to be >= 0
         * @param startTemplate environment start template to use, must not be null and has to exist
         * @param endTemplate environment end template to use, must not be null and has to exist
         * 
         * @return the used builder
         */
        public Builder withEnvironmentTemplates(int level, String startTemplate, String endTemplate) {
            checkArgument(level >= 0);
            validateTemplate(startTemplate);
            validateTemplate(endTemplate);
            if (level2startTemplate.containsKey(level)) {
                throw new ConfigurationException("Level " + level + " is already configured to use environment templates " + 
                                level2startTemplate.get(level) + " " + level2endTemplate.get(level));
            }
            this.level2startTemplate.put(level, startTemplate);
            this.level2endTemplate.put(level, endTemplate);
            return this;
        }
        
        private void validateTemplate(String template) {
            checkNotNull(template);
            try {
                XMindToLatexExporter.templateConfig.getTemplate(template);
            }
            catch (IOException e) {
                TemplateNotExistsException ce = new TemplateNotExistsException(template);
                ce.addSuppressed(e);
                throw ce;
            }
        }

        /**
         * Sets the maximal level used for template processing. 
         * -1 corresponds to 'process all available templates'.
         * 
         * @param level the maximal level to set, must be >= -1

         * @return the used builder
         */
        public Builder withMaxLevel(int level) {
            checkArgument(level >= -1);
            this.maxLevel = level;
            return this;
        }
        
        /**
         * 
         * @param targetFile the target file, must not be null.
         * @return the used builder
         */
        public Builder withTargetFile(File targetFile) {
            checkNotNull(targetFile);
            this.targetFile = targetFile;
            return this;
        }
        
        /**
         * Sets the template for a specific level.
         * 
         * @param level level to set the template for, has to be >= 0
         * @param template template for the given level, must not be null and has to exist
         * @return the used builder
         */
        public Builder withTemplate(int level, String template) {
            checkArgument(level >= 0);
            validateTemplate(template);
            // warn, if added templates will not be used, because max level is set
            if (maxLevel != -1 && level > maxLevel) {
                throw new ConfigurationException("The added template for level " 
                        + level + 
                        " will not be used because max template level has been configured to level " 
                        + maxLevel);
            }
            
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
            return this;
        }
    }
    
    /** Used as indention. */
    public static final String INDENT = "  ";
    public static final String NEW_LINE = "\n";
    
    public static final String TEMPLATE_PACKAGE = "de.haber.xmind2latex.templates.";
    public static final String TEXT = "#text";
    
    public static final String TITLE = "title";
    
    public static final String TOPIC = "topic";
    
    private int depthCounter = 0;
    
    private final Map<Integer, String> level2endTemplate;
    /**
     * Stores indent strings for a specific level.
     */
    private final Map<Integer, String> level2indent = Maps.newHashMap();
    private final Map<Integer, String> level2startTemplate;
    
    /**
     * The maximal level used for template processing. -1 corresponds to
     * 'process all available templates'.
     */
    private final int maxLevel;
    
    private final boolean overwriteExistingFile;
    
    /**
     * Target file.
     */
    private final File targetFile;
    
    private static final Configuration templateConfig;
    
    static {
        templateConfig = new Configuration();
        templateConfig.setClassForTemplateLoading(XMindToLatexExporter.class, "");
        templateConfig.setTemplateLoader(new XMindTemplateLoader(XMindToLatexExporter.class.getClassLoader()));
        templateConfig.setLocalizedLookup(false);
    }

    private final List<String> templates;
    
    private final InputStream xMindSourceStream;
    
    /**
     * Creates a new {@link XMindToLatexExporter}.
     * 
     * @throws ConfigurationException for invalid input files
     */
    private XMindToLatexExporter(Builder builder) {

        
        try {
            xMindSourceStream = setxMindSourceInputStream(builder.in);
        }
        catch (Exception e) {
            ConfigurationException e1 = new ConfigurationException(e.getMessage());
            e1.addSuppressed(e);
            throw e1;
        }

        targetFile = builder.targetFile;
        overwriteExistingFile = builder.overwriteExistingFile;
        templates = ImmutableList.copyOf(builder.templates);
        maxLevel = builder.maxLevel;
        this.level2startTemplate = ImmutableMap.copyOf(builder.level2startTemplate);
        this.level2endTemplate = ImmutableMap.copyOf(builder.level2endTemplate);
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
                sb.append(getTextForLevel(depthCounter, text));
                sb.append(NEW_LINE);
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
     * 
     * @param inner indention level
     * @return an indention string for the given level 
     */
    private String getIndention(int inner) {
    	if (!level2indent.containsKey(inner)) {
    		StringBuilder sb = new StringBuilder();
    		for ( int i = 0; i < inner; i++) {
    			sb.append(INDENT);
    		}
    		level2indent.put(inner, sb.toString());
    	}
		return level2indent.get(inner);
	}

    /**
     * @return The maximal level used for template processing. -1 corresponds to
     * 'process all available templates'.
     */
    public int getMaxLevel() {
        return maxLevel;
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
        int maxLevel = getMaxLevel() != -1 ? getMaxLevel() + 1 : templates.size();
        int inner = level - maxLevel;
        data.put("text", text);
        data.put("level", "" + level);
        data.put("innerLevel", "" + inner);
        StringBuilder currentIndent = new StringBuilder();
        currentIndent.append(getIndention(inner));
        data.put("indent", currentIndent.toString());
        

        StringWriter writer = new StringWriter();
        try {
            Template t = templateConfig.getTemplate(template);
            t.process(data, writer);
        }
        catch (Exception e) {
            throw new TemplateNotExistsException(template);
        }
        return writer.getBuffer().toString();
    }
    
    /**
     * Stores the given <b>content</b> into the configured target file.
     *  
     * @param content content to save
     * @throws IOException either writer {@link IOException}, or if the target file already exists and fore overwrite is not enabled.
     */
    private void save(String content) throws IOException {
        File tf = getTargetFile();
        if (tf.getParentFile() != null && !tf.getParentFile().exists()) {
            tf.getParentFile().mkdirs();
        }
        if (!tf.exists() || isOverwriteExistingFile()) {
            PrintWriter pw = new PrintWriter(tf, "UTF-8");
            pw.write(content);
            pw.close();
        }
        else {
            throw new FileAlreadyExistsException(tf.getAbsolutePath(), "", "If you want to overwrite existing files use param " + CliParameters.FORCE);
        }
    }
   
   

    /**
     * @param xMindSource the xMindSource to set, must not be null
     * 
     * @throws ZipException if a given XMind file may not be extracted.
     * @throws IOException 
     */
    private InputStream setxMindSourceInputStream(File xMindSource) throws ZipException, IOException {
        Preconditions.checkNotNull(xMindSource);
        File usedFile = xMindSource;
        if (!usedFile.exists()) {
            throw new FileNotFoundException("The given input file " + xMindSource+ " does not exist!");
        }
        if (usedFile.getName().endsWith(".xmind")) {
            ZipFile zip = new ZipFile(usedFile);
            FileHeader fh = zip.getFileHeader("content.xml");
            return zip.getInputStream(fh);
        }
        else {
            return FileUtils.openInputStream(usedFile);
        }
    }
}
