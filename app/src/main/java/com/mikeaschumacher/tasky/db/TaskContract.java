package com.mikeaschumacher.tasky.db;

import android.provider.BaseColumns;

public class TaskContract {
    public static final String DB_NAME = "package todo.shilohsoftware.com.todo.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String COL_DAY_TITLE = "day";
        public static final String COL_MONTH_TITLE = "month";
        public static final String COL_TASK_TITLE = "title";
        public static final String COL_YEAR_TITLE = "year";
        public static final String COL_COLOR_TITLE = "color";
        public static final String TABLE = "tasks";
    }
}
