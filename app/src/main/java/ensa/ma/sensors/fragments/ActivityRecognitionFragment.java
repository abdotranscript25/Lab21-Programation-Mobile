package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class ActivityRecognitionFragment extends Fragment implements SensorEventListener {

    private SensorManager gestionnaireCapteurs;
    private Sensor accelerometre;
    private TextView vueValeurs;
    private TextView vueActivite;
    private TextView vueStatut;

    private final float[] gravite = new float[3];
    private final Queue<Float> fenetreMouvement = new LinkedList<>();

    private static final int TAILLE_FENETRE = 30;
    private static final float ALPHA = 0.8f;
    private boolean calibrationTerminee = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));  // ✅ Fond gris clair

        // Titre
        TextView titre = new TextView(requireContext());
        titre.setText("🏃 RECONNAISSANCE D'ACTIVITE");
        titre.setTextSize(22);
        titre.setTypeface(Typeface.DEFAULT_BOLD);
        titre.setGravity(Gravity.CENTER);
        titre.setPadding(0, 0, 0, 24);
        titre.setTextColor(Color.rgb(33, 150, 243));

        // Valeurs des axes (avec fond blanc)
        vueValeurs = new TextView(requireContext());
        vueValeurs.setTextSize(14);
        vueValeurs.setPadding(16, 16, 16, 16);
        vueValeurs.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        vueValeurs.setBackgroundColor(Color.WHITE);  // ✅ Fond blanc
        vueValeurs.setTextColor(Color.parseColor("#333333"));  // ✅ Texte gris foncé

        // Activité détectée (grand)
        vueActivite = new TextView(requireContext());
        vueActivite.setTextSize(28);
        vueActivite.setTypeface(Typeface.DEFAULT_BOLD);
        vueActivite.setGravity(Gravity.CENTER);
        vueActivite.setPadding(0, 24, 0, 24);
        vueActivite.setTextColor(Color.rgb(76, 175, 80));

        // Statut / calibration
        vueStatut = new TextView(requireContext());
        vueStatut.setTextSize(12);
        vueStatut.setGravity(Gravity.CENTER);
        vueStatut.setTextColor(Color.parseColor("#666666"));  // ✅ Gris moyen
        vueStatut.setPadding(0, 8, 0, 0);

        layout.addView(titre);
        layout.addView(vueValeurs);
        layout.addView(vueActivite);
        layout.addView(vueStatut);

        gestionnaireCapteurs = (SensorManager) requireActivity()
                .getSystemService(Context.SENSOR_SERVICE);

        if (gestionnaireCapteurs != null) {
            accelerometre = gestionnaireCapteurs.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometre != null) {
            gestionnaireCapteurs.registerListener(this, accelerometre,
                    SensorManager.SENSOR_DELAY_GAME);
            vueStatut.setText("🟢 Calibration en cours... (5 secondes)");
        } else {
            vueActivite.setText("❌ Accéléromètre indisponible");
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

        // Filtre passe-bas pour estimer la gravité
        gravite[0] = ALPHA * gravite[0] + (1 - ALPHA) * x;
        gravite[1] = ALPHA * gravite[1] + (1 - ALPHA) * y;
        gravite[2] = ALPHA * gravite[2] + (1 - ALPHA) * z;

        // Acceleration lineaire (sans gravite)
        float accX = x - gravite[0];
        float accY = y - gravite[1];
        float accZ = z - gravite[2];

        // Intensite du mouvement
        float mouvement = (float) Math.sqrt(accX * accX + accY * accY + accZ * accZ);

        ajouterValeurMouvement(mouvement);

        String activite = classifierActivite(x, y, z, mouvement);
        String emoji = getActiviteEmoji(activite);

        // Affichage des valeurs (formaté)
        String valeurs = String.format(Locale.FRANCE,
                "📐 X : %.2f\n📐 Y : %.2f\n📐 Z : %.2f\n📊 Mouvement : %.3f",
                x, y, z, mouvement);

        vueValeurs.setText(valeurs);
        vueActivite.setText(String.format("%s %s", emoji, activite));

        if (!calibrationTerminee && fenetreMouvement.size() >= TAILLE_FENETRE) {
            calibrationTerminee = true;
            vueStatut.setText("🟢 Calibration terminee - Detection active");
        }
    }

    private void ajouterValeurMouvement(float mouvement) {
        if (fenetreMouvement.size() >= TAILLE_FENETRE) {
            fenetreMouvement.poll();
        }
        fenetreMouvement.add(mouvement);
    }

    private String classifierActivite(float x, float y, float z, float mouvement) {
        if (fenetreMouvement.size() < TAILLE_FENETRE) {
            return "Calibration";
        }

        float somme = 0f;
        float max = 0f;
        for (float val : fenetreMouvement) {
            somme += val;
            max = Math.max(max, val);
        }
        float moyenne = somme / fenetreMouvement.size();

        float variance = 0f;
        for (float val : fenetreMouvement) {
            variance += (val - moyenne) * (val - moyenne);
        }
        variance = variance / fenetreMouvement.size();
        float ecartType = (float) Math.sqrt(variance);

        if (max > 12f) {
            return "SAUT";
        }
        if (ecartType > 1.2f && mouvement > 2f) {
            return "MARCHE";
        }
        if (Math.abs(z) > 9f && mouvement < 1.5f) {
            return "STABLE (plat)";
        }
        if (Math.abs(y) > 8f || Math.abs(x) > 8f) {
            return "ASSIS/DEBOUT";
        }
        return "POSITION STABLE";
    }

    private String getActiviteEmoji(String activite) {
        switch (activite) {
            case "SAUT": return "🏃‍♂️💨";
            case "MARCHE": return "🚶‍♂️";
            case "STABLE (plat)": return "📱";
            case "ASSIS/DEBOUT": return "🪑";
            default: return "⚪";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilise
    }
}