package org.mx.msecret;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.*;
import org.mx.oauth.client.Credential;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by fsbsilva on 2/20/17.
 */
public class MicroSecretFactory {

    public static void main(String[] args) throws FileNotFoundException {
        Credential credential = new Credential();
        credential.setKeyStorePassword("MasterPassword", false);
        credential.setKeyStoreLocation("/opt/mxdeploy/mxdeploy.kdbx");
        MicroSecretFactory.createDataBase(credential);
    }

    public static void createDataBase(Credential credential) throws FileNotFoundException {
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            if ( Modifier.isFinal(field.getModifiers()) ) {
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            field.setAccessible(true);
            field.setBoolean(null, false); // isRestricted = false;
            field.setAccessible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Build KeePass model
        Group root = new GroupBuilder()
                .addGroup(new GroupBuilder("mxdeploy").build())
                .build();
        KeePassFile keePassFile = new KeePassFileBuilder("mxdeploy")
                .addTopGroups(root)
                .build();

        // Write KeePass file to disk
        KeePassDatabase.write(keePassFile
                , credential.getKeyStorePassword().toString()
                , new FileOutputStream(credential.getKeyStoreLocation()));
    }

    public static void createEntry(String alias, String username, String password, String path) throws FileNotFoundException {
        Entry entry = new EntryBuilder(alias).username(alias).password(alias).build();
        KeePassFile keePassFile = new KeePassFileBuilder("mxdeploy").addTopEntries(entry).build();

        KeePassDatabase.write(keePassFile, "123", new FileOutputStream("/opt/mxdeploy/Database3.kdbx"));

    }
}
