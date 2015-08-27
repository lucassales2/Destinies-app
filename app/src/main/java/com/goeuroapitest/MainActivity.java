package com.goeuroapitest;

import android.app.DatePickerDialog;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nineoldandroids.animation.Animator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest;
    private DestinyAdapter mFromAdapter, mToAdapter;
    private int departureDay, departureMounth, departureYear;
    private int returnDay, returnMounth, returnYear;
    private TextView departureTextView;
    private TextView returnTextView;
    private AutoCompleteTextView fromAutoComplete;
    private AutoCompleteTextView toAutoComplete;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromAutoComplete = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        toAutoComplete = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.returnDatePicker).setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        });

        searchButton = (Button) findViewById(R.id.search_button);
        departureTextView = (TextView) findViewById(R.id.departure_date);
        returnTextView = (TextView) findViewById(R.id.return_date);

        mFromAdapter = new DestinyAdapter(this, new ArrayList<Destiny>());
        mToAdapter = new DestinyAdapter(this, new ArrayList<Destiny>());
        fromAutoComplete.setAdapter(mFromAdapter);
        toAutoComplete.setAdapter(mToAdapter);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        departureYear = calendar.get(Calendar.YEAR);
        departureMounth = calendar.get(Calendar.MONTH);
        departureDay = calendar.get(Calendar.DAY_OF_MONTH);
        returnYear = calendar.get(Calendar.YEAR);
        returnMounth = calendar.get(Calendar.MONTH);
        returnDay = calendar.get(Calendar.DAY_OF_MONTH);

        updateDepartureDate();
        updateReturnDate();
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.search_not_implemented, Toast.LENGTH_LONG).show();
            }
        });

        departureTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        departureDay = dayOfMonth;
                        departureMounth = monthOfYear;
                        departureYear = year;
                        returnYear = year;
                        returnDay = dayOfMonth;
                        returnMounth = monthOfYear;
                        updateDepartureDate();
                        updateReturnDate();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMinDate(calendar.getTime().getTime());
                dialog.show();
            }
        });

        returnTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(departureYear, departureMounth, departureDay);
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        returnDay = dayOfMonth;
                        returnMounth = monthOfYear;
                        returnYear = year;
                        updateReturnDate();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(calendar.getTime().getTime());
                dialog.show();
            }
        });

        fromAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkIfCompleted();
            }
        });

        toAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkIfCompleted();
            }
        });

        initLocation();
    }

    private void checkIfCompleted() {
        if (fromAutoComplete.getText().length() > 3 && toAutoComplete.getText().length() > 3) {
            if (searchButton.getVisibility() == View.GONE) {
                searchButton.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn)
                        .duration(200)
                        .playOn(searchButton);
            }
        } else {
            if (searchButton.getVisibility() == View.VISIBLE) {
                YoYo.with(Techniques.FadeOut)
                        .duration(200)
                        .withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                searchButton.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .playOn(searchButton);
            }

        }
    }

    private void updateReturnDate() {
        returnTextView.setText(String.format("%d/%d/%d", returnDay, returnMounth, returnYear));
    }

    private void updateDepartureDate() {
        departureTextView.setText(String.format("%d/%d/%d", departureDay, departureMounth, departureYear));
    }

    private void initLocation() {
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(300000);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(1);

        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // outState.putParcelableArrayList(DESTINY, destinies);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        9000);

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }

        } else {

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mFromAdapter.setUserLocation(location);
        mToAdapter.setUserLocation(location);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            mLocationClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
