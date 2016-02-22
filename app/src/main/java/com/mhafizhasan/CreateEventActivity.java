package com.mhafizhasan;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mhafizhasan.eventbook.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final int PLACE_PICKER_REQUEST = 1001;

    @Bind(R.id.mapView) MapView mapView;
    @Bind(R.id.cce_address)TextView addressView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.cce_start_date_layout) TextInputLayout startingDateLayout;
    @Bind(R.id.cce_start_date) EditText startingDateView;
    @Bind(R.id.cce_end_date_layout) TextInputLayout endingDateLayout;
    @Bind(R.id.cce_end_date) EditText endingDateView;

    // Form
    boolean isStartingDate = false;
    Calendar startingDate = null;
    Calendar endingDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Inflate and bind layout
        setContentView(R.layout.activity_create_event);
        ButterKnife.bind(this);

        // Setup toolbar
        setSupportActionBar(toolbar);

        // Setup Map View
        mapView.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    Marker marker = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            // User finished selecting location
            final Place place = PlacePicker.getPlace(this, data);
            addressView.setText(place.getAddress());
            // Move canvas to this location and add marker
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    // Zoom to selected place
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15f));
                    // Show marker
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(place.getLatLng());
                    markerOptions.title(place.getName().toString());
                    if(marker != null)
                        marker.remove();
                    marker = googleMap.addMarker(markerOptions);
                }
            });
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.cce_location_button)
    void onClickSelectLocation() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (Throwable e) {
            e.printStackTrace();
            // TODO: Inform user locations are broken
        }
    }

    void showDatePicker(boolean isStartingDate) {
        this.isStartingDate = isStartingDate;
        Calendar calendar = Calendar.getInstance();

        // Show dialog
        DatePickerDialog dialog = new DatePickerDialog(
                this, this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String dateText = formatter.format(calendar.getTime());

        // TODO: update date views
        if(isStartingDate) {
            startingDate = calendar;
            startingDateView.setText(dateText);
        } else {
            endingDate = calendar;
            endingDateView.setText(dateText);
        }
    }

    @OnTouch(R.id.cce_start_date)
    boolean onTouchStartingDate(View view, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP)
            showDatePicker(true);
        return true;
    }


    @OnTouch(R.id.cce_end_date)
    boolean onTouchEndingDate(View view, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP)
            showDatePicker(false);
        return true;
    }
}
