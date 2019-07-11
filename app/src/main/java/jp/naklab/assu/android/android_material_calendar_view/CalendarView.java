package jp.naklab.assu.android.android_material_calendar_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class CalendarView extends LinearLayout {
    // for logging
    private static final String LOGTAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";

    // date format
    private String dateFormat;

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();

    //event handling
    private OnDayLongClickListener onDayLongClickListener = null;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;
    CalendarAdapter adapter;

    // seasons' rainbow
    int[] rainbow = new int[]{
            R.color.summer,
            R.color.fall,
            R.color.winter,
            R.color.spring
    };

    // month-season association (northern hemisphere, sorry australia :)
    int[] monthSeason = new int[]{2, 2, 3, 3, 3, 0, 0, 0, 1, 1, 1, 2};

    public CalendarView(Context context) {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initControl(context, attrs);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);

        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();

        updateCalendar();
    }

    private void loadDateFormat(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);

        try {
            // try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.CalendarView_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;
        } finally {
            ta.recycle();
        }
    }

    private void assignUiElements() {
        // layout is inflated, assign local variables to components
        header = (LinearLayout) findViewById(R.id.calendar_header);
        btnPrev = (ImageView) findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView) findViewById(R.id.calendar_next_button);
        txtDate = (TextView) findViewById(R.id.calendar_date_display);
        grid = (GridView) findViewById(R.id.calendar_grid);
    }

    private void assignClickHandlers() {
        // add one month and refresh UI
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        // subtract one month and refresh UI
        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });

        // long-pressing a day
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> view, View cell, int position, long id) {
                // handle long-press
                if (onDayLongClickListener == null)
                    return false;

                onDayLongClickListener.onDayLongClick((Date) view.getItemAtPosition(position));
                return true;
            }
        });
    }

    // Activity から体重のデータをもらえるようにする
    public void setWeightData(HashMap<String, Integer> hashMap) {
        // adapter の体重データを更新する
        adapter.setWeightData(hashMap);
        // adapter にデータセットが変わったことを知らせる: getView を実行させる
        adapter.notifyDataSetChanged();
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar() {
        updateCalendar(null);
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar(HashSet<Date> events) {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar) currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        adapter = new CalendarAdapter(getContext(), cells, events);
        // update grid
        grid.setAdapter(adapter);

        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        txtDate.setText(sdf.format(currentDate.getTime()));

        // set header color according to current season
        int month = currentDate.get(Calendar.MONTH);
        int season = monthSeason[month];
        int color = rainbow[season];

        header.setBackgroundColor(getResources().getColor(color));
    }


    private class CalendarAdapter extends ArrayAdapter<Date> {
        // days with events
        private HashSet<Date> eventDays;

        // 体重のデータを Adapter に持たせる
        //  例:  "Thu Jul 18 21:19:30 GMT+09:00 2019" で 59 が保存される
        //      date.toString() で、 int を保存する
        private HashMap<String, Integer> weightData;
        // 体重のデータがない時
        private final int NO_WEIGHT_DATA = -1;

        // 体重のデータを渡せるようにする

        public void setWeightData(HashMap<String, Integer> weightData) {
            this.weightData = weightData;
        }
        public int getWeight(String dateString) {
            if (weightData == null) return NO_WEIGHT_DATA;
            // HashMap に keyValue のデータがない時 null が帰ってくる
            if (weightData.get(dateString) == null) return NO_WEIGHT_DATA;

            // 上の２つの場合以外は weightData から 日付で体重を 取り出せる
            return weightData.get(dateString);
        }

        // for view inflation
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays) {
            super(context, R.layout.control_calendar_day, days);
            this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            // day in question
            Date date = getItem(position);
            int day = date.getDate();
            int month = date.getMonth();
            int year = date.getYear();

            // today
            Date today = new Date();

            // inflate item if it does not exist yet
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.control_calendar_day, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // if this day has an event, specify event image
//            convertView.setBackgroundResource(0);
//            if (eventDays != null) {
//                for (Date eventDate : eventDays) {
//                    if (eventDate.getDate() == day &&
//                            eventDate.getMonth() == month &&
//                            eventDate.getYear() == year) {
//                        // mark this day for event
////                        convertView.setBackgroundResource(R.drawable.reminder);
//                        viewHolder.imageViewEventTag.setBackgroundColor(getResources().getColor(R.color.colorAccent));
//                        break;
//                    }
//                }
//            }


            // clear styling
            viewHolder.textViewDay.setTypeface(null, Typeface.NORMAL);
            viewHolder.textViewDay.setTextColor(Color.BLACK);

            if (month != today.getMonth() || year != today.getYear()) {
                // if this day is outside current month, grey it out
                viewHolder.textViewDay.setTextColor(getResources().getColor(R.color.greyed_out));
            } else if (day == today.getDate()) {
                // if it is today, set it to blue/bold
                viewHolder.textViewDay.setTypeface(null, Typeface.BOLD);
                viewHolder.textViewDay.setTextColor(getResources().getColor(R.color.today));
            }
            // set text
            viewHolder.textViewDay.setText(String.valueOf(date.getDate()));


            // カレンダーに体重を表示させる
            int weight = adapter.getWeight(date.toString());
            if (weight != NO_WEIGHT_DATA) {
                String weightString = String.valueOf(weight);
                viewHolder.textViewWeight.setText(weightString);

                // 体重が入力された日にタグをつける
                viewHolder.imageViewEventTag.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }


            return convertView;
        }

        class ViewHolder {
            TextView textViewDay;
            ImageView imageViewEventTag;
            TextView textViewWeight;

            ViewHolder(View v) {
                textViewDay = v.findViewById(R.id.textview_day);
                imageViewEventTag = v.findViewById(R.id.imageview_eventtag);
                textViewWeight = v.findViewById(R.id.textview_weight);
            }
        }
    }

    /**
     * Assign event handler to be passed needed events
     */
    public void setOnDayLongClickListener(OnDayLongClickListener l) {
        this.onDayLongClickListener = l;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface OnDayLongClickListener {
        void onDayLongClick(Date date);
    }
}
