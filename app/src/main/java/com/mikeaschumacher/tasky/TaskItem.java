package com.mikeaschumacher.tasky;

/**
 * Created by Michael on 1/26/2018.
 *
 * Structure for storing fields related to tasks
 */

import java.util.Date;
import java.util.GregorianCalendar;

public class TaskItem {
    public int dueDay;
    public int dueMonth;
    public int dueYear;
    public Date taskDateCreation;
    public String taskName;
    int color;

    public TaskItem(int yearIn, int monthIn, int dayIn, String name, int color) {
        this.taskName = name;
        this.dueYear = yearIn;
        this.dueMonth = monthIn;
        this.dueDay = dayIn;
        this.taskName = name;
        this.taskDateCreation = new GregorianCalendar(yearIn, monthIn - 1, dayIn).getTime();
        this.color = color;
    }

    public Date getDate() {
        Date temp = new Date();
        temp.setDate(dueDay);
        temp.setMonth(dueMonth - 1);
        temp.setYear(dueYear - 1900);

        return temp;
    }

    //returns the desired string format:
    //  *NAME* due *MM/DD/YYYY*
    public String toString() {
        return this.taskName + "       due       " + this.dueMonth + "-" + this.dueDay + "-" + this.dueYear;
    }
}
