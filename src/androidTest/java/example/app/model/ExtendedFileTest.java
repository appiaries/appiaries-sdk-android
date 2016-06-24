package example.app.model;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;

import com.appiaries.baas.sdk.ABQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ExtendedFileTest extends InstrumentationTestCase {

    ExtendedFile file;

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test__constructor__default() throws Exception {
        file = new ExtendedFile();
        assertEquals(ExtendedFile.class, file.getClass());
        assertEquals("ExtendedFile", file.getCollectionID());
        assertEquals(0, file.getEstimatedData().size());
        assertEquals(0, file.getFilteredEstimatedData().size());
        assertEquals(0, file.getOriginalData().size());
        assertEquals(0, file.getAddedKeys().size());
        assertEquals(0, file.getAddedKeysAndValues().size());
        assertEquals(0, file.getUpdatedKeys().size());
        assertEquals(0, file.getUpdatedKeysAndValues().size());
        assertEquals(0, file.getRemovedKeys().size());
        assertEquals(0, file.getRemovedKeysAndValues().size());
        assertTrue(file.isNew());
        assertFalse(file.isDirty());
        assertNull(file.getID());
        assertNull(file.getCreated());
        assertNull(file.getCreatedBy());
        assertNull(file.getUpdated());
        assertNull(file.getUpdatedBy());
        assertNull(file.getUrl());
        assertNull(file.getName());
        assertNull(file.getContentType());
        assertEquals(-1, file.getLength());
        assertNull(file.getTags());
        assertNull(file.getData());
    }

    @Test
    public void test__constructor__collectionID() throws Exception {
        file = new ExtendedFile("item_images");
        assertEquals(ExtendedFile.class, file.getClass());
        assertEquals("item_images", file.getCollectionID());
        assertEquals(0, file.getEstimatedData().size());
        assertEquals(0, file.getFilteredEstimatedData().size());
        assertEquals(0, file.getOriginalData().size());
        assertEquals(0, file.getAddedKeys().size());
        assertEquals(0, file.getAddedKeysAndValues().size());
        assertEquals(0, file.getUpdatedKeys().size());
        assertEquals(0, file.getUpdatedKeysAndValues().size());
        assertEquals(0, file.getRemovedKeys().size());
        assertEquals(0, file.getRemovedKeysAndValues().size());
        assertTrue(file.isNew());
        assertFalse(file.isDirty());
        assertNull(file.getID());
        assertNull(file.getCreated());
        assertNull(file.getCreatedBy());
        assertNull(file.getUpdated());
        assertNull(file.getUpdatedBy());
        assertNull(file.getUrl());
        assertNull(file.getName());
        assertNull(file.getContentType());
        assertEquals(-1, file.getLength());
        assertNull(file.getTags());
        assertNull(file.getData());
    }

    @Test
    public void test__constructor__collectionID__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ExtendedFile.Field.ID.getKey(), "IMG_01_001");
                put(ExtendedFile.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedFile.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedFile.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedFile.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedFile.Field.URL.getKey(), "https://example.com/path/to/file/bin");
                put(ExtendedFile.Field.NAME.getKey(), "t-shirt.png");
                put(ExtendedFile.Field.CONTENT_TYPE.getKey(), "image/png");
                put(ExtendedFile.Field.LENGTH.getKey(), 1024);
                put(ExtendedFile.Field.TAGS.getKey(), new HashSet<String>(){{
                    add("image");
                    add("thumbnail");
                }});
                put(ExtendedFile.Field.DATA.getKey(), new byte[1024]);
            }
        };
        file = new ExtendedFile("item_images", map);
        assertEquals(ExtendedFile.class, file.getClass());
        assertEquals("item_images", file.getCollectionID());
        assertEquals(11, file.getEstimatedData().size());
        assertEquals(11, file.getFilteredEstimatedData().size());
        assertEquals(11, file.getOriginalData().size());
        assertEquals(0, file.getAddedKeys().size());
        assertEquals(0, file.getAddedKeysAndValues().size());
        assertEquals(0, file.getUpdatedKeys().size());
        assertEquals(0, file.getUpdatedKeysAndValues().size());
        assertEquals(0, file.getRemovedKeys().size());
        assertEquals(0, file.getRemovedKeysAndValues().size());
        assertTrue(file.isNew());
        assertFalse(file.isDirty());
        assertEquals("IMG_01_001", file.getID());
        assertEquals(1436951840123L / 1000, file.getCreated().getTime());
        assertEquals("fooman", file.getCreatedBy());
        assertEquals(1436951850987L / 1000, file.getUpdated().getTime());
        assertEquals("barman", file.getUpdatedBy());
        assertEquals("https://example.com/path/to/file/bin", file.getUrl());
        assertEquals("t-shirt.png", file.getName());
        assertEquals("image/png", file.getContentType());
        assertEquals(1024, file.getLength());
        assertEquals(2, file.getTags().size());
        assertEquals(1024, file.getData().length);
    }

    @Test
    public void test__constructor__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ExtendedFile.Field.ID.getKey(), "IMG_01_001");
                put(ExtendedFile.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedFile.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedFile.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedFile.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedFile.Field.URL.getKey(), "https://example.com/path/to/file/bin");
                put(ExtendedFile.Field.NAME.getKey(), "t-shirt.png");
                put(ExtendedFile.Field.CONTENT_TYPE.getKey(), "image/png");
                put(ExtendedFile.Field.LENGTH.getKey(), 1024);
                put(ExtendedFile.Field.TAGS.getKey(), new HashSet<String>(){{
                    add("image");
                    add("thumbnail");
                }});
                put(ExtendedFile.Field.DATA.getKey(), new byte[1024]);
            }
        };
        file = new ExtendedFile(map);
        assertEquals(ExtendedFile.class, file.getClass());
        assertEquals("ExtendedFile", file.getCollectionID());
        assertEquals(11, file.getEstimatedData().size());
        assertEquals(11, file.getFilteredEstimatedData().size());
        assertEquals(11, file.getOriginalData().size());
        assertEquals(0, file.getAddedKeys().size());
        assertEquals(0, file.getAddedKeysAndValues().size());
        assertEquals(0, file.getUpdatedKeys().size());
        assertEquals(0, file.getUpdatedKeysAndValues().size());
        assertEquals(0, file.getRemovedKeys().size());
        assertEquals(0, file.getRemovedKeysAndValues().size());
        assertTrue(file.isNew());
        assertFalse(file.isDirty());
        assertEquals("IMG_01_001", file.getID());
        assertEquals(1436951840123L / 1000, file.getCreated().getTime());
        assertEquals("fooman", file.getCreatedBy());
        assertEquals(1436951850987L / 1000, file.getUpdated().getTime());
        assertEquals("barman", file.getUpdatedBy());
        assertEquals("https://example.com/path/to/file/bin", file.getUrl());
        assertEquals("t-shirt.png", file.getName());
        assertEquals("image/png", file.getContentType());
        assertEquals(1024, file.getLength());
        assertEquals(2, file.getTags().size());
        assertEquals(1024, file.getData().length);
    }

    @Test
    public void test__query() throws Exception {
        ABQuery query = ExtendedFile.query();
        assertEquals("ExtendedFile", query.getCollectionID());
        assertEquals("/ExtendedFile/-", query.toString());
    }

}