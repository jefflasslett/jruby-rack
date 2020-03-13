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

import javax.servlet.http.WebConnection;

public class RackHijackIO extends RubyObject {

    private static final ObjectAllocator ALLOCATOR = new ObjectAllocator() {
            public IRubyObject allocate(Ruby runtime, RubyClass klass) {
                return new RackHijackIO(runtime, klass);
            }
        };

    public static RubyClass getRackHijackIOClass(final Ruby runtime) {

        RubyModule jruby = runtime.getOrCreateModule("JRuby");
        RubyModule rack = (RubyModule) jruby.getConstantAt("Rack");
        if (rack == null) {
            rack = runtime.defineModuleUnder("Rack", jruby);
        }

        RubyClass klass = rack.getClass("HijackIO");
        if (klass == null) {
            final RubyClass parent = runtime.getObject();
            klass = rack.defineClassUnder("HijackIO", parent, ALLOCATOR);
            klass.defineAnnotatedMethods(RackHijackIO.class);
        }

        if (jruby.getConstantAt("RackHijackIO") == null) { // backwards compatibility
            jruby.setConstant("RackHijackIO", klass);
        }

        return klass;
    }

    private WebConnection web_socket;

    public RackHijackIO(Ruby runtime, RubyClass klass) {
        super(runtime, klass);
        System.out.println("DBG: RackHijackIO: cons");
    }

    public void setWebConnection(WebConnection conn) {
        this.web_socket  = conn;
    }

    @JRubyMethod()
    public IRubyObject write_nonblock(ThreadContext context) {
        System.out.println("DBG: RackHijackIO: write_nonblock");
        return null;
    }
}
