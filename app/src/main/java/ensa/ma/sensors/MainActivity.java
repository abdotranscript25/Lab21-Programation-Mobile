package ensa.ma.sensors;

import android.hardware.Sensor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import ensa.ma.sensors.fragments.ActivityRecognitionFragment;
import ensa.ma.sensors.fragments.CompassFragment;
import ensa.ma.sensors.fragments.MotionSensorFragment;
import ensa.ma.sensors.fragments.SensorGraphFragment;
import ensa.ma.sensors.fragments.SensorsListFragment;
import ensa.ma.sensors.fragments.StepCounterFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // ✅ Ajouter le bouton hamburger ☰
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Afficher le fragment par défaut
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new SensorsListFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_sensors);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Toast.makeText(this, "Paramètres", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_sensors) {
            openFragment(new SensorsListFragment());
        }
        else if (id == R.id.nav_temp) {
            openFragment(SensorGraphFragment.newInstance(Sensor.TYPE_AMBIENT_TEMPERATURE, "🌡️ Température", "FIRST_VALUE"));
        }
        else if (id == R.id.nav_humd) {
            openFragment(SensorGraphFragment.newInstance(Sensor.TYPE_RELATIVE_HUMIDITY, "💧 Humidité", "FIRST_VALUE"));
        }
        else if (id == R.id.nav_proximity) {
            openFragment(SensorGraphFragment.newInstance(Sensor.TYPE_PROXIMITY, "📡 Proximité", "FIRST_VALUE"));
        }
        else if (id == R.id.nav_magnetic) {
            openFragment(SensorGraphFragment.newInstance(Sensor.TYPE_MAGNETIC_FIELD, "🧲 Champ magnétique", "MAGNITUDE"));
        }
        else if (id == R.id.nav_accelerometer) {
            openFragment(MotionSensorFragment.newInstance(Sensor.TYPE_ACCELEROMETER, "📊 Accéléromètre"));
        }
        else if (id == R.id.nav_gravity) {
            openFragment(MotionSensorFragment.newInstance(Sensor.TYPE_GRAVITY, "🌍 Gravité"));
        }
        else if (id == R.id.nav_gyroscope) {
            openFragment(MotionSensorFragment.newInstance(Sensor.TYPE_GYROSCOPE, "🔄 Gyroscope"));
        }
        else if (id == R.id.nav_steps) {
            openFragment(new StepCounterFragment());
        }
        else if (id == R.id.nav_compass) {
            openFragment(new CompassFragment());
        }
        else if (id == R.id.nav_activity) {
            openFragment(new ActivityRecognitionFragment());
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}