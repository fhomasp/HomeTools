package com.peeterst.android.observer;

import java.util.Date;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;

public class CalendarObserver extends ContentObserver
{

    private Context   context;
    Cursor         cur, curval;
    String         title, summ, loc;
    Date                 start, end;
    private      boolean   monitorStatus;
    private      int           contactCount, count, curCount;
    String         name, update;
    StringBuilder           sb;
    SmsManager      sms;

    public CalendarObserver(Handler handler, Context con)
    {
        super(handler);
        context = con;
    }

    public void startMonitoring()
    {
        Log.v(".............................................", "Entered in monitoring");
        monitorStatus = false;
        if (!monitorStatus)
        {
            curval = context.getContentResolver().query(Uri.parse("content://calendar/events"), new String[] { "title", "dtstart", "dtend", "description" }, null, null, null);
            if (curval != null && curval.getCount() > 0)
            {
                contactCount = curval.getCount();
            }
        }
    }

    @Override
    public boolean deliverSelfNotifications()
    {
        return true;
    }

    public void onChange(boolean selfChange)
    {
        super.onChange(selfChange);
        Thread thread = new Thread() {
            public void run()
            {
                try
                {
                    cur = context.getContentResolver().query(Uri.parse("content://calendar/events"), new String[] { "title", "dtstart", "dtend", "description" }, null, null, null);
                    curCount = 0;
                    if (cur != null && cur.getCount() > 0)
                    {
                        curCount = cur.getCount();
                    }
                    if ((curCount >= contactCount))
                    {
                        if (count == 3)
                        {
                            contactCount = curCount;
                            cur.moveToLast();
                            title = cur.getString(cur.getColumnIndex("title"));
                            start = new Date(cur.getLong(cur.getColumnIndex("dtstart")));
                            end = new Date(cur.getLong(cur.getColumnIndex("dtend")));
                            summ = cur.getString(cur.getColumnIndex("description"));
                            sb.append("New Event Added :\n");
                            sb.append("Title:");
                            sb.append(title + "\n");
                            sb.append("Start Time:");
                            sb.append(start + "\n");
                            sb.append("End Time:");
                            sb.append(end + "\n");
                            sb.append("Summary:");
                            sb.append(summ + "\n");
                        }
                    }
                    if ((curCount < contactCount))
                    {
                        Log.v(".............................................................", "Event deleted");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}


