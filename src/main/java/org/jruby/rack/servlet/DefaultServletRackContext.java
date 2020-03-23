/*
 * Copyright (c) 2010-2012 Engine Yard, Inc.
 * Copyright (c) 2007-2009 Sun Microsystems, Inc.
 * This source code is available under the MIT license.
 * See the file LICENSE.txt for details.
 */

package org.jruby.rack.servlet;

import org.jruby.rack.RackApplicationFactory;
import org.jruby.rack.RackConfig;
import org.jruby.rack.RackLogger;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.SessionTrackingMode;
import javax.servlet.SessionCookieConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;
import javax.servlet.Filter;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.EventListener;
import java.util.Map;

/**
 *
 * @author nicksieger
 */
@SuppressWarnings("deprecation")
public class DefaultServletRackContext implements ServletRackContext {

    private final RackConfig config;
    private final ServletContext context;
    private final RackLogger logger;

    public void setRequestCharacterEncoding(String encoding) {
        System.out.println("DBG:DefaultServletRackContext#setRequestCharacterEncoding");
        throw new UnsupportedOperationException("setRequestCharacterEncoding()");
    }

    public String getRequestCharacterEncoding() {
        System.out.println("DBG:DefaultServletRackContext#getRequestCharacterEncoding");
        throw new UnsupportedOperationException("getRequestCharacterEncoding()");
    }

    public void setSessionTimeout(int time_out) {
        throw new UnsupportedOperationException("setSessionTimeout()");
    }

    public int getSessionTimeout() {
        throw new UnsupportedOperationException("getSessionTimeout()");
    }

    public String getVirtualServerName() {
        throw new UnsupportedOperationException("getVirtualServerName()");
    }

    public void declareRoles(String... role_names) {
        throw new UnsupportedOperationException("declareRoles()");
    }

    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException("getClassLoader()");
    }

    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException("getJspConfigDescriptor()");
    }

    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException("createListener()");
    }

    public void addListener(Class<? extends EventListener> listenerClass) {
        throw new UnsupportedOperationException("addListener()");
    }

    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException("addListener(T)");
    }

    public void addListener(String className) {
        throw new UnsupportedOperationException("addListener(String)");
    }

    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new UnsupportedOperationException("getEffectiveSessionTrackingModes(String)");
    }

    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new UnsupportedOperationException("getDefaultSessionTrackingModes()");
    }

    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        throw new UnsupportedOperationException("setSessionTrackingModes()");
    }

    public SessionCookieConfig getSessionCookieConfig() {
        throw new UnsupportedOperationException("getSessionCookieConfig()");
    }

    public Map<String,? extends FilterRegistration> getFilterRegistrations()  {
        throw new UnsupportedOperationException("getFilterRegistrations()");
    }

    public FilterRegistration getFilterRegistration(String filterName) {
        throw new UnsupportedOperationException("getFilterRegistration()");
    }

    public <T extends Filter> T createFilter(Class<T> clazz)
        throws ServletException {
        throw new UnsupportedOperationException("createFilter(Class<T>)");
    }

    public FilterRegistration.Dynamic addFilter(String filterName,
                                                Class<? extends Filter> filterClass) {
        throw new UnsupportedOperationException("addFilter(String, Class<? extends Filter>)");
    }

    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        throw new UnsupportedOperationException("addFilter(String, Filter)");
    }

    public FilterRegistration.Dynamic addFilter(String filterName,
                                                String className) {
        throw new UnsupportedOperationException("addFilter(String, String)");
    }

    public Map<String,? extends ServletRegistration> getServletRegistrations() {
        throw new UnsupportedOperationException("getServletRegistrations()");
    }

    public ServletRegistration getServletRegistration(String servletName) {
        throw new UnsupportedOperationException("getServletRegistrations(String)");
    }

    public <T extends Servlet> T createServlet(Class<T> clazz)
        throws ServletException {
        throw new UnsupportedOperationException("createServlet(Class<T>)");
    }

    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        throw new UnsupportedOperationException("addJspFile(String, String)");
    }

    public ServletRegistration.Dynamic 	addServlet(String servletName, String className) {
        throw new UnsupportedOperationException("addServlet(String, String)");
    }

    public ServletRegistration.Dynamic 	addServlet(String servletName, Servlet servlet) {
        throw new UnsupportedOperationException("addServlet(String, Servlet)");
    }

    public ServletRegistration.Dynamic 	addServlet(String servletName, Class<? extends Servlet> servletClass) {
        throw new UnsupportedOperationException("addServlet(String, Class<? extends Servlet>)");
    }

    public boolean 	setInitParameter(String name, String value) {
        throw new  UnsupportedOperationException("setInitParameter(String,String)");
    }

    public int 	getEffectiveMajorVersion() {
        // Returns the major version of the Servlet API that this servlet container supports.
        return 4;
    }

    public int 	getEffectiveMinorVersion() {
        return 0;
    }

    public DefaultServletRackContext(ServletRackConfig config) {
        this.config  = config;
        this.context = config.getServletContext();
        this.logger  = config.getLogger();
    }

    public String getInitParameter(final String key) {
        return config.getProperty(key);
    }

    public String getRealPath(final String path) {
        String realPath = context.getRealPath(path);
        if (realPath == null) { // some servers don't like getRealPath, e.g. w/o exploded war
            try {
                final URL url = context.getResource(path);
                if (url != null) {
                    final String urlPath = url.getPath();
                    // still might end up as an URL with path "file:/home"
                    if (urlPath.startsWith("file:")) {
                        // handles "file:/home" and "file:///home" as well
                        realPath = new URL(urlPath).getPath(); // "/home"
                    }
                    else {
                        realPath = urlPath;
                    }
                }
            }
            catch (MalformedURLException e) { /* ignored */ }
        }
        return realPath;
    }

    public RackApplicationFactory getRackFactory() {
        return (RackApplicationFactory) context.getAttribute(RackApplicationFactory.FACTORY);
    }

    public ServletContext getContext() {
        return context;
    }

    public ServletContext getContext(String path) {
        return context.getContext(path);
    }

    public String getContextPath() {
        return context.getContextPath();
    }

    public int getMajorVersion() {
        return context.getMajorVersion();
    }

    public int getMinorVersion() {
        return context.getMinorVersion();
    }

    public String getMimeType(String file) {
        return context.getMimeType(file);
    }

    public Set getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }

    public URL getResource(String path) throws MalformedURLException {
        return context.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
        return context.getResourceAsStream(path);
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return context.getRequestDispatcher(path);
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        return context.getNamedDispatcher(name);
    }

    @Deprecated
    public Servlet getServlet(String name) throws ServletException {
        return context.getServlet(name);
    }

    @Deprecated
    public Enumeration getServlets() {
        return context.getServlets();
    }

    @Deprecated
    public Enumeration getServletNames() {
        return context.getServletNames();
    }

    @Deprecated
    public void log(Exception e, String msg) {
        logger.log(msg, e);
    }

    public String getServerInfo() {
        return context.getServerInfo();
    }

    public Enumeration getInitParameterNames() {
        return context.getInitParameterNames();
    }

    public Object getAttribute(String key) {
        return context.getAttribute(key);
    }

    public RackConfig getConfig() {
        return config;
    }

    public Enumeration getAttributeNames() {
        return context.getAttributeNames();
    }

    public void setAttribute(String key, Object val) {
        context.setAttribute(key, val);
    }

    public void removeAttribute(String key) {
        context.removeAttribute(key);
    }

    public String getServletContextName() {
        return context.getServletContextName();
    }

    // RackLogger

    public void log(String message) {
        logger.log(message);
    }

    public void log(String message, Throwable e) {
        logger.log(message, e);
    }

    public void log(String level, String message) {
        logger.log(level, message);
    }

    public void log(String level, String message, Throwable e) {
        logger.log(level, message, e);
    }

    // Helpers

    ServletContext getRealContext() { return getContext(); }

    public static ServletContext getRealContext(final ServletContext context) {
        if ( context instanceof DefaultServletRackContext ) {
            return ((DefaultServletRackContext) context).getRealContext();
        }
        return context;
    }

    // @Override
    // public void setResponseCharacterEncoding(String encoding) {
    //     throw new UnsupportedOperationException("setResponseCharacterEncoding()");
    // }

    // @Override
    // public String getResponseCharacterEncoding() {
    //     throw new UnsupportedOperationException("getResponseCharacterEncoding()");
    // }

}
