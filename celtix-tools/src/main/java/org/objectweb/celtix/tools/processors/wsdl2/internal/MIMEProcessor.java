package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.Iterator;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaType;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.utils.ProcessorUtil;

public class MIMEProcessor
    extends AbstractProcessor {

    public MIMEProcessor(ProcessorEnvironment penv) {
        super(penv);
    }

    private static String getJavaTypeForMimeType(MIMEPart mPart) {
        if (mPart.getExtensibilityElements().size() > 1) {
            return "javax.activation.DataHandler";
        } else {
            ExtensibilityElement extElement = (ExtensibilityElement)mPart
                .getExtensibilityElements().get(0);
            if (extElement instanceof MIMEContent) {
                MIMEContent mimeContent = (MIMEContent)extElement;
                if (mimeContent.getType().equals("image/jpeg") 
                        || mimeContent.getType().equals("image/gif")) {
                    return "java.awt.Image";
                } else if (mimeContent.getType().equals("text/xml") 
                        || mimeContent.getType().equals("application/xml")) {
                    return "javax.xml.transform.Source";
                }
            }
        }
        return "javax.activation.DataHandler";
    }

    public void process(JavaMethod jm, MIMEMultipartRelated ext, JavaType.Style style)
        throws ToolException {
        List mimeParts = ext.getMIMEParts();
        Iterator itParts = mimeParts.iterator();
        while (itParts.hasNext()) {
            MIMEPart mPart = (MIMEPart)itParts.next();
            Iterator extns = mPart.getExtensibilityElements().iterator();
            while (extns.hasNext()) {
                ExtensibilityElement extElement = (ExtensibilityElement)extns.next();
                if (extElement instanceof MIMEContent) {
                    MIMEContent mimeContent = (MIMEContent)extElement;
                    String mimeJavaType = getJavaTypeForMimeType(mPart);
                    if (JavaType.Style.IN.equals(style)) {
                        String paramName = ProcessorUtil.mangleNameToVariableName(mimeContent
                            .getPart());
                        JavaParameter jp = jm.getParameter(paramName);
                        if (jp == null) {
                            throw new ToolException(
                                            "MIME part "
                                                + mimeContent.getPart()
                                                + " could not be mapped to available parts in portType!");
                        }
                        if (!jp.getClassName().equals(mimeJavaType)) {
                            // jp.setType(mimeJavaType);
                            jp.setClassName(mimeJavaType);
                        }
                    } else if (JavaType.Style.OUT.equals(style)) {
                        if (mimeParts.size() > 2) {
                            // more than 1 mime:content part (1 root soap body),
                            // javaReturn will be set to void and
                            // all output parameter will be treated as the
                            // holder class
                            String paramName = ProcessorUtil.mangleNameToVariableName(mimeContent
                                .getPart());
                            JavaParameter jp = jm.getParameter(paramName);
                            if (jp == null) {
                                throw new ToolException(
                                            "MIME part "
                                                + mimeContent.getPart()
                                                + " could not be mapped to available parts in portType!");
                            } else {
                                if (!jp.getClassName().equals(mimeJavaType)) {
                                    // jp.setType(mimeJavaType);
                                    jp.setClassName(mimeJavaType);
                                    jp.setHolderClass(mimeJavaType);
                                }
                            }
                        } else {
                            if (!jm.getReturn().getClassName().equals(mimeJavaType)) {
                                // jm.getReturn().setType(mimeJavaType);
                                jm.getReturn().setClassName(mimeJavaType);
                            }
                        }
                    }
                }
            }
        }
    }
}
