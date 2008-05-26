/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.maven_plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.tools.common.toolspec.ToolRunner;
import org.apache.cxf.tools.validator.WSDLValidator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal wsdlvalidator
 * @description CXF WSDL Validation
 */
public class WSDLValidatorMojo extends AbstractMojo {
    /**
     * @parameter
     */
    private Boolean verbose;

    /**
     * @parameter
     */
    private Boolean quiet;


    /**
     * @parameter expression="${cxf.wsdlRoot}" default-value="${basedir}/src/main/resources/wsdl"
     */
    private File wsdlRoot;
    
    /**
     * @parameter expression="${cxf.testWsdlRoot}" default-value="${basedir}/src/test/resources/wsdl"
     */
    private File testWsdlRoot;
    
    /**
     * Directory in which the "DONE" markers are saved that 
     * @parameter expression="${cxf.markerDirectory}" 
     *            default-value="${project.build.directory}/cxf-wsdl-validator-markers"
     */
    private File markerDirectory;
    /**
     * A list of wsdl files to include. Can contain ant-style wildcards and double wildcards. Defaults to
     * *.wsdl
     * 
     * @parameter
     */
    private String includes[];
    /**
     * A list of wsdl files to exclude. Can contain ant-style wildcards and double wildcards.
     * 
     * @parameter
     */
    private String excludes[];

    private String getIncludeExcludeString(String[] arr) {
        if (arr == null) {
            return "";
        }
        StringBuilder str = new StringBuilder();

        if (arr != null) {
            for (String s : arr) {
                if (str.length() > 0) {
                    str.append(',');
                }
                str.append(s);
            }
        }
        return str.toString();
    }
    
    private List<File> getWsdlFiles(File dir)
        throws MojoExecutionException {

        List<String> exList = new ArrayList<String>();
        if (excludes != null) {
            exList.addAll(Arrays.asList(excludes));
        }
        exList.addAll(Arrays.asList(org.codehaus.plexus.util.FileUtils.getDefaultExcludes()));

        String inc = getIncludeExcludeString(includes);
        String ex = getIncludeExcludeString(exList.toArray(new String[exList.size()]));

        try {
            List newfiles = org.codehaus.plexus.util.FileUtils.getFiles(dir, inc, ex);
            return CastUtils.cast(newfiles);
        } catch (IOException exc) {
            throw new MojoExecutionException(exc.getMessage(), exc);
        }
    }
    
    private void processWsdl(File file, long cgtimestamp) throws MojoExecutionException {
        
        // If URL to WSDL, replace ? and & since they're invalid chars for file names
        File doneFile =
            new File(markerDirectory, "." + file.getName().replace('?', '_').replace('&', '_') + ".DONE");
        boolean doWork = cgtimestamp > doneFile.lastModified();
        if (!doneFile.exists()) {
            doWork = true;
        } else if (file.lastModified() > doneFile.lastModified()) {
            doWork = true;
        } 

        if (doWork) {
            doneFile.delete();
            
            List<String> list = new ArrayList<String>();

            // verbose arg
            if (verbose != null && verbose.booleanValue()) {
                list.add("-verbose");
            }

            // quiet arg
            if (quiet != null && quiet.booleanValue()) {
                list.add("-quiet");
            }

            getLog().debug("Calling wsdlvalidator with args: " + list);
            try {
                list.add(file.getCanonicalPath());
                String[] pargs = list.toArray(new String[list.size()]);
                ToolRunner.runTool(WSDLValidator.class, WSDLValidator.class
                                   .getResourceAsStream("wsdlvalidator.xml"), false, pargs);
                doneFile.createNewFile();
            } catch (Throwable e) {
                getLog().debug(e);
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    public void execute() throws MojoExecutionException {
        if (includes == null) {
            includes = new String[] {
                "*.wsdl"
            };
        }
        
        markerDirectory.mkdirs();
        
        List<File> wsdls = new ArrayList<File>();
        if (wsdlRoot != null && wsdlRoot.exists()) {
            wsdls.addAll(getWsdlFiles(wsdlRoot));
        }
        if (testWsdlRoot != null && testWsdlRoot.exists()) {
            wsdls.addAll(getWsdlFiles(testWsdlRoot));
        }

        long timestamp = CodegenUtils.getCodegenTimestamp();
        for (File wsdl : wsdls) {
            processWsdl(wsdl, timestamp);
        }


    }
}
