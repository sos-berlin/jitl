package com.sos.jitl.inventory.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.query.Query;

import com.google.common.base.Charsets;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.reporting.db.DBItemDocumentation;
import com.sos.jitl.reporting.db.DBItemDocumentationImage;
import com.sos.jitl.reporting.db.DBLayer;

public class InventorySosDocuImport {

    private static final String DIRECTORY = "/sos/jitl-jobs";
    private static SOSHibernateSession connection = null;
    private static SOSHibernateFactory factory = null;

    private static List<DBItemDocumentation> getAlreadyExistingSosDocus(String schedulerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBItemDocumentation.class.getSimpleName()).append(" where schedulerId = :schedulerId").append(
                " and directory = :directory");
        Query<DBItemDocumentation> query = connection.createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("directory", DIRECTORY);
        return query.getResultList();
    }

    private static List<DBItemDocumentation> createNewSosDocuDBItems(String schedulerId, Path path) throws IOException {
        List<DBItemDocumentation> docusFromFileSystem = new ArrayList<DBItemDocumentation>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        for (Path filePath : stream) {
            DBItemDocumentation docu = new DBItemDocumentation();
            docu.setDirectory(DIRECTORY);
            docu.setSchedulerId(schedulerId);
            docu.setName(filePath.getFileName().toString());
            docu.setPath(DIRECTORY + "/" + docu.getName());
            docu.setType(docu.getName().replaceFirst(".*\\.([^\\.]+)$", "$1"));
            byte[] content = Files.readAllBytes(filePath);
            if ("gif".equals(docu.getType())) {
                docu.setImage(content);
                docu.setHasImage(true);
            } else {
                if ("js".equals(docu.getType())) {
                    docu.setType("javascript");
                }
                docu.setContent(new String(content, Charsets.UTF_8));
            }
            docusFromFileSystem.add(docu);
        }
        return docusFromFileSystem;
    }

    private static void updateExistingItem(DBItemDocumentation oldItem, DBItemDocumentation newItem) throws SOSHibernateException {
        if (oldItem.getImageId() != null && newItem.image() != null) {
            String md5Hash = DigestUtils.md5Hex(newItem.image());
            DBItemDocumentationImage oldImage = connection.get(DBItemDocumentationImage.class, oldItem.getImageId());
            if (md5Hash != null && !oldImage.getMd5Hash().equals(md5Hash)) {
                oldImage.setImage(newItem.image());
                oldImage.setMd5Hash(md5Hash);
                connection.update(oldImage);
            }
        } else {
            if (!oldItem.getContent().equals(newItem.getContent())) {
                oldItem.setContent(newItem.getContent());
                oldItem.setType(newItem.getType());
                oldItem.setModified(Date.from(Instant.now()));
                connection.update(oldItem);
            }
        }
    }

    private static void saveOrUpdate(String schedulerId, List<DBItemDocumentation> oldDocus, List<DBItemDocumentation> newDocus)
            throws SOSHibernateException {
        for (DBItemDocumentation newItem : newDocus) {
            if (oldDocus.contains(newItem)) {
                DBItemDocumentation oldItem = oldDocus.get(oldDocus.indexOf(newItem));
                updateExistingItem(oldItem, newItem);
            } else {
                if (newItem.hasImage()) {
                    DBItemDocumentationImage newImage = new DBItemDocumentationImage();
                    newImage.setSchedulerId(schedulerId);
                    newImage.setImage(newItem.image());
                    newImage.setMd5Hash(DigestUtils.md5Hex(newItem.image()));
                    connection.save(newImage);
                    newItem.setImageId(newImage.getId());
                }
                newItem.setCreated(Date.from(Instant.now()));
                newItem.setModified(newItem.getCreated());
                connection.save(newItem);
            }
        }

    }

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            try {
                Path hibernateConfigPath = Paths.get(args[0]);
                if (!Files.exists(hibernateConfigPath)) {
                    throw new FileNotFoundException(args[0]);
                }
                String schedulerId = args[1];
                Path docuPath = Paths.get(args[2]);
                factory = new SOSHibernateFactory(hibernateConfigPath);
                factory.setAutoCommit(false);
                factory.addClassMapping(DBLayer.getInventoryClassMapping());
                factory.build();
                connection = factory.openStatelessSession(InventorySosDocuImport.class.getName());
                connection.beginTransaction();
                List<DBItemDocumentation> alreadyExisting = getAlreadyExistingSosDocus(schedulerId);
                connection.commit();
                List<DBItemDocumentation> newItems = createNewSosDocuDBItems(schedulerId, docuPath);
                connection.beginTransaction();
                saveOrUpdate(schedulerId, alreadyExisting, newItems);
                connection.commit();
                System.out.println("... done");
            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace(System.err);
                System.exit(1);
            } finally {
                if (connection != null) {
                    connection.close();
                }
                if (factory != null) {
                    factory.close();
                }
            }
        } else {
            System.err.println("USAGE: java " + InventorySosDocuImport.class.getName() + " /path/to/reporting.hibernate.cfg.xml"
                    + " JobSchedulerID /path/to/jitl-jobs-folder");
            System.err.println();
            System.exit(1);
        }
        System.exit(0);
    }

}
