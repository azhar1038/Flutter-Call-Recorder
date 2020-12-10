package com.az.call_recorder;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import java.io.FileDescriptor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.az.call_recorder";
    private static final String TAG = "Call Recorder";
    private static final int RECORD_PERMISSION_REQUEST_CODE = 63539;
    private static final int DIRECTORY_ACCESS_REQUEST_CODE = 78292;
    private MethodChannel.Result finalResult;
    private MediaRecorder callRecorder;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result)->{
                            finalResult = result;
                            switch (call.method) {
                                case "checkRecordPermission":
                                    checkRecordPermission();
                                    break;
                                case "requestStoragePermission":
                                    requestStoragePermission();
                                    break;
                                case "startRecord":
                                    startRecording();
                                    break;
                                case "stopRecord":
                                    stopRecording();
                                    break;
                                default:
                                    result.notImplemented();
                                    break;
                            }
                        }
                );
    }

    // Get persistent storage using SAF
    private void requestStoragePermission() {
        List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
        // If already a persistent storage has been allocated
        if (!permissions.isEmpty()) {
            Uri treeUri = permissions.get(0).getUri();
            try {
                DocumentFile folder = DocumentFile.fromTreeUri(this, treeUri);
                // Check if allocated storage is accessible
                if (folder != null && folder.isDirectory()) {
                    finalResult.success(true);
                }else{
                    // If not accessible, ask for new location
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, DIRECTORY_ACCESS_REQUEST_CODE);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            // No persistent storage present, ask for new one
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, DIRECTORY_ACCESS_REQUEST_CODE);
        }
    }

    //Get RECORD_AUDIO permission
    private void checkRecordPermission(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            finalResult.success(true);
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO},
                        RECORD_PERMISSION_REQUEST_CODE);
            }else{
                finalResult.success(false);
            }
        }
    }

    //Initialise and start the MediaRecorder
    private void startRecording() {
        // Initialise MediaRecorder
        callRecorder = new MediaRecorder();
        callRecorder.reset();
        callRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        callRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        callRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        Uri fileUri = createNewFile();
        try{
            // Get FileDescriptor for storing using SAF
            FileDescriptor fileDescriptor = getContentResolver()
                    .openFileDescriptor(fileUri, "rw")
                    .getFileDescriptor();
            callRecorder.setOutputFile(fileDescriptor);

            // Increase the volume to max
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);

            // Start recording
            callRecorder.prepare();
            callRecorder.start();
            Toast.makeText(this, "Started Recording", Toast.LENGTH_SHORT).show();
            finalResult.success("Started recording");
        }catch(Exception e){
            Log.e(TAG, "startRecording: ", e);
            finalResult.error("START RECORD", "Failed to start recording", null);
        }
    }

    // Stop the MediaRecorder if available
    private void stopRecording() {
        if(callRecorder != null) {
            callRecorder.stop();
            callRecorder.release();
            callRecorder = null;
            Toast.makeText(this, "Stopped Recording", Toast.LENGTH_SHORT).show();
        }
        finalResult.success("Stopped recording");
    }

    // Create new File to store audio and send back Uri using SAF
    private Uri createNewFile() {
        ContentResolver contentResolver = getContentResolver();
        List<UriPermission> permissions = contentResolver.getPersistedUriPermissions();
        if (permissions.isEmpty()) {
            finalResult.error("Storage Error", "Failed to store file", null);
            return null;
        }
        Uri treeUri = permissions.get(0).getUri();
        DocumentFile folder = DocumentFile.fromTreeUri(this, treeUri);
        if (folder == null || !folder.isDirectory()) {
            finalResult.error("Storage Error", "Failed to store file", null);
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM HH-mm-ss", Locale.US);
        String fileName = "Recording "+dateFormat.format(new Date());
        DocumentFile file = folder.createFile("audio/aac", fileName);
        return file.getUri();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RECORD_PERMISSION_REQUEST_CODE) {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                finalResult.success(true);
            }else{
                finalResult.success(false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DIRECTORY_ACCESS_REQUEST_CODE){
            if (resultCode == RESULT_OK && data.getData() != null) {
                ContentResolver resolver = getContentResolver();
                List<UriPermission> permissions = resolver.getPersistedUriPermissions();
                int extraFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                for(UriPermission permission : permissions){
                    if(!permission.getUri().toString().equals(data.getData().toString())) {
                        resolver.releasePersistableUriPermission(permission.getUri(), extraFlags);
                    }
                }
                resolver.takePersistableUriPermission(
                        data.getData(), data.getFlags() & extraFlags);
                finalResult.success(true);
            } else {
                finalResult.success(false);
            }
        }
    }
}
