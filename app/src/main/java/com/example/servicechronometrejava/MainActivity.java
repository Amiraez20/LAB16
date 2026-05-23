package com.example.servicechronometrejava;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView affichageTemps;
    private Button boutonLancer, boutonArreter;
    private TimerService monTimerService;
    private boolean serviceConnecte = false;
    private android.os.Handler affichageHandler;
    private Runnable tacheAffichage;

    private final ServiceConnection liaisonService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName composant, IBinder lien) {
            TimerService.AgentLocal agent = (TimerService.AgentLocal) lien;
            monTimerService = agent.recupererService();
            serviceConnecte = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName composant) {
            serviceConnecte = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        affichageTemps = findViewById(R.id.tvTemps);
        boutonLancer = findViewById(R.id.btnStart);
        boutonArreter = findViewById(R.id.btnStop);

        boutonLancer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lancerLeService();
            }
        });

        boutonArreter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                couperLeService();
            }
        });
        affichageHandler = new android.os.Handler(getMainLooper());
        tacheAffichage = new Runnable() {
            @Override
            public void run() {
                if (serviceConnecte && monTimerService != null) {
                    int sec = monTimerService.getCompteur();
                    int mins = sec / 60;
                    int secsR = sec % 60;
                    affichageTemps.setText(String.format("%02d:%02d", mins, secsR));
                }
                affichageHandler.postDelayed(this, 1000);
            }
        };
        affichageHandler.post(tacheAffichage);
    }

    private void lancerLeService() {
        Intent intentDemarrage = new Intent(this, TimerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intentDemarrage);
        } else {
            startService(intentDemarrage);
        }
        bindService(intentDemarrage, liaisonService, Context.BIND_AUTO_CREATE);
    }

    private void couperLeService() {
        Intent intentArret = new Intent(this, TimerService.class);
        intentArret.setAction("ARRETER");
        stopService(intentArret);

        if (serviceConnecte) {
            unbindService(liaisonService);
            serviceConnecte = false;
        }
        affichageTemps.setText("00:00");
    }

    @Override
    protected void onDestroy() {
        if (serviceConnecte) {
            unbindService(liaisonService);
        }
        affichageHandler.removeCallbacks(tacheAffichage);
        super.onDestroy();
    }
}