package io.openexchange.model;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class UrlClassLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeEach
    void setUp() throws IOException {
        temporaryFolder.create();

        URL modelPackage = this.getClass().getClassLoader().getResource("io/openexchange/model");
        assertNotNull("Should not be null", modelPackage);

        File generatedCode = temporaryFolder.newFolder("io", "openexchange", "model");
        FileUtils.copyDirectory(new File(modelPackage.getFile()), generatedCode);

        List<File> sources = asList(new File(generatedCode + "/Person.java"), new File(generatedCode + "/House.java"));
        sources.forEach(file -> assertTrue("Should exist", file.exists()));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjectsFromFiles(sources)).call();
        }

        List<File> generated = asList(new File(generatedCode + "/Person.class"), new File(generatedCode + "/House.class"));
        generated.forEach(file -> assertTrue("Should exist", file.exists()));
    }

    @Test
    void loadSingleClassloader() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ClassLoader classLoader = new URLClassLoader(new URL[]{temporaryFolder.getRoot().toURI().toURL()});

        Class<?> personClazz = classLoader.loadClass("io.openexchange.model.Person");
        Object person = personClazz.getConstructor(String.class, String.class).newInstance("John", "Smith");

        Class<?> houseClazz = classLoader.loadClass("io.openexchange.model.House");
        Object house = houseClazz.getConstructor(String.class, personClazz).newInstance("Fort Worth", person);

        assertEquals("Should be equals",
                "Fort Worth", houseClazz.getMethod("getAddress").invoke(house));
        assertEquals("Should be equals",
                person, houseClazz.getMethod("getOwner").invoke(house));
    }

    @Test
    void loadMultipleClassloaders() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ClassLoader classLoaderPerson = new URLClassLoader(new URL[]{temporaryFolder.getRoot().toURI().toURL()});

        Class<?> personClazz = classLoaderPerson.loadClass("io.openexchange.model.Person");
        Object person = personClazz.getConstructor(String.class, String.class).newInstance("John", "Smith");

        ClassLoader classLoaderHouse = new URLClassLoader(new URL[]{temporaryFolder.getRoot().toURI().toURL()});

        Class<?> houseClazz = classLoaderHouse.loadClass("io.openexchange.model.House");
        Assertions.assertThrows(NoSuchMethodException.class, () -> {
            houseClazz.getConstructor(String.class, personClazz).newInstance("Fort Worth", person);
        });
    }

    @AfterEach
    void tearDown() {
        temporaryFolder.delete();
    }
}
