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

import java.io.IOException;

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
    public IRubyObject call(ThreadContext context) {
        final Ruby runtime = getRuntime();
        System.out.println("DBG: RackHijack: call");

        RackHijackIO rh_io =
            new RackHijackIO(runtime,
                             RackHijackIO.getRackHijackIOClass(runtime));

        RackHijackUpgradeHandler rh_upgrade_handler = null;

        try {
            rh_upgrade_handler =
                http_servlet_request_wrapper.upgrade(RackHijackUpgradeHandler.class);
            rh_io.setWebConnection(rh_upgrade_handler.getWebConnection());
        }
        catch(ServletException se) {
            System.out.println("DBG: RackHijack: call: ServletException: " + se.getMessage());
            rh_io = null;
        }
        catch(IOException e) {
            System.out.println("DBG: RackHijack: call: IOException");
        }
        finally {
            System.out.println("DBG: RackHijack: call: upgrade handler finished");
        }


        /*
         * Now the game is to Upgrade the socket and return the ...
         * ... what?
         *
         * Figure out what goes here
         * According to ActionCable::Connection::Stream, this returns
         * "the underlying IO object" and it must respond to `write_nonblock`
         * like all good Ruby IO objects.
         */

        // Upgrade the socket, jetty/java style.
        // Obtain the websocket
        // Wrap the socket in a RackHijackIO
        // return the RackHijackIO
        return rh_io;
    }
}
