package com.mikeaschumacher.tasky;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michael on 1/30/2018.
 */


public class AddDialog extends Dialog {

    private static final int DILOG_ID = 999;
    public Activity activity;
    public Dialog dialog;
    public Button color, cancel, add;

    Date setDate;

    String name;

    public AddDialog(Activity a) {
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.addition_dialog);

        //assign buttons to id in layout
        color = findViewById(R.id.colorBtn);
        cancel = findViewById(R.id.cancelBtn);
        add = findViewById(R.id.addBtn);

    }
}
