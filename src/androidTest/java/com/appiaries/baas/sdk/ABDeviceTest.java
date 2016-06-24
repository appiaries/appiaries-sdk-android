package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)
public class ABDeviceTest extends InstrumentationTestCase {

    ABDevice device;

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }

    @Before
    public void setUp() throws Exception {
        AB.Config.setDatastoreID("ds");
        AB.Config.setApplicationID("app");
        AB.Config.setApplicationToken("tokentokentokentokentokentokentokentokentokentokentoken");
        AB.activate(getApplicationContext());

        device = null;
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test__constructor__default() throws Exception {
        device = new ABDevice(); //NOTE: protected
        assertEquals(ABDevice.class, device.getClass());
        assertEquals(ABDevice.class.getName(), device.getCollectionID());
        assertEquals(0, device.getEstimatedData().size());
        assertEquals(0, device.getFilteredEstimatedData().size());
        assertEquals(0, device.getOriginalData().size());
        assertEquals(0, device.getAddedKeys().size());
        assertEquals(0, device.getAddedKeysAndValues().size());
        assertEquals(0, device.getUpdatedKeys().size());
        assertEquals(0, device.getUpdatedKeysAndValues().size());
        assertEquals(0, device.getRemovedKeys().size());
        assertEquals(0, device.getRemovedKeysAndValues().size());
        assertTrue(device.isNew());
        assertFalse(device.isDirty());
        assertNull(device.getID());
        assertNull(device.getCreated());
        assertNull(device.getCreatedBy());
        assertNull(device.getUpdated());
        assertNull(device.getUpdatedBy());
        assertNull(device.getRegistrationID());
        assertEquals(AB.Platform.UNKNOWN, device.getPlatform());
        assertNull(device.getType());
        assertEquals(-1, device.getEnvironment());
        assertNull(device.getReservedPushIds());
        assertEquals(0, device.getAttributes().size());
    }

    @Test
    public void test__constructor__collectionID() throws Exception {
        device = new ABDevice("MyDevice");
        assertEquals(ABDevice.class, device.getClass());
        assertEquals("MyDevice", device.getCollectionID()); //XXX: ABDeviceはコレクションを持たないので常に com.appiaries.baas.sdk.ABDevice が返されるはずなんですが。。。
        assertEquals(0, device.getEstimatedData().size());
        assertEquals(0, device.getFilteredEstimatedData().size());
        assertEquals(0, device.getOriginalData().size());
        assertEquals(0, device.getAddedKeys().size());
        assertEquals(0, device.getAddedKeysAndValues().size());
        assertEquals(0, device.getUpdatedKeys().size());
        assertEquals(0, device.getUpdatedKeysAndValues().size());
        assertEquals(0, device.getRemovedKeys().size());
        assertEquals(0, device.getRemovedKeysAndValues().size());
        assertTrue(device.isNew());
        assertFalse(device.isDirty());
        assertNull(device.getID());
        assertNull(device.getCreated());
        assertNull(device.getCreatedBy());
        assertNull(device.getUpdated());
        assertNull(device.getUpdatedBy());
        assertNull(device.getRegistrationID());
        assertEquals(AB.Platform.UNKNOWN, device.getPlatform());
        assertNull(device.getType());
        assertEquals(-1, device.getEnvironment());
        assertNull(device.getReservedPushIds());
        assertEquals(0, device.getAttributes().size());
    }

    @Test
    public void test__constructor__collectionID__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ABDevice.Field.ID.getKey(), "Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0");
                put(ABDevice.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABDevice.Field.CREATED_BY.getKey(), "fooman");
                put(ABDevice.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABDevice.Field.UPDATED_BY.getKey(), "barman");

                put(ABDevice.Field.REGISTRATION_ID.getKey(), "Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0");
                put(ABDevice.Field.TYPE.getKey(), "gcm");
                put(ABDevice.Field.ENVIRONMENT.getKey(), 1);
                put(ABDevice.Field.RESERVED_PUSH_IDS.getKey(), Sets.newSet(1, 2, 3));
                put("attr1", "custom attribute 1"); //String
                put("attr2", 2); //Number
                put("attr3", true); //Boolean
                put("attr4", new HashMap<String, Object>(){{ put("attr3.1", "nested custom attribute 4-1"); }}); //Map
                put("attr5", new ArrayList<String>(){{ add("5-1"); }}); //List
            }
        };
        device = new ABDevice("MyDevice", map);
        assertEquals(ABDevice.class, device.getClass());
        assertEquals("MyDevice", device.getCollectionID()); //XXX: ABDeviceはコレクションを持たないので常に com.appiaries.baas.sdk.ABDevice が返されるはずなんですが。。。
        assertEquals(14, device.getEstimatedData().size());
        assertEquals(14, device.getFilteredEstimatedData().size());
        assertEquals(14, device.getOriginalData().size());
        assertEquals(0, device.getAddedKeys().size());
        assertEquals(0, device.getAddedKeysAndValues().size());
        assertEquals(0, device.getUpdatedKeys().size());
        assertEquals(0, device.getUpdatedKeysAndValues().size());
        assertEquals(0, device.getRemovedKeys().size());
        assertEquals(0, device.getRemovedKeysAndValues().size());
        assertTrue(device.isNew());
        assertFalse(device.isDirty());
        assertEquals("Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0", device.getID());
        assertEquals(1436951840123L / 1000, device.getCreated().getTime());
        assertEquals("fooman", device.getCreatedBy());
        assertEquals(1436951850987L / 1000, device.getUpdated().getTime());
        assertEquals("barman", device.getUpdatedBy());
        assertEquals("Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0", device.getRegistrationID());
        assertEquals(AB.Platform.ANDROID, device.getPlatform());
        assertEquals("gcm", device.getType());
        assertEquals(1, device.getEnvironment());
        assertEquals(3, device.getReservedPushIds().size());
        assertEquals(5, device.getAttributes().size());
        assertEquals("custom attribute 1", device.get("attr1"));
        assertEquals(2, device.get("attr2"));
        assertEquals(true, device.get("attr3"));
        assertEquals(1, ((HashMap<String, Object>) device.get("attr4")).size());
        assertEquals(1, ((ArrayList<String>) device.get("attr5")).size());
    }

    @Test
    public void test__constructor__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ABDevice.Field.ID.getKey(), "Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0");
                put(ABDevice.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABDevice.Field.CREATED_BY.getKey(), "fooman");
                put(ABDevice.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABDevice.Field.UPDATED_BY.getKey(), "barman");

                put(ABDevice.Field.REGISTRATION_ID.getKey(), "Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0");
                put(ABDevice.Field.TYPE.getKey(), "gcm");
                put(ABDevice.Field.ENVIRONMENT.getKey(),  1);
                put(ABDevice.Field.RESERVED_PUSH_IDS.getKey(), Sets.newSet(1, 2, 3));
                put("attr1", "custom attribute 1"); //String
                put("attr2", 2); //Number
                put("attr3", true); //Boolean
                put("attr4", new HashMap<String, Object>(){{ put("attr3.1", "nested custom attribute 4-1"); }}); //Map
                put("attr5", new ArrayList<String>(){{ add("5-1"); }}); //List
            }
        };
        device = new ABDevice(map);
        assertEquals(ABDevice.class, device.getClass());
        assertEquals(ABDevice.class.getName(), device.getCollectionID()); //NOTE: ABDeviceはコレクションを持たないので常に com.appiaries.baas.sdk.ABDevice が返される。
        assertEquals(14, device.getEstimatedData().size());
        assertEquals(14, device.getFilteredEstimatedData().size());
        assertEquals(14, device.getOriginalData().size());
        assertEquals(0, device.getAddedKeys().size());
        assertEquals(0, device.getAddedKeysAndValues().size());
        assertEquals(0, device.getUpdatedKeys().size());
        assertEquals(0, device.getUpdatedKeysAndValues().size());
        assertEquals(0, device.getRemovedKeys().size());
        assertEquals(0, device.getRemovedKeysAndValues().size());
        assertTrue(device.isNew());
        assertFalse(device.isDirty());
        assertEquals("Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0", device.getID());
        assertEquals(1436951840123L / 1000, device.getCreated().getTime());
        assertEquals("fooman", device.getCreatedBy());
        assertEquals(1436951850987L / 1000, device.getUpdated().getTime());
        assertEquals("barman", device.getUpdatedBy());
        assertEquals("Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0", device.getRegistrationID());
        assertEquals(AB.Platform.ANDROID, device.getPlatform());
        assertEquals("gcm", device.getType());
        assertEquals(1, device.getEnvironment());
        assertEquals(3, device.getReservedPushIds().size());
        assertEquals(5, device.getAttributes().size());
        assertEquals("custom attribute 1", device.get("attr1"));
        assertEquals(2, device.get("attr2"));
        assertEquals(true, device.get("attr3"));
        assertEquals(1, ((HashMap<String, Object>) device.get("attr4")).size());
        assertEquals(1, ((ArrayList<String>) device.get("attr5")).size());
    }
/*
    @Test
    public void test__registerSynchronously() throws Exception {

    }

    @Test
    public void test__registerSynchronously1() throws Exception {

    }

    @Test
    public void test__register() throws Exception {

    }

    @Test
    public void test__register1() throws Exception {

    }

    @Test
    public void test__register2() throws Exception {

    }

    @Test
    public void test__register3() throws Exception {

    }

    @Test
    public void test__unregisterSynchronously() throws Exception {

    }

    @Test
    public void test__unregisterSynchronously1() throws Exception {

    }

    @Test
    public void test__unregister() throws Exception {

    }

    @Test
    public void test__unregister1() throws Exception {

    }

    @Test
    public void test__unregister2() throws Exception {

    }

    @Test
    public void test__unregister3() throws Exception {

    }

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
    public void test__query() throws Exception {

    }

    @Test
    public void test__getRegistrationID() throws Exception {

    }

    @Test
    public void test__setRegistrationID() throws Exception {

    }

    @Test
    public void test__getPlatform() throws Exception {

    }

    @Test
    public void test__setPlatform() throws Exception {

    }

    @Test
    public void test__getType() throws Exception {

    }

    @Test
    public void test__setType() throws Exception {

    }

    @Test
    public void test__getEnv() throws Exception {

    }

    @Test
    public void test__setEnv() throws Exception {

    }

    @Test
    public void test__getAttributes() throws Exception {

    }

    @Test
    public void test__setAttributes() throws Exception {

    }

    @Test
    public void test__getReservedPushIds() throws Exception {

    }

    @Test
    public void test__setReservedPushIds() throws Exception {

    }
*/
}