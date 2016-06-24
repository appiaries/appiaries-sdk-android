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
public class ExtendedPushMessageTest extends InstrumentationTestCase {

    ExtendedPushMessage message;
    
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
        message = new ExtendedPushMessage(); //NOTE: protected
        assertEquals(ExtendedPushMessage.class, message.getClass());
        assertEquals(ExtendedPushMessage.class.getName(), message.getCollectionID());
        assertEquals(0, message.getEstimatedData().size());
        assertEquals(0, message.getFilteredEstimatedData().size());
        assertEquals(0, message.getOriginalData().size());
        assertEquals(0, message.getAddedKeys().size());
        assertEquals(0, message.getAddedKeysAndValues().size());
        assertEquals(0, message.getUpdatedKeys().size());
        assertEquals(0, message.getUpdatedKeysAndValues().size());
        assertEquals(0, message.getRemovedKeys().size());
        assertEquals(0, message.getRemovedKeysAndValues().size());
        assertTrue(message.isNew());
        assertFalse(message.isDirty());
        assertNull(message.getID());
        assertNull(message.getCreated());
        assertNull(message.getCreatedBy());
        assertNull(message.getUpdated());
        assertNull(message.getUpdatedBy());
        long pushId = message.getPushId();
        assertEquals(0L, pushId);
        assertNull(message.getTitle());
        assertNull(message.getMessage());
        assertNull(message.getUrl());
        assertEquals(0L, message.getFrom());
        assertNull(message.getCollapseKey());
        assertEquals(0, message.getTimeToLive());
        assertFalse(message.isDelayWhileIdle());
        assertNull(message.getRestrictedPackageName());
        assertFalse(message.isDryRun());
        assertNull(message.getRegistrationIds());
    }

    @Test
    public void test__constructor__collectionID() throws Exception {
        message = new ExtendedPushMessage("MyDevice");
        assertEquals(ExtendedPushMessage.class, message.getClass());
        assertEquals("MyDevice", message.getCollectionID()); //XXX: ABDeviceはコレクションを持たないので常に com.appiaries.baas.sdk.ABDevice が返されるはずなんですが。。。
        assertEquals(0, message.getEstimatedData().size());
        assertEquals(0, message.getFilteredEstimatedData().size());
        assertEquals(0, message.getOriginalData().size());
        assertEquals(0, message.getAddedKeys().size());
        assertEquals(0, message.getAddedKeysAndValues().size());
        assertEquals(0, message.getUpdatedKeys().size());
        assertEquals(0, message.getUpdatedKeysAndValues().size());
        assertEquals(0, message.getRemovedKeys().size());
        assertEquals(0, message.getRemovedKeysAndValues().size());
        assertTrue(message.isNew());
        assertFalse(message.isDirty());
        assertNull(message.getID());
        assertNull(message.getCreated());
        assertNull(message.getCreatedBy());
        assertNull(message.getUpdated());
        assertNull(message.getUpdatedBy());
        long pushId = message.getPushId();
        assertEquals(0L, pushId);
        assertNull(message.getTitle());
        assertNull(message.getMessage());
        assertNull(message.getUrl());
        assertEquals(0L, message.getFrom());
        assertNull(message.getCollapseKey());
        assertEquals(0, message.getTimeToLive());
        assertFalse(message.isDelayWhileIdle());
        assertNull(message.getRestrictedPackageName());
        assertFalse(message.isDryRun());
        assertNull(message.getRegistrationIds());
    }

    @Test
    public void test__constructor__collectionID__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ExtendedPushMessage.Field.ID.getKey(), "1");
                put(ExtendedPushMessage.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedPushMessage.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedPushMessage.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedPushMessage.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedPushMessage.Field.PUSH_ID.getKey(), 1L);
                put(ExtendedPushMessage.Field.TITLE.getKey(), "お知らせ");
                put(ExtendedPushMessage.Field.MESSAGE.getKey(), "こんにちは！");
                put(ExtendedPushMessage.Field.URL.getKey(), "http://www.appiaries.com/jp/");
                put(ExtendedPushMessage.Field.FROM.getKey(), 1L);
                put(ExtendedPushMessage.Field.COLLAPSE_KEY.getKey(), "folding-key");
                put(ExtendedPushMessage.Field.TIME_TO_LIVE.getKey(), 100);
                put(ExtendedPushMessage.Field.DELAY_WHILE_IDLE.getKey(), true);
                put(ExtendedPushMessage.Field.RESTRICTED_PACKAGE_NAME.getKey(), "com.appiaries.sample");
                put(ExtendedPushMessage.Field.DRY_RUN.getKey(), true);
                put(ExtendedPushMessage.Field.REGISTRATION_IDS.getKey(), Arrays.asList("Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0", "33fb7c83f6181e800f3e762ea73ecbc33a9ee5e77cf11dd7c34fc368b43fda74"));
                put("attr1", "custom attribute 1");
                put("attr2", "custom attribute 2");
                put("attr3", "custom attribute 3");
            }
        };
        message = new ExtendedPushMessage("MyPushMessage", map);
        assertEquals(ExtendedPushMessage.class, message.getClass());
        assertEquals("MyPushMessage", message.getCollectionID()); //XXX: ExtendedPushMessageはコレクションを持たないので常に com.appiaries.baas.sdk.ExtendedPushMessage が返されるはずなんですが。。。
        assertEquals(19, message.getEstimatedData().size());
        assertEquals(19, message.getFilteredEstimatedData().size());
        assertEquals(19, message.getOriginalData().size());
        assertEquals(0, message.getAddedKeys().size());
        assertEquals(0, message.getAddedKeysAndValues().size());
        assertEquals(0, message.getUpdatedKeys().size());
        assertEquals(0, message.getUpdatedKeysAndValues().size());
        assertEquals(0, message.getRemovedKeys().size());
        assertEquals(0, message.getRemovedKeysAndValues().size());
        assertTrue(message.isNew());
        assertFalse(message.isDirty());
        assertEquals("1", message.getID());
        assertEquals(1436951840123L / 1000, message.getCreated().getTime());
        assertEquals("fooman", message.getCreatedBy());
        assertEquals(1436951850987L / 1000, message.getUpdated().getTime());
        assertEquals("barman", message.getUpdatedBy());
        long pushId = message.getPushId();
        assertEquals(1L, pushId);
        assertEquals("お知らせ", message.getTitle());
        assertEquals("こんにちは！", message.getMessage());
        assertEquals("http://www.appiaries.com/jp/", message.getUrl());
        assertEquals(1L, message.getFrom());
        assertEquals("folding-key", message.getCollapseKey());
        assertEquals(100, message.getTimeToLive());
        assertEquals(true, message.isDelayWhileIdle());
        assertEquals("com.appiaries.sample", message.getRestrictedPackageName());
        assertEquals(true, message.isDryRun());
        assertEquals(2, message.getRegistrationIds().size());
        assertEquals("custom attribute 1", message.get("attr1"));
        assertEquals("custom attribute 2", message.get("attr2"));
        assertEquals("custom attribute 3", message.get("attr3"));
    }

    @Test
    public void test__constructor__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ExtendedPushMessage.Field.ID.getKey(), "1");
                put(ExtendedPushMessage.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedPushMessage.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedPushMessage.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedPushMessage.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedPushMessage.Field.PUSH_ID.getKey(), 1L);
                put(ExtendedPushMessage.Field.TITLE.getKey(), "お知らせ");
                put(ExtendedPushMessage.Field.MESSAGE.getKey(), "こんにちは！");
                put(ExtendedPushMessage.Field.URL.getKey(), "http://www.appiaries.com/jp/");
                put(ExtendedPushMessage.Field.FROM.getKey(), 1L);
                put(ExtendedPushMessage.Field.COLLAPSE_KEY.getKey(), "folding-key");
                put(ExtendedPushMessage.Field.TIME_TO_LIVE.getKey(), 100);
                put(ExtendedPushMessage.Field.DELAY_WHILE_IDLE.getKey(), true);
                put(ExtendedPushMessage.Field.RESTRICTED_PACKAGE_NAME.getKey(), "com.appiaries.sample");
                put(ExtendedPushMessage.Field.DRY_RUN.getKey(), true);
                put(ExtendedPushMessage.Field.REGISTRATION_IDS.getKey(), Arrays.asList("Aed613596b3b8cec8f67da2b7a79fdfccb76fedede56e9e494a31139b73f59c0", "33fb7c83f6181e800f3e762ea73ecbc33a9ee5e77cf11dd7c34fc368b43fda74"));
                put("attr1", "custom attribute 1");
                put("attr2", "custom attribute 2");
                put("attr3", "custom attribute 3");
            }
        };
        message = new ExtendedPushMessage(map);
        assertEquals(ExtendedPushMessage.class, message.getClass());
        assertEquals(ExtendedPushMessage.class.getName(), message.getCollectionID()); //NOTE: ExtendedPushMessageはコレクションを持たないので常に com.appiaries.baas.sdk.ExtendedPushMessage が返される。
        assertEquals(19, message.getEstimatedData().size());
        assertEquals(19, message.getFilteredEstimatedData().size());
        assertEquals(19, message.getOriginalData().size());
        assertEquals(0, message.getAddedKeys().size());
        assertEquals(0, message.getAddedKeysAndValues().size());
        assertEquals(0, message.getUpdatedKeys().size());
        assertEquals(0, message.getUpdatedKeysAndValues().size());
        assertEquals(0, message.getRemovedKeys().size());
        assertEquals(0, message.getRemovedKeysAndValues().size());
        assertTrue(message.isNew());
        assertFalse(message.isDirty());
        assertEquals("1", message.getID());
        assertEquals(1436951840123L / 1000, message.getCreated().getTime());
        assertEquals("fooman", message.getCreatedBy());
        assertEquals(1436951850987L / 1000, message.getUpdated().getTime());
        assertEquals("barman", message.getUpdatedBy());
        long pushId = message.getPushId();
        assertEquals(1L, pushId);
        assertEquals("お知らせ", message.getTitle());
        assertEquals("こんにちは！", message.getMessage());
        assertEquals("http://www.appiaries.com/jp/", message.getUrl());
        assertEquals(1L, message.getFrom());
        assertEquals("folding-key", message.getCollapseKey());
        assertEquals(100, message.getTimeToLive());
        assertEquals(true, message.isDelayWhileIdle());
        assertEquals("com.appiaries.sample", message.getRestrictedPackageName());
        assertEquals(true, message.isDryRun());
        assertEquals(2, message.getRegistrationIds().size());
        assertEquals("custom attribute 1", message.get("attr1"));
        assertEquals("custom attribute 2", message.get("attr2"));
        assertEquals("custom attribute 3", message.get("attr3"));
    }

}