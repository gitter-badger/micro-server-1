import org.mx.secret.MicroSecretFactory
import org.mx.oauth.client.Credential;

println "Creating mxdeploy.kdbx"

try {
    if( args.length == 0 ){
        println "KeystorePassword parameter is required"
        exit
    }

    def keystorePassword = args[0]
    def keystoreLocation = args[1]

    Credential credential = new Credential();
    credential.setKeyStorePassword(keystorePassword, false);
    credential.setKeyStoreLocation(keystoreLocation);
    MicroSecretFactory.createDataBase(credential);

//    MicroSecretFactory.createDataBase()
    println "mxdeploy.kdbx created !"
} catch (Exception e ) {
    e.printStackTrace()
}

