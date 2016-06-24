package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class ABQueryTest extends InstrumentationTestCase {

    private ABQuery q;

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
        q = null;
    }

    @Test
    public void test__selectAll() throws Exception {
        //[N]
        q = new ABQuery().selectAll();
        assertFalse(q.getQueryString().contains("sel="));
        assertFalse(q.getQueryString().contains("excpt="));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertFalse(q.getQueryString().contains("oonly="));
        assertFalse(q.toString().contains("sel="));
        assertFalse(q.toString().contains("excpt="));
        assertFalse(q.toString().contains("proc=count"));
        assertFalse(q.toString().contains("oonly="));
    }

    @Test
    public void test__selectAllWithExcludedFields() throws Exception {
        //[N] 単一フィールドを指定したケース
        q = new ABQuery().selectAllWithExcludedFields(Arrays.asList("foo"));
        assertFalse(q.getQueryString().contains("sel="));
        assertTrue(q.getQueryString().contains("excpt=foo"));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertFalse(q.getQueryString().contains("oonly="));
        assertFalse(q.toString().contains("sel="));
        assertTrue(q.toString().contains("excpt=foo"));
        assertFalse(q.toString().contains("proc=count"));
        assertFalse(q.toString().contains("oonly="));
        //[N]　複数のフィールドを指定したケース
        q = new ABQuery().selectAllWithExcludedFields(Arrays.asList("foo", "bar"));
        assertFalse(q.getQueryString().contains("sel="));
        assertTrue(q.getQueryString().contains("excpt=foo,bar"));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertFalse(q.getQueryString().contains("oonly="));
        assertFalse(q.toString().contains("sel="));
        assertTrue(q.toString().contains("excpt=foo,bar"));
        assertFalse(q.toString().contains("proc=count"));
        assertFalse(q.toString().contains("oonly="));
    }

    @Test
    public void test__select() throws Exception {
        //[N] 単一フィールドを指定したケース
        q = new ABQuery().select(Collections.singletonList("foo"));
        assertTrue(q.getQueryString().contains("sel=foo"));
        assertFalse(q.getQueryString().contains("excpt="));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertFalse(q.getQueryString().contains("oonly="));
        assertTrue(q.toString().contains("sel=foo"));
        assertFalse(q.toString().contains("excpt="));
        assertFalse(q.toString().contains("proc=count"));
        assertFalse(q.toString().contains("oonly="));
        //[N]　複数のフィールドを指定したケース
        q = new ABQuery().select(Arrays.asList("foo", "bar"));
        assertTrue(q.getQueryString().contains("sel=foo,bar"));
        assertFalse(q.getQueryString().contains("excpt="));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertFalse(q.getQueryString().contains("oonly="));
        assertTrue(q.toString().contains("sel=foo,bar"));
        assertFalse(q.toString().contains("excpt="));
        assertFalse(q.toString().contains("proc=count"));
        assertFalse(q.toString().contains("oonly="));
    }

    @Test
    public void test__selectAllObjects() throws Exception {
        //[N]
        q = new ABQuery().selectAllObjects();
        assertFalse(q.getQueryString().contains("sel="));
        assertFalse(q.getQueryString().contains("excpt="));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertTrue(q.getQueryString().contains("oonly=true"));
        assertFalse(q.toString().contains("sel="));
        assertFalse(q.toString().contains("excpt="));
        assertFalse(q.toString().contains("proc=count"));
        assertTrue(q.toString().contains("oonly=true"));
    }

    @Test
    public void test__selectAllObjectsWithExcludedFields() throws Exception {
        //[N] 単一フィールドを指定したケース
        q = new ABQuery().selectAllObjectsWithExcludedFields(Collections.singletonList("foo"));
        assertFalse(q.getQueryString().contains("sel="));
        assertTrue(q.getQueryString().contains("excpt=foo"));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertTrue(q.getQueryString().contains("oonly=true"));
        assertFalse(q.toString().contains("sel="));
        assertTrue(q.toString().contains("excpt=foo"));
        assertFalse(q.toString().contains("proc=count"));
        assertTrue(q.toString().contains("oonly=true"));
        //[N]　複数のフィールドを指定したケース
        q = new ABQuery().selectAllObjectsWithExcludedFields(Arrays.asList("foo", "bar"));
        assertFalse(q.getQueryString().contains("sel="));
        assertTrue(q.getQueryString().contains("excpt=foo,bar"));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertTrue(q.getQueryString().contains("oonly=true"));
        assertFalse(q.toString().contains("sel="));
        assertTrue(q.toString().contains("excpt=foo,bar"));
        assertFalse(q.toString().contains("proc=count"));
        assertTrue(q.toString().contains("oonly=true"));
    }

    @Test
    public void test__selectObjects() throws Exception {
        //[N] 単一フィールドを指定したケース
        q = new ABQuery().selectObjects(Collections.singletonList("foo"));
        assertTrue(q.getQueryString().contains("sel=foo"));
        assertFalse(q.getQueryString().contains("excpt="));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertTrue(q.getQueryString().contains("oonly="));
        assertTrue(q.toString().contains("sel=foo"));
        assertFalse(q.toString().contains("excpt="));
        assertFalse(q.toString().contains("proc=count"));
        assertTrue(q.toString().contains("oonly=true"));
        //[N]　複数のフィールドを指定したケース
        q = new ABQuery().selectObjects(Arrays.asList("foo", "bar"));
        assertTrue(q.getQueryString().contains("sel=foo,bar"));
        assertFalse(q.getQueryString().contains("excpt="));
        assertFalse(q.getQueryString().contains("proc=count"));
        assertTrue(q.getQueryString().contains("oonly="));
        assertTrue(q.toString().contains("sel=foo,bar"));
        assertFalse(q.toString().contains("excpt="));
        assertFalse(q.toString().contains("proc=count"));
        assertTrue(q.toString().contains("oonly=true"));
    }

    @Test
    public void test__selectCount() throws Exception {
        //[N]
        q = new ABQuery().count();
        assertFalse(q.getQueryString().contains("sel="));
        assertFalse(q.getQueryString().contains("excpt="));
        assertTrue(q.getQueryString().contains("proc=count"));
        assertFalse(q.getQueryString().contains("oonly="));
        assertFalse(q.toString().contains("sel="));
        assertFalse(q.toString().contains("excpt="));
        assertTrue(q.toString().contains("proc=count"));
        assertFalse(q.toString().contains("oonly="));
    }

    @Test
    public void test__from() throws Exception {
        //[N]
        q = new ABQuery().from("col1");
        assertEquals("col1", q.getCollectionID());
        assertTrue(q.toString().startsWith("/col1"));
        //[N] インスタンス生成後のコレクションを変更してみる
        q.setCollectionID("col2");
        assertEquals("col2", q.getCollectionID());
        assertTrue(q.toString().startsWith("/col2"));
    }

    /*@Test
    public void test__fromQuery() throws Exception {

    }*/

    @Test
    public void test__where__exists() throws Exception {
        //[N] trueを指定するケース (exists)
        q = new ABQuery("col1").where("foo").exists();
        assertEquals("/-;foo.exist.true", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true", q.toString());
        //[N] falseを指定するケース (notExists)
        q = new ABQuery("col1").where("foo").notExists();
        assertEquals("/-;foo.exist.false", q.getConditionString());
        assertEquals("/col1/-;foo.exist.false", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").where("foo").exists().and("bar").notExists();
        assertEquals("/-;foo.exist.true;bar.exist.false", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true;bar.exist.false", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").where("foo").exists().and("foo").exists();
        assertEquals("/-;foo.exist.true", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").where("foo").exists().and("bar").equalsTo("dummy");
        assertEquals("/-;foo.exist.true;bar.eq.dummy", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true;bar.eq.dummy", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").where("foo").exists().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.exist.true", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").where("foo").exists().and("bar").equalsTo("dummy").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.exist.true;bar.eq.dummy", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true;bar.eq.dummy?order=baz", q.toString());
    }

    @Test
    public void test__and__exists() {
        //[N] trueを指定するケース (exists)
        q = new ABQuery("col1").and("foo").exists();
        assertEquals("/-;foo.exist.true", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true", q.toString());
        //[N] falseを指定するケース (notExists)
        q = new ABQuery("col1").and("foo").notExists();
        assertEquals("/-;foo.exist.false", q.getConditionString());
        assertEquals("/col1/-;foo.exist.false", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").and("foo").exists().and("bar").notExists();
        assertEquals("/-;foo.exist.true;bar.exist.false", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true;bar.exist.false", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").and("foo").exists().and("foo").exists();
        assertEquals("/-;foo.exist.true", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").and("foo").exists().and("bar").equalsTo("dummy");
        assertEquals("/-;foo.exist.true;bar.eq.dummy", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true;bar.eq.dummy", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").and("foo").exists().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.exist.true", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").and("foo").exists().and("bar").equalsTo("dummy").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.exist.true;bar.eq.dummy", q.getConditionString());
        assertEquals("/col1/-;foo.exist.true;bar.eq.dummy?order=baz", q.toString());
    }

    @Test
    public void test__or__exists() throws Exception {
        //[N] trueを指定するケース (exists)
        q = new ABQuery("col1").or("foo").exists();
        assertEquals("/-;or%7Bfoo.exist.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.exist.true%7D", q.toString());
        //[N] falseを指定するケース (notExists)
        q = new ABQuery("col1").or("foo").notExists();
        assertEquals("/-;or%7Bfoo.exist.false%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.exist.false%7D", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").or("foo").exists().and("bar").notExists();
        assertEquals("/-;bar.exist.false;or%7Bfoo.exist.true%7D", q.getConditionString());
        assertEquals("/col1/-;bar.exist.false;or%7Bfoo.exist.true%7D", q.toString());
        //[N] 同条件を複数回指定するケース
        // NOTE: 本来 "or{foo.exist.val%7D" という結果を期待するところだが、"or{foo.exist.val%7D;or{foo.exist.val%7D" といった結果が返される。
        //       実害は無いという点と、ABQuery.ConditionBundle の中身を重複チェックするオーバーヘッドが気になるという2観点から、このままそっとしておく。
        q = new ABQuery("col1").or("foo").exists().or("foo").exists();
        assertEquals("/-;or%7Bfoo.exist.true%7D;or%7Bfoo.exist.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.exist.true%7D;or%7Bfoo.exist.true%7D", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").or("foo").exists().or("bar").equalsTo("dummy");
        assertEquals("/-;or%7Bfoo.exist.true%7D;or%7Bbar.eq.dummy%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.exist.true%7D;or%7Bbar.eq.dummy%7D", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").or("foo").exists().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.exist.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.exist.true%7D?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").or("foo").exists().or("bar").equalsTo("dummy").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.exist.true%7D;or%7Bbar.eq.dummy%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.exist.true%7D;or%7Bbar.eq.dummy%7D?order=baz", q.toString());
    }

    @Test
    public void test__where__isTrue() throws Exception {
        //[N] trueを指定するケース (isTrue)
        q = new ABQuery("col1").where("foo").isTrue();
        assertEquals("/-;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;foo.is.true", q.toString());
        //[N] falseを指定するケース (isFalse)
        q = new ABQuery("col1").where("foo").isFalse();
        assertEquals("/-;foo.is.false", q.getConditionString());
        assertEquals("/col1/-;foo.is.false", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").where("foo").isTrue().and("bar").isTrue();
        assertEquals("/-;foo.is.true;bar.is.true", q.getConditionString());
        assertEquals("/col1/-;foo.is.true;bar.is.true", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").where("foo").isTrue().and("foo").isTrue();
        assertEquals("/-;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;foo.is.true", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").where("foo").isTrue().and("bar").equalsTo("dummy");
        assertEquals("/-;bar.eq.dummy;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;bar.eq.dummy;foo.is.true", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").where("foo").isTrue().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;foo.is.true?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").where("foo").isTrue().and("bar").equalsTo("dummy").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;bar.eq.dummy;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;bar.eq.dummy;foo.is.true?order=baz", q.toString());
    }

    @Test
    public void test__and__isTrue() throws Exception {
        //[N] trueを指定するケース (isTrue)
        q = new ABQuery("col1").and("foo").isTrue();
        assertEquals("/-;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;foo.is.true", q.toString());
        //[N] falseを指定するケース (isFalse)
        q = new ABQuery("col1").and("foo").isFalse();
        assertEquals("/-;foo.is.false", q.getConditionString());
        assertEquals("/col1/-;foo.is.false", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").and("foo").isTrue().and("bar").isTrue();
        assertEquals("/-;foo.is.true;bar.is.true", q.getConditionString());
        assertEquals("/col1/-;foo.is.true;bar.is.true", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").and("foo").isTrue().and("foo").isTrue();
        assertEquals("/-;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;foo.is.true", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").and("foo").isTrue().and("bar").equalsTo("dummy");
        assertEquals("/-;bar.eq.dummy;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;bar.eq.dummy;foo.is.true", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").and("foo").isTrue().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;foo.is.true?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").and("foo").isTrue().and("bar").equalsTo("dummy").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;bar.eq.dummy;foo.is.true", q.getConditionString());
        assertEquals("/col1/-;bar.eq.dummy;foo.is.true?order=baz", q.toString());
    }

    @Test
    public void test__or__isTrue() throws Exception {
        //[N] trueを指定するケース (isTrue)
        q = new ABQuery("col1").or("foo").isTrue();
        assertEquals("/-;or%7Bfoo.is.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.true%7D", q.toString());
        //[N] falseを指定するケース (isFalse)
        q = new ABQuery("col1").or("foo").isFalse();
        assertEquals("/-;or%7Bfoo.is.false%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.false%7D", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").or("foo").isTrue().or("bar").isTrue();
        assertEquals("/-;or%7Bfoo.is.true%7D;or%7Bbar.is.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.true%7D;or%7Bbar.is.true%7D", q.toString());
        //[N] 同条件を複数回指定するケース
        // NOTE: 本来 "or{foo.exist.val%7D" という結果を期待するところだが、"or{foo.exist.val%7D;or{foo.exist.val%7D" といった結果が返される。
        //       実害は無いという点と、ABQuery.ConditionBundle の中身を重複チェックするオーバーヘッドが気になるという2観点から、このままそっとしておく。
        q = new ABQuery("col1").or("foo").isTrue().or("foo").isTrue();
        assertEquals("/-;or%7Bfoo.is.true%7D;or%7Bfoo.is.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.true%7D;or%7Bfoo.is.true%7D", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").or("foo").isTrue().or("bar").equalsTo("dummy");
        assertEquals("/-;or%7Bfoo.is.true%7D;or%7Bbar.eq.dummy%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.true%7D;or%7Bbar.eq.dummy%7D", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").or("foo").isTrue().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.is.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.true%7D?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").or("foo").isTrue().or("bar").equalsTo("dummy").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.is.true%7D;or%7Bbar.eq.dummy%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.true%7D;or%7Bbar.eq.dummy%7D?order=baz", q.toString());
    }

    @Test
    public void test__where__isNull() throws Exception {
        //[N] trueを指定するケース (isNull)
        q = new ABQuery("col1").where("foo").isNull();
        assertEquals("/-;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;foo.is.null", q.toString());
        //[N] falseを指定するケース (isNotNull)
        q = new ABQuery("col1").where("foo").isNotNull();
        assertEquals("/-;foo.isn.null", q.getConditionString());
        assertEquals("/col1/-;foo.isn.null", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").where("foo").isNull().and("bar").isNull();
        assertEquals("/-;foo.is.null;bar.is.null", q.getConditionString());
        assertEquals("/col1/-;foo.is.null;bar.is.null", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").where("foo").isNull().and("foo").isNull();
        assertEquals("/-;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;foo.is.null", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").where("foo").isNull().and("bar").equalsTo("dummy");
        assertEquals("/-;bar.eq.dummy;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;bar.eq.dummy;foo.is.null", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").where("foo").isNull().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;foo.is.null?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").where("foo").isNull().and("bar").equalsTo("dummy").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;bar.eq.dummy;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;bar.eq.dummy;foo.is.null?order=baz", q.toString());
    }

    @Test
    public void test__and__isNull() throws Exception {
        //[N] trueを指定するケース (isNull)
        q = new ABQuery("col1").and("foo").isNull();
        assertEquals("/-;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;foo.is.null", q.toString());
        //[N] falseを指定するケース (isNotNull)
        q = new ABQuery("col1").and("foo").isNotNull();
        assertEquals("/-;foo.isn.null", q.getConditionString());
        assertEquals("/col1/-;foo.isn.null", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").and("foo").isNull().and("bar").isNull();
        assertEquals("/-;foo.is.null;bar.is.null", q.getConditionString());
        assertEquals("/col1/-;foo.is.null;bar.is.null", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").and("foo").isNull().and("foo").isNull();
        assertEquals("/-;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;foo.is.null", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").and("foo").isNull().and("bar").equalsTo("dummy");
        assertEquals("/-;bar.eq.dummy;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;bar.eq.dummy;foo.is.null", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").and("foo").isNull().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;foo.is.null?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").and("foo").isNull().and("bar").equalsTo("dummy").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;bar.eq.dummy;foo.is.null", q.getConditionString());
        assertEquals("/col1/-;bar.eq.dummy;foo.is.null?order=baz", q.toString());
    }

    @Test
    public void test__or__isNull() throws Exception {
        //[N] trueを指定するケース (isNull)
        q = new ABQuery("col1").or("foo").isNull();
        assertEquals("/-;or%7Bfoo.is.null%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.null%7D", q.toString());
        //[N] falseを指定するケース (isNotNull)
        q = new ABQuery("col1").or("foo").isNotNull();
        assertEquals("/-;or%7Bfoo.isn.null%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.isn.null%7D", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").or("foo").isNull().or("bar").isNull();
        assertEquals("/-;or%7Bfoo.is.null%7D;or%7Bbar.is.null%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.null%7D;or%7Bbar.is.null%7D", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").or("foo").isNull().or("foo").isNull();
        assertEquals("/-;or%7Bfoo.is.null%7D;or%7Bfoo.is.null%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.null%7D;or%7Bfoo.is.null%7D", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").or("foo").isNull().or("bar").equalsTo("dummy");
        assertEquals("/-;or%7Bfoo.is.null%7D;or%7Bbar.eq.dummy%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.null%7D;or%7Bbar.eq.dummy%7D", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").or("foo").isNull().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.is.null%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.null%7D?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").or("foo").isNull().or("bar").equalsTo("dummy").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.is.null%7D;or%7Bbar.eq.dummy%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.is.null%7D;or%7Bbar.eq.dummy%7D?order=baz", q.toString());
    }

    @Test
    public void test__where__equalsTo() throws Exception {
        //[N]
        q = new ABQuery("col1").where("foo").equalsTo("val");
        assertEquals("/-;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;foo.eq.val", q.toString());
        //[N] 値にマルチバイト文字列を指定する
        q = new ABQuery("col1").where("foo").equalsTo("吉田");
        assertEquals("/-;foo.eq.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.eq.%E5%90%89%E7%94%B0", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").equalsTo("va;l");
        assertEquals("/-;foo.eq.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.eq.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").equalsTo("va.l");
        assertEquals("/-;foo.eq.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.eq.va.l", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").where("foo").equalsTo("val1").and("bar").equalsTo("val2");
        assertEquals("/-;foo.eq.val1;bar.eq.val2", q.getConditionString());
        assertEquals("/col1/-;foo.eq.val1;bar.eq.val2", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").where("foo").equalsTo("val").and("foo").equalsTo("val");
        assertEquals("/-;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;foo.eq.val", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").where("foo").equalsTo("val").and("bar").exists();
        assertEquals("/-;bar.exist.true;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;bar.exist.true;foo.eq.val", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").where("foo").equalsTo("val").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;foo.eq.val?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").where("foo").equalsTo("val").and("bar").exists().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;bar.exist.true;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;bar.exist.true;foo.eq.val?order=baz", q.toString());
        //TODO: null, Number, List, Map が渡された場合のケースを追加
    }

    @Test
    public void test__and__equalsTo() throws Exception {
        //[N]
        q = new ABQuery("col1").and("foo").equalsTo("val");
        assertEquals("/-;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;foo.eq.val", q.toString());
        //[N] 値にマルチバイト文字列を指定する
        q = new ABQuery("col1").and("foo").equalsTo("吉田");
        assertEquals("/-;foo.eq.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.eq.%E5%90%89%E7%94%B0", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").equalsTo("va;l");
        assertEquals("/-;foo.eq.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.eq.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").equalsTo("va.l");
        assertEquals("/-;foo.eq.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.eq.va.l", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").and("foo").equalsTo("val1").and("bar").equalsTo("val2");
        assertEquals("/-;foo.eq.val1;bar.eq.val2", q.getConditionString());
        assertEquals("/col1/-;foo.eq.val1;bar.eq.val2", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").and("foo").equalsTo("val").and("foo").equalsTo("val");
        assertEquals("/-;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;foo.eq.val", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").and("foo").equalsTo("val").and("bar").exists();
        assertEquals("/-;bar.exist.true;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;bar.exist.true;foo.eq.val", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").and("foo").equalsTo("val").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;foo.eq.val?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").and("foo").equalsTo("val").and("bar").exists().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;bar.exist.true;foo.eq.val", q.getConditionString());
        assertEquals("/col1/-;bar.exist.true;foo.eq.val?order=baz", q.toString());
        //TODO: null, Number, List, Map が渡された場合のケースを追加
    }

    @Test
    public void test__or__equalsTo() throws Exception {
        //[N]
        q = new ABQuery("col1").or("foo").equalsTo("val");
        assertEquals("/-;or%7Bfoo.eq.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.val%7D", q.toString());
        //[N] 値にマルチバイト文字列を指定する
        q = new ABQuery("col1").or("foo").equalsTo("吉田");
        assertEquals("/-;or%7Bfoo.eq.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.%E5%90%89%E7%94%B0%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").equalsTo("va;l");
        assertEquals("/-;or%7Bfoo.eq.va%3Bl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.va%3Bl%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").equalsTo("va.l");
        assertEquals("/-;or%7Bfoo.eq.va.l%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.va.l%7D", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").or("foo").equalsTo("val1").or("bar").equalsTo("val2");
        assertEquals("/-;or%7Bfoo.eq.val1%7D;or%7Bbar.eq.val2%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.val1%7D;or%7Bbar.eq.val2%7D", q.toString());
        //[N] 同条件を複数回指定するケース
        // NOTE: 本来 "or{foo.exist.val%7D" という結果を期待するところだが、"or{foo.exist.val%7D;or{foo.exist.val%7D" といった結果が返される。
        //       実害は無いという点と、ABQuery.ConditionBundle の中身を重複チェックするオーバーヘッドが気になるという2観点から、このままそっとしておく。
        q = new ABQuery("col1").or("foo").equalsTo("val").or("foo").equalsTo("val");
        assertEquals("/-;or%7Bfoo.eq.val%7D;or%7Bfoo.eq.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.val%7D;or%7Bfoo.eq.val%7D", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").or("foo").equalsTo("val").or("bar").exists();
        assertEquals("/-;or%7Bfoo.eq.val%7D;or%7Bbar.exist.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.val%7D;or%7Bbar.exist.true%7D", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").or("foo").equalsTo("val").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.eq.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.val%7D?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").or("foo").equalsTo("val").or("bar").exists().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.eq.val%7D;or%7Bbar.exist.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.val%7D;or%7Bbar.exist.true%7D?order=baz", q.toString());
        //TODO: null, Number, List, Map が渡された場合のケースを追加
    }

    @Test
    public void test__where__notEqualsTo() throws Exception {
        //[N]
        q = new ABQuery("col1").where("foo").notEqualsTo("val");
        assertEquals("/-;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;foo.neq.val", q.toString());
        //[N] 値にマルチバイト文字列を指定する
        q = new ABQuery("col1").where("foo").notEqualsTo("吉田");
        assertEquals("/-;foo.neq.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.neq.%E5%90%89%E7%94%B0", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").notEqualsTo("va;l");
        assertEquals("/-;foo.neq.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.neq.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").notEqualsTo("va.l");
        assertEquals("/-;foo.neq.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.neq.va.l", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").where("foo").notEqualsTo("val1").and("bar").notEqualsTo("val2");
        assertEquals("/-;foo.neq.val1;bar.neq.val2", q.getConditionString());
        assertEquals("/col1/-;foo.neq.val1;bar.neq.val2", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").where("foo").notEqualsTo("val").and("foo").notEqualsTo("val");
        assertEquals("/-;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;foo.neq.val", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").where("foo").notEqualsTo("val").and("bar").exists();
        assertEquals("/-;bar.exist.true;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;bar.exist.true;foo.neq.val", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").where("foo").notEqualsTo("val").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;foo.neq.val?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").where("foo").notEqualsTo("val").and("bar").exists().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;bar.exist.true;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;bar.exist.true;foo.neq.val?order=baz", q.toString());
        //TODO: null, Number, List, Map が渡された場合のケースを追加
    }

    @Test
    public void test__and__notEqualsTo() throws Exception {
        //[N]
        q = new ABQuery("col1").and("foo").notEqualsTo("val");
        assertEquals("/-;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;foo.neq.val", q.toString());
        //[N] 値にマルチバイト文字列を指定する
        q = new ABQuery("col1").and("foo").notEqualsTo("吉田");
        assertEquals("/-;foo.neq.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.neq.%E5%90%89%E7%94%B0", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").notEqualsTo("va;l");
        assertEquals("/-;foo.neq.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.neq.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").notEqualsTo("va.l");
        assertEquals("/-;foo.neq.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.neq.va.l", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").and("foo").notEqualsTo("val1").and("bar").notEqualsTo("val2");
        assertEquals("/-;foo.neq.val1;bar.neq.val2", q.getConditionString());
        assertEquals("/col1/-;foo.neq.val1;bar.neq.val2", q.toString());
        //[N] 同条件を複数回指定するケース
        q = new ABQuery("col1").and("foo").notEqualsTo("val").and("foo").notEqualsTo("val");
        assertEquals("/-;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;foo.neq.val", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").and("foo").notEqualsTo("val").and("bar").exists();
        assertEquals("/-;bar.exist.true;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;bar.exist.true;foo.neq.val", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").and("foo").notEqualsTo("val").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;foo.neq.val?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").and("foo").notEqualsTo("val").and("bar").exists().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;bar.exist.true;foo.neq.val", q.getConditionString());
        assertEquals("/col1/-;bar.exist.true;foo.neq.val?order=baz", q.toString());
        //TODO: null, Number, List, Map が渡された場合のケースを追加
    }

    @Test
    public void test__or__notEqualsTo() throws Exception {
        //[N]
        q = new ABQuery("col1").or("foo").notEqualsTo("val");
        assertEquals("/-;or%7Bfoo.neq.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.neq.val%7D", q.toString());
        //[N] 値にマルチバイト文字列を指定する
        q = new ABQuery("col1").or("foo").notEqualsTo("吉田");
        assertEquals("/-;or%7Bfoo.neq.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.neq.%E5%90%89%E7%94%B0%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").notEqualsTo("va;l");
        assertEquals("/-;or%7Bfoo.neq.va%3Bl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.neq.va%3Bl%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").notEqualsTo("va.l");
        assertEquals("/-;or%7Bfoo.neq.va.l%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.neq.va.l%7D", q.toString());
        //[N] 同オペレータが混在するケース
        q = new ABQuery("col1").or("foo").notEqualsTo("val1").or("bar").notEqualsTo("val2");
        assertEquals("/-;or%7Bfoo.neq.val1%7D;or%7Bbar.neq.val2%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.neq.val1%7D;or%7Bbar.neq.val2%7D", q.toString());
        //[N] 同条件を複数回指定するケース
        // NOTE: 本来 "or%7Bfoo.exist.val%7D" という結果を期待するところだが、"or%7Bfoo.exist.val%7D;or%7Bfoo.exist.val%7D" といった結果が返される。
        //       実害は無いという点と、ABQuery.ConditionBundle の中身を重複チェックするオーバーヘッドが気になるという2観点から、このままそっとしておく。
        q = new ABQuery("col1").or("foo").notEqualsTo("val").or("foo").notEqualsTo("val");
        assertEquals("/-;or%7Bfoo.neq.val%7D;or%7Bfoo.neq.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.neq.val%7D;or%7Bfoo.neq.val%7D", q.toString());
        //[N] 他検索条件が混在するケース
        q = new ABQuery("col1").or("foo").notEqualsTo("val").or("bar").exists();
        assertEquals("/-;or%7Bfoo.neq.val%7D;or%7Bbar.exist.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.neq.val%7D;or%7Bbar.exist.true%7D", q.toString());
        //[N] クエリが混在するケース
        q = new ABQuery("col1").or("foo").notEqualsTo("val").orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.neq.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.neq.val%7D?order=baz", q.toString());
        //[N] 他検索条件とクエリが混在するケース
        q = new ABQuery("col1").or("foo").notEqualsTo("val").or("bar").exists().orderBy("baz", ABQuery.SortDirection.ASC);
        assertEquals("/-;or%7Bfoo.neq.val%7D;or%7Bbar.exist.true%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.neq.val%7D;or%7Bbar.exist.true%7D?order=baz", q.toString());
        //TODO: null, Number, List, Map が渡された場合のケースを追加
    }

    @Test
    public void test__where__greaterThan() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan(0);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan(+0);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        q = new ABQuery("col1").where("foo").greaterThan(-0);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan(1);
        assertEquals("/-;foo.gt.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.1n", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan(-1);
        assertEquals("/-;foo.gt.-1n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.-1n", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan(0.0f);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan(+0.0f);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        q = new ABQuery("col1").where("foo").greaterThan(-0.0f);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan(1.1f);
        assertEquals("/-;foo.gt.1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.1.1n", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan(-1.1f);
        assertEquals("/-;foo.gt.-1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.-1.1n", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan("100");
        assertEquals("/-;foo.gt.100", q.getConditionString());
        assertEquals("/col1/-;foo.gt.100", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").where("foo").greaterThan("abc");
        assertEquals("/-;foo.gt.abc", q.getConditionString());
        assertEquals("/col1/-;foo.gt.abc", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").where("foo").greaterThan("吉田");
        assertEquals("/-;foo.gt.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.gt.%E5%90%89%E7%94%B0", q.toString());*/
    }

    @Test
    public void test__and__greaterThan() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan(0);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan(+0);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        q = new ABQuery("col1").and("foo").greaterThan(-0);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan(1);
        assertEquals("/-;foo.gt.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.1n", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan(-1);
        assertEquals("/-;foo.gt.-1n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.-1n", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan(0.0f);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan(+0.0f);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        q = new ABQuery("col1").and("foo").greaterThan(-0.0f);
        assertEquals("/-;foo.gt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.0n", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan(1.1f);
        assertEquals("/-;foo.gt.1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.1.1n", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan(-1.1f);
        assertEquals("/-;foo.gt.-1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gt.-1.1n", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan("100");
        assertEquals("/-;foo.gt.100", q.getConditionString());
        assertEquals("/col1/-;foo.gt.100", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").and("foo").greaterThan("abc");
        assertEquals("/-;foo.gt.abc", q.getConditionString());
        assertEquals("/col1/-;foo.gt.abc", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").and("foo").greaterThan("吉田");
        assertEquals("/-;foo.gt.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.gt.%E5%90%89%E7%94%B0", q.toString());*/
    }

    @Test
    public void test__or__greaterThan() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan(0);
        assertEquals("/-;or%7Bfoo.gt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.0n%7D", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan(+0);
        assertEquals("/-;or%7Bfoo.gt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.0n%7D", q.toString());
        q = new ABQuery("col1").or("foo").greaterThan(-0);
        assertEquals("/-;or%7Bfoo.gt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.0n%7D", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan(1);
        assertEquals("/-;or%7Bfoo.gt.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.1n%7D", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan(-1);
        assertEquals("/-;or%7Bfoo.gt.-1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.-1n%7D", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan(0.0f);
        assertEquals("/-;or%7Bfoo.gt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.0n%7D", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan(+0.0f);
        assertEquals("/-;or%7Bfoo.gt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.0n%7D", q.toString());
        q = new ABQuery("col1").or("foo").greaterThan(-0.0f);
        assertEquals("/-;or%7Bfoo.gt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.0n%7D", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan(1.1f);
        assertEquals("/-;or%7Bfoo.gt.1.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.1.1n%7D", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan(-1.1f);
        assertEquals("/-;or%7Bfoo.gt.-1.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.-1.1n%7D", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan("100");
        assertEquals("/-;or%7Bfoo.gt.100%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.100%7D", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").or("foo").greaterThan("abc");
        assertEquals("/-;or%7Bfoo.gt.abc%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.abc%7D", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").or("foo").greaterThan("吉田");
        assertEquals("/-;or%7Bfoo.gt.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gt.%E5%90%89%E7%94%B0%7D", q.toString());*/
    }

    @Test
    public void test__where__greaterThanOrEqualsTo() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(0);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(+0);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(-0);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(1);
        assertEquals("/-;foo.gte.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.1n", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(-1);
        assertEquals("/-;foo.gte.-1n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.-1n", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(0.0f);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(+0.0f);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(-0.0f);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(1.1f);
        assertEquals("/-;foo.gte.1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.1.1n", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo(-1.1f);
        assertEquals("/-;foo.gte.-1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.-1.1n", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo("100");
        assertEquals("/-;foo.gte.100", q.getConditionString());
        assertEquals("/col1/-;foo.gte.100", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo("abc");
        assertEquals("/-;foo.gte.abc", q.getConditionString());
        assertEquals("/col1/-;foo.gte.abc", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").where("foo").greaterThanOrEqualsTo("吉田");
        assertEquals("/-;foo.gte.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.gte.%E5%90%89%E7%94%B0", q.toString());*/
    }

    @Test
    public void test__and__greaterThanOrEqualsTo() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(0);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(+0);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(-0);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(1);
        assertEquals("/-;foo.gte.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.1n", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(-1);
        assertEquals("/-;foo.gte.-1n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.-1n", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(0.0f);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(+0.0f);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(-0.0f);
        assertEquals("/-;foo.gte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.0n", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(1.1f);
        assertEquals("/-;foo.gte.1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.1.1n", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo(-1.1f);
        assertEquals("/-;foo.gte.-1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.-1.1n", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo("100");
        assertEquals("/-;foo.gte.100", q.getConditionString());
        assertEquals("/col1/-;foo.gte.100", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo("abc");
        assertEquals("/-;foo.gte.abc", q.getConditionString());
        assertEquals("/col1/-;foo.gte.abc", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").and("foo").greaterThanOrEqualsTo("吉田");
        assertEquals("/-;foo.gte.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.gte.%E5%90%89%E7%94%B0", q.toString());*/
    }

    @Test
    public void test__or__greaterThanOrEqualsTo() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(0);
        assertEquals("/-;or%7Bfoo.gte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.0n%7D", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(+0);
        assertEquals("/-;or%7Bfoo.gte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.0n%7D", q.toString());
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(-0);
        assertEquals("/-;or%7Bfoo.gte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.0n%7D", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(1);
        assertEquals("/-;or%7Bfoo.gte.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.1n%7D", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(-1);
        assertEquals("/-;or%7Bfoo.gte.-1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.-1n%7D", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(0.0f);
        assertEquals("/-;or%7Bfoo.gte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.0n%7D", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(+0.0f);
        assertEquals("/-;or%7Bfoo.gte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.0n%7D", q.toString());
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(-0.0f);
        assertEquals("/-;or%7Bfoo.gte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.0n%7D", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(1.1f);
        assertEquals("/-;or%7Bfoo.gte.1.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.1.1n%7D", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo(-1.1f);
        assertEquals("/-;or%7Bfoo.gte.-1.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.-1.1n%7D", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo("100");
        assertEquals("/-;or%7Bfoo.gte.100%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.100%7D", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo("abc");
        assertEquals("/-;or%7Bfoo.gte.abc%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.abc%7D", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").or("foo").greaterThanOrEqualsTo("吉田");
        assertEquals("/-;or%7Bfoo.gte.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.%E5%90%89%E7%94%B0%7D", q.toString());*/
    }

    @Test
    public void test__where__lessThan() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").where("foo").lessThan(0);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").where("foo").lessThan(+0);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        q = new ABQuery("col1").where("foo").lessThan(-0);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").where("foo").lessThan(1);
        assertEquals("/-;foo.lt.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.1n", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").where("foo").lessThan(-1);
        assertEquals("/-;foo.lt.-1n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.-1n", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").where("foo").lessThan(0.0f);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").where("foo").lessThan(+0.0f);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        q = new ABQuery("col1").where("foo").lessThan(-0.0f);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").where("foo").lessThan(1.1f);
        assertEquals("/-;foo.lt.1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.1.1n", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").where("foo").lessThan(-1.1f);
        assertEquals("/-;foo.lt.-1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.-1.1n", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").where("foo").lessThan("100");
        assertEquals("/-;foo.lt.100", q.getConditionString());
        assertEquals("/col1/-;foo.lt.100", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").where("foo").lessThan("abc");
        assertEquals("/-;foo.lt.abc", q.getConditionString());
        assertEquals("/col1/-;foo.lt.abc", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").where("foo").lessThan("吉田");
        assertEquals("/-;foo.lt.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.lt.%E5%90%89%E7%94%B0", q.toString());*/
    }

    @Test
    public void test__and__lessThan() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").and("foo").lessThan(0);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").and("foo").lessThan(+0);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        q = new ABQuery("col1").and("foo").lessThan(-0);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").and("foo").lessThan(1);
        assertEquals("/-;foo.lt.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.1n", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").and("foo").lessThan(-1);
        assertEquals("/-;foo.lt.-1n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.-1n", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").and("foo").lessThan(0.0f);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").and("foo").lessThan(+0.0f);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        q = new ABQuery("col1").and("foo").lessThan(-0.0f);
        assertEquals("/-;foo.lt.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.0n", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").and("foo").lessThan(1.1f);
        assertEquals("/-;foo.lt.1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.1.1n", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").and("foo").lessThan(-1.1f);
        assertEquals("/-;foo.lt.-1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lt.-1.1n", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").and("foo").lessThan("100");
        assertEquals("/-;foo.lt.100", q.getConditionString());
        assertEquals("/col1/-;foo.lt.100", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").and("foo").lessThan("abc");
        assertEquals("/-;foo.lt.abc", q.getConditionString());
        assertEquals("/col1/-;foo.lt.abc", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").and("foo").lessThan("吉田");
        assertEquals("/-;foo.lt.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.lt.%E5%90%89%E7%94%B0", q.toString());*/
    }

    @Test
    public void test__or__lessThan() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").or("foo").lessThan(0);
        assertEquals("/-;or%7Bfoo.lt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.0n%7D", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").or("foo").lessThan(+0);
        assertEquals("/-;or%7Bfoo.lt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.0n%7D", q.toString());
        q = new ABQuery("col1").or("foo").lessThan(-0);
        assertEquals("/-;or%7Bfoo.lt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.0n%7D", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").or("foo").lessThan(1);
        assertEquals("/-;or%7Bfoo.lt.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.1n%7D", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").or("foo").lessThan(-1);
        assertEquals("/-;or%7Bfoo.lt.-1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.-1n%7D", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").or("foo").lessThan(0.0f);
        assertEquals("/-;or%7Bfoo.lt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.0n%7D", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").or("foo").lessThan(+0.0f);
        assertEquals("/-;or%7Bfoo.lt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.0n%7D", q.toString());
        q = new ABQuery("col1").or("foo").lessThan(-0.0f);
        assertEquals("/-;or%7Bfoo.lt.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.0n%7D", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").or("foo").lessThan(1.1f);
        assertEquals("/-;or%7Bfoo.lt.1.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.1.1n%7D", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").or("foo").lessThan(-1.1f);
        assertEquals("/-;or%7Bfoo.lt.-1.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.-1.1n%7D", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").or("foo").lessThan("100");
        assertEquals("/-;or%7Bfoo.lt.100%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.100%7D", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").or("foo").lessThan("abc");
        assertEquals("/-;or%7Bfoo.lt.abc%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.abc%7D", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").or("foo").lessThan("吉田");
        assertEquals("/-;or%7Bfoo.lt.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lt.%E5%90%89%E7%94%B0%7D", q.toString());*/
    }

    @Test
    public void test__where__lessThanOrEqualsTo() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(0);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(+0);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(-0);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(1);
        assertEquals("/-;foo.lte.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.1n", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(-1);
        assertEquals("/-;foo.lte.-1n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.-1n", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(0.0f);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(+0.0f);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(-0.0f);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(1.1f);
        assertEquals("/-;foo.lte.1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.1.1n", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo(-1.1f);
        assertEquals("/-;foo.lte.-1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.-1.1n", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo("100");
        assertEquals("/-;foo.lte.100", q.getConditionString());
        assertEquals("/col1/-;foo.lte.100", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo("abc");
        assertEquals("/-;foo.lte.abc", q.getConditionString());
        assertEquals("/col1/-;foo.lte.abc", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").where("foo").lessThanOrEqualsTo("吉田");
        assertEquals("/-;foo.lte.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.lte.%E5%90%89%E7%94%B0", q.toString());*/
    }

    @Test
    public void test__and__lessThanOrEqualsTo() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(0);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(+0);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(-0);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(1);
        assertEquals("/-;foo.lte.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.1n", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(-1);
        assertEquals("/-;foo.lte.-1n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.-1n", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(0.0f);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(+0.0f);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(-0.0f);
        assertEquals("/-;foo.lte.0n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.0n", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(1.1f);
        assertEquals("/-;foo.lte.1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.1.1n", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo(-1.1f);
        assertEquals("/-;foo.lte.-1.1n", q.getConditionString());
        assertEquals("/col1/-;foo.lte.-1.1n", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo("100");
        assertEquals("/-;foo.lte.100", q.getConditionString());
        assertEquals("/col1/-;foo.lte.100", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo("abc");
        assertEquals("/-;foo.lte.abc", q.getConditionString());
        assertEquals("/col1/-;foo.lte.abc", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").and("foo").lessThanOrEqualsTo("吉田");
        assertEquals("/-;foo.lte.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.lte.%E5%90%89%E7%94%B0", q.toString());*/
    }

    @Test
    public void test__or__lessThanOrEqualsTo() throws Exception {
        //[N] ゼロ(整数)を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(0);
        assertEquals("/-;or%7Bfoo.lte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.0n%7D", q.toString());
        //[N] 符号付きゼロ(整数)を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(+0);
        assertEquals("/-;or%7Bfoo.lte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.0n%7D", q.toString());
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(-0);
        assertEquals("/-;or%7Bfoo.lte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.0n%7D", q.toString());
        //[N] 正の整数を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(1);
        assertEquals("/-;or%7Bfoo.lte.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.1n%7D", q.toString());
        //[N] 負の整数を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(-1);
        assertEquals("/-;or%7Bfoo.lte.-1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.-1n%7D", q.toString());
        //[N] ゼロ(実数)を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(0.0f);
        assertEquals("/-;or%7Bfoo.lte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.0n%7D", q.toString());
        //[N] 符号付きゼロ(実数)を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(+0.0f);
        assertEquals("/-;or%7Bfoo.lte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.0n%7D", q.toString());
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(-0.0f);
        assertEquals("/-;or%7Bfoo.lte.0n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.0n%7D", q.toString());
        //[N] 正の実数を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(1.1f);
        assertEquals("/-;or%7Bfoo.lte.1.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.1.1n%7D", q.toString());
        //[N] 負の実数を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo(-1.1f);
        assertEquals("/-;or%7Bfoo.lte.-1.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.-1.1n%7D", q.toString());
        /*//[N] 文字列の数値を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo("100");
        assertEquals("/-;or%7Bfoo.lte.100%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.100%7D", q.toString());*/
        /*//[N] 非数値の文字列を指定するケース
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo("abc");
        assertEquals("/-;or%7Bfoo.lte.abc%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.abc%7D", q.toString());*/
        /*//[N] マルチバイト文字列を指定する
        q = new ABQuery("col1").or("foo").lessThanOrEqualsTo("吉田");
        assertEquals("/-;or%7Bfoo.lte.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lte.%E5%90%89%E7%94%B0%7D", q.toString());*/
    }

    @Test
    public void test__where__contains() throws Exception {
        //[N] 単一要素を指定するケース
        q = new ABQuery("col1").where("foo").contains(Collections.singletonList("elm1"));
        assertEquals("/-;foo.in.elm1", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm1", q.toString());
        //[N] 複数要素を指定するケース
        q = new ABQuery("col1").where("foo").contains(Arrays.asList("elm1", "elm2", "elm3"));
        assertEquals("/-;foo.in.elm1,elm2,elm3", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm1,elm2,elm3", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").contains(Collections.singletonList("elm;1"));
        assertEquals("/-;foo.in.elm%3B1", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm%3B1", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").contains(Collections.singletonList("elm.1"));
        assertEquals("/-;foo.in.elm.1", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm.1", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").where("foo").contains(Collections.singletonList("elm,1"));
        assertEquals("/-;foo.in.elm%2C1", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm%2C1", q.toString());
    }

    @Test
    public void test__and__contains() throws Exception {
        //[N] 単一要素を指定するケース
        q = new ABQuery("col1").and("foo").contains(Collections.singletonList("elm1"));
        assertEquals("/-;foo.in.elm1", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm1", q.toString());
        //[N] 複数要素を指定するケース
        q = new ABQuery("col1").and("foo").contains(Arrays.asList("elm1", "elm2", "elm3"));
        assertEquals("/-;foo.in.elm1,elm2,elm3", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm1,elm2,elm3", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").contains(Collections.singletonList("elm;1"));
        assertEquals("/-;foo.in.elm%3B1", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm%3B1", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").contains(Collections.singletonList("elm.1"));
        assertEquals("/-;foo.in.elm.1", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm.1", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").and("foo").contains(Collections.singletonList("elm,1"));
        assertEquals("/-;foo.in.elm%2C1", q.getConditionString());
        assertEquals("/col1/-;foo.in.elm%2C1", q.toString());
    }

    @Test
    public void test__or__contains() throws Exception {
        //[N] 単一要素を指定するケース
        q = new ABQuery("col1").or("foo").contains(Collections.singletonList("elm1"));
        assertEquals("/-;or%7Bfoo.in.elm1%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.in.elm1%7D", q.toString());
        //[N] 複数要素を指定するケース
        q = new ABQuery("col1").or("foo").contains(Arrays.asList("elm1", "elm2", "elm3"));
        assertEquals("/-;or%7Bfoo.in.elm1,elm2,elm3%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.in.elm1,elm2,elm3%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").contains(Collections.singletonList("elm;1"));
        assertEquals("/-;or%7Bfoo.in.elm%3B1%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.in.elm%3B1%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").contains(Collections.singletonList("elm.1"));
        assertEquals("/-;or%7Bfoo.in.elm.1%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.in.elm.1%7D", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").or("foo").contains(Collections.singletonList("elm,1"));
        assertEquals("/-;or%7Bfoo.in.elm%2C1%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.in.elm%2C1%7D", q.toString());
    }

    @Test
    public void test__where__notContains() throws Exception {
        //[N] 単一要素を指定するケース
        q = new ABQuery("col1").where("foo").notContains(Collections.singletonList("elm1"));
        assertEquals("/-;foo.nin.elm1", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm1", q.toString());
        //[N] 複数要素を指定するケース
        q = new ABQuery("col1").where("foo").notContains(Arrays.asList("elm1", "elm2", "elm3"));
        assertEquals("/-;foo.nin.elm1,elm2,elm3", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm1,elm2,elm3", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").notContains(Collections.singletonList("elm;1"));
        assertEquals("/-;foo.nin.elm%3B1", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm%3B1", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").notContains(Collections.singletonList("elm.1"));
        assertEquals("/-;foo.nin.elm.1", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm.1", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").where("foo").notContains(Collections.singletonList("elm,1"));
        assertEquals("/-;foo.nin.elm%2C1", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm%2C1", q.toString());
    }

    @Test
    public void test__and__notContains() throws Exception {
        //[N] 単一要素を指定するケース
        q = new ABQuery("col1").and("foo").notContains(Collections.singletonList("elm1"));
        assertEquals("/-;foo.nin.elm1", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm1", q.toString());
        //[N] 複数要素を指定するケース
        q = new ABQuery("col1").and("foo").notContains(Arrays.asList("elm1", "elm2", "elm3"));
        assertEquals("/-;foo.nin.elm1,elm2,elm3", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm1,elm2,elm3", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").notContains(Collections.singletonList("elm;1"));
        assertEquals("/-;foo.nin.elm%3B1", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm%3B1", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").notContains(Collections.singletonList("elm.1"));
        assertEquals("/-;foo.nin.elm.1", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm.1", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").and("foo").notContains(Collections.singletonList("elm,1"));
        assertEquals("/-;foo.nin.elm%2C1", q.getConditionString());
        assertEquals("/col1/-;foo.nin.elm%2C1", q.toString());
    }

    @Test
    public void test__or__notContains() throws Exception {
        //[N] 単一要素を指定するケース
        q = new ABQuery("col1").or("foo").notContains(Collections.singletonList("elm1"));
        assertEquals("/-;or%7Bfoo.nin.elm1%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nin.elm1%7D", q.toString());
        //[N] 複数要素を指定するケース
        q = new ABQuery("col1").or("foo").notContains(Arrays.asList("elm1", "elm2", "elm3"));
        assertEquals("/-;or%7Bfoo.nin.elm1,elm2,elm3%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nin.elm1,elm2,elm3%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").notContains(Collections.singletonList("elm;1"));
        assertEquals("/-;or%7Bfoo.nin.elm%3B1%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nin.elm%3B1%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").notContains(Collections.singletonList("elm.1"));
        assertEquals("/-;or%7Bfoo.nin.elm.1%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nin.elm.1%7D", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").or("foo").notContains(Collections.singletonList("elm,1"));
        assertEquals("/-;or%7Bfoo.nin.elm%2C1%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nin.elm%2C1%7D", q.toString());
    }

    @Test
    public void test__where__startsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").where("foo").startsWith("x");
        assertEquals("/-;foo.sw.x", q.getConditionString());
        assertEquals("/col1/-;foo.sw.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").where("foo").startsWith("val");
        assertEquals("/-;foo.sw.val", q.getConditionString());
        assertEquals("/col1/-;foo.sw.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").where("foo").startsWith("吉田");
        assertEquals("/-;foo.sw.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.sw.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").where("foo").startsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.swi.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.swi.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").startsWith("va;l");
        assertEquals("/-;foo.sw.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.sw.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").startsWith("va.l");
        assertEquals("/-;foo.sw.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.sw.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").where("foo").startsWith("va,l");
        assertEquals("/-;foo.sw.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.sw.va%2Cl", q.toString());
    }

    @Test
    public void test__and__startsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").and("foo").startsWith("x");
        assertEquals("/-;foo.sw.x", q.getConditionString());
        assertEquals("/col1/-;foo.sw.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").and("foo").startsWith("val");
        assertEquals("/-;foo.sw.val", q.getConditionString());
        assertEquals("/col1/-;foo.sw.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").and("foo").startsWith("吉田");
        assertEquals("/-;foo.sw.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.sw.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").and("foo").startsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.swi.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.swi.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").startsWith("va;l");
        assertEquals("/-;foo.sw.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.sw.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").startsWith("va.l");
        assertEquals("/-;foo.sw.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.sw.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").and("foo").startsWith("va,l");
        assertEquals("/-;foo.sw.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.sw.va%2Cl", q.toString());
    }

    @Test
    public void test__or__startsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").or("foo").startsWith("x");
        assertEquals("/-;or%7Bfoo.sw.x%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.sw.x%7D", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").or("foo").startsWith("val");
        assertEquals("/-;or%7Bfoo.sw.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.sw.val%7D", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").or("foo").startsWith("吉田");
        assertEquals("/-;or%7Bfoo.sw.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.sw.%E5%90%89%E7%94%B0%7D", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").or("foo").startsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;or%7Bfoo.swi.VaL%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.swi.VaL%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").startsWith("va;l");
        assertEquals("/-;or%7Bfoo.sw.va%3Bl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.sw.va%3Bl%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").startsWith("va.l");
        assertEquals("/-;or%7Bfoo.sw.va.l%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.sw.va.l%7D", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").or("foo").startsWith("va,l");
        assertEquals("/-;or%7Bfoo.sw.va%2Cl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.sw.va%2Cl%7D", q.toString());
    }

    @Test
    public void test__where__notStartsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").where("foo").notStartsWith("x");
        assertEquals("/-;foo.nsw.x", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").where("foo").notStartsWith("val");
        assertEquals("/-;foo.nsw.val", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").where("foo").notStartsWith("吉田");
        assertEquals("/-;foo.nsw.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").where("foo").notStartsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.nswi.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.nswi.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").notStartsWith("va;l");
        assertEquals("/-;foo.nsw.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").notStartsWith("va.l");
        assertEquals("/-;foo.nsw.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").where("foo").notStartsWith("va,l");
        assertEquals("/-;foo.nsw.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.va%2Cl", q.toString());
    }

    @Test
    public void test__and__notStartsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").and("foo").notStartsWith("x");
        assertEquals("/-;foo.nsw.x", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").and("foo").notStartsWith("val");
        assertEquals("/-;foo.nsw.val", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").and("foo").notStartsWith("吉田");
        assertEquals("/-;foo.nsw.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").and("foo").notStartsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.nswi.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.nswi.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").notStartsWith("va;l");
        assertEquals("/-;foo.nsw.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").notStartsWith("va.l");
        assertEquals("/-;foo.nsw.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").and("foo").notStartsWith("va,l");
        assertEquals("/-;foo.nsw.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.nsw.va%2Cl", q.toString());
    }

    @Test
    public void test__or__notStartsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").or("foo").notStartsWith("x");
        assertEquals("/-;or%7Bfoo.nsw.x%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nsw.x%7D", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").or("foo").notStartsWith("val");
        assertEquals("/-;or%7Bfoo.nsw.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nsw.val%7D", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").or("foo").notStartsWith("吉田");
        assertEquals("/-;or%7Bfoo.nsw.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nsw.%E5%90%89%E7%94%B0%7D", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").or("foo").notStartsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;or%7Bfoo.nswi.VaL%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nswi.VaL%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").notStartsWith("va;l");
        assertEquals("/-;or%7Bfoo.nsw.va%3Bl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nsw.va%3Bl%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").notStartsWith("va.l");
        assertEquals("/-;or%7Bfoo.nsw.va.l%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nsw.va.l%7D", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").or("foo").notStartsWith("va,l");
        assertEquals("/-;or%7Bfoo.nsw.va%2Cl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nsw.va%2Cl%7D", q.toString());
    }

    @Test
    public void test__where__endsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").where("foo").endsWith("x");
        assertEquals("/-;foo.ew.x", q.getConditionString());
        assertEquals("/col1/-;foo.ew.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").where("foo").endsWith("val");
        assertEquals("/-;foo.ew.val", q.getConditionString());
        assertEquals("/col1/-;foo.ew.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").where("foo").endsWith("吉田");
        assertEquals("/-;foo.ew.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.ew.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").where("foo").endsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.ewi.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.ewi.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").endsWith("va;l");
        assertEquals("/-;foo.ew.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.ew.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").endsWith("va.l");
        assertEquals("/-;foo.ew.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.ew.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").where("foo").endsWith("va,l");
        assertEquals("/-;foo.ew.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.ew.va%2Cl", q.toString());
    }

    @Test
    public void test__and__endsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").and("foo").endsWith("x");
        assertEquals("/-;foo.ew.x", q.getConditionString());
        assertEquals("/col1/-;foo.ew.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").and("foo").endsWith("val");
        assertEquals("/-;foo.ew.val", q.getConditionString());
        assertEquals("/col1/-;foo.ew.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").and("foo").endsWith("吉田");
        assertEquals("/-;foo.ew.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.ew.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").and("foo").endsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.ewi.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.ewi.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").endsWith("va;l");
        assertEquals("/-;foo.ew.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.ew.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").endsWith("va.l");
        assertEquals("/-;foo.ew.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.ew.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").and("foo").endsWith("va,l");
        assertEquals("/-;foo.ew.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.ew.va%2Cl", q.toString());
    }

    @Test
    public void test__or__endsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").or("foo").endsWith("x");
        assertEquals("/-;or%7Bfoo.ew.x%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.ew.x%7D", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").or("foo").endsWith("val");
        assertEquals("/-;or%7Bfoo.ew.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.ew.val%7D", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").or("foo").endsWith("吉田");
        assertEquals("/-;or%7Bfoo.ew.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.ew.%E5%90%89%E7%94%B0%7D", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").or("foo").endsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;or%7Bfoo.ewi.VaL%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.ewi.VaL%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").endsWith("va;l");
        assertEquals("/-;or%7Bfoo.ew.va%3Bl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.ew.va%3Bl%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").endsWith("va.l");
        assertEquals("/-;or%7Bfoo.ew.va.l%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.ew.va.l%7D", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").or("foo").endsWith("va,l");
        assertEquals("/-;or%7Bfoo.ew.va%2Cl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.ew.va%2Cl%7D", q.toString());
    }

    @Test
    public void test__where__notEndsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").where("foo").notEndsWith("x");
        assertEquals("/-;foo.new.x", q.getConditionString());
        assertEquals("/col1/-;foo.new.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").where("foo").notEndsWith("val");
        assertEquals("/-;foo.new.val", q.getConditionString());
        assertEquals("/col1/-;foo.new.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").where("foo").notEndsWith("吉田");
        assertEquals("/-;foo.new.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.new.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").where("foo").notEndsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.newi.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.newi.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").notEndsWith("va;l");
        assertEquals("/-;foo.new.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.new.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").notEndsWith("va.l");
        assertEquals("/-;foo.new.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.new.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").where("foo").notEndsWith("va,l");
        assertEquals("/-;foo.new.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.new.va%2Cl", q.toString());
    }

    @Test
    public void test__and__notEndsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").and("foo").notEndsWith("x");
        assertEquals("/-;foo.new.x", q.getConditionString());
        assertEquals("/col1/-;foo.new.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").and("foo").notEndsWith("val");
        assertEquals("/-;foo.new.val", q.getConditionString());
        assertEquals("/col1/-;foo.new.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").and("foo").notEndsWith("吉田");
        assertEquals("/-;foo.new.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.new.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").and("foo").notEndsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.newi.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.newi.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").notEndsWith("va;l");
        assertEquals("/-;foo.new.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.new.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").notEndsWith("va.l");
        assertEquals("/-;foo.new.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.new.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").and("foo").notEndsWith("va,l");
        assertEquals("/-;foo.new.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.new.va%2Cl", q.toString());
    }

    @Test
    public void test__or__notEndsWith() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").or("foo").notEndsWith("x");
        assertEquals("/-;or%7Bfoo.new.x%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.new.x%7D", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").or("foo").notEndsWith("val");
        assertEquals("/-;or%7Bfoo.new.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.new.val%7D", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").or("foo").notEndsWith("吉田");
        assertEquals("/-;or%7Bfoo.new.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.new.%E5%90%89%E7%94%B0%7D", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").or("foo").notEndsWith("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;or%7Bfoo.newi.VaL%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.newi.VaL%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").notEndsWith("va;l");
        assertEquals("/-;or%7Bfoo.new.va%3Bl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.new.va%3Bl%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").notEndsWith("va.l");
        assertEquals("/-;or%7Bfoo.new.va.l%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.new.va.l%7D", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").or("foo").notEndsWith("va,l");
        assertEquals("/-;or%7Bfoo.new.va%2Cl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.new.va%2Cl%7D", q.toString());
    }

    @Test
    public void test__where__like() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").where("foo").like("x");
        assertEquals("/-;foo.li.x", q.getConditionString());
        assertEquals("/col1/-;foo.li.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").where("foo").like("val");
        assertEquals("/-;foo.li.val", q.getConditionString());
        assertEquals("/col1/-;foo.li.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").where("foo").like("吉田");
        assertEquals("/-;foo.li.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.li.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").where("foo").like("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.lii.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.lii.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").like("va;l");
        assertEquals("/-;foo.li.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.li.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").like("va.l");
        assertEquals("/-;foo.li.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.li.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").where("foo").like("va,l");
        assertEquals("/-;foo.li.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.li.va%2Cl", q.toString());
    }

    @Test
    public void test__and__like() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").and("foo").like("x");
        assertEquals("/-;foo.li.x", q.getConditionString());
        assertEquals("/col1/-;foo.li.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").and("foo").like("val");
        assertEquals("/-;foo.li.val", q.getConditionString());
        assertEquals("/col1/-;foo.li.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").and("foo").like("吉田");
        assertEquals("/-;foo.li.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.li.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").and("foo").like("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.lii.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.lii.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").like("va;l");
        assertEquals("/-;foo.li.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.li.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").like("va.l");
        assertEquals("/-;foo.li.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.li.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").and("foo").like("va,l");
        assertEquals("/-;foo.li.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.li.va%2Cl", q.toString());
    }

    @Test
    public void test__or__like() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").or("foo").like("x");
        assertEquals("/-;or%7Bfoo.li.x%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.li.x%7D", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").or("foo").like("val");
        assertEquals("/-;or%7Bfoo.li.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.li.val%7D", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").or("foo").like("吉田");
        assertEquals("/-;or%7Bfoo.li.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.li.%E5%90%89%E7%94%B0%7D", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").or("foo").like("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;or%7Bfoo.lii.VaL%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.lii.VaL%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").like("va;l");
        assertEquals("/-;or%7Bfoo.li.va%3Bl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.li.va%3Bl%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").like("va.l");
        assertEquals("/-;or%7Bfoo.li.va.l%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.li.va.l%7D", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").or("foo").like("va,l");
        assertEquals("/-;or%7Bfoo.li.va%2Cl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.li.va%2Cl%7D", q.toString());
    }

    @Test
    public void test__where__notLike() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").where("foo").notLike("x");
        assertEquals("/-;foo.nli.x", q.getConditionString());
        assertEquals("/col1/-;foo.nli.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").where("foo").notLike("val");
        assertEquals("/-;foo.nli.val", q.getConditionString());
        assertEquals("/col1/-;foo.nli.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").where("foo").notLike("吉田");
        assertEquals("/-;foo.nli.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.nli.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").where("foo").notLike("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.nlii.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.nlii.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").where("foo").notLike("va;l");
        assertEquals("/-;foo.nli.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.nli.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").where("foo").notLike("va.l");
        assertEquals("/-;foo.nli.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.nli.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").where("foo").notLike("va,l");
        assertEquals("/-;foo.nli.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.nli.va%2Cl", q.toString());
    }

    @Test
    public void test__and__notLike() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").and("foo").notLike("x");
        assertEquals("/-;foo.nli.x", q.getConditionString());
        assertEquals("/col1/-;foo.nli.x", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").and("foo").notLike("val");
        assertEquals("/-;foo.nli.val", q.getConditionString());
        assertEquals("/col1/-;foo.nli.val", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").and("foo").notLike("吉田");
        assertEquals("/-;foo.nli.%E5%90%89%E7%94%B0", q.getConditionString());
        assertEquals("/col1/-;foo.nli.%E5%90%89%E7%94%B0", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").and("foo").notLike("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;foo.nlii.VaL", q.getConditionString());
        assertEquals("/col1/-;foo.nlii.VaL", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").and("foo").notLike("va;l");
        assertEquals("/-;foo.nli.va%3Bl", q.getConditionString());
        assertEquals("/col1/-;foo.nli.va%3Bl", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").and("foo").notLike("va.l");
        assertEquals("/-;foo.nli.va.l", q.getConditionString());
        assertEquals("/col1/-;foo.nli.va.l", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").and("foo").notLike("va,l");
        assertEquals("/-;foo.nli.va%2Cl", q.getConditionString());
        assertEquals("/col1/-;foo.nli.va%2Cl", q.toString());
    }

    @Test
    public void test__or__notLike() throws Exception {
        //[N] 一文字指定するケース
        q = new ABQuery("col1").or("foo").notLike("x");
        assertEquals("/-;or%7Bfoo.nli.x%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nli.x%7D", q.toString());
        //[N] 文字列を指定するケース
        q = new ABQuery("col1").or("foo").notLike("val");
        assertEquals("/-;or%7Bfoo.nli.val%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nli.val%7D", q.toString());
        //[N] マルチバイト文字列を指定するケース
        q = new ABQuery("col1").or("foo").notLike("吉田");
        assertEquals("/-;or%7Bfoo.nli.%E5%90%89%E7%94%B0%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nli.%E5%90%89%E7%94%B0%7D", q.toString());
        //[N] 大小文字無視オプションを指定するケース
        q = new ABQuery("col1").or("foo").notLike("VaL", ABQuery.RegexOption.CASE_INSENSITIVE);
        assertEquals("/-;or%7Bfoo.nlii.VaL%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nlii.VaL%7D", q.toString());
        //[N] 値に条件セパレータ(";")を含むケース
        q = new ABQuery("col1").or("foo").notLike("va;l");
        assertEquals("/-;or%7Bfoo.nli.va%3Bl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nli.va%3Bl%7D", q.toString());
        //[N] 値にオペレータ／オペランド用セパレータ(".")を含むケース
        q = new ABQuery("col1").or("foo").notLike("va.l");
        assertEquals("/-;or%7Bfoo.nli.va.l%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nli.va.l%7D", q.toString());
        //[N] 値に値セパレータ(",")を含むケース
        q = new ABQuery("col1").or("foo").notLike("va,l");
        assertEquals("/-;or%7Bfoo.nli.va%2Cl%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.nli.va%2Cl%7D", q.toString());
    }

    @Test
    public void test__where__between() throws Exception {
        //[N] from　< to の数値を指定するケース
        q = new ABQuery("col1").where("foo").between(1, 10);
        assertEquals("/-;foo.gte.1n;foo.lte.10n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.1n;foo.lte.10n", q.toString());
        //[N] from　= to の数値を指定するケース
        q = new ABQuery("col1").where("foo").between(1, 1);
        assertEquals("/-;foo.eq.1n", q.getConditionString());
        assertEquals("/col1/-;foo.eq.1n", q.toString());
        //[N] from　> to の数値を指定するケース
        q = new ABQuery("col1").where("foo").between(10, 1);
        assertEquals("/-;foo.gte.1n;foo.lte.10n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.1n;foo.lte.10n", q.toString());
    }

    @Test
    public void test__and__between() throws Exception {
        //[N] from　< to の数値を指定するケース
        q = new ABQuery("col1").and("foo").between(1, 10);
        assertEquals("/-;foo.gte.1n;foo.lte.10n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.1n;foo.lte.10n", q.toString());
        //[N] from　= to の数値を指定するケース
        q = new ABQuery("col1").and("foo").between(1, 1);
        assertEquals("/-;foo.eq.1n", q.getConditionString());
        assertEquals("/col1/-;foo.eq.1n", q.toString());
        //[N] from　> to の数値を指定するケース
        q = new ABQuery("col1").and("foo").between(10, 1);
        assertEquals("/-;foo.gte.1n;foo.lte.10n", q.getConditionString());
        assertEquals("/col1/-;foo.gte.1n;foo.lte.10n", q.toString());
    }

    @Test
    public void test__or__between() throws Exception {
        //[N] from　< to の数値を指定するケース
        q = new ABQuery("col1").or("foo").between(1, 10);
        assertEquals("/-;or%7Bfoo.gte.1n;foo.lte.10n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.1n;foo.lte.10n%7D", q.toString());
        //[N] from　= to の数値を指定するケース
        q = new ABQuery("col1").or("foo").between(1, 1);
        assertEquals("/-;or%7Bfoo.eq.1n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.eq.1n%7D", q.toString());
        //[N] from　> to の数値を指定するケース
        q = new ABQuery("col1").or("foo").between(10, 1);
        assertEquals("/-;or%7Bfoo.gte.1n;foo.lte.10n%7D", q.getConditionString());
        assertEquals("/col1/-;or%7Bfoo.gte.1n;foo.lte.10n%7D", q.toString());
    }

    @Test
    public void test__where__withinCircle() throws Exception {
        //[N]
        q = new ABQuery("col1").where("_coord").withinCircle(new ABGeoPoint(12.345678, 23.456789), 2.5f);
        assertEquals("/-;_coord.wic.23.456789,12.345678,2.5", q.getConditionString());
        assertEquals("/col1/-;_coord.wic.23.456789,12.345678,2.5", q.toString());
    }

    @Test
    public void test__and__withinCircle() throws Exception {
        //[N]
        q = new ABQuery("col1").and("_coord").withinCircle(new ABGeoPoint(12.345678, 23.456789), 2.5f);
        assertEquals("/-;_coord.wic.23.456789,12.345678,2.5", q.getConditionString());
        assertEquals("/col1/-;_coord.wic.23.456789,12.345678,2.5", q.toString());
    }

    @Test
    public void test__or__withinCircle() throws Exception {
        //[N]
        q = new ABQuery("col1").or("_coord").withinCircle(new ABGeoPoint(12.345678, 23.456789), 2.5f);
        assertEquals("/-;or%7B_coord.wic.23.456789,12.345678,2.5%7D", q.getConditionString());
        assertEquals("/col1/-;or%7B_coord.wic.23.456789,12.345678,2.5%7D", q.toString());
    }

    @Test
    public void test__where__withinBox() throws Exception {
        //[N]
        q = new ABQuery("col1").where("_coord").withinBox(new ABGeoPoint(12.345678, 23.456789), new ABGeoPoint(87.654321, 98.765432));
        assertEquals("/-;_coord.wib.23.456789,12.345678,98.765432,87.654321", q.getConditionString());
        assertEquals("/col1/-;_coord.wib.23.456789,12.345678,98.765432,87.654321", q.toString());
        //[N] 基準点を指定するケース
        q = new ABQuery("col1").where("_coord").withinBox(new ABGeoPoint(12.345678, 23.456789), new ABGeoPoint(87.654321, 98.765432), new ABGeoPoint(11.111111, 22.222222));
        assertEquals("/-;_coord.wib.23.456789,12.345678,98.765432,87.654321,rc,22.222222,11.111111", q.getConditionString());
        assertEquals("/col1/-;_coord.wib.23.456789,12.345678,98.765432,87.654321,rc,22.222222,11.111111", q.toString());
        //TODO: 反転したポイントを指定するケース
        //TODO: 同一ポイントを指定するケース
    }

    @Test
    public void test__and__withinBox() throws Exception {
        //[N]
        q = new ABQuery("col1").and("_coord").withinBox(new ABGeoPoint(12.345678, 23.456789), new ABGeoPoint(87.654321, 98.765432));
        assertEquals("/-;_coord.wib.23.456789,12.345678,98.765432,87.654321", q.getConditionString());
        assertEquals("/col1/-;_coord.wib.23.456789,12.345678,98.765432,87.654321", q.toString());
        //[N] 基準点を指定するケース
        q = new ABQuery("col1").and("_coord").withinBox(new ABGeoPoint(12.345678, 23.456789), new ABGeoPoint(87.654321, 98.765432), new ABGeoPoint(11.111111, 22.222222));
        assertEquals("/-;_coord.wib.23.456789,12.345678,98.765432,87.654321,rc,22.222222,11.111111", q.getConditionString());
        assertEquals("/col1/-;_coord.wib.23.456789,12.345678,98.765432,87.654321,rc,22.222222,11.111111", q.toString());
        //TODO: 反転したポイントを指定するケース
        //TODO: 同一ポイントを指定するケース
    }

    @Test
    public void test__or__withinBox() throws Exception {
        //[N]
        q = new ABQuery("col1").or("_coord").withinBox(new ABGeoPoint(12.345678, 23.456789), new ABGeoPoint(87.654321, 98.765432));
        assertEquals("/-;or%7B_coord.wib.23.456789,12.345678,98.765432,87.654321%7D", q.getConditionString());
        assertEquals("/col1/-;or%7B_coord.wib.23.456789,12.345678,98.765432,87.654321%7D", q.toString());
        //[N] 基準点を指定するケース
        q = new ABQuery("col1").or("_coord").withinBox(new ABGeoPoint(12.345678, 23.456789), new ABGeoPoint(87.654321, 98.765432), new ABGeoPoint(11.111111, 22.222222));
        assertEquals("/-;or%7B_coord.wib.23.456789,12.345678,98.765432,87.654321,rc,22.222222,11.111111%7D", q.getConditionString());
        assertEquals("/col1/-;or%7B_coord.wib.23.456789,12.345678,98.765432,87.654321,rc,22.222222,11.111111%7D", q.toString());
        //TODO: 反転したポイントを指定するケース
        //TODO: 同一ポイントを指定するケース
    }

    @Test
    public void test__where__withinPolygon() throws Exception {
        //[N]
        q = new ABQuery("col1").where("_coord").withinPolygon(Arrays.asList(
                new ABGeoPoint(12.345678, 23.456789),
                new ABGeoPoint(22.345678, 33.456789),
                new ABGeoPoint(32.345678, 43.456789),
                new ABGeoPoint(42.345678, 53.456789),
                new ABGeoPoint(52.345678, 63.456789)
        ));
        assertEquals("/-;_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678", q.getConditionString());
        assertEquals("/col1/-;_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678", q.toString());
        //[N] 基準点を指定するケース
        q = new ABQuery("col1").where("_coord").withinPolygon(Arrays.asList(
                new ABGeoPoint(12.345678, 23.456789),
                new ABGeoPoint(22.345678, 33.456789),
                new ABGeoPoint(32.345678, 43.456789),
                new ABGeoPoint(42.345678, 53.456789),
                new ABGeoPoint(52.345678, 63.456789)
        ), new ABGeoPoint(11.111111, 22.222222));
        assertEquals("/-;_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678,rc,22.222222,11.111111", q.getConditionString());
        assertEquals("/col1/-;_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678,rc,22.222222,11.111111", q.toString());
        //TODO: 反転したポイントを指定するケース
        //TODO: 同一ポイントを指定するケース
    }

    @Test
    public void test__and__withinPolygon() throws Exception {
        //[N]
        q = new ABQuery("col1").and("_coord").withinPolygon(Arrays.asList(
                new ABGeoPoint(12.345678, 23.456789),
                new ABGeoPoint(22.345678, 33.456789),
                new ABGeoPoint(32.345678, 43.456789),
                new ABGeoPoint(42.345678, 53.456789),
                new ABGeoPoint(52.345678, 63.456789)
        ));
        assertEquals("/-;_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678", q.getConditionString());
        assertEquals("/col1/-;_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678", q.toString());
        //[N] 基準点を指定するケース
        q = new ABQuery("col1").and("_coord").withinPolygon(Arrays.asList(
                new ABGeoPoint(12.345678, 23.456789),
                new ABGeoPoint(22.345678, 33.456789),
                new ABGeoPoint(32.345678, 43.456789),
                new ABGeoPoint(42.345678, 53.456789),
                new ABGeoPoint(52.345678, 63.456789)
        ), new ABGeoPoint(11.111111, 22.222222));
        assertEquals("/-;_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678,rc,22.222222,11.111111", q.getConditionString());
        assertEquals("/col1/-;_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678,rc,22.222222,11.111111", q.toString());
        //TODO: 反転したポイントを指定するケース
        //TODO: 同一ポイントを指定するケース
    }

    @Test
    public void test__or__withinPolygon() throws Exception {
        //[N]
        q = new ABQuery("col1").or("_coord").withinPolygon(Arrays.asList(
                new ABGeoPoint(12.345678, 23.456789),
                new ABGeoPoint(22.345678, 33.456789),
                new ABGeoPoint(32.345678, 43.456789),
                new ABGeoPoint(42.345678, 53.456789),
                new ABGeoPoint(52.345678, 63.456789)
        ));
        assertEquals("/-;or%7B_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678%7D", q.getConditionString());
        assertEquals("/col1/-;or%7B_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678%7D", q.toString());
        //[N] 基準点を指定するケース
        q = new ABQuery("col1").or("_coord").withinPolygon(Arrays.asList(
                new ABGeoPoint(12.345678, 23.456789),
                new ABGeoPoint(22.345678, 33.456789),
                new ABGeoPoint(32.345678, 43.456789),
                new ABGeoPoint(42.345678, 53.456789),
                new ABGeoPoint(52.345678, 63.456789)
        ), new ABGeoPoint(11.111111, 22.222222));
        assertEquals("/-;or%7B_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678,rc,22.222222,11.111111%7D", q.getConditionString());
        assertEquals("/col1/-;or%7B_coord.wip.23.456789,12.345678,33.456789,22.345678,43.456789,32.345678,53.456789,42.345678,63.456789,52.345678,rc,22.222222,11.111111%7D", q.toString());
        //TODO: 反転したポイントを指定するケース
        //TODO: 同一ポイントを指定するケース
    }

    @Test
    public void test__bundle() throws Exception {
        //[N] 単一条件を指定するケース (AND)
        q = new ABQuery("items").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;foo.eq.fff", q.getConditionString());
        assertEquals("/items/-;foo.eq.fff", q.toString());
        //[N] where条件と組み合わせて単一条件を指定するケース (AND)
        q = new ABQuery("items").where("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;baz.sw.Zzz;foo.eq.fff", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;foo.eq.fff", q.toString());
        //[N] and条件と組み合わせて単一条件を指定するケース (AND)
        q = new ABQuery("items").and("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;baz.sw.Zzz;foo.eq.fff", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;foo.eq.fff", q.toString());
        //[N] or条件と組み合わせて単一条件を指定するケース (AND)
        q = new ABQuery("items").or("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;foo.eq.fff;or%7Bbaz.sw.Zzz%7D", q.getConditionString());
        assertEquals("/items/-;foo.eq.fff;or%7Bbaz.sw.Zzz%7D", q.toString());
        //[N] 複数条件を指定するケース (AND)
        q = new ABQuery("items").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;foo.eq.fff;bar.neq.bbb", q.getConditionString());
        assertEquals("/items/-;foo.eq.fff;bar.neq.bbb", q.toString());
        //[N] where条件と組み合わせて複数条件を指定するケース (AND)
        q = new ABQuery("items").where("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;baz.sw.Zzz;foo.eq.fff;bar.neq.bbb", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;foo.eq.fff;bar.neq.bbb", q.toString());
        //[N] and条件と組み合わせて複数条件を指定するケース (AND)
        q = new ABQuery("items").and("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;baz.sw.Zzz;foo.eq.fff;bar.neq.bbb", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;foo.eq.fff;bar.neq.bbb", q.toString());
        //[N] or条件と組み合わせて複数条件を指定するケース (AND)
        q = new ABQuery("items").or("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;foo.eq.fff;bar.neq.bbb;or%7Bbaz.sw.Zzz%7D", q.getConditionString());
        assertEquals("/items/-;foo.eq.fff;bar.neq.bbb;or%7Bbaz.sw.Zzz%7D", q.toString());
        //[N] bundle(AND)と組み合わせて複数条件を指定するケース (AND)
        q = new ABQuery("items").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addStartsWith("baz", "Zzz");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND).bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;baz.sw.Zzz;foo.eq.fff;bar.neq.bbb", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;foo.eq.fff;bar.neq.bbb", q.toString());
        //[N] bundle(OR)と組み合わせて複数条件を指定するケース (AND)
        q = new ABQuery("items").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addStartsWith("baz", "Zzz");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR).bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND);
        assertEquals("/-;foo.eq.fff;bar.neq.bbb;or%7Bbaz.sw.Zzz%7D", q.getConditionString());
        assertEquals("/items/-;foo.eq.fff;bar.neq.bbb;or%7Bbaz.sw.Zzz%7D", q.toString());

        //[N] 単一条件を指定するケース (OR)
        q = new ABQuery("items").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;or%7Bfoo.eq.fff%7D", q.getConditionString());
        assertEquals("/items/-;or%7Bfoo.eq.fff%7D", q.toString());
        //[N] where条件と組み合わせて単一条件を指定するケース (OR)
        q = new ABQuery("items").where("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;baz.sw.Zzz;or%7Bfoo.eq.fff%7D", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;or%7Bfoo.eq.fff%7D", q.toString());
        //[N] and条件と組み合わせて単一条件を指定するケース (OR)
        q = new ABQuery("items").and("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;baz.sw.Zzz;or%7Bfoo.eq.fff%7D", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;or%7Bfoo.eq.fff%7D", q.toString());
        //[N] or条件と組み合わせて単一条件を指定するケース (OR)
        q = new ABQuery("items").or("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;or%7Bbaz.sw.Zzz%7D;or%7Bfoo.eq.fff%7D", q.getConditionString());
        assertEquals("/items/-;or%7Bbaz.sw.Zzz%7D;or%7Bfoo.eq.fff%7D", q.toString());
        //[N] 複数条件を指定するケース (OR)
        q = new ABQuery("items").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.getConditionString());
        assertEquals("/items/-;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.toString());
        //[N] where条件と組み合わせて複数条件を指定するケース (OR)
        q = new ABQuery("items").where("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;baz.sw.Zzz;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.toString());
        //[N] and条件と組み合わせて複数条件を指定するケース (OR)
        q = new ABQuery("items").and("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;baz.sw.Zzz;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.toString());
        //[N] or条件と組み合わせて複数条件を指定するケース (OR)
        q = new ABQuery("items").or("baz").startsWith("Zzz").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;or%7Bbaz.sw.Zzz%7D;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.getConditionString());
        assertEquals("/items/-;or%7Bbaz.sw.Zzz%7D;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.toString());
        //[N] bundle(AND)と組み合わせて複数条件を指定するケース (OR)
        q = new ABQuery("items").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addStartsWith("baz", "Zzz");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.AND).bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;baz.sw.Zzz;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.getConditionString());
        assertEquals("/items/-;baz.sw.Zzz;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.toString());
        //[N] bundle(OR)と組み合わせて複数条件を指定するケース (OR)
        q = new ABQuery("items").bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addStartsWith("baz", "Zzz");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR).bundle(new ConditionBundler() {
            @Override
            public ABQuery.ConditionBundle bundle(ABQuery.ConditionBundle conditionBundle) {
                conditionBundle.addEqualsTo("foo", "fff");
                conditionBundle.addNotEqualsTo("bar", "bbb");
                return conditionBundle;
            }
        }, ABQuery.Conjunction.OR);
        assertEquals("/-;or%7Bbaz.sw.Zzz%7D;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.getConditionString());
        assertEquals("/items/-;or%7Bbaz.sw.Zzz%7D;or%7Bfoo.eq.fff;bar.neq.bbb%7D", q.toString());
    }

    @Test
    public void test__orderBy() throws Exception {
        //[N] 単一フィールドを昇順ソート指定するケース
        q = new ABQuery("col1").orderBy("foo", ABQuery.SortDirection.ASC);
        assertEquals("?order=foo", q.getQueryString());
        assertEquals("/col1/-?order=foo", q.toString());
        //[N] 単一フィールドを降順ソート指定するケース
        q = new ABQuery("col1").orderBy("foo", ABQuery.SortDirection.DESC);
        assertEquals("?order=-foo", q.getQueryString());
        assertEquals("/col1/-?order=-foo", q.toString());
        //[N] 複数フィールドを昇順ソート指定するケース
        q = new ABQuery("col1").orderBy(Arrays.asList("foo", "bar"), Arrays.asList(ABQuery.SortDirection.ASC, ABQuery.SortDirection.ASC));
        assertEquals("?order=foo,bar", q.getQueryString());
        assertEquals("/col1/-?order=foo,bar", q.toString());
        //[N] 複数フィールドを降順ソート指定するケース
        q = new ABQuery("col1").orderBy(Arrays.asList("foo", "bar"), Arrays.asList(ABQuery.SortDirection.DESC, ABQuery.SortDirection.DESC));
        assertEquals("?order=-foo,-bar", q.getQueryString());
        assertEquals("/col1/-?order=-foo,-bar", q.toString());
        //[N] 昇順・降順を混在指定するケース
        q = new ABQuery("col1").orderBy(Arrays.asList("foo", "bar"), Arrays.asList(ABQuery.SortDirection.ASC, ABQuery.SortDirection.DESC));
        assertEquals("?order=foo,-bar", q.getQueryString());
        assertEquals("/col1/-?order=foo,-bar", q.toString());
    }

    @Test
    public void test__limit() throws Exception {
        //[N]
        q = new ABQuery("col1");
        assertEquals("/-", q.getConditionString());
        assertEquals("/col1/-", q.toString());
        assertFalse(q.toString().contains("depth="));
        //[N]
        q = new ABQuery("col1").limit(10);
        assertEquals("/1-10", q.getConditionString());
        assertEquals("/col1/1-10", q.toString());
        assertFalse(q.toString().contains("depth="));
        //[N]
        q = new ABQuery("col1").limit(10).skip(20);
        assertEquals("/21-30", q.getConditionString());
        assertEquals("/col1/21-30", q.toString());
        assertFalse(q.toString().contains("depth="));
    }

    @Test
    public void test__skip() throws Exception {
        //[N]
        q = new ABQuery("col1");
        assertEquals("/-", q.getConditionString());
        assertEquals("/col1/-", q.toString());
        assertFalse(q.toString().contains("depth="));
        //[N]
        q = new ABQuery("col1").skip(20);
        assertEquals("/21-", q.getConditionString());
        assertEquals("/col1/21-", q.toString());
        assertFalse(q.toString().contains("depth="));
        //[N]
        q = new ABQuery("col1").skip(20).limit(10);
        assertEquals("/21-30", q.getConditionString());
        assertEquals("/col1/21-30", q.toString());
        assertFalse(q.toString().contains("depth="));
    }

    @Test
    public void test__depth() throws Exception {
        /*//[N]
        q = new ABQuery("col1").limit(10).skip(20).depth(3);
        assertEquals("/21-30", q.getConditionString());
        assertEquals("/col1/21-30?depth=3", q.toString());*/
        /*//[N]
        q = new ABQuery("col1").skip(20).depth(3);
        assertEquals("/21-", q.getConditionString());
        assertEquals("/col1/21-?depth=3", q.toString());
        assertFalse(q.toString().contains("depth="));*/
        /*//[N]
        q = new ABQuery("col1").limit(10).depth(3);
        assertEquals("/1-10", q.getConditionString());
        assertEquals("/col1/1-10?depth=3", q.toString());*/
        /*//[N]
        q = new ABQuery("col1").depth(3);
        assertEquals("/-", q.getConditionString());
        assertEquals("/col1/-?depth=3", q.toString());*/
    }

    /*@Test
    public void test__execute() throws Exception {

    }*/

    /*@Test
    public void test__toString() throws Exception {

    }*/

    /*@Test
    public void test__getConditionString() throws Exception {

    }*/

    /*@Test
    public void test__getQueryString() throws Exception {

    }*/

    /*@Test
    public void test__getCollectionID() throws Exception {

    }*/

    /*@Test
    public void test__setCollectionID() throws Exception {

    }*/

    /*@Test
    public void test__clone() throws Exception {

    }*/

}