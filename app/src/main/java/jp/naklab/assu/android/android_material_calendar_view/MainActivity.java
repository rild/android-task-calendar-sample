package jp.naklab.assu.android.android_material_calendar_view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    CalendarView calendarView;
    HashSet<Date> eventDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendar_view);

        initEventHandler();
        initDummyEvent();
    }

    private void initDummyEvent() {
        eventDays = new HashSet<Date>();
        eventDays.add(new Date());

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String dateInString = "05-06-2019";
            Date date = sdf.parse(dateInString);
            eventDays.add(date);
        } catch (ParseException e) {

        }
        calendarView.updateCalendar(eventDays);
    }

    private void initEventHandler() {
        calendarView.setOnDayLongClickListener(new CalendarView.OnDayLongClickListener() {
            @Override
            public void onDayLongClick(Date date) {
                Toast.makeText(MainActivity.this, date.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
