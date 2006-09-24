/*
 * $Id$
 *
 * Copyright 2006 The Apache Software Foundation.
 *
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
 */
package org.apache.struts2.dispatcher;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ognl.Ognl;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.StrutsTestCase;
import org.apache.struts2.config.StrutsXmlConfigurationProvider;
import org.springframework.mock.web.MockServletContext;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.mock.MockActionInvocation;
import com.opensymphony.xwork2.util.OgnlValueStack;


/**
 */
public class ServletRedirectResultTest extends StrutsTestCase implements StrutsStatics {

    protected ServletRedirectResult view;
    private Mock requestMock;
    private Mock responseMock;
    protected ActionInvocation ai;


    public void testAbsoluteRedirect() {
        view.setLocation("/bar/foo.jsp");
        responseMock.expectAndReturn("encodeRedirectURL", "/context/bar/foo.jsp", "/context/bar/foo.jsp");
        responseMock.expect("sendRedirect", C.args(C.eq("/context/bar/foo.jsp")));

        try {
            view.execute(ai);
            requestMock.verify();
            responseMock.verify();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testPrependServletContextFalse() {
        view.setLocation("/bar/foo.jsp");
        view.setPrependServletContext(false);
        responseMock.expectAndReturn("encodeRedirectURL", "/bar/foo.jsp", "/bar/foo.jsp");
        responseMock.expect("sendRedirect", C.args(C.eq("/bar/foo.jsp")));

        try {
            view.execute(ai);
            requestMock.verify();
            responseMock.verify();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testRelativeRedirect() throws Exception {
        view.setLocation("foo.jsp");
        requestMock.expectAndReturn("getParameterMap", new HashMap());
        requestMock.expectAndReturn("getServletPath", "/namespace/some.action");
        requestMock.expectAndReturn("getAttribute", C.ANY_ARGS, null);
        responseMock.expectAndReturn("encodeRedirectURL", "/context/namespace/foo.jsp", "/context/namespace/foo.jsp");
        responseMock.expect("sendRedirect", C.args(C.eq("/context/namespace/foo.jsp")));

        view.execute(ai);

        requestMock.verify();
        responseMock.verify();
    }

    protected void setUp() throws Exception {
        super.setUp();
        Dispatcher du = new Dispatcher(new MockServletContext());
        Dispatcher.setInstance(du);
        ConfigurationManager cm = new ConfigurationManager();
        cm.addConfigurationProvider(new StrutsXmlConfigurationProvider("struts.xml", false));
        du.setConfigurationManager(cm);
        du.getConfigurationManager().getConfiguration().
            addPackageConfig("foo", new PackageConfig("foo", "/namespace", false, null));
        

        view = new ServletRedirectResult();

        responseMock = new Mock(HttpServletResponse.class);

        requestMock = new Mock(HttpServletRequest.class);
        requestMock.matchAndReturn("getContextPath", "/context");

        ActionContext ac = new ActionContext(Ognl.createDefaultContext(null));
        ac.put(ServletActionContext.HTTP_REQUEST, requestMock.proxy());
        ac.put(ServletActionContext.HTTP_RESPONSE, responseMock.proxy());
        MockActionInvocation ai = new MockActionInvocation();
        ai.setInvocationContext(ac);
        this.ai = ai;
        ai.setStack(new OgnlValueStack());
    }
}
