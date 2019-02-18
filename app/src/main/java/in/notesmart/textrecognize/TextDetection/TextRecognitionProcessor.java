package in.notesmart.textrecognize.TextDetection;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import in.notesmart.textrecognize.Additional.FrameMetaData;
import in.notesmart.textrecognize.Additional.GraphicOverlay;

public class TextRecognitionProcessor {

    private static final String TAG = "TextRecProc";

    private final FirebaseVisionTextRecognizer detector;
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);

    public TextRecognitionProcessor() {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    }

    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
        }
    }


    public void process(ByteBuffer data, FrameMetaData frameMetadata, GraphicOverlay graphicOverlay) {

        if (shouldThrottle.get()) {
            return;
        }
        FirebaseVisionImageMetadata metadata =
                new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        detectInVisionImage(FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay);
    }

    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }


    protected void onSuccess(@NonNull FirebaseVisionText results, @NonNull FrameMetaData frameMetadata, @NonNull GraphicOverlay graphicOverlay) {

        graphicOverlay.clear();

        List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, elements.get(k));
                    graphicOverlay.add(textGraphic);

                }
            }
        }
    }

    protected void onFailure(@NonNull Exception e) {
    }

    private void detectInVisionImage(FirebaseVisionImage image, final FrameMetaData metadata, final GraphicOverlay graphicOverlay) {

        detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText results) {
                                shouldThrottle.set(false);
                                TextRecognitionProcessor.this.onSuccess(results, metadata, graphicOverlay);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                shouldThrottle.set(false);
                                TextRecognitionProcessor.this.onFailure(e);
                            }
                        });
        shouldThrottle.set(true);
    }
}
