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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ExtendedDBObjectTest extends InstrumentationTestCase {

    ExtendedDBObject obj;

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
        obj = new ExtendedDBObject();
        assertEquals(ExtendedDBObject.class, obj.getClass());
        assertEquals("ExtendedDBObject", obj.getCollectionID());
        assertEquals(0, obj.getEstimatedData().size());
        assertEquals(0, obj.getFilteredEstimatedData().size());
        assertEquals(0, obj.getOriginalData().size());
        assertEquals(0, obj.getAddedKeys().size());
        assertEquals(0, obj.getAddedKeysAndValues().size());
        assertEquals(0, obj.getUpdatedKeys().size());
        assertEquals(0, obj.getUpdatedKeysAndValues().size());
        assertEquals(0, obj.getRemovedKeys().size());
        assertEquals(0, obj.getRemovedKeysAndValues().size());
        assertTrue(obj.isNew());
        assertFalse(obj.isDirty());
        assertNull(obj.getID());
        assertNull(obj.getCreated());
        assertNull(obj.getCreatedBy());
        assertNull(obj.getUpdated());
        assertNull(obj.getUpdatedBy());
        assertNull(obj.getGeoPoint());
        assertEquals(0.0, obj.getDistance());
    }

    @Test
    public void test__constructor__collectionID() throws Exception {
        obj = new ExtendedDBObject("items");
        assertEquals(ExtendedDBObject.class, obj.getClass());
        assertEquals("items", obj.getCollectionID());
        assertEquals(0, obj.getEstimatedData().size());
        assertEquals(0, obj.getFilteredEstimatedData().size());
        assertEquals(0, obj.getOriginalData().size());
        assertEquals(0, obj.getAddedKeys().size());
        assertEquals(0, obj.getAddedKeysAndValues().size());
        assertEquals(0, obj.getUpdatedKeys().size());
        assertEquals(0, obj.getUpdatedKeysAndValues().size());
        assertEquals(0, obj.getRemovedKeys().size());
        assertEquals(0, obj.getRemovedKeysAndValues().size());
        assertTrue(obj.isNew());
        assertFalse(obj.isDirty());
        assertNull(obj.getID());
        assertNull(obj.getCreated());
        assertNull(obj.getCreatedBy());
        assertNull(obj.getUpdated());
        assertNull(obj.getUpdatedBy());
        assertNull(obj.getGeoPoint());
        assertEquals(0.0, obj.getDistance());
    }

    @Test
    public void test__constructor__collectionID__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ExtendedDBObject.Field.ID.getKey(), "ITM_01_001");
                put(ExtendedDBObject.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedDBObject.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedDBObject.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedDBObject.Field.UPDATED_BY.getKey(), "barman");
                put(ExtendedDBObject.Field.GEO_POINT.getKey(), Arrays.asList(23.456789, 12.345678));
                put(ExtendedDBObject.Field.DISTANCE.getKey(), 34.567890);
                put("type",  "T-Shirt");
                put("size",  "M");
                put("color", "Orange");
            }
        };
        obj = new ExtendedDBObject("items", map);
        assertEquals(ExtendedDBObject.class, obj.getClass());
        assertEquals("items", obj.getCollectionID());
        assertEquals(10, obj.getEstimatedData().size());
        assertEquals(10, obj.getFilteredEstimatedData().size());
        assertEquals(10, obj.getOriginalData().size());
        assertEquals(0, obj.getAddedKeys().size());
        assertEquals(0, obj.getAddedKeysAndValues().size());
        assertEquals(0, obj.getUpdatedKeys().size());
        assertEquals(0, obj.getUpdatedKeysAndValues().size());
        assertEquals(0, obj.getRemovedKeys().size());
        assertEquals(0, obj.getRemovedKeysAndValues().size());
        assertTrue(obj.isNew());
        assertFalse(obj.isDirty());
        assertEquals("ITM_01_001", obj.getID());
        assertEquals(1436951840123L / 1000, obj.getCreated().getTime());
        assertEquals("fooman", obj.getCreatedBy());
        assertEquals(1436951850987L / 1000, obj.getUpdated().getTime());
        assertEquals("barman", obj.getUpdatedBy());
        assertEquals(12.345678, obj.getGeoPoint().getLatitude());
        assertEquals(23.456789, obj.getGeoPoint().getLongitude());
        assertEquals(34.567890, obj.getDistance());
        assertEquals("T-Shirt", obj.get("type"));
        assertEquals("M", obj.get("size"));
        assertEquals("Orange", obj.get("color"));
    }

    @Test
    public void test__constructor__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ExtendedDBObject.Field.ID.getKey(), "ITM_01_001");
                put(ExtendedDBObject.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedDBObject.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedDBObject.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedDBObject.Field.UPDATED_BY.getKey(), "barman");
                put(ExtendedDBObject.Field.GEO_POINT.getKey(), Arrays.asList(23.456789, 12.345678));
                put(ExtendedDBObject.Field.DISTANCE.getKey(), 34.567890);
                put("type",  "T-Shirt");
                put("size",  "M");
                put("color", "Orange");
            }
        };
        obj = new ExtendedDBObject(map);
        assertEquals(ExtendedDBObject.class, obj.getClass());
        assertEquals("ExtendedDBObject", obj.getCollectionID());
        assertEquals(10, obj.getEstimatedData().size());
        assertEquals(10, obj.getFilteredEstimatedData().size());
        assertEquals(10, obj.getOriginalData().size());
        assertEquals(0, obj.getAddedKeys().size());
        assertEquals(0, obj.getAddedKeysAndValues().size());
        assertEquals(0, obj.getUpdatedKeys().size());
        assertEquals(0, obj.getUpdatedKeysAndValues().size());
        assertEquals(0, obj.getRemovedKeys().size());
        assertEquals(0, obj.getRemovedKeysAndValues().size());
        assertTrue(obj.isNew());
        assertFalse(obj.isDirty());
        assertEquals("ITM_01_001", obj.getID());
        assertEquals(1436951840123L / 1000, obj.getCreated().getTime());
        assertEquals("fooman", obj.getCreatedBy());
        assertEquals(1436951850987L / 1000, obj.getUpdated().getTime());
        assertEquals(12.345678, obj.getGeoPoint().getLatitude());
        assertEquals(23.456789, obj.getGeoPoint().getLongitude());
        assertEquals(34.567890, obj.getDistance());
        assertEquals("T-Shirt", obj.get("type"));
        assertEquals("M", obj.get("size"));
        assertEquals("Orange", obj.get("color"));
    }

    @Test
    public void test__query() throws Exception {
        ABQuery query = ExtendedDBObject.query();
        assertEquals("ExtendedDBObject", query.getCollectionID());
        assertEquals("/ExtendedDBObject/-", query.toString());
    }

    /*@Test
    public void testGetNickname() throws Exception {

    }

    @Test
    public void testSetNickname() throws Exception {

    }

    @Test
    public void testGetGender() throws Exception {

    }

    @Test
    public void testSetGender() throws Exception {

    }

    @Test
    public void testGetAge() throws Exception {

    }

    @Test
    public void testSetAge() throws Exception {

    }*/
}