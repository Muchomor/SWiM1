package pl.edu.pwr.lab1zimny.lab1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {
    @Bind(R.id.calculate)
    Button calculate;
    @Bind(R.id.massInput)
    EditText massInput;
    @Bind(R.id.heightInput)
    EditText heightInput;
    @Bind(R.id.resultString)
    TextView resultString;
    @Bind(R.id.result)
    TextView result;
    @Bind(R.id.metricsSpinner)
    Spinner metricsSpinner;

    ICountBMI BMICounter;
    String actualBMI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.metricsSpinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        metricsSpinner.setAdapter(adapter);

        loadBMI();

        BMICounter = new CountBMIForKGM();

        metricsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BMICounter = position==0? new CountBMIForKGM() : new CountBMIForLBIN();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //it will never happen, because application starts with first option selected
            }
        });
    }

    /*
    Method for calculate button on click. Calculates BMI, sets resultString visible,
    sets value and color of result.
    If one of params are invalid, sets error text.
     */
    @OnClick(R.id.calculate)
    public void submit() {
        closeSoftKeyboard();

        float res = 0;
        try {
            float mass = Float.parseFloat(massInput.getText().toString());
            float height = Float.parseFloat(heightInput.getText().toString());
            res = BMICounter.calculateBMI(mass, height);
            actualBMI = String.valueOf(res);
            resultString.setVisibility(View.VISIBLE);
            colorOfText(res, result);
            result.setText(actualBMI);
        } catch (IllegalArgumentException iae) {
            setErrorText();
        }
    }

    /*
    Sets color of result text depending on BMI value

    @param BMI  calculatedBMI
    @param t    text view of which color we want to set
     */
    private void colorOfText(float BMI, TextView t) {
        if (BMI <= 24.9) {
            t.setTextColor(Color.GREEN);
        } else if (BMI <= 29.9) {
            t.setTextColor(Color.parseColor("#e5e500"));
        } else {
            t.setTextColor(Color.RED);
        }
    }

    /*
    Closes soft keyboard when calculate button is clicked.
     */
    private void closeSoftKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /*
    Sets text of result view.
     */
    private void setErrorText() {
        resultString.setVisibility(View.VISIBLE);
        result.setTextColor(Color.BLACK);
        result.setText(R.string.wrongInput);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("MyMass", massInput.getText().toString());
        savedInstanceState.putString("MyHeight", heightInput.getText().toString());
        savedInstanceState.putString("MyBMI", result.getText().toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        String mass = savedInstanceState.getString("MyMass");
        String height = savedInstanceState.getString("MyHeight");
        String savedBMI = savedInstanceState.getString("MyBMI");

        massInput.setText(mass);
        heightInput.setText(height);
        result.setText(savedBMI);
        resultString.setVisibility(View.VISIBLE);
        if(!savedBMI.equals("It is impossible!")) {
            colorOfText(Float.valueOf(savedBMI), result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /*

     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveBMI();
                return true;
            case R.id.share:
                share();
                return true;
            case R.id.author:
                startActivity(new Intent(this, AboutAuthor.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    Saves actual values in text views to shared preferences.
     */
    private void saveBMI() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("mass", massInput.getText().toString());
        editor.putString("height", heightInput.getText().toString());
        editor.putString("BMI", result.getText().toString());
        editor.putInt("actualSpinnerState", metricsSpinner.getSelectedItemPosition());
        editor.apply();
        Context context = getApplicationContext();
        CharSequence text = "Saved";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /*
    Loads saved shared preferences.
     */
    private void loadBMI() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String BMI = preferences.getString("BMI", "");
        String mass = preferences.getString("mass", "");
        String height = preferences.getString("height", "");
        int spinnerPosition = preferences.getInt("actualSpinnerState",0);
        System.out.println(spinnerPosition);
        massInput.setText(mass);
        heightInput.setText(height);
        result.setText(BMI);
        metricsSpinner.setSelection(spinnerPosition);
        if (BMI.length() > 0 && !BMI.equals("It is impossible!")) {
            colorOfText(Float.valueOf(BMI), result);
        }
    }

    /*
    Shares BMI value from result view via accessible, installed applications.
     */
    private void share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Hey, take a look at my BMI: " + result.getText().toString();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My BMI");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}


