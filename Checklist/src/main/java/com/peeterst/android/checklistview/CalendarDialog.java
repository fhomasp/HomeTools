package com.peeterst.android.checklistview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.DatePicker;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.adapter.AgendaAdapter;
import com.peeterst.android.content.AgendaContentProviderHandler;
import com.peeterst.android.model.CalendarItem;
import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ICSCalendarItem;
import com.peeterst.android.util.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 7/09/11
 * Time: 18:21
 * Custom datepicker that allows to enter/modify this into the android calendar
 */
public class CalendarDialog extends Dialog  {


    private DatePicker datepicker;
    private OnCalendarDateSetListener onCalendarDateSetListener;
    private CheckBox insertCheck;
    private Checklist checklist;

    private int year,month,day,hour,minute;
    private Map<Integer,String> calendars;
    private GridView calendarGrid;
    private AgendaContentProviderHandler handler;
    private Button setButton;
    private TextView datePickerHeader;
    private TimePicker timePicker;

    public CalendarDialog(Context context,int year,int month, int day, Checklist checklist) {
        super(context);
        this.year = year;
        this.month = month;
        this.day = day;
        this.checklist = checklist;
    }

    public CalendarDialog(Context context) {
        super(context);
    }

    public CalendarDialog(Context context, int theme) {
        super(context, theme);
    }

    protected CalendarDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datepicker);

        datePickerHeader = (TextView) this.findViewById(R.id.datePickerHeader);
        datepicker = (DatePicker) this.findViewById(R.id.calendarDatePicker);
        setButton = (Button) this.findViewById(R.id.CalendarSetButton);
        setButton.setOnClickListener(onCalendarDateSetListener);
        timePicker = (TimePicker) this.findViewById(R.id.calendarTimePicker);
        this.insertCheck = (CheckBox) this.findViewById(R.id.calendarCheckbox);
        insertCheck.setOnClickListener(new InsertCheckListener());
        Button cancel = (Button) this.findViewById(R.id.CalendarCancelButton);
        cancel.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View view) {
                CalendarDialog.this.dismiss();
            }
        });
        datepicker.init(this.year,this.month,this.day,new DatePicker.OnDateChangedListener(){

            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                datePickerHeader.setText(com.peeterst.android.util.Formatter.formatDate(i, i1, i2, getContext()));
            }
        });

        timePicker.setCurrentHour(this.hour);
        timePicker.setCurrentMinute(this.minute);
//        datepicker.updateDate(this.year,this.month,this.day);
        datePickerHeader.setText(com.peeterst.android.util.Formatter.formatDate(this.year, this.month, this.day, getContext()));

        if(this.checklist == null){
            insertCheck.setEnabled(false);
        }

        initAgenda();


    }

    public void addOnCalendarDateSetListener(OnCalendarDateSetListener listener){
        this.onCalendarDateSetListener = listener;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    private void initAgenda(){

        handler = AgendaContentProviderHandler.getInstance();
        this.calendars = handler.getCalendars(getContext());

        calendarGrid = (GridView) findViewById(R.id.CalendarsGrid);
        calendarGrid.setAdapter(new AgendaAdapter(getContext(), this.calendars));



        if(calendars == null || calendars.isEmpty()){
            insertCheck.setEnabled(false);
        }else {
            //We check if we have a calendar entry and refresh it (multitasking!)
            if(checklist.getCalendarItem() != null){
                CalendarItem item = checklist.getCalendarItem();
                CalendarItem actual = handler.getCalendarItem(this.getContext(), item.getId(), item.getCalendarId(), item.getTitle());
                checklist.setCalendarItem(actual);
                if(actual == null){
                    //TODO: notify that previous calendar stuff has been deleted, probably elsewhere
                }

            }
            populateAgendaFields(checklist.getCalendarItem());
        }

    }

    private void populateAgendaFields(CalendarItem item){
        if(item == null){
            calendarGrid.setVisibility(View.INVISIBLE);
        }else {
            insertCheck.setChecked(true);
            AgendaAdapter adapter = (AgendaAdapter) calendarGrid.getAdapter();
            adapter.setItemPopulated(item.getCalendarId());
//            if(adapter.getEnabledCalendarId() == 0){
//                this.setButton.setEnabled(false);
//            }
        }

    }

    protected class InsertCheckListener implements CheckBox.OnClickListener {

        public void onClick(View view) {
            if(insertCheck.isChecked()){
                calendarGrid.setVisibility(View.VISIBLE);
//                AgendaAdapter adapter = (AgendaAdapter) calendarGrid.getAdapter();
//                if(adapter.getEnabledCalendarId() == 0) {
//                    adapter.setItemPopulated(1);
//                    adapter.notifyDataSetChanged();
//                }

            }else {
                calendarGrid.setVisibility(View.INVISIBLE);
            }
        }



    }

    private CalendarItem pollForExistingCalendarEntry(Checklist checklist){
        Integer calendar_id = null;
        Integer _id = null;
        if(checklist.getCalendarItem() != null){
            calendar_id = checklist.getCalendarItem().getCalendarId();
            _id = checklist.getCalendarItem().getId();
        }

        List<CalendarItem> items = handler.getCalendarItems(getContext(), _id,calendar_id ,null);
        if(items != null && !items.isEmpty()){
            return items.get(0);
        }
        return null;
    }

    private CalendarItem createCalendarItemFromUIValues(){
        CalendarItem item;
        if(AgendaContentProviderHandler.isICSCalendar){
            item = new ICSCalendarItem(); //TODO: factory method!
        } else {
            item = new CalendarItem();
        }


        AgendaAdapter adapter = (AgendaAdapter) calendarGrid.getAdapter();
        item.setCalendarId(adapter.getEnabledCalendarId());
        item.setTitle(checklist.getTitle());

        Calendar calendar = new GregorianCalendar(datepicker.getYear(),datepicker.getMonth(),datepicker.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY,timePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE,timePicker.getCurrentMinute());
        item.setStartDate(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH,1);
        item.setEndDate(calendar.getTime());
        item.setDescription(String.valueOf(checklist.getCreationDatestamp()));
        return item;
    }


    public abstract class OnCalendarDateSetListener implements Button.OnClickListener {


        public void onClick(View view) {

            AgendaAdapter adapter = (AgendaAdapter) calendarGrid.getAdapter();
            if(insertCheck.isChecked()){
                if(checklist.getCalendarItem() != null){
                    //update or if equal leave
                    CalendarItem item = pollForExistingCalendarEntry(checklist);
                    if(item != null){

                        CalendarItem updateTarget = createCalendarItemFromUIValues();
                        if(updateTarget.hasChangedFields(item)) {
                            updateTarget.setId(item.getId());//adding the id from the sqlite calendar contentprovider db
                            checklist.setCalendarItem(updateTarget);
                            updateTarget.setDirty(true);

//                            handler.updateCalendarItem(getContext(),updateTarget);
                            Toast.makeText(getContext(),"Your calendar: \""+adapter.getCalendarName(updateTarget.getCalendarId())
                                    +"\" has been updated",Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(getContext(),"Your calendar: \""+adapter.getCalendarName(item.getCalendarId())
                                    +"\" was not changed",Toast.LENGTH_LONG).show();
                        }

                    }else {
                        Toast.makeText(getContext(),"Synchronization issue found",Toast.LENGTH_LONG).show();
                    }

                    //todo equals and if not delete old entry then add new if in
                }else {
                    //new entry
                    CalendarItem item = createCalendarItemFromUIValues();
                    checklist.setCalendarItem(item);
//                    handler.insertCalendarItem(getContext(),item);
                    Toast.makeText(getContext(),"Your calendar:\""+adapter.getCalendarName(item.getCalendarId())
                            +"\" has been updated",Toast.LENGTH_LONG).show();
                }


            }else if(checklist.getCalendarItem() != null){
                //there is a calendar item registered but we don't want it anymore
                handler.deleteCalendarItem(getContext(),checklist.getCalendarItem());
                String calName = adapter.getCalendarName(checklist.getCalendarItem().getCalendarId());
                Toast.makeText(getContext(),"calendar item with id: "+checklist.getCalendarItem().getId()+" " +
                        "has been deleted from "+calName,Toast.LENGTH_LONG).show();
                checklist.setCalendarItem(null); //target was deleted
            }


            onDateSet(datepicker,datepicker.getYear(),datepicker.getMonth(),datepicker.getDayOfMonth(),
                    timePicker.getCurrentHour(),timePicker.getCurrentMinute());  //needs to trigger save
        }



        public abstract void onDateSet(android.widget.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth, int hour, int minute);
    }


}
