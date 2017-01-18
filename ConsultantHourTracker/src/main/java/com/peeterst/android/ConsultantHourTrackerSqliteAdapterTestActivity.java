package com.peeterst.android;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import com.peeterst.android.data.adapter.SQLiteAdapter;
import com.peeterst.android.model.Client;
import com.peeterst.android.model.WorkDay;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 28/05/12
 * Time: 16:42
 * Main screen controller
 */
public class ConsultantHourTrackerSqliteAdapterTestActivity extends Activity {

    private SQLiteAdapter<Client> clientSQLiteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clientSQLiteAdapter = new SQLiteAdapter<Client>(this,Client.class);
        Client client = new Client();
        client.setMonthlyHour(160);
        client.setName("KBC");
        client.setNoonOffset(40);

        clientSQLiteAdapter.insert(client);
//        clientSQLiteAdapter.close();

        if(isClientApplied()){
            List<Client> values = clientSQLiteAdapter.findBySelection("ClientName = 'KBC'");
            System.err.println(values);
            Client first = values.get(0);

            Client persisted = clientSQLiteAdapter.findById(first.getId());
            System.err.println(persisted);
            persisted.setName("CBC");
            persisted.setActive(true);
            int okValue = clientSQLiteAdapter.update(persisted);
            System.err.println(okValue);
            List<Client> updatedBunch = clientSQLiteAdapter.findBySelection("ClientName = 'CBC'");
            Client updated = updatedBunch.get(0); //throws if not ok
            okValue = clientSQLiteAdapter.delete(updated.getId());
            System.err.println(okValue);
            updatedBunch = clientSQLiteAdapter.findBySelection("ClientName = 'CBC'");
            if(updatedBunch == null || updatedBunch.size() == 0){
                System.err.println("ok deleted");
            }

            client.setId(0);
            clientSQLiteAdapter.insert(client);
            values = clientSQLiteAdapter.findBySelection("ClientName = 'KBC'");
            client = values.get(0);
            WorkDay workDay = new WorkDay();
            workDay.setClientId(client.getId());
            workDay.toggleProgress();
            client.setActive(true);
            client.addWorkDay(workDay);
            clientSQLiteAdapter.update(client);
            values = clientSQLiteAdapter.findBySelection("ClientName = 'KBC'");
            client = values.get(0);

            SQLiteAdapter<WorkDay> workDayAdapter = new SQLiteAdapter<WorkDay>(this,WorkDay.class);
//            long row = workDayAdapter.insert(workDay);
//            System.err.println("Workday inserted");
            WorkDay dbWorkDay = client.getWorkDays().get(0);
            workDay = workDayAdapter.findById(workDay.getId());
            System.out.println(dbWorkDay.getClientId());
            dbWorkDay.toggleProgress();
            System.err.println("rows updated: " + workDayAdapter.update(dbWorkDay));


        }else {

        }

    }

    private boolean isClientApplied() {
//        clientSQLiteAdapter.openToRead();
        Cursor cursor = clientSQLiteAdapter.queueAll();
        boolean result = cursor != null && cursor.getCount() > 0;
        if(cursor != null) {
            cursor.close();
        }
        clientSQLiteAdapter.close();
        return result;
    }
}
