/*
 * Copyright 2016-2018 Talsma ICT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.talsmasoftware.umldoclet.rendering.plantuml;

import net.sourceforge.plantuml.FileFormat;
import nl.talsmasoftware.umldoclet.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Sjoerd Talsma
 */
public class PlantumlImageTest {
    private static String uml = "@startuml\nversion\n@enduml";

    private Configuration config;
    private File tempdir;

    @Before
    public void createTempdir() throws IOException {
        config = mock(Configuration.class);
        tempdir = File.createTempFile("plantuml-image-", ".tmp");
        assertThat("Created temporary directory", tempdir.delete() && tempdir.mkdirs(), is(true));
    }

    @After
    public void cleanupTempdir() {
        Stream.of(tempdir.listFiles()).forEach(f -> assertThat("Delete " + f, f.delete(), is(true)));
        assertThat("Delete " + tempdir, tempdir.delete(), is(true));
        verifyNoMoreInteractions(config);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_nameNull() {
        new PlantumlImage(config, null, FileFormat.PNG, ByteArrayOutputStream::new) {
        };
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_fileFormatNull() {
        new PlantumlImage(config, new File("diagram.png"), null, ByteArrayOutputStream::new) {
        };
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_outputStreamSupplierNull() {
        new PlantumlImage(config, new File("diagram.png"), FileFormat.PNG, null) {
        };
    }

    @Test
    public void testFromFile_null() {
        assertThat(PlantumlImage.fromFile(config, null), is(Optional.empty()));
    }

    @Test
    public void testFromFile_unrecognizedFileFormat() {
        assertThat(PlantumlImage.fromFile(config, new File("diagram.doc")), is(Optional.empty()));
    }

    @Test
    public void testFromFile_PngFormat() {
        File png = new File("diagram.png");
        assertThat(PlantumlImage.fromFile(config, png), not(Optional.empty()));
        assertThat(PlantumlImage.fromFile(config, png), hasToString("Optional[diagram.png]"));
        assertThat(png.exists(), is(false));
    }

    @Test
    public void testName() {
        assertThat(PlantumlImage.fromFile(config, new File("diagram.png")).get().getName(), equalTo("diagram.png"));
        assertThat(PlantumlImage.fromFile(config, new File(tempdir, "diagram.png")).get().getName(),
                equalTo(new File(tempdir, "diagram.png").getPath()));
    }

    @Test
    public void testToString() {
        assertThat(PlantumlImage.fromFile(config, new File("diagram.png")), hasToString("Optional[diagram.png]"));
        assertThat(PlantumlImage.fromFile(config, new File(tempdir, "diagram.png")), hasToString("Optional[diagram.png]"));
    }

    @Test
    public void testRender_cannotCreateFile() throws IOException {
        File pngDir = new File(tempdir, "directory.png");
        assertThat(pngDir.mkdir(), is(true));
        try {
            PlantumlImage.fromFile(config, pngDir).get().renderPlantuml(uml);
            fail("Runtime exception expected");
        } catch (RuntimeException expected) {
            assertThat(expected, hasToString(stringContainsInOrder(asList("Could not create writer", "directory.png"))));
        } finally {
            assertThat(pngDir.delete(), is(true));
        }
    }

    @Test
    public void testRenderImage() throws IOException {
        File png = new File(tempdir, "diagram.png");
        PlantumlImage.fromFile(config, png).get().renderPlantuml(uml);
        assertThat(png.isFile(), is(true));
        assertThat(png.length(), is(greaterThan(0L)));
    }
}
