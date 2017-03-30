package org.mx.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mx.server.GlobalVariableService;
import org.mx.server.MicroServerBean;
import org.mx.server.MicroServerFactory;
import org.mx.var.MicroVariableFactory;
import org.mx.var.MicroVariableMap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by fsbsilva on 11/29/16.
 */

/**
 * Reads the MicroClient stream from the client through the command, then hands off
 * processing to the appropriate class. The MicroSession obtains its sockets from
 * an MicroSessionPool, which created this MicroSession.
 *
 * @author <a href="http://www.mxdeploy.com">Fabio S B Silva</a>
 */
public class MicroClient {

    private DataOutputStream sockout = null;
    private DataInputStream sockin = null;
    private SSLSocket sslsocket = null;
    private int CHUNCKTYPE = MicroConstants.CHUNKTYPE_LISTTHREAD;
    private String MACTION_NAME = "List Threads";

    private String PARENT_BASEDIR;
    private String MCLIENT_NAME=null;
    private String MSERVER_NAME=null;
    private boolean MCLIENT_ASYNC = false;
    private String MSERVER_HOST;
    private int MSERVER_PORT;
    private String MODULE_NAME=null;

    public static void main(String[] args) {
        try {
            (new MicroClient()).execute(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        PARENT_BASEDIR = System.getProperty("parent.basedir");
        MCLIENT_NAME   = System.getProperty("mclient.name");
        MSERVER_NAME   = System.getProperty("mserver.name");

        if( System.getProperty("mclient.async") != null ) {
            MCLIENT_ASYNC = Boolean.valueOf(System.getProperty("mclient.async"));
        }

        String VAR_YML = PARENT_BASEDIR+ "/micro-env-variables.yml";
        String MCLIENT_YML_PATH = PARENT_BASEDIR+ "/micro-clients.yml";

        MicroVariableMap microVariableMap = MicroVariableFactory.parser(VAR_YML);
        GlobalVariableService.setMicroServerEnvironmetVariable(microVariableMap);

        if( System.getProperty("mclient.chunktype")!= null ) {
            CHUNCKTYPE = Integer.valueOf(System.getProperty("mclient.chunktype"));
        }

        if( System.getProperty("maction.name") != null) {
            MACTION_NAME = System.getProperty("maction.name");
        }

        if( System.getProperty("module.name") != null) {
            MODULE_NAME    = System.getProperty("module.name");
//            System.out.print("MODULE_NAME: "+MODULE_NAME);
        }

        if( System.getProperty("java.protocol.handler.pkgs") == null ) {
            System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        }

        if( CHUNCKTYPE == 0 ){
            String BASEDIR = microVariableMap.get("basedir");
            if( MSERVER_NAME == null ){
                MSERVER_NAME=microVariableMap.get("mserver.name");
            }
            MicroServerBean microServer = MicroServerFactory.parser(BASEDIR+"/micro-servers.yml").get(MSERVER_NAME);
            MSERVER_HOST= microServer.getHost();
            MSERVER_PORT= microServer.getPort();

            Map<String,MicroClientYml> mclientMap = MicroClientYmlFactory.parser(MCLIENT_YML_PATH);
            for (String name : mclientMap.keySet()) {
                MicroClientYml mclientYml = mclientMap.get(name);
                if( MSERVER_HOST.equals(mclientYml.getHost()) && MSERVER_PORT == mclientYml.getPort() ){
                    System.setProperty("javax.net.ssl.trustStore",mclientYml.getTruststorePath());
                }
            }
        } else {
            // Grab default micro-server-client name
            if( MCLIENT_NAME == null ) {
                MCLIENT_NAME = microVariableMap.get("mclient.name");
            }

            MicroClientYml microClientYml = MicroClientYmlFactory.parser(MCLIENT_YML_PATH).get(MCLIENT_NAME);
            MSERVER_HOST=microClientYml.getHost();
            MSERVER_PORT=microClientYml.getPort();
            System.setProperty("javax.net.ssl.trustStore",microClientYml.getTruststorePath());
        }

    }

    private void execute(String[] args) throws IOException {
        init();
        try {
            Security.addProvider ( new com.sun.net.ssl.internal.ssl.Provider());

            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslsocket = (SSLSocket) factory.createSocket(MSERVER_HOST, MSERVER_PORT);
            sslsocket.setEnabledCipherSuites(sslsocket.getEnabledCipherSuites());
            sslsocket.startHandshake();

            sockout = new DataOutputStream(sslsocket.getOutputStream());
            sockin = new DataInputStream(sslsocket.getInputStream());

            MicroClientSession session =  new MicroClientSession();
            session.setActionName(this.MACTION_NAME);
            session.setCallDate(new Date());
            if( MODULE_NAME != null ){
                session.setModuleName(MODULE_NAME);
            }
            //session.setScriptPath(this.scriptPath);
            session.setASync(this.MCLIENT_ASYNC);
            if( CHUNCKTYPE == MicroConstants.CHUNKTYPE_STOPSERVER ) {
                session.setASync(this.MCLIENT_ASYNC);
            }

            session.setArgs(args);

            ObjectMapper objectMapper = new ObjectMapper();

            String jsonStr = objectMapper.writeValueAsString(session);
            sockout.writeInt(this.CHUNCKTYPE);
            sockout.writeInt(jsonStr.length());
            sockout.writeBytes(jsonStr);

            if( session.isASync() && this.CHUNCKTYPE == MicroConstants.CHUNKTYPE_RUNSCRIPT ) {
                System.out.println( "MicroServerGateway is going to perform the script in Asynchronous mode" );
            } else {
                int bytesToRead = sockin.readInt();
                while (bytesToRead != -1) {
                    int chunktype = sockin.readByte();
                    byte[] b = new byte[(int) bytesToRead];
                    sockin.readFully(b);
                    String response = new String(b, "UTF-8");
                    System.out.print(response);

                    bytesToRead = sockin.readInt();
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (ConnectException ce){
            System.out.println("Micro Server looks down or host:port is not well set up");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if( sockout != null ) {
                sockout.close();
                sockin.close();
                sslsocket.close();
            }
        }
    }
}