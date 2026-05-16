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

import java.util.Locale;

public class CompassFragment extends Fragment implements SensorEventListener {

    private SensorManager gestionnaireCapteurs;
    private Sensor accelerometre;
    private Sensor magnetometre;
    private TextView vueDirection;
    private TextView vueAngle;
    private TextView vueStatut;

    private final float[] valeursGravite = new float[3];
    private final float[] valeursMagnetiques = new float[3];
    private boolean aGravite = false;
    private boolean aMagnetique = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));  // ✅ Fond gris clair

        // Titre
        TextView titre = new TextView(requireContext());
        titre.setText("🧭 BOUSSOLE NUMERIQUE");
        titre.setTextSize(24);
        titre.setTypeface(Typeface.DEFAULT_BOLD);
        titre.setGravity(Gravity.CENTER);
        titre.setPadding(0, 0, 0, 32);
        titre.setTextColor(Color.rgb(33, 150, 243));

        // Angle (grand chiffre)
        vueAngle = new TextView(requireContext());
        vueAngle.setTextSize(48);
        vueAngle.setTypeface(Typeface.DEFAULT_BOLD);
        vueAngle.setGravity(Gravity.CENTER);
        vueAngle.setPadding(16, 16, 16, 16);
        vueAngle.setText("0°");
        vueAngle.setTextColor(Color.parseColor("#2196F3"));  // ✅ Bleu
        vueAngle.setBackgroundColor(Color.WHITE);  // ✅ Fond blanc

        // Direction
        vueDirection = new TextView(requireContext());
        vueDirection.setTextSize(32);
        vueDirection.setTypeface(Typeface.DEFAULT_BOLD);
        vueDirection.setGravity(Gravity.CENTER);
        vueDirection.setPadding(0, 16, 0, 32);
        vueDirection.setText("⬆️ NORD");
        vueDirection.setTextColor(Color.parseColor("#333333"));  // ✅ Gris foncé

        // Statut
        vueStatut = new TextView(requireContext());
        vueStatut.setTextSize(14);
        vueStatut.setGravity(Gravity.CENTER);
        vueStatut.setTextColor(Color.parseColor("#666666"));  // ✅ Gris moyen
        vueStatut.setText("🟡 En attente des capteurs...");

        layout.addView(titre);
        layout.addView(vueAngle);
        layout.addView(vueDirection);
        layout.addView(vueStatut);

        gestionnaireCapteurs = (SensorManager) requireActivity()
                .getSystemService(Context.SENSOR_SERVICE);

        if (gestionnaireCapteurs != null) {
            accelerometre = gestionnaireCapteurs.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometre = gestionnaireCapteurs.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometre != null && magnetometre != null) {
            gestionnaireCapteurs.registerListener(this, accelerometre,
                    SensorManager.SENSOR_DELAY_UI);
            gestionnaireCapteurs.registerListener(this, magnetometre,
                    SensorManager.SENSOR_DELAY_UI);
            vueStatut.setText("🟢 Capteurs actifs - Tournez le telephone");
        } else {
            String msg = "";
            if (accelerometre == null) msg = msg + "Accelerometre manquant\n";
            if (magnetometre == null) msg = msg + "Magnetometre manquant";
            vueStatut.setText(msg);
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, valeursGravite, 0, 3);
            aGravite = true;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, valeursMagnetiques, 0, 3);
            aMagnetique = true;
        }

        if (aGravite && aMagnetique) {
            float[] matriceRotation = new float[9];
            float[] orientation = new float[3];

            boolean succes = SensorManager.getRotationMatrix(
                    matriceRotation,
                    null,
                    valeursGravite,
                    valeursMagnetiques);

            if (succes) {
                SensorManager.getOrientation(matriceRotation, orientation);

                float azimuthRadians = orientation[0];
                float azimuthDegres = (float) Math.toDegrees(azimuthRadians);

                if (azimuthDegres < 0) {
                    azimuthDegres += 360;
                }

                String direction = getDirectionName(azimuthDegres);
                String emoji = getDirectionEmoji(azimuthDegres);

                vueAngle.setText(String.format(Locale.FRANCE, "%.0f°", azimuthDegres));
                vueDirection.setText(String.format("%s %s", emoji, direction));
            }
        }
    }

    private String getDirectionName(float degre) {
        if (degre >= 337.5 || degre < 22.5) return "NORD";
        if (degre < 67.5) return "NORD-EST";
        if (degre < 112.5) return "EST";
        if (degre < 157.5) return "SUD-EST";
        if (degre < 202.5) return "SUD";
        if (degre < 247.5) return "SUD-OUEST";
        if (degre < 292.5) return "OUEST";
        return "NORD-OUEST";
    }

    private String getDirectionEmoji(float degre) {
        if (degre >= 337.5 || degre < 22.5) return "⬆️";
        if (degre < 67.5) return "↗️";
        if (degre < 112.5) return "➡️";
        if (degre < 157.5) return "↘️";
        if (degre < 202.5) return "⬇️";
        if (degre < 247.5) return "↙️";
        if (degre < 292.5) return "⬅️";
        return "↖️";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String precision;
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                precision = "Haute precision";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                precision = "Precision moyenne";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                precision = "Basse precision";
                break;
            default:
                precision = "Precision inconnue";
        }
        vueStatut.setText(precision);
    }
}