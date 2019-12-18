package com.app.facerecogn;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends FragmentActivity implements View.OnTouchListener,
        SurfaceHolder.Callback {

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;
    String logoImageId = "";
    Bitmap bitmap = null;
    private android.hardware.Camera camera = null;
    private SurfaceView cameraSurfaceView = null;
    private SurfaceHolder cameraSurfaceHolder = null;
    private boolean previewing = false;
    RelativeLayout relativeLayout;
    int currentCameraId = 0;
    private Button btnCapture = null;
    ImageButton useOtherCamera = null;
    ImageView logoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main_two);
        logoImageView = (ImageView) findViewById(R.id.image_view);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            logoImageId = extras.getString("logoImageId ");
        }
        try {
            /*File file = new File(Environment.getExternalStorageDirectory()
                    + "/" + getPackageName() + "/logo/" + logoImageId
                    + ".jpg");
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());*/
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.front);
            logoImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logoImageView.setOnTouchListener(this);
        relativeLayout = (RelativeLayout) findViewById(R.id.frame_view);
        relativeLayout.setDrawingCacheEnabled(true);
        cameraSurfaceView = (SurfaceView) findViewById(R.id.texture);
        cameraSurfaceHolder = cameraSurfaceView.getHolder();
        cameraSurfaceHolder.addCallback(this);
        btnCapture = (Button) findViewById(R.id.btn_takepicture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                camera.takePicture(null, null, cameraPictureCallbackJpeg);
            }
        });

    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    public boolean onTouch(View v, MotionEvent event) {
        // handle touch events here
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view.getWidth() / 2) * sx;
                        float yc = (view.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);

        return true;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    Camera.PictureCallback cameraPictureCallbackJpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            BitmapFactory.Options options = new BitmapFactory.Options();
            //o.inJustDecodeBounds = true;
            Bitmap cameraBitmapNull = BitmapFactory.decodeByteArray(data, 0,
                    data.length, options);

            int wid = options.outWidth;
            int hgt = options.outHeight;
            Matrix nm = new Matrix();

            Camera.Size cameraSize = camera.getParameters().getPictureSize();
            float ratio = relativeLayout.getHeight()*1f/cameraSize.height;
            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                nm.postRotate(90);
                nm.postTranslate(hgt, 0);
                wid = options.outHeight;
                hgt = options.outWidth;
                ratio = relativeLayout.getWidth()*1f/cameraSize.height;

            }else {
                wid = options.outWidth;
                hgt = options.outHeight;
                ratio = relativeLayout.getHeight()*1f/cameraSize.height;
            }

            float[] f = new float[9];
            matrix.getValues(f);

            f[0] = f[0]/ratio;
            f[4] = f[4]/ratio;
            f[5] = f[5]/ratio;
            f[2] = f[2]/ratio;
            matrix.setValues(f);

            Bitmap newBitmap = Bitmap.createBitmap(wid, hgt,
                    Bitmap.Config.ARGB_8888);

           Bitmap bmp= rotateImage(newBitmap,0);

            Canvas canvas = new Canvas(bmp);
            Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data, 0,
                    data.length, options);

            canvas.drawBitmap(cameraBitmap, nm, null);
            cameraBitmap.recycle();

            canvas.drawBitmap(bitmap, matrix, null);






            bitmap.recycle();


            File storagePath = new File(
                    Environment.getExternalStorageDirectory() + "/PhotoAR/");
            storagePath.mkdirs();

            File myImage = new File(storagePath, Long.toString(System
                    .currentTimeMillis()) + ".jpg");

            try {
                FileOutputStream out = new FileOutputStream(myImage);
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);

                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                Log.d("In Saving File", e + "");
            } catch (IOException e) {
                Log.d("In Saving File", e + "");
            }

        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub

        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }
        try {

            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                camera.setDisplayOrientation(90);
                Camera.Size cameraSize = camera.getParameters().getPictureSize();
                int wr = relativeLayout.getWidth();
                int hr = relativeLayout.getHeight();
                float ratio = relativeLayout.getWidth()*1f/cameraSize.height;
                float w = cameraSize.width*ratio;
                float h = cameraSize.height*ratio;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int)h, (int)w);
                cameraSurfaceView.setLayoutParams(lp);
            }else {
                camera.setDisplayOrientation(0);
                Camera.Size cameraSize = camera.getParameters().getPictureSize();
                float ratio = relativeLayout.getHeight()*1f/cameraSize.height;
                float w = cameraSize.width*ratio;
                float h = cameraSize.height*ratio;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int)w, (int)h);
                cameraSurfaceView.setLayoutParams(lp);
            }

            camera.setPreviewDisplay(cameraSurfaceHolder);
            camera.startPreview();
            previewing = true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        try {
            camera = Camera.open(1);
            Camera.Parameters params = camera.getParameters();


            // Check what resolutions are supported by your camera
            List<Camera.Size> sizes = params.getSupportedPictureSizes();

            // setting small image size in order to avoid OOM error
            Camera.Size cameraSize = null;
            for (Camera.Size size : sizes) {
                //set whatever size you need
                //if(size.height<500) {
                    cameraSize = size;
                    break;
                //}
            }

            if (cameraSize != null) {
                params.setPictureSize(cameraSize.width, cameraSize.height);
                camera.setParameters(params);

                float ratio = relativeLayout.getHeight()*1f/cameraSize.height;
                float w = cameraSize.width*ratio;
                float h = cameraSize.height*ratio;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int)w, (int)h);
                cameraSurfaceView.setLayoutParams(lp);
            }
        } catch (RuntimeException e) {
            Toast.makeText(
                    getApplicationContext(),
                    "Device camera  is not working properly, please try after sometime.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }
}