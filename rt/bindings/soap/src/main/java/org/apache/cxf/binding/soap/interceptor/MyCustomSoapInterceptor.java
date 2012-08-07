package org.apache.cxf.binding.soap.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/7/12
 */
public class MyCustomSoapInterceptor extends AbstractSoapInterceptor {


    public MyCustomSoapInterceptor() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        Logger LOG = LogUtils.getLogger(MyCustomSoapInterceptor.class);
        LOG.severe("RULES!");
    }
}
