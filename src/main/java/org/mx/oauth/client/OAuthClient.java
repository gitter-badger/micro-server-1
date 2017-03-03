package org.mx.oauth.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;


import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.Security;

/**
* Reads the MXAgent stream from the client through the command, then hands off
* processing to the appropriate class. The MXSession obtains its sockets from
* an MXSessionPool, which created this MXSession.
*
* @author <a href="http://www.mxdeploy.com">Fabio S B Silva</a>
*/
public class OAuthClient {

	final static Logger logger = Logger.getLogger(OAuthClient.class);

	private DataOutputStream sockout = null;
	private DataInputStream sockin = null;
	private SSLSocket sslsocket = null;

	public void connect(String oauthProviderAddr, int oauthPort) throws IOException {
		logger.debug("Starting OAuthClient");
		Security.addProvider ( new com.sun.net.ssl.internal.ssl.Provider());

		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		sslsocket = (SSLSocket) factory.createSocket(oauthProviderAddr, oauthPort);
		sslsocket.setEnabledCipherSuites(sslsocket.getEnabledCipherSuites());
		sslsocket.startHandshake();

		sockout = new DataOutputStream(sslsocket.getOutputStream());
		sockin = new DataInputStream(sslsocket.getInputStream());
	}

	public void connect(int oauthPort) throws IOException {
		connect("localhost",oauthPort);
	}

	public void close(){
		try {
			sockin.close();
			sockout.close();
			sslsocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public OAuthToken registerToken() throws IOException {
		sendData(null, OAuthConstants.CHUNKTYPE_REGISTER_TOKEN);
		String oauthTokenJson = responseJson();

		ObjectMapper mapper = new ObjectMapper();
		OAuthToken oauthToken = mapper.readValue(oauthTokenJson, OAuthToken.class);
		logger.debug("Token "+oauthToken.getToken()+" Registered");

		return oauthToken;
	}

	public OAuthToken validateToken(OAuthToken oauthToken) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String oauthTokenJson = mapper.writeValueAsString(oauthToken);
		sendData(oauthTokenJson, OAuthConstants.CHUNKTYPE_VALIDATE_TOKEN);

		return null;
	}

	private String responseJson() throws IOException {
		int chunkType = sockin.readInt();
		String result = "";

		switch (chunkType) {

			case OAuthConstants.CHUNKTYPE_ERROR:
				logger.error("ERROR 404");
				new IOException("Error 404");
				break;
			default:
				while (chunkType != OAuthConstants.CHUNKTYPE_STDIN_EOF) {
					int bytesToRead = sockin.readInt();
					byte[] b = new byte[(int) bytesToRead];
					sockin.readFully(b);
					String line = new String(b, "UTF-8");
					result = result + line;
					chunkType = sockin.readInt();
				}
		}
		return result;
	}

	private void sendData(String strJson, int type ) throws IOException {
		sockout.writeInt(type);
		if( strJson != null ) {
			sockout.writeInt(strJson.length());
			sockout.writeBytes(strJson);
		}
	}

	public static void main(String args[]) throws IOException {
		DataOutputStream sockout = null;
		DataInputStream sockin = null;
		SSLSocket sslsocket = null;
		
//		Double NSEC_PER_SEC = 1000000000.0;
//		Double HEARTBEAT_TIMEOUT_NANOS = NSEC_PER_SEC / 2;
//		Double HEARTBEAT_TIMEOUT_SECS = HEARTBEAT_TIMEOUT_NANOS / (NSEC_PER_SEC * 1.0);
		try {
			System.out.println("STRRT...");
			Security.addProvider ( new com.sun.net.ssl.internal.ssl.Provider());
			
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			sslsocket = (SSLSocket) factory.createSocket("localhost", 25000);
			sslsocket.setEnabledCipherSuites(sslsocket.getEnabledCipherSuites());
			sslsocket.startHandshake();
			
			sockout = new DataOutputStream(sslsocket.getOutputStream());
			sockin = new DataInputStream(sslsocket.getInputStream());
			
			String str = "vmstat";
			sockout.writeInt(str.length());
			sockout.writeBytes(str);

            int chunkType = sockin.readInt();
            while (chunkType != -1 ) {
            	int bytesToRead = sockin.readInt();
                byte[] b = new byte[(int) bytesToRead];
                sockin.readFully(b);
                String line = new String(b, "UTF-8");
                
                System.out.println(line);
                chunkType = sockin.readInt();
            }
						
			System.out.println("END...");


		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
	        sockout.close();
			sockin.close();
			sslsocket.close();				
		}
	}
}
