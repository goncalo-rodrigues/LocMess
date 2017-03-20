package pt.ulisboa.tecnico.locmess;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;



public class DatePicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    Calendar myDate;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // get current date
        if (myDate == null)
            myDate = Calendar.getInstance();

        int day = myDate.get(Calendar.DAY_OF_MONTH);
        int month = myDate.get(Calendar.MONTH);
        int year = myDate.get(Calendar.YEAR);



        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);

    }


    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        myDate.set(year,month,dayOfMonth);
    }

    public Calendar getDate(){
        return myDate;
    }

    public void setDate(Calendar date){
        myDate = date;
    }
}