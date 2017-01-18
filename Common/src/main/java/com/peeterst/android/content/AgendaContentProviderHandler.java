package com.peeterst.android.content;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;
import com.peeterst.android.model.CalendarItem;
import com.peeterst.android.model.ICSCalendarItem;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 5/10/11
 * Time: 0:55
 * //TODO: get CalendarItem by factory method! (ICS / no-ICS)
 */
public class AgendaContentProviderHandler implements ContentProviderHandler {

    private static AgendaContentProviderHandler instance;
    private final Uri eventUri = Uri.parse("content://com.android.calendar/events");
    private final Uri calendarUri = Uri.parse("content://com.android.calendar/calendars");

    public static boolean isICSCalendar = true;
    private final String[] projection = new String[] { "_id", "name" };
    private final String path = "calendars";

    private AgendaContentProviderHandler(){
        if (Build.VERSION.SDK_INT >= 14) {
            isICSCalendar = true;
        }
    }

    public static AgendaContentProviderHandler getInstance(){
        if(instance == null){
            instance = new AgendaContentProviderHandler();
        }
        return instance;

    }

    public Map<Integer,String> getCalendars(Context source){
        ContentResolver contentResolver = source.getContentResolver();

        Cursor cursor = getCalendarCursor(projection,null,path,contentResolver);

        if(cursor != null){
            boolean isMovedToFirst = cursor.moveToFirst();

            Map<Integer,String> calendars = new TreeMap<Integer, String>();
            if(isMovedToFirst) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    calendars.put(cursor.getInt(0), cursor.getString(1));
                    boolean toNext = cursor.moveToNext();
                    if (!toNext) break;
                }
            }

            return calendars;

        }else {
            return null;
        }
    }

    private int getCalendar_ID(Context source) {
        int calendar_id         = 0;
        ContentResolver contentResolver = source.getContentResolver();
        Cursor calendarCursor   = getCalendarCursor(projection, null, path,contentResolver);

        if (calendarCursor != null && calendarCursor.moveToFirst()) {
            int nameColumn      = calendarCursor.getColumnIndex("name");
            int idColumn        = calendarCursor.getColumnIndex("_id");
            do {
                String calName  = calendarCursor.getString(nameColumn);
                String calId    = calendarCursor.getString(idColumn);
                if (calName != null /*&& calName.contains("Test")*/) {
                    calendar_id = Integer.parseInt(calId);
                }
            } while (calendarCursor.moveToNext());
        }
        return calendar_id;
    }

    private Cursor getCalendarCursor(String[] projection, String selection, String path, ContentResolver contentResolver) {
        Cursor cursor;

        try{
            cursor = contentResolver.query(
                    calendarUri, projection, selection, null, null);
            return cursor;

        }catch(SQLiteException e){
            throw new RuntimeException(e); //todo: create own exception and catch at caller to disable calendar in app
        }
    }


    public List<CalendarItem> getCalendarItems(Context source,Integer id, Integer calendarId, String title){
        ContentResolver cr = source.getContentResolver();

        getCalendars(source);

        final CalendarItem target;

        if(isICSCalendar){
            target = new ICSCalendarItem();
        }else {
            target = new CalendarItem();
        }


        List<CalendarItem> items = null;



        StringBuilder cBuilder = new StringBuilder();
        boolean isNew = true;
        ArrayList<String> values = new ArrayList<String>();

        final String q = "=?";

        if(title != null){
            cBuilder.append(target.getTITLE());
            cBuilder.append(q);
            values.add(title);
            isNew = false;
        }


        if(calendarId != null){
            if(!isNew){
                cBuilder.append(AND);
            }
            cBuilder.append(target.getCALENDAR_ID());
            cBuilder.append(q);
            values.add(String.valueOf(calendarId));
            isNew = false;
        }
        if(id != null){
            if(!isNew){
                cBuilder.append(AND);
            }

            cBuilder.append(target.getID());
            cBuilder.append(q);
            values.add(String.valueOf(id));
            isNew = false;
        }

        String qColumns = cBuilder.toString();
        String[] qValues = values.toArray(new String[values.size()]);
        Cursor cursor = cr.query(eventUri,new String[]{target.getID(),target.getCALENDAR_ID(),target.getTITLE(),target.getDESCR(),
                target.getSTART_DATE(),target.getEND_DATE()},qColumns,qValues,null);
        if(cursor != null){

            boolean isContent = cursor.moveToFirst();
            items  = new ArrayList<CalendarItem>();
            if(isContent){
                do {
                    CalendarItem item;
                    if(isICSCalendar){
                        item = new ICSCalendarItem();
//                        ((ICSCalendarItem)item).setEventTimezone();
                    }else {
                        item = new CalendarItem();
                    }
                    item.setId(cursor.getInt(0));
                    item.setCalendarId(cursor.getInt(1));
                    item.setTitle(cursor.getString(2));
                    item.setDescription(cursor.getString(3));

                    Calendar calendar = new GregorianCalendar();
                    calendar.setTimeInMillis(cursor.getLong(4));
                    item.setStartDate(calendar.getTime());

                    calendar.setTimeInMillis(cursor.getLong(5));
                    item.setEndDate(calendar.getTime());
                    items.add(item);
                }while (cursor.moveToNext());
//                System.out.println("");
            }

        }
        return items;
    }

    public List<CalendarItem> getCalendarItems(Context source, Integer calendarId, String title){
        return getCalendarItems(source,null,calendarId,title);
    }

    public CalendarItem getCalendarItem(Context source, Integer id, Integer calendarId, String title){
        List<CalendarItem> items = getCalendarItems(source, id,calendarId,title);
        if(items != null && !items.isEmpty()){
            return items.get(0);
        }else return null;
    }

    private ContentValues createMinimalCalendarContent(CalendarItem item){
        ContentValues event = new ContentValues();

        event.put(item.getCALENDAR_ID(),item.getCalendarId());
        event.put(item.getDESCR(),item.getDescription());
        event.put(item.getEND_DATE(),item.getEndDate().getTime());
        event.put(item.getSTART_DATE(),item.getStartDate().getTime());
        event.put(item.getTITLE(),item.getTitle());
        event.put("eventTimezone",TimeZone.getDefault().getID());

        event.put(item.getHAS_ALARM(),1);
        return event;
    }


    public void insertCalendarItem(Context source, CalendarItem item) {
        ContentResolver cr = source.getContentResolver();
        ContentValues event = createMinimalCalendarContent(item);
        cr.insert(eventUri,event);

    }

    public void updateCalendarItem(Context source, CalendarItem item){
        ContentResolver cr = source.getContentResolver();
        if(item.getId() == 0){
            throw new IllegalArgumentException("An id is required to update a record");
        }
        Uri updateUri = ContentUris.withAppendedId(eventUri,item.getId());
        ContentValues event = createMinimalCalendarContent(item);
        event.put(item.getID(),item.getId());
        int res = cr.update(updateUri, event, null, null);
        System.out.println(res);
        item.setDirty(false);
    }

    public void deleteCalendarItem(Context source, CalendarItem item){
        ContentResolver cr = source.getContentResolver();
        if(item.getId() == 0){
            throw new IllegalArgumentException("An id is required to delete a record");
        }
        Uri deleteUri = ContentUris.withAppendedId(eventUri,item.getId());
        cr.delete(deleteUri,null,null);
    }
}
