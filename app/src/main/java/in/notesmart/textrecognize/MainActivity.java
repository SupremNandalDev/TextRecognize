package in.notesmart.textrecognize;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

import in.notesmart.textrecognize.Additional.GraphicOverlay;
import in.notesmart.textrecognize.Camera.CameraSource;
import in.notesmart.textrecognize.Camera.CameraSourcePreview;
import in.notesmart.textrecognize.TextDetection.TextRecognitionProcessor;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName().trim();
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.camera_source_preview);
        if (preview == null) {
        }
        graphicOverlay = findViewById(R.id.graphics_overlay);
        if (graphicOverlay == null) {
        }

        createCameraSource();
        startCameraSource();
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private void createCameraSource() {

        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
            cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
        }

        cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor());
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                }
                if (graphicOverlay == null) {
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

}
