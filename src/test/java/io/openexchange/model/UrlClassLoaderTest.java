package io.openexchange.model;

import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.apache.commons.io.FileUtils.copyFileToDirectory;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UrlClassLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File generatedCode;

    @BeforeEach
    void setUp() throws IOException {
        temporaryFolder.create();

        URL modelPackage = this.getClass().getClassLoader().getResource("io/openexchange/model");
        assertNotNull("Should not be null", modelPackage);

        generatedCode = temporaryFolder.newFolder("generatedSources");
        copyFileToDirectory(new File(modelPackage.getFile() + "/Person.java"), generatedCode);
        copyFileToDirectory(new File(modelPackage.getFile() + "/House.java"), generatedCode);

        assertTrue("Should exist", new File(generatedCode + "/Person.java" ).exists());
        assertTrue("Should exist", new File(generatedCode + "/House.java" ).exists());
    }

    @Test
    void generateAndLoad() throws IOException {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    }

    @AfterEach
    void tearDown() {
        temporaryFolder.delete();
    }
}
