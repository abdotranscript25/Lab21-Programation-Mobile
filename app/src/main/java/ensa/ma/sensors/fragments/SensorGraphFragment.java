package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ensa.ma.sensors.views.LineChartView;

public class SensorGraphFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE = "title";
    private static final String ARG_MODE = "mode";

    private SensorManager sensorManager;
    private Sensor capteur;
    private TextView valeurTextView;
    private LineChartView graphique;
    private int typeCapteur;
    private String titre;
    private String mode;

    private final Handler simulateurHandler = new Handler(Looper.getMainLooper());
    private float tempsSimulation = 0f;

    public static SensorGraphFragment newInstance(int typeCapteur, String titre, String mode) {
        SensorGraphFragment fragment = new SensorGraphFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, typeCapteur);
        args.putString(ARG_TITLE, titre);
        args.putString(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            typeCapteur = getArguments().getInt(ARG_SENSOR_TYPE);
            titre = getArguments().getString(ARG_TITLE);
            mode = getArguments().getString(ARG_MODE);
        }

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        capteur = sensorManager.getDefaultSensor(typeCapteur);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));  // ✅ Fond gris clair

        TextView titreView = new TextView(requireContext());
        titreView.setText(titre);
        titreView.setTextSize(22);
        titreView.setTypeface(Typeface.DEFAULT_BOLD);
        titreView.setPadding(0, 0, 0, 20);
        titreView.setTextColor(Color.parseColor("#2196F3"));  // ✅ Bleu

        valeurTextView = new TextView(requireContext());
        valeurTextView.setTextSize(18);
        valeurTextView.setPadding(16, 16, 16, 16);
        valeurTextView.setBackgroundColor(Color.WHITE);  // ✅ Fond blanc
        valeurTextView.setTextColor(Color.parseColor("#333333"));  // ✅ Texte gris foncé
        valeurTextView.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);

        graphique = new LineChartView(requireContext());
        graphique.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 600));

        layout.addView(titreView);
        layout.addView(valeurTextView);
        layout.addView(graphique);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (capteur != null) {
            sensorManager.registerListener(this, capteur, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            valeurTextView.setText("📡 Capteur indisponible. Mode simulation activé.");
            demarrerSimulation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        simulateurHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float valeur = extraireValeur(event.values);
        mettreAJourUI(valeur);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private float extraireValeur(float[] valeurs) {
        if ("MAGNITUDE".equals(mode)) {
            return (float) Math.sqrt(valeurs[0] * valeurs[0] +
                    valeurs[1] * valeurs[1] +
                    valeurs[2] * valeurs[2]);
        }
        return valeurs[0];
    }

    private void mettreAJourUI(float valeur) {
        valeurTextView.setText("📊 Valeur détectée : " + String.format("%.3f", valeur));
        graphique.addValue(valeur);
    }

    private void demarrerSimulation() {
        simulateurHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tempsSimulation++;
                float valeur;

                if (typeCapteur == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    valeur = 22f + (float) Math.sin(tempsSimulation / 5f) * 3f;
                } else if (typeCapteur == Sensor.TYPE_RELATIVE_HUMIDITY) {
                    valeur = 55f + (float) Math.sin(tempsSimulation / 7f) * 15f;
                } else if (typeCapteur == Sensor.TYPE_PROXIMITY) {
                    valeur = tempsSimulation % 6 < 3 ? 0f : 5f;
                } else if (typeCapteur == Sensor.TYPE_MAGNETIC_FIELD) {
                    valeur = 45f + (float) Math.sin(tempsSimulation / 4f) * 10f;
                } else {
                    valeur = (float) Math.sin(tempsSimulation);
                }

                mettreAJourUI(valeur);
                simulateurHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }
}