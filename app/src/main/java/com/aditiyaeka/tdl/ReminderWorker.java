package com.aditiyaeka.tdl;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aditiyaeka.tdl.Model.ToDoModel;
import com.aditiyaeka.tdl.Utils.DataBaseHelper;

import java.util.List;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        DataBaseHelper db = new DataBaseHelper(getApplicationContext());
        List<ToDoModel> tasks = db.getAllTasks();

        boolean hasUnfinished = false;
        for (ToDoModel task : tasks) {
            if (task.getStatus() == 0) {
                hasUnfinished = true;
                break;
            }
        }

        if (hasUnfinished) {
            sendReminderNotification();
        }

        return Result.success();
    }

    private void sendReminderNotification() {
        Context context = getApplicationContext();

        // Cek izin notifikasi Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d("ReminderWorker", "Izin notifikasi belum diberikan");
                return;
            }
        }

        // Intent saat notif ditekan
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE
        );

        // Suara custom dari res/raw
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.reminder_sound);

        // Buat channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "reminder_channel",
                    "Reminder Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reminder_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Ingat Aku?")
                .setContentText("Ada tugas yang belum selesai nih.")
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(2002, builder.build());
    }
}
