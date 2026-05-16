package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ensa.ma.sensors.utils.SensorFormatter;
import java.util.List;

public class SensorsListFragment extends Fragment {

    private SensorManager sensorManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setBackgroundColor(Color.parseColor("#F5F5F5"));

        LinearLayout containerLayout = new LinearLayout(requireContext());
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        containerLayout.setPadding(24, 24, 24, 24);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> capteurs = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor capteur : capteurs) {
            TextView textView = new TextView(requireContext());
            textView.setText(SensorFormatter.format(capteur));
            textView.setTextSize(14);
            textView.setPadding(16, 16, 16, 16);
            textView.setBackgroundColor(Color.WHITE);
            textView.setTextColor(Color.parseColor("#333333"));

            containerLayout.addView(textView);

            View separator = new View(requireContext());
            separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            separator.setBackgroundColor(Color.parseColor("#E0E0E0"));
            containerLayout.addView(separator);
        }

        scrollView.addView(containerLayout);
        return scrollView;
    }
}