package ensa.ma.sensors.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class StepCounterFragment extends Fragment implements SensorEventListener {

    private SensorManager gestionnaireCapteurs;
    private Sensor capteurPas;
    private TextView vueTexte;
    private float pasInitial = -1;
    private boolean capteurDisponible = true;

    private final ActivityResultLauncher<String> lanceurPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            accordee -> {
                if (accordee) {
                    demarrerCapteur();
                } else {
                    vueTexte.setText("❌ Permission refusée. Impossible de compter les pas.");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));  // ✅ Fond gris clair
        layout.setGravity(Gravity.CENTER);

        // Titre
        TextView titre = new TextView(requireContext());
        titre.setText("👣 COMPTEUR DE PAS");
        titre.setTextSize(24);
        titre.setTypeface(Typeface.DEFAULT_BOLD);
        titre.setGravity(Gravity.CENTER);
        titre.setPadding(0, 0, 0, 32);
        titre.setTextColor(Color.parseColor("#2196F3"));  // ✅ Bleu

        // Contenu
        vueTexte = new TextView(requireContext());
        vueTexte.setTextSize(18);
        vueTexte.setPadding(24, 24, 24, 24);
        vueTexte.setLineSpacing(8, 1.2f);
        vueTexte.setBackgroundColor(Color.WHITE);  // ✅ Fond blanc
        vueTexte.setTextColor(Color.parseColor("#333333"));  // ✅ Texte gris foncé
        vueTexte.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);

        layout.addView(titre);
        layout.addView(vueTexte);

        gestionnaireCapteurs = (SensorManager) requireActivity()
                .getSystemService(Context.SENSOR_SERVICE);

        if (gestionnaireCapteurs != null) {
            capteurPas = gestionnaireCapteurs.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (capteurPas == null) {
            vueTexte.setText("❌ Capteur de pas non disponible sur ce téléphone");
            capteurDisponible = false;
            return;
        }

        // Android 10+ (API 29+) nécessite permission explicite
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED) {

            lanceurPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        } else {
            demarrerCapteur();
        }
    }

    private void demarrerCapteur() {
        if (capteurPas != null && gestionnaireCapteurs != null) {
            gestionnaireCapteurs.registerListener(this, capteurPas,
                    SensorManager.SENSOR_DELAY_NORMAL);
            vueTexte.setText("🟢 Compteur de pas activé\nEn attente des données...");
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
        if (!capteurDisponible) return;

        float totalPas = event.values[0];

        if (pasInitial < 0) {
            pasInitial = totalPas;
        }

        int pasSession = (int) (totalPas - pasInitial);

        String affichage = String.format(
                "📊 Pas depuis le dernier redémarrage :\n   %d pas\n\n" +
                        "🏃 Pas de cette session :\n   %d pas\n\n" +
                        "💡 Astuce : Marchez avec le téléphone pour voir les pas augmenter",
                (int) totalPas, pasSession);

        vueTexte.setText(affichage);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé
    }
}