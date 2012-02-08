package org.ozsoft.documentstore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

public class DocumentStoreTest {

    private static final File DATA_DIR = new File("src/test/resources/data");

    private static final Logger LOG = Logger.getLogger(DocumentStoreTest.class);

    @Test
    public void schemas() throws Exception {
        LOG.info("Test 'schemas' started");

        // Verify no schemas are available.
        DocumentStore docs = new DocumentStore();
        Assert.assertTrue(docs.listSchemas().isEmpty());

        // Get content of first schema.
        String namespace1 = "http://www.example.org/XMLSchema/Generic/v1.0";
        File file = new File(DATA_DIR, "schemas/Generic_v1.0.xsd");
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        String content = IOUtils.toString(is);
        is.close();

        // Store first schema.
        is = new BufferedInputStream(new FileInputStream(file));
        docs.storeSchema(namespace1, is);
        is.close();
        Assert.assertEquals(1, docs.listSchemas().size());

        // Retrieve first schema.
        is = docs.retrieveSchema(namespace1);
        Assert.assertEquals(content, IOUtils.toString(is));
        is.close();

        // Get content of second schema.
        String namespace2 = "http://www.example.org/XMLSchema/Address/v1.0";
        file = new File(DATA_DIR, "schemas/Address_v1.0.xsd");
        is = new BufferedInputStream(new FileInputStream(file));
        content = IOUtils.toString(is);
        is.close();

        // Store second schema.
        is = new BufferedInputStream(new FileInputStream(file));
        docs.storeSchema(namespace2, is);
        is.close();
        Assert.assertEquals(2, docs.listSchemas().size());

        // Retrieve second schema.
        is = docs.retrieveSchema(namespace2);
        Assert.assertEquals(content, IOUtils.toString(is));
        is.close();

        // Delete second schema.
        docs.deleteSchema(namespace2);
        Assert.assertEquals(1, docs.listSchemas().size());

        // Delete first schema.
        docs.deleteSchema(namespace1);
        Assert.assertTrue(docs.listSchemas().isEmpty());

        LOG.info("Test 'schemas' finished");
    }

}
