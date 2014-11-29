package com.example.flyboyz.rotoroute;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;


public class MapActivity extends Activity {
    private static final String[] BUILDINGS = new String[] {
            "CSIC", "Hornbake", "Stamp Student Union", "AVW", "EGR"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        String destination = getIntent().getStringExtra("destination");
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.mapDestination);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, BUILDINGS);
        textView.setAdapter(adapter);
        textView.setText(destination);

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean foundMatch = false;
                for (String str: BUILDINGS) {
                    if (str.equals(s)) {
                        foundMatch = true;
                    }
                }

                Button button = (Button) findViewById(R.id.destSearch);
                if (foundMatch) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMap(View view) {
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.destination);
        String dest = textView.getText().toString();


    }
}
