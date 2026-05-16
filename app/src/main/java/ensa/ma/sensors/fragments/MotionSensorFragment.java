package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import ensa.ma.sensors.views.LineChartView;

public class MotionSensorFragment extends Fragment implements SensorEventListener {

    private static final String ARG_TYPE_CAPTEUR = "type_capteur";
    private static final String ARG_TITRE = "titre";

    private SensorManager gestionnaireCapteurs;
    private Sensor capteur;
    private TextView affichageValeurs;
    private LineChartView vueGraphique;
    private int typeCapteur;
    private String titre;

    public static MotionSensorFragment newInstance(int typeCapteur, String titre) {
        MotionSensorFragment fragment = new MotionSensorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE_CAPTEUR, typeCapteur);
        args.putString(ARG_TITRE, titre);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            typeCapteur = getArguments().getInt(ARG_TYPE_CAPTEUR);
            titre = getArguments().getString(ARG_TITRE);
        }

        gestionnaireCapteurs = (SensorManager) requireActivity()
                .getSystemService(Context.SENSOR_SERVICE);
        capteur = gestionnaireCapteurs.getDefaultSensor(typeCapteur);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));  // ✅ Fond gris clair

        TextView vueTitre = new TextView(requireContext());
        vueTitre.setText(titre);
        vueTitre.setTextSize(22);
        vueTitre.setTypeface(Typeface.DEFAULT_BOLD);
        vueTitre.setPadding(0, 0, 0, 16);
        vueTitre.setTextColor(Color.parseColor("#2196F3"));  // ✅ Bleu

        affichageValeurs = new TextView(requireContext());
        affichageValeurs.setTextSize(16);
        affichageValeurs.setPadding(16, 16, 16, 16);
        affichageValeurs.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        affichageValeurs.setBackgroundColor(Color.WHITE);  // ✅ Fond blanc
        affichageValeurs.setTextColor(Color.parseColor("#333333"));  // ✅ Texte gris foncé

        vueGraphique = new LineChartView(requireContext());
        vueGraphique.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 500));

        layout.addView(vueTitre);
        layout.addView(affichageValeurs);
        layout.addView(vueGraphique);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (capteur != null) {
            gestionnaireCapteurs.registerListener(this, capteur,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            affichageValeurs.setText("❌ Capteur non disponible sur ce téléphone");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (gestionnaireCapteurs != null) {
            gestionnaireCapteurs.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float intensite = (float) Math.sqrt(x * x + y * y + z * z);

        String texte = String.format(Locale.FRANCE,
                "📐 Axe X : %.3f\n📐 Axe Y : %.3f\n📐 Axe Z : %.3f\n⚡ Intensité : %.3f",
                x, y, z, intensite);

        affichageValeurs.setText(texte);
        vueGraphique.addValue(intensite);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé
    }
}