package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;
import android.text.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ABDBObjectTest extends InstrumentationTestCase {

    ABDBObject obj;

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }
/*
    void sandbox() {

        device = new ABDBObject("user_profiles");
        device.setID("obj1");
        device.put("name", "Melissa");
        device.put("gender", "female");
        device.put("age", 8);
        device.put("message", "Hello, Appiaries!");

        // 作成 (非同期)
        device.save();

        // 作成 (非同期) (コールバックあり)
        device.save(new ResultCallback<ABDBObject>() {
            @Override
            public void done(ABResult<ABDBObject> result, ABException e) {
                if (e == null) {
                    ABDBObject created = result.getData();
                    ABLogger.d(TAG, String.format("SUCCESS [status=%d, response=%s", result.getCode(), created));
                } else {
                    ABLogger.e(TAG, String.format("ERROR [%s]", e.getMessage()));
                }
            }
        });

        // 作成 (同期)
        try {
            ABResult<ABDBObject> result = device.saveSynchronously();
            ABDBObject created = result.getData();
            Log.d(TAG, String.format("SUCCESS [status=%d, response=%s", result.getCode(), created));
        } catch (ABException e) {
            Log.e(TAG, String.format("ERROR [%s]", e.getMessage()));
        }

        device = new ABDBObject("option_test");
        device.save(new ResultCallback<ABDBObject>() {
            @Override
            public void done(ABResult<ABDBObject> result, ABException e) {
                if (result.getCode() == 200) {
                    Log.d("", "success");
                }
            }
        }, EnumSet.of(AB.DBObjectSaveOption.NONE));

    }
*/
    @Before
    public void setUp() throws Exception {
        AB.Config.setDatastoreID("ds");
        AB.Config.setApplicationID("app");
        AB.Config.setApplicationToken("tokentokentokentokentokentokentokentokentokentokentoken");
        AB.activate(getApplicationContext());

        obj = null;
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test__constructor__default() throws Exception {
        obj = new ABDBObject(); //NOTE: protected
        assertEquals(ABDBObject.class, obj.getClass());
        assertEquals(ABDBObject.class.getName(), obj.getCollectionID());
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
        obj = new ABDBObject("items");
        assertEquals(ABDBObject.class, obj.getClass());
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
                put(ABDBObject.Field.ID.getKey(), "ITM_01_001");
                put(ABDBObject.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABDBObject.Field.CREATED_BY.getKey(), "fooman");
                put(ABDBObject.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABDBObject.Field.UPDATED_BY.getKey(), "barman");
                put(ABDBObject.Field.GEO_POINT.getKey(), Arrays.asList(23.456789, 12.345678));
                put(ABDBObject.Field.DISTANCE.getKey(), 34.567890);
                put("type",  "T-Shirt");
                put("size",  "M");
                put("color", "Orange");
            }
        };
        obj = new ABDBObject("items", map);
        assertEquals(ABDBObject.class, obj.getClass());
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
                put(ABDBObject.Field.ID.getKey(), "ITM_01_001");
                put(ABDBObject.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABDBObject.Field.CREATED_BY.getKey(), "fooman");
                put(ABDBObject.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABDBObject.Field.UPDATED_BY.getKey(), "barman");
                put(ABDBObject.Field.GEO_POINT.getKey(), Arrays.asList(23.456789, 12.345678));
                put(ABDBObject.Field.DISTANCE.getKey(), 34.567890);
                put("type",  "T-Shirt");
                put("size",  "M");
                put("color", "Orange");
            }
        };
        obj = new ABDBObject(map);
        assertEquals(ABDBObject.class, obj.getClass());
        assertEquals(ABDBObject.class.getName(), obj.getCollectionID());
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
/*
    @Test
    public void test__saveSynchronously() throws Exception {
        try {
            device = new ABDBObject("items");
            Assert.assertNotNull(device.getEstimatedData());
            Assert.assertNotNull(device);
            device.setID("AAAA");
            ABResult<ABDBObject> result = device.saveSynchronously();
            ABDBObject created = result.getData();
            Assert.assertNotNull(result);
            Assert.assertNotNull(created);
        } catch (ABException e) {
            Assert.fail();
        }
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
    public void test__deleteSynchronously() throws Exception {

    }

    @Test
    public void test__deleteSynchronously1() throws Exception {

    }

    @Test
    public void test__delete() throws Exception {

    }
*/
}