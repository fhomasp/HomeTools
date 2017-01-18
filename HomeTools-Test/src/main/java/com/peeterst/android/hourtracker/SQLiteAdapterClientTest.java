package com.peeterst.android.hourtracker;

import android.test.ActivityInstrumentationTestCase2;
import com.peeterst.android.ConsultantHourTrackerSqliteAdapterTestActivity;
import com.peeterst.android.data.adapter.SQLiteAdapter;
import com.peeterst.android.data.persist.Field;
import com.peeterst.android.model.Client;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 29/05/12
 * Time: 13:32
 *
 */
//@RunWith(RobolectricTestRunner.class)
public class SQLiteAdapterClientTest extends ActivityInstrumentationTestCase2<ConsultantHourTrackerSqliteAdapterTestActivity> {

    public SQLiteAdapterClientTest() {
            super("com.peeterst.android.hourtracker", ConsultantHourTrackerSqliteAdapterTestActivity.class);
        }

    //    @Before
    public void setUp() throws Exception {
        super.setUp();
//        Robolectric
//        Activity activity = new ConsultantHourTrackerSqliteAdapterTestActivity();
    }

//    @Ignore
//    @Test
    public void testAnnotationsWithClient() {
        Client client = new Client();
        client.setMonthlyHour(160);
        client.setName("KBC");
        client.setNoonOffset(40);



        SQLiteAdapter<Client> adapter = new SQLiteAdapter<Client>(getActivity(),Client.class);

        Map<String,Field> fields = adapter.getAnnotatedFields();
        assertTrue(fields.size() > 0);

        adapter.insert(client);

    }

}
