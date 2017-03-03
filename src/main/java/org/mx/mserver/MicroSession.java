package org.mx.mserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.mx.maction.MicroAction;
import org.mx.mmodule.MicroModule;
import org.mx.mmodule.MicroModuleFactory;
import org.mx.mrepo.MicroRepository;
import org.mx.mclient.MicroClientSession;
import org.mx.mclient.MicroConstants;

import javax.net.ssl.SSLSocket;
import java.io.*;

/**
* Reads the MXOAuth stream from the client through the command, then hands off
* processing to the appropriate class. The MicroSession obtains its sockets from
* an MicroSessionPool, which created this MicroSession.
*
* @author <a href="http://www.mxdeploy.com">Fabio S B Silva</a>
*/
public class MicroSession extends MicroSessionImpl {

    private Logger logger = Logger.getLogger(MicroSession.class);

    // can't create MicroInputStream until we've received a command, because at
    // that point the stream from the client will only include stdin and stdin-eof
    // chunks
    private InputStream in = null;
    private PrintStream out = null;
    private PrintStream err = null;
    private PrintStream exit = null;

    private DataInputStream sockin = null;
    private DataOutputStream sockout = null;

    private String MSERVER_NAME = null;
    /**
     * Creates a new MicroSession running for the specified MicroSessionPool and
     * MicroServerGateway.
     *
     * @param sessionPool The MicroSessionPool we're working for
     */
    MicroSession(MicroSessionPool sessionPool, SSLSocketThread sslSocketThread) {
        super(sessionPool, sslSocketThread);
    }

    public void run(){
        updateThreadName(null);

        SSLSocket sslsocket = nextSocket();
        while (sslsocket != null) {
            MicroClientSession session = null;
            try {
                sockin = new DataInputStream(sslsocket.getInputStream());
                sockout = new DataOutputStream(sslsocket.getOutputStream());

                // Read a integer with type of action
                int chunkType = sockin.readInt();
                int bytesToRead = sockin.readInt();

                /* Read Jason String */
                byte[] b = new byte[(int) bytesToRead];
                sockin.readFully(b);
                String jsonInString = new String(b, "UTF-8");
                ObjectMapper mapper = new ObjectMapper();

                try {
                    session = mapper.readValue(jsonInString, MicroClientSession.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String threadName;
                if (sslsocket.getInetAddress() != null) {
                    threadName = sslsocket.getInetAddress().getHostAddress() + ": " + session.getActionName();
                } else {
                    threadName = session.getScriptPath();
                }
                updateThreadName(threadName);

                try {

                    switch (chunkType) {
                        case MicroConstants.CHUNKTYPE_RUNSCRIPT:
                            if( ! session.isASync() ) {
                                createClientStreamSync(sockin,sockout);
                            }
                            logger.debug("Running Job");

                            MicroServer mserverYml = GlobalVariableService.getMicroServer();

                            MicroRepository microRepository = GlobalVariableService.getMicroRepository();
                            MicroAction maction = null;

                            if( session.getModuleName() != null ){
                                logger.debug("Module Name: "+session.getModuleName());
                                MicroModule microModule = microRepository.getMicroModuleMap().get(session.getModuleName());
//                                System.out.println(microModule.getSrc());
                                microModule = MicroModuleFactory.parser(microModule.getSrc());
                                maction = microModule.getMicroActionMap().get(session.getActionName());
                            } else {
                                logger.debug("-Dmodule.name is null");
                                maction = microRepository.getMicroActionMap().get(session.getActionName());
                            }
                            if( maction == null ){
                                String errorMsg = "  Error: MicroAction "+session.getActionName()+" in "+mserverYml.getRepositoryPath()+" doesn't exit";
                                System.out.println(errorMsg);
                                logger.error(errorMsg);
                            } else {
                                if( ! ( new File(maction.getSrc())).exists() ){
                                    String errorMsg = "  Error: Script "+maction.getSrc()+" doesn't exit";
                                    System.out.println(errorMsg);
                                    logger.error(errorMsg);
                                } else {
                                    ScriptGateway scritpGatway = new ScriptGateway();
                                    scritpGatway.execute(maction.getSrc(), session.getArgs());
                                }
                            }
                            break;
                        case MicroConstants.CHUNKTYPE_STOPSERVER:
                            logger.debug("Stopping JVM");
                            try {
                                sockout.writeInt(-1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            stopMicroServer();
                            break;
                        case MicroConstants.CHUNKTYPE_LISTTHREAD:
                            createClientStreamSync(sockin,sockout);
                            logger.debug("List Thread");
                            listThreads(threadName);
                            break;
                        case MicroConstants.CHUNKTYPE_STOPTHREAD:
                            createClientStreamSync(sockin,sockout);
                            logger.debug("Stop Thread");
                            stopThread( session.getArgs()[0] );
                            break;
                        default:
                            sockout.writeInt(MicroConstants.CHUNKTYPE_ERROR);
                    }
                } finally {
                    if( ! session.isASync() ) {
                        try {
                            sockout.writeInt(-1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        destroyClientStreamSync(sockin,sockout);
                    }

                    sslsocket.close();
                }

            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                if( session != null && ! session.isASync() ) {
                    ((ThreadLocalInputStream) System.in).init(null);
                    ((ThreadLocalPrintStream) System.out).init(null);
                    ((ThreadLocalPrintStream) System.err).init(null);
                }
                //updateThreadName(null);
                sessionPool.give(this);
                sslsocket = nextSocket();
            }
        }
    }

    private void createClientStreamSync( DataInputStream sockin, DataOutputStream sockout ){
        in = new MicroInputStream(sockin, sockout, sslSocketThread.out, MicroConstants.HEARTBEAT_TIMEOUT_MILLIS);
        out = new PrintStream(new MicroOutputStream(sockout, MicroConstants.CHUNKTYPE_STDOUT));
        err = new PrintStream(new MicroOutputStream(sockout, MicroConstants.CHUNKTYPE_STDERR));
        exit = new PrintStream(new MicroOutputStream(sockout, MicroConstants.CHUNKTYPE_EXIT));

        // ThreadLocal streams for System.in/out/err redirection
        ((ThreadLocalInputStream) System.in).init(in);
        ((ThreadLocalPrintStream) System.out).init(out);
        ((ThreadLocalPrintStream) System.err).init(err);
    }

    private void destroyClientStreamSync( DataInputStream sockin, DataOutputStream sockout ) throws IOException {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (err != null) {
            err.close();
        }
        if (exit != null) {
            exit.close();
        }
        sockout.flush();
    }

    private void stopMicroServer(){
        sslSocketThread.getMicroSessionHashMap().forEach((s, t) -> {
            if( t.isAlive() )
                t.interrupt();
        }  );
        System.exit(0);
    }

    private void listThreads(String threadName){
        System.out.println("#----------------------------------------------------------------------------------------");
        System.out.println("# UUID                                 |  Thread name  ");
        System.out.println("#----------------------------------------------------------------------------------------");

        sslSocketThread.getMicroSessionHashMap().forEach((s, t) -> {
            if (t.isAlive() && !s.isIdle() ) {
                System.out.println("  " + s.getUUID() + "    " + t.getName());
            }
        });
    }

    private void stopThread(String uuid ){
        sslSocketThread.getMicroSessionHashMap().forEach((s, t) -> {
            if( s.getUUID() == uuid && !s.isIdle() )
                t.interrupt();
        }  );
        System.exit(0);
    }


}
