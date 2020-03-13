
package org.jruby.rack;

import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;

import java.lang.Exception;
import java.io.IOException;

public class RackHijackUpgradeHandler implements HttpUpgradeHandler {

    private WebConnection connexion;

    public void init(WebConnection conn) {
        System.out.println("DBG: RackHijackUpgradeHandler: init");
        connexion = conn;
    }

    public void destroy() {
        System.out.println("DBG: RackHijackUpgradeHandler: destroy");
        try {
            connexion.close();
        }
        catch(Exception e) {
            System.out.println("DBG: RackHijackUpgradeHandler: destroy: close failed");
        }
        finally {
            System.out.println("DBG: RackHijackUpgradeHandler: destroy: close finished");
        }
    }

    public WebConnection getWebConnection() {
        return connexion;
    }

    public ServletInputStream getInputStream() throws IOException {
        return connexion.getInputStream();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return connexion.getOutputStream();
    }

}
