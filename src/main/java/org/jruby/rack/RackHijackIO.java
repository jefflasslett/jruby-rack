package org.jruby.rack;

import org.jruby.Ruby;
import org.jruby.RubyIO;
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

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

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

    private AsyncContext async_context;
    private ServletInputStream istream;
    private ServletOutputStream ostream;
    private RubyIO in_io;
    private RubyIO out_io;
    private boolean _is_closed = false;

    public RackHijackIO(Ruby runtime, RubyClass klass) {
        super(runtime, klass);
        async_context = null;
        System.out.println("DBG: RackHijackIO: cons");
    }

    public void init(AsyncContext async_context) {
        ServletRequest req = async_context.getRequest();
        ServletResponse rsp = async_context.getResponse();
        final Ruby runtime = getRuntime();

        this.async_context = async_context;
        try {
            this.istream = req.getInputStream();
            this.ostream = rsp.getOutputStream();
            this.in_io = new RubyIO(runtime, this.istream);
            this.out_io = new RubyIO(runtime, this.ostream);
        }
        catch(IOException io_e) {
            System.out.println("DBG: RackHijackIO: init: IOException: " + io_e.getMessage());
        }
    }

    /*
     * IO#write
     * =======================================================================
     */

    @JRubyMethod(name = "write", required = 1)
    public IRubyObject write(ThreadContext context, IRubyObject str) {
        return out_io.write(context, str);
    }

    /*
     * IO#write_nonblock
     * =======================================================================
     */

    @JRubyMethod(name = "write_nonblock", required = 1, optional = 1)
    public IRubyObject write_nonblock(ThreadContext context, IRubyObject argv) {
        return out_io.write_nonblock(context, argv);
    }

    /*
     * IO#read
     * =======================================================================
     */

    @JRubyMethod(name = "read")
    public IRubyObject read(ThreadContext context) {
        return in_io.read(context, context.nil, context.nil);
    }

    @JRubyMethod(name = "read")
    public IRubyObject read(ThreadContext context, IRubyObject arg0) {
        return in_io.read(context, arg0, context.nil);
    }

    @JRubyMethod(name = "read")
    public IRubyObject read(ThreadContext context, IRubyObject length, IRubyObject str) {
        return in_io.read(context, length, str);
    }

    /*
     * IO#read_nonblock
     * =======================================================================
     */
    @JRubyMethod(name = "read_nonblock", required = 1, optional = 2)
    public IRubyObject read_nonblock(ThreadContext context, IRubyObject[] args) {
        return in_io.read_nonblock(context, args);
    }

    /*
     * IO#flush
     * =======================================================================
     */

    @JRubyMethod()
    public IRubyObject flush() {
        return out_io.flush();
    }

    /*
     * IO#close
     * =======================================================================
     */

    @JRubyMethod()
    public IRubyObject close(ThreadContext context) {
        if (!_is_closed) {
            async_context.complete();
            in_io.close();
            out_io.close();
            _is_closed = true;
        }
        IRubyObject nil = getRuntime().getNil();
        return nil;
    }

    @JRubyMethod()
    public IRubyObject close_read(ThreadContext context) {
        return this.close(context);
    }

    @JRubyMethod()
    public IRubyObject close_write(ThreadContext context) {
        return this.close(context);
    }

    @JRubyMethod()
    public IRubyObject is_closed(ThreadContext context) {
        return getRuntime().newBoolean(this._is_closed);
    }

    @JRubyMethod(name = "to_io")
    public RubyIO to_io() {
        return this.in_io;
    }
}

