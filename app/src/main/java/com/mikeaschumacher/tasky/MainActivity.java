package com.mikeaschumacher.tasky;

import android.annotation.SuppressLint;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //resources for populating list view
    private ArrayAdapter<TaskItem> itemsAdapter;
    private ListView lvItems;
    private ArrayList<TaskItem> items;
    static final int DILOG_ID = 999;
    Calendar c = Calendar.getInstance();

    //resources for tracking date of added items
    int dayX = this.c.get(Calendar.DAY_OF_MONTH);
    int monthX = (this.c.get(Calendar.MONTH) + 1);
    int yearX = this.c.get(Calendar.YEAR);

    //resources for creating and modifying custom calendar
    CalendarView cv;
    HashSet<Date> events = new HashSet<>();

    //Related to SQLite database
    private TaskDbHelper mHelper;
    private String tempTaskName;
    private Color tempTaskColor =

    //used for changing the hint of the EditText in the "add" dialog
    String hints[] = new String[]{
            "The best way to predict your future is to create it." + "\n" + "\n" + "Unknown",
            "If your ship doesn't come in, swim out to it." + "\n" + "\n" + "Johnathan Winters",
            "Do, or do not. There is no 'try'." + "\n" + "\n" + "Yoda",
            "Motivation is what gets you started. Habit is what keeps you going." + "\n" + "\n" + "Jim Ryun",
            "You will never find time for anything. If you want time you must make it." + "\n" + "\n" + "Charles Buxton",
            "If we did all we were capable of doing, we would literally astonish ourselves." + "\n" + "\n" + "Thomas Edison",
            "Will you look back on life and say, \"I wish I had,\" or \"I'm glad I did\"?" + "\n" + "\n" + "Zig Ziglar",
            "You miss 100% of the shots you don’t take." + "\n" + "Wayne Gretzky -Michael Scott",
            "The most difficult thing is the decision to act, the rest is merely tenacity." + "\n" + "\n" + " Amelia Earhart",
            "Definiteness of purpose is the starting point of all achievement." + "\n" + "\n" + "W. Clement Stone",
            "We become what we think about." + "\n" + "Earl Nightingale",
            "The best time to plant a tree was 20 years ago. The second best time is now." + "\n" + "\n" + "Chinese Proverb",
            "Your time is limited, so don’t waste it living someone else’s life." + "\n" + "\n" + "Steve Jobs",
            "Either you run the day, or the day runs you." + "\n" + "\n" + "Jim Rohn",
            "The best revenge is massive success." + "\n" + "\n" + "Frank Sinatra",
            "Believe you can and you’re halfway there." + "\n" + "\n" + "Theodore Roosevelt",
            "Happiness is not something readymade. It comes from your own actions." + "\n" + "\n" + "Dalai Lama",
            "If the wind will not serve, take to the oars." + "\n" + "\n" + "Latin Proverb",
            "Too many of us are not living our dreams because we are living our fears." + "\n" + "\n" + "Les Brown",
            "A person who never made a mistake never tried anything new." + "\n" + "\n" + "Albert Einstein",
            "I would rather die of passion than of boredom." + "\n" + "\n" + "Vincent van Gogh",
            "It does not matter how slowly you go as long as you do not stop." + "\n" + "\n" + "Confucius"
    };

    LayoutInflater inflater;

    boolean dateChange = false;

    TaskItem currentTask;

    //listener for Date Selection, stores the date selected by used and calls addItem
    private DatePickerDialog.OnDateSetListener myDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            MainActivity.this.yearX = year;
            MainActivity.this.monthX = month + 1;
            MainActivity.this.dayX = dayOfMonth;
            view.updateDate(MainActivity.this.c.get(Calendar.YEAR), MainActivity.this.c.get(Calendar.MONTH), MainActivity.this.c.get(Calendar.DAY_OF_MONTH));
            if (!dateChange)
                MainActivity.this.onAddItem();
            else
                changeDate();
            //reset dateChange checker
            dateChange = false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inflater = this.getLayoutInflater();

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

        //listen for touch and hold to remove item from list
        this.lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View item, int pos, long id) {
                removeTask(items.get(pos));
                return true;
            }
        });

        this.lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                //creating dialog
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                final View dialogView = inflater.inflate(R.layout.edit_task, null);
                dialogBuilder.setView(dialogView);

                //create button to listen for click
                Button save = dialogView.findViewById(R.id.save);
                final Button changeDate = dialogView.findViewById(R.id.editDate);

                //create EditText to modify hint, get user input
                final EditText mEdit = dialogView.findViewById(R.id.editEntry);
                mEdit.setHintTextColor(getResources().getColor(R.color.hintColor));
                mEdit.setHint("Change Task Name");

                //change text of the title to task name
                TextView header = dialogView.findViewById(R.id.editTitle);
                header.setText(items.get(position).taskName);

                //create dialog
                final AlertDialog alertDialog = dialogBuilder.create();

                final boolean[] intentToChangeName = {false};

                //listen for touch on EditText to change the font size,
                //  make cursor visible
                mEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEdit.setTextSize(20);
                        mEdit.setSingleLine();
                        mEdit.setHint("New Task Name");
                        mEdit.setCursorVisible(true);
                        intentToChangeName[0] = true;
                    }
                });

                //handle "add" button click
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //the user clicked the editText, assume name was changed
                        if (intentToChangeName[0]) {

                            //check to see if new name was left empty
                            if (mEdit.getText().toString().equals("")) {

                                //name was left empty, show alert
                                Toast.makeText(getBaseContext(), "Task Needs a New Name", Toast.LENGTH_LONG).show();
                            }
                            else {
                                items.get(position).taskName = mEdit.getText().toString();
                            }
                        }

                        alertDialog.cancel();
                    }
                });

                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dateChange = true;
                        currentTask = items.get(position);
                        MainActivity.this.showDialog(MainActivity.DILOG_ID);
                        //alertDialog.cancel();
                    }
                });

                alertDialog.show();
            }
        });
    }

    //create dialog for date picker
    protected Dialog onCreateDialog(int id) {
        if (id != DILOG_ID)
            return null;

        return new DatePickerDialog(this, this.myDatePickerListener, this.c.get(Calendar.YEAR), this.c.get(Calendar.MONTH), this.c.get(Calendar.DAY_OF_MONTH));
    }

    //open "addition_dialog" to get info from user
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public void onDateClick(View v) throws InterruptedException {

        //creating dialog
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.addition_dialog, null);
        dialogBuilder.setView(dialogView);

        //create button to listen for click
        Button add = dialogView.findViewById(R.id.addBtn);
        Button color = dialogView.findViewById(R.id.colorBtn);
        Button cancel = dialogView.findViewById(R.id.cancelBtn);

        //create EditText to modify hint, get user input
        final EditText mEdit = dialogView.findViewById(R.id.alertEntry);
        mEdit.setHintTextColor(getResources().getColor(R.color.hintColor));
        Random r = new Random();
        mEdit.setHint(hints[r.nextInt(hints.length)]);
        mEdit.setTextSize(16);

        //create dialog
        final AlertDialog alertDialog = dialogBuilder.create();

        //listen for touch on EditText to change the font size,
        //  make cursor visible
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdit.setTextSize(20);
                mEdit.setHint("Task Name");
                mEdit.setCursorVisible(true);
            }
        });

        //handle "add" button click
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tempTaskName = mEdit.getText().toString();
                if (!tempTaskName.equals("")) {
                    MainActivity.this.showDialog(MainActivity.DILOG_ID);
                    alertDialog.cancel();
                }
                else {
                    Toast.makeText(getBaseContext(), "Task Needs a Name", Toast.LENGTH_LONG).show();
                }

            }
        });

        color.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //creating dialog
                final View dialogView = inflater.inflate(R.layout.pick_color, null);
                dialogBuilder.setView(dialogView);
                AlertDialog colorDialog = dialogBuilder.create();

                ImageView colorOne = findViewById(R.id.circle_one);
                ImageView colorTwo = findViewById(R.id.circle_two);
                ImageView colorThree = findViewById(R.id.circle_three);
                ImageView colorFour = findViewById(R.id.circle_four);
                ImageView colorFive = findViewById(R.id.circle_five);
                ImageView colorSix = findViewById(R.id.circle_six);
                ImageView colorSeven = findViewById(R.id.circle_seven);
                ImageView colorEight = findViewById(R.id.circle_eight);


                dialogBuilder.show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }

    //add item to the database, refresh calendar view
    public void onAddItem() {

        SQLiteDatabase db = this.mHelper.getWritableDatabase();

        //create task to be added
        TaskItem toAdd = new TaskItem(this.yearX, this.monthX, this.dayX, this.tempTaskName);

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COL_TASK_TITLE, toAdd.taskName);
        values.put(TaskContract.TaskEntry.COL_DAY_TITLE, Integer.valueOf(toAdd.dueDay));
        values.put("month", Integer.valueOf(toAdd.dueMonth));
        values.put("year", Integer.valueOf(toAdd.dueYear));
        db.insert(TaskContract.TaskEntry.TABLE, null, values);
        db.close();

        //reset tempTaskName to avoid garbage values
        this.tempTaskName = "";

        this.itemsAdapter.add(toAdd);

        //refresh calendar to add event to day
        events.add(toAdd.getDate());
        cv.setEvents(events);

        //reorder the items on the listview
        orderTasks();
    }

    //Reorders the tasks in arraylist in closest to furthest date order
    public void orderTasks() {

        //bubble sort
        int i;
        ArrayList<TaskItem> temp = new ArrayList();
        int indexTracker = this.items.size();

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

    private void changeDate() {

        removeTask(currentTask);

        tempTaskName = currentTask.taskName;

        onAddItem();
    }

    private void removeTask(TaskItem task) {

        //get database
        SQLiteDatabase db = MainActivity.this.mHelper.getWritableDatabase();
        //remove item from database
        db.delete(TaskContract.TaskEntry.TABLE, "title = ?", new String[]{(task).taskName});
        db.close();
        //remove item from list adapter
        MainActivity.this.items.remove(task);

        //remove item from calendar
        events.clear();
        for (TaskItem currentTask : items) {
            events.add(currentTask.getDate());
        }
        cv.setEvents(events);

        MainActivity.this.itemsAdapter.notifyDataSetChanged();
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
