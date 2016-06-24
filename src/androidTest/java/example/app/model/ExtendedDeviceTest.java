package example.app.model;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;

import com.appiaries.baas.sdk.AB;
import com.appiaries.baas.sdk.ABQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ExtendedDeviceTest extends InstrumentationTestCase {

    ExtendedDevice device;
    
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
        device = new ExtendedDevice(); //NOTE: protected
        assertEquals(ExtendedDevice.class, device.getClass());
        assertEquals(ExtendedDevice.class.getName(), device.getCollectionID());
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
        device = new ExtendedDevice("MyDevice");
        assertEquals(ExtendedDevice.class, device.getClass());
        assertEquals("MyDevice", device.getCollectionID());
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
                put(ExtendedDevice.Field.ID.getKey(), "Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0");
                put(ExtendedDevice.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedDevice.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedDevice.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedDevice.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedDevice.Field.REGISTRATION_ID.getKey(), "Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0");
                put(ExtendedDevice.Field.TYPE.getKey(), "gcm");
                put(ExtendedDevice.Field.ENVIRONMENT.getKey(), 1);
                put(ExtendedDevice.Field.RESERVED_PUSH_IDS.getKey(), Sets.newSet(1, 2, 3));
                put("attr1", "custom attribute 1"); //String
                put("attr2", 2); //Number
                put("attr3", true); //Boolean
                put("attr4", new HashMap<String, Object>(){{ put("attr3.1", "nested custom attribute 4-1"); }}); //Map
                put("attr5", new ArrayList<String>(){{ add("5-1"); }}); //List
            }
        };
        device = new ExtendedDevice("MyDevice", map);
        assertEquals(ExtendedDevice.class, device.getClass());
        assertEquals("MyDevice", device.getCollectionID());
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
                put(ExtendedDevice.Field.ID.getKey(), "Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0");
                put(ExtendedDevice.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedDevice.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedDevice.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedDevice.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedDevice.Field.REGISTRATION_ID.getKey(), "Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0");
                put(ExtendedDevice.Field.TYPE.getKey(), "gcm");
                put(ExtendedDevice.Field.ENVIRONMENT.getKey(),  1);
                put(ExtendedDevice.Field.RESERVED_PUSH_IDS.getKey(), Sets.newSet(1, 2, 3));
                put("attr1", "custom attribute 1"); //String
                put("attr2", 2); //Number
                put("attr3", true); //Boolean
                put("attr4", new HashMap<String, Object>(){{ put("attr3.1", "nested custom attribute 4-1"); }}); //Map
                put("attr5", new ArrayList<String>(){{ add("5-1"); }}); //List
            }
        };
        device = new ExtendedDevice(map);
        assertEquals(ExtendedDevice.class, device.getClass());
        assertEquals(ExtendedDevice.class.getName(), device.getCollectionID());
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

}