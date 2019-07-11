package jp.naklab.assu.android.android_material_calendar_view;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    CalendarView calendarView;
    HashSet<Date> eventDays;

    // 体重のデータ
    // TODO あとで保存する必要がある
    HashMap<String, Integer> hashMap = new HashMap<>();

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
                // 日付が長押しされたら入力ダイアログを開く
                createInputDialog(date);
            }
        });
    }


    // 入力するダイアログの表示
    AlertDialog mDialog;
    private void createInputDialog(Date date) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate( R.layout.input_dialog, null );

        EditText weightInput = (EditText) view.findViewById(R.id.edittext_input);

        Button btnPositive = (Button) view.findViewById(R.id.button_dialog_positive);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText から体重のデータを int 型に変換して hashMap に保存する
                // その時の鍵 (keyValue) は String型の日付
                int weight = Integer.parseInt(weightInput.getText().toString());
                hashMap.put(date.toString(), weight);
                calendarView.setWeightData(hashMap);

                // ダイアログを閉じる
                mDialog.dismiss();
            }
        });

        mDialog = new AlertDialog.Builder(MainActivity.this)
                .setView( view )
                .create();
        mDialog.show();
    }
}
