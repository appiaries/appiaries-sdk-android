package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class ABFileTest extends InstrumentationTestCase {

    ABFile file;
    Object val;

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }

    @Before
    public void setUp() throws Exception {
        AB.Config.setDatastoreID("ds");
        AB.Config.setApplicationID("app");
        AB.Config.setApplicationToken("tokentokentokentokentokentokentokentokentokentokentoken");
        AB.activate(getApplicationContext());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test__constructor__default() throws Exception {
        file = new ABFile(); //NOTE: protected
        assertEquals(ABFile.class, file.getClass());
        assertEquals(ABFile.class.getName(), file.getCollectionID());
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
        file = new ABFile("item_images");
        assertEquals(ABFile.class, file.getClass());
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
                put(ABFile.Field.ID.getKey(), "IMG_01_001");
                put(ABFile.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABFile.Field.CREATED_BY.getKey(), "fooman");
                put(ABFile.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABFile.Field.UPDATED_BY.getKey(), "barman");

                put(ABFile.Field.URL.getKey(), "https://example.com/path/to/file/bin");
                put(ABFile.Field.NAME.getKey(), "t-shirt.png");
                put(ABFile.Field.CONTENT_TYPE.getKey(), "image/png");
                put(ABFile.Field.LENGTH.getKey(), 1024);
                put(ABFile.Field.TAGS.getKey(), new HashSet<String>(){{
                    add("image");
                    add("thumbnail");
                }});
                put(ABFile.Field.DATA.getKey(), new byte[1024]);
            }
        };
        file = new ABFile("item_images", map);
        assertEquals(ABFile.class, file.getClass());
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
                put(ABFile.Field.ID.getKey(), "IMG_01_001");
                put(ABFile.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABFile.Field.CREATED_BY.getKey(), "fooman");
                put(ABFile.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABFile.Field.UPDATED_BY.getKey(), "barman");

                put(ABFile.Field.URL.getKey(), "https://example.com/path/to/file/bin");
                put(ABFile.Field.NAME.getKey(), "t-shirt.png");
                put(ABFile.Field.CONTENT_TYPE.getKey(), "image/png");
                put(ABFile.Field.LENGTH.getKey(), 1024);
                put(ABFile.Field.TAGS.getKey(), new HashSet<String>(){{
                    add("image");
                    add("thumbnail");
                }});
                put(ABFile.Field.DATA.getKey(), new byte[1024]);
            }
        };
        file = new ABFile(map);
        assertEquals(ABFile.class, file.getClass());
        assertEquals(ABFile.class.getName(), file.getCollectionID());
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
/*
    @Test
    public void test__saveSynchronously() throws Exception {

    }

    @Test
    public void test__saveSynchronously1() throws Exception {

    }

    @Test
    public void test__save() throws Exception {

    }

    @Test
    public void test__save1() throws Exception {

    }

    @Test
    public void test__save2() throws Exception {

    }

    @Test
    public void test__save3() throws Exception {

    }

    @Test
    public void test__save4() throws Exception {

    }

    @Test
    public void test__save5() throws Exception {

    }

    @Test
    public void test__deleteSynchronously() throws Exception {

    }

    @Test
    public void test__deleteSynchronously1() throws Exception {

    }

    @Test
    public void test__delete() throws Exception {

    }

    @Test
    public void test__delete1() throws Exception {

    }

    @Test
    public void test__delete2() throws Exception {

    }

    @Test
    public void test__delete3() throws Exception {

    }

    @Test
    public void test__refreshSynchronously() throws Exception {

    }

    @Test
    public void test__refreshSynchronously1() throws Exception {

    }

    @Test
    public void test__refresh() throws Exception {

    }

    @Test
    public void test__refresh1() throws Exception {

    }

    @Test
    public void test__refresh2() throws Exception {

    }

    @Test
    public void test__refresh3() throws Exception {

    }

    @Test
    public void test__query() throws Exception {

    }

    @Test
    public void test__getUrl() throws Exception {

    }

    @Test
    public void test__setUrl() throws Exception {

    }

    @Test
    public void test__getName() throws Exception {

    }

    @Test
    public void test__setName() throws Exception {

    }

    @Test
    public void test__getContentType() throws Exception {

    }

    @Test
    public void test__setContentType() throws Exception {

    }

    @Test
    public void test__getLength() throws Exception {

    }

    @Test
    public void test__setLength() throws Exception {

    }

    @Test
    public void test__getTags() throws Exception {

    }

    @Test
    public void test__setTags() throws Exception {

    }

    @Test
    public void test__getData() throws Exception {

    }

    @Test
    public void test__setData() throws Exception {

    }
*/
}