package com.mikeaschumacher.tasky;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikeaschumacher.tasky.db.TaskContract;
import com.mikeaschumacher.tasky.db.TaskDbHelper;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity {

    //resources for populating list view
    private ArrayAdapter<TaskItem> itemsAdapter;
    private ListView lvItems;
    private ArrayList<TaskItem> items;
    static final int DILOG_ID = 999;
    Calendar c = Calendar.getInstance();

    //resources for tracking date of added items
    Date today = new Date();
    int dayX = this.c.get(Calendar.DAY_OF_MONTH);
    int monthX = (this.c.get(Calendar.MONTH) + 1);
    int yearX = this.c.get(Calendar.YEAR);

    //resources for creating and modifying custom calendar
    CalendarView cv;
    HashSet<Date> events = new HashSet<>();

    //Related to SQLite database
    private TaskDbHelper mHelper;
    private String tempTaskName;

    private DatePickerDialog.OnDateSetListener myDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            MainActivity.this.yearX = year;
            MainActivity.this.monthX = month + 1;
            MainActivity.this.dayX = dayOfMonth;
            Toast.makeText(MainActivity.this, "Task Added: " + MainActivity.this.monthX + " / " + MainActivity.this.dayX + " / " + MainActivity.this.yearX, Toast.LENGTH_SHORT).show();
            view.updateDate(MainActivity.this.c.get(Calendar.YEAR), MainActivity.this.c.get(Calendar.MONTH), MainActivity.this.c.get(Calendar.DAY_OF_MONTH));
            MainActivity.this.onAddItem();
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SQLite setup
        this.mHelper = new TaskDbHelper(this);
        SQLiteDatabase db = this.mHelper.getReadableDatabase();
        db.query(TaskContract.TaskEntry.TABLE, new String[]{TaskContract.TaskEntry.COL_TASK_TITLE}, null, null, null, null, null).close();
        db.close();
        this.lvItems = (ListView) findViewById(R.id.lvItems);
        this.items = new ArrayList();

        //processes values from storage, changes calendar cell color
        readItems();

        this.itemsAdapter = new ArrayAdapter<TaskItem>(this, android.R.layout.simple_list_item_2, android.R.id.text1, this.items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                if (MainActivity.this.items.size() == 0) {
                    text1.setText("");
                    text2.setText("");
                } else {
                    text1.setText(((TaskItem) MainActivity.this.items.get(position)).taskName);
                    text2.setText(new SimpleDateFormat("MMM d, yyyy").format(((TaskItem) MainActivity.this.items.get(position)).taskDateCreation));
                }
                text1.setTextSize(17);
                text2.setTextSize(12);
                return view;
            }
        };
        this.lvItems.setAdapter(this.itemsAdapter);

        orderTasks();
        setupListViewListener();

        cv = ((CalendarView) findViewById(R.id.calendar_view));
        cv.setEvents(events);

        // assign event handler
        cv.setEventHandler(new CalendarView.EventHandler() {
            @Override
            public void onDayLongPress(Date date) {
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(MainActivity.this, df.format(date), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Attaches a long click listener to the listview
    //Long click used for removing item from list and from database
    private void setupListViewListener() {
        this.lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View item, int pos, long id) {
                SQLiteDatabase db = MainActivity.this.mHelper.getWritableDatabase();
                db.delete(TaskContract.TaskEntry.TABLE, "title = ?", new String[]{((TaskItem) MainActivity.this.items.get(pos)).taskName});
                db.close();
                Date date = new GregorianCalendar(((TaskItem) MainActivity.this.items.get(pos)).dueYear, ((TaskItem) MainActivity.this.items.get(pos)).dueMonth - 1, ((TaskItem) MainActivity.this.items.get(pos)).dueDay).getTime();
                TaskItem tempTask = new TaskItem(((TaskItem) MainActivity.this.items.get(pos)).dueYear, ((TaskItem) MainActivity.this.items.get(pos)).dueMonth, ((TaskItem) MainActivity.this.items.get(pos)).dueDay, ((TaskItem) MainActivity.this.items.get(pos)).taskName);
                MainActivity.this.items.remove(pos);

                boolean secondTask = false;
                Iterator it = MainActivity.this.items.iterator();
                while (it.hasNext()) {
                    TaskItem task = (TaskItem) it.next();
                    if (tempTask.dueDay == task.dueDay && tempTask.dueMonth == task.dueMonth && tempTask.dueYear == task.dueYear) {
                        secondTask = true;
                    }
                }

                events.clear();

                for (TaskItem task : items) {
                    events.add(task.getDate());
                }

                cv.setEvents(events);

                MainActivity.this.itemsAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    protected Dialog onCreateDialog(int id) {
        if (id != DILOG_ID) {
            return null;
        }
        return new DatePickerDialog(this, this.myDatePickerListener, this.c.get(Calendar.YEAR), this.c.get(Calendar.MONTH), this.c.get(Calendar.DAY_OF_MONTH));
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public void onDateClick(View v) throws InterruptedException {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.addition_dialog, null);
        dialogBuilder.setView(dialogView);

        Button add = dialogView.findViewById(R.id.addBtn);

        final EditText mEdit = dialogView.findViewById(R.id.alertEntry);

        final AlertDialog alertDialog = dialogBuilder.create();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), mEdit.getText(), Toast.LENGTH_SHORT).show();
                tempTaskName = mEdit.getText().toString();
                MainActivity.this.showDialog(MainActivity.DILOG_ID);
                alertDialog.cancel();
            }
        });

        alertDialog.show();


        /*final EditText mEdit = new EditText(this);
        mEdit.setSingleLine();
        mEdit.setInputType(16385);
        new AlertDialog.Builder(this).setTitle((CharSequence) "Add A New Task").setView(mEdit).setPositiveButton((CharSequence) "Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mEdit.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Enter a Task Name", Toast.LENGTH_SHORT).show();
                }
                MainActivity.this.tempTaskName = mEdit.getText().toString();
                MainActivity.this.showDialog(MainActivity.DILOG_ID);
            }
        }).setNegativeButton((CharSequence) "Cancel", null).create().show();*/
    }

    public void onAddItem() {

        SQLiteDatabase db = this.mHelper.getWritableDatabase();
        TaskItem toAdd = new TaskItem(this.yearX, this.monthX, this.dayX, this.tempTaskName);
        this.tempTaskName = "";
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COL_TASK_TITLE, toAdd.taskName);
        values.put(TaskContract.TaskEntry.COL_DAY_TITLE, Integer.valueOf(toAdd.dueDay));
        values.put("month", Integer.valueOf(toAdd.dueMonth));
        values.put("year", Integer.valueOf(toAdd.dueYear));
        db.insert(TaskContract.TaskEntry.TABLE, null, values);
        db.close();
        this.itemsAdapter.add(toAdd);

        events.add(toAdd.getDate());
        cv.setEvents(events);

        orderTasks();
    }

    //Reorders the tasks in arraylist in closest to furthest date order
    public void orderTasks() {

        int i;
        ArrayList<TaskItem> temp = new ArrayList();
        int indexTracker = this.items.size();

        //
        for (i = 0; i < indexTracker; i++) {
            temp.add(this.items.remove(0));
        }

        for (i = temp.size(); i > 0; i--) {
            for (int j = 0; j < i - 1; j++) {
                if (compareTasks((TaskItem) temp.get(j), (TaskItem) temp.get(j + 1)) == -1) {
                    TaskItem tempItem = (TaskItem) temp.get(j);
                    temp.set(j, temp.get(j + 1));
                    temp.set(j + 1, tempItem);
                }
            }
        }
        Iterator it = temp.iterator();
        while (it.hasNext()) {
            this.items.add((TaskItem) it.next());
        }
    }

    //read tasks from the SQLite database
    private void readItems() {
        this.items = new ArrayList();
        Cursor curseYou = this.mHelper.getReadableDatabase().query(TaskContract.TaskEntry.TABLE, new String[]{TaskContract.TaskEntry.COL_TASK_TITLE, TaskContract.TaskEntry.COL_DAY_TITLE, "month", "year"}, null, null, null, null, null);
        while (curseYou.moveToNext()) {
            TaskItem toAdd = new TaskItem(curseYou.getInt(3), curseYou.getInt(2), curseYou.getInt(1), curseYou.getString(0));
            this.items.add(toAdd);
        }

        for (TaskItem task : items) {
            events.add(task.getDate());
        }

        orderTasks();
    }

    //compares two tasks and returns an int based on their due date
    public int compareTasks(TaskItem obj1, TaskItem obj2) {
        if (obj1.dueYear > obj2.dueYear) {
            return -1;
        }
        if (obj1.dueYear != obj2.dueYear) {
            return 1;
        }
        if (obj1.dueMonth > obj2.dueMonth) {
            return -1;
        }
        if (obj1.dueMonth != obj2.dueMonth) {
            return 1;
        }
        if (obj1.dueDay > obj2.dueDay) {
            return -1;
        }
        if (obj1.dueDay == obj2.dueDay) {
            return 0;
        }
        return 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
