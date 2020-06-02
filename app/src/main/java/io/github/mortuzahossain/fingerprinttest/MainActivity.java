package io.github.mortuzahossain.fingerprinttest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wacom.ink.rasterization.InkCanvas;
import com.wacom.ink.rasterization.Layer;
import com.wacom.ink.rendering.EGLRenderingContext;

public class MainActivity extends AppCompatActivity {

    private InkCanvas inkCanvas;
    private Layer viewLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView surfaceView = findViewById(R.id.surfaceview);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (inkCanvas != null && !inkCanvas.isDisposed()) {
                    releaseResources();
                }

                inkCanvas = InkCanvas.create(holder, new EGLRenderingContext.EGLConfiguration());
                viewLayer = inkCanvas.createViewLayer(width, height);

                renderView();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseResources();
            }
        });


    }

    private void renderView() {
        inkCanvas.setTarget(viewLayer);
        inkCanvas.clearColor(Color.RED);
        inkCanvas.invalidate();
    }

    private void releaseResources() {
        inkCanvas.dispose();
    }
}