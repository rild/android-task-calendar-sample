package jp.naklab.assu.android.android_material_calendar_view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

        calendarView.updateCalendar(eventDays);
    }

    private void initEventHandler() {
        calendarView.setOnDayLongClickListener(new CalendarView.OnDayLongClickListener() {
            @Override
            public void onDayLongClick(Date date) {

            }
        });
    }
}
