package pt.ulisboa.tecnico.locmess;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;


public class TimePicker extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    Calendar myDate;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //get current time an use it as default
        if (myDate == null)
             myDate = Calendar.getInstance();
        int hour = myDate.get(Calendar.HOUR_OF_DAY);
        int min = myDate.get(Calendar.MINUTE);
        //TODO chose one



        //Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, min,
                DateFormat.is24HourFormat(getActivity()));
    }


    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        myDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        myDate.set(Calendar.MINUTE,minute);
        ((TimePickerCallback)getActivity()).onSetTime(hourOfDay,minute);

    }

    public static interface TimePickerCallback {
        void onSetTime(int hourOfDay, int minute);
    }

    public Calendar getDate(){
        return myDate;
    }

    public void setDate(Calendar date){
        myDate = date;
    }

}
