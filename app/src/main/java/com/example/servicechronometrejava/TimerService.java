package com.example.servicechronometrejava;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerService extends Service {

    // Agent (binder) pour permettre a l'Activity de se lier au service
    private final IBinder agentLiaison = new AgentLocal();

    private int compteurSecondes = 0;
    private boolean enFonctionnement = false;
    private ScheduledExecutorService planificateur;
    private static final int ID_NOTIFICATION = 2002;
    private NotificationManager gestionnaireNotif;

    // Classe interne retournant l'instance courante du service
    public class AgentLocal extends Binder {
        public TimerService recupererService() {
            return TimerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gestionnaireNotif = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        initialiserCanalNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int idDemarrage) {
        String actionRecue = (intent != null) ? intent.getAction() : null;

        // Action "ARRETER" pour stopper proprement le service
        if ("ARRETER".equals(actionRecue)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!enFonctionnement) {
            enFonctionnement = true;
            startForeground(ID_NOTIFICATION, construireNotification());
            activerCompteur();
        }
        return START_STICKY;
    }

    private void activerCompteur() {
        planificateur = Executors.newSingleThreadScheduledExecutor();
        planificateur.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                compteurSecondes++;
                rafraichirNotification();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void initialiserCanalNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    "canal_timer",
                    "Timer en Arriere-Plan",
                    NotificationManager.IMPORTANCE_LOW
            );
            gestionnaireNotif.createNotificationChannel(canal);
        }
    }

    private Notification construireNotification() {
        return new NotificationCompat.Builder(this, "canal_timer")
                .setContentTitle("Timer actif")
                .setContentText("Duree ecoulee : " + convertirEnMinutes(compteurSecondes))
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void rafraichirNotification() {
        gestionnaireNotif.notify(ID_NOTIFICATION, construireNotification());
    }

    private String convertirEnMinutes(int totalSecondes) {
        int mins = totalSecondes / 60;
        int secsRestantes = totalSecondes % 60;
        return String.format("%02d:%02d", mins, secsRestantes);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return agentLiaison;
    }

    @Override
    public void onDestroy() {
        enFonctionnement = false;
        if (planificateur != null) {
            planificateur.shutdown();
        }
        stopForeground(true);
        super.onDestroy();
    }
    public int getCompteur() {
        return compteurSecondes;
    }
}