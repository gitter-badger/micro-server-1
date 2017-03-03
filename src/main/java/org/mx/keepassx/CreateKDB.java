package org.mx.keepassx;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by fsbsilva on 2/20/17.
 */
public class CreateKDB {

    public static void main(String[] args) throws FileNotFoundException {

        Entry entry = new EntryBuilder("First entry").username("Peter").password("Peters secret").build();


        KeePassFile keePassFile = new KeePassFileBuilder("DataBase")
                .addTopEntries(entry)
                .build();

        // Write KeePass file to disk
        KeePassDatabase.write(keePassFile, "MasterPassword", new FileOutputStream("/opt/mxdeploy/Database2.kdbx"));
    }
}
