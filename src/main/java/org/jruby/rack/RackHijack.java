package org.jruby.rack;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;

import org.jruby.runtime.Block;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.AsyncContext;

import java.io.IOException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.io.EndPoint;

import java.lang.ClassLoader;

public class RackHijack extends RubyObject {

    private static final ObjectAllocator ALLOCATOR = new ObjectAllocator() {
            public IRubyObject allocate(Ruby runtime, RubyClass klass) {
                return new RackHijack(runtime, klass);
            }
        };

    public static RubyClass getRackHijackClass(final Ruby runtime) { // JRuby::Rack::Hijack

        RubyModule jruby = runtime.getOrCreateModule("JRuby");
        RubyModule rack = (RubyModule) jruby.getConstantAt("Rack");
        if (rack == null) {
            rack = runtime.defineModuleUnder("Rack", jruby);
        }

        RubyClass klass = rack.getClass("Hijack"); // JRuby::Rack getClass('Hijack')
        if (klass == null) {
            final RubyClass parent = runtime.getObject();
            klass = rack.defineClassUnder("Hijack", parent, ALLOCATOR);
            klass.defineAnnotatedMethods(RackHijack.class);
        }

        if (jruby.getConstantAt("RackHijack") == null) { // backwards compatibility
            jruby.setConstant("RackHijack", klass); // JRuby::RackInput #deprecated
        }

        return klass;
    }

    private HttpServletRequestWrapper http_servlet_request_wrapper;

    public RackHijack(Ruby runtime, RubyClass klass) {
        super(runtime, klass);
        System.out.println("DBG: RackHijack: cons");
    }

    public void setHttpServletRequestWrapper(HttpServletRequestWrapper req_wrapper) {
        this.http_servlet_request_wrapper = req_wrapper;
    }

    @JRubyMethod()
    public IRubyObject call(ThreadContext context) throws Exception {
        System.out.println("DBG: RackHijack: call");

        final Ruby runtime = getRuntime();
        final IRubyObject nil = runtime.getNil();
        /*
        ServletRequest srv_req = this.http_servlet_request_wrapper.getRequest();

        System.out.println("DBG: RackHijack: call: srv_req class: " + srv_req.getClass().getName());

        Request req = Request.getBaseRequest(srv_req);

        HttpChannel http_channel;
        EndPoint end_point;
        if (req != null) {
            System.out.println("DBG: RackHijack: call: req class: " + req.getClass().getName());
            http_channel = req.getHttpChannel();
            end_point = http_channel.getEndPoint();
            System.out.println("DBG: RackHijack: call: http_channel class: " + http_channel.getClass().getName());
            System.out.println("DBG: RackHijack: call: end_point class: " + end_point.getClass().getName());
        } else {
            System.out.println("DBG: RackHijack: call: DO NOT HAVE base request.");
            throw new Exception("shit balls");
        }
        */

        // Get the TCP socket out of the request.
        // Wrap the socket in something that has ruby IO semantics
        // Return th IO

        RackHijackIO rh_io;
        try {
            rh_io =
                new RackHijackIO(runtime,
                                 RackHijackIO.getRackHijackIOClass(runtime));

            final AsyncContext async_context =
                this.http_servlet_request_wrapper.startAsync();

            rh_io.init(async_context);
        }
        catch(IllegalStateException illegal_state_e) {
            System.out.println("DBG: RackHijack: call: illegal_state_exception: " + illegal_state_e.getMessage());
            rh_io = null;
        }
        catch(Exception e) {
            System.out.println("DBG: RackHijack: call: exception: " + e.getMessage());
            rh_io = null;
        }
        finally {
            System.out.println("DBG: RackHijack: call: finished");
        }

        return (rh_io != null ? JavaEmbedUtils.javaToRuby(runtime, rh_io) : runtime.getNil());
    }
}

