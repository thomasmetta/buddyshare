package com.thomas.buddyshare.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.squareup.otto.Subscribe;
import com.thomas.buddyshare.R;
import com.thomas.buddyshare.model.DrawMePath;
import com.thomas.buddyshare.ui.event.ColorPicked;
import com.thomas.buddyshare.ui.event.LineThicknessPicked;
import com.thomas.buddyshare.ui.widget.ColorPicker;
import com.thomas.buddyshare.ui.widget.DrawingCanvas;
import com.thomas.buddyshare.ui.widget.LineThicknessPicker;
import com.thomas.buddyshare.util.BitmapHelper;
import com.thomas.buddyshare.util.otto.ApplicationBus;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


public class DrawFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MyActivity";
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final String SAVE_STATE_DRAWING_PATHS = "drawingPaths";

    ImageButton mPencil;
    ImageButton mEraser;

    View mColorSwatch;
    FrameLayout mDrawingContainer;
    FrameLayout mDrawingCanvasContainer;
    DrawingCanvas mDrawingCanvas;
    int mPencilColor;
    ImageView mPhotoView;
    Bitmap mPhoto;
    int mLineThickness;
    int mDrawingCanvasState = DrawingCanvas.DRAWING;
    LinearLayout mToolbar;
    Button redButton;
    Button greenButton;
    Button blueButton;
    Bitmap operation;




    public DrawFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_draw, container, false);

        initViews(v);


        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            LinkedList<DrawMePath> paths = new LinkedList<>();;
            ArrayList<String> drawnPathsString = savedInstanceState.getStringArrayList(SAVE_STATE_DRAWING_PATHS);
            for (String s: drawnPathsString) {
                paths.addFirst(new Gson().fromJson(s,DrawMePath.class));
            }
            mDrawingCanvas.setDrawingPaths(paths);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ApplicationBus.getInstance().register(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        ApplicationBus.getInstance().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPhoto != null) {
            mPhoto.recycle();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.pencil:
                LineThicknessPicker.newInstance(mPencilColor).show(getFragmentManager(), "");
                break;

            case R.id.eraser:
                mDrawingCanvas.erase();
                mPhotoView.setImageBitmap(null);
                Log.v(TAG, "erase");

                break;

            case R.id.color_swatch:
                ColorPicker.newInstance().show(getFragmentManager(), "");

                break;

            case R.id.redbutton:
                red();
                Log.v(TAG, "red");
                break;

            case R.id.greenbutton:
                green();
                Log.v(TAG, "green");
                break;
            case R.id.bluebutton:
                blue();
                Log.v(TAG, "blue");
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_add_photo:
                loadPhotoGallery();
                return true;
            case R.id.action_email:
                emailFriend(getActivity(),R.id.drawing_container);
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);


        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {


            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            try {
                final Uri imageUri = intent.getData();
                String picFullPath = "";

                if (imageUri != null) {


                    Cursor cursor = getActivity().getContentResolver().query(imageUri,
                            new String[]{MediaStore.Images.Media.DATA},
                            null, null, null);
                    cursor.moveToFirst();
                    picFullPath = cursor.getString(0);

                    cursor.close();
                }

                mPhoto = BitmapHelper.optimize(picFullPath, width, height);

                boolean wideScreen = BitmapHelper.wideScreen(mPhoto.getWidth(), mPhoto.getHeight());

                //handle landscape pic
                if (mPhoto.getWidth() > mPhoto.getHeight()) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    mPhoto = Bitmap.createBitmap(mPhoto, 0, 0, mPhoto.getWidth(),
                            mPhoto.getHeight(), matrix, true);
                }



                if (wideScreen) {
                    mPhotoView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
                else {
                    mPhotoView.setScaleType(ImageView.ScaleType.FIT_XY);
                }

                mPhotoView.setImageBitmap(mPhoto);





            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<String> drawnPathsString = new ArrayList<>();

        LinkedList<DrawMePath> drawnPaths = mDrawingCanvas.getPaths();
        for (DrawMePath p : drawnPaths) {
            drawnPathsString.add(new Gson().toJson(p));
        }
        outState.putStringArrayList(SAVE_STATE_DRAWING_PATHS, drawnPathsString);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {


        MenuItem picture = menu.findItem(R.id.action_add_photo);
        MenuItem email = menu.findItem(R.id.action_email);


        if (mDrawingCanvasState == DrawingCanvas.DRAWING) {

            picture.setVisible(true);
            email.setVisible(true);
        }
        else if (mDrawingCanvasState == DrawingCanvas.REPLAY_PLAY) {

            picture.setVisible(false);
            email.setVisible(false);

        }

        super.onPrepareOptionsMenu(menu);
    }

    @Subscribe
    public void onColorPicked(ColorPicked event) {

        mColorSwatch.setBackgroundColor(getResources().getColor(event.getColorId()));
        mPencilColor = event.getColorId();
        mDrawingCanvas.setPencilColor(mPencilColor);
    }

    @Subscribe
    public void onLineStrokePicked(LineThicknessPicked event) {
        mLineThickness = event.getLineThickness();
        mDrawingCanvas.setStroke(mLineThickness);
    }



    private void initViews(View v) {

        mToolbar = (LinearLayout) v.findViewById(R.id.toolbar);
        mPhotoView = (ImageView) v.findViewById(R.id.photo);


        mDrawingContainer = (FrameLayout) v.findViewById(R.id.drawing_container);
        mDrawingCanvasContainer = (FrameLayout) v.findViewById(R.id.drawing_canvas_container);
        mPencil = (ImageButton) v.findViewById(R.id.pencil);
        mEraser = (ImageButton) v.findViewById(R.id.eraser);
        redButton = (Button) v.findViewById(R.id.redbutton);
        greenButton = (Button) v.findViewById(R.id.greenbutton);
        blueButton = (Button) v.findViewById(R.id.bluebutton);

        mColorSwatch = v.findViewById(R.id.color_swatch);

        mPencil.setOnClickListener(this);
        mEraser.setOnClickListener(this);
        redButton.setOnClickListener(this);
        greenButton.setOnClickListener(this);
        blueButton.setOnClickListener(this);

        mColorSwatch.setOnClickListener(this);

        mPencilColor = R.color.blue;
        mDrawingCanvas = new DrawingCanvas(getActivity());
        mDrawingCanvas.setPencilColor(mPencilColor);
        mDrawingCanvasContainer.addView(mDrawingCanvas);
    }

    private void emailFriend(Activity activity, int resourceId) {
        takeScreenshotAndEmail(activity, resourceId);
    }

    private void takeScreenshotAndEmail(Activity activity, int resourceId) {
        long iterator= new DateTime().getMillis() / 1000L; //unix time

        String mPath = Environment.getExternalStorageDirectory().toString() + "/buddyshare/";
        View v1 = activity.getWindow().getDecorView().findViewById(resourceId);
        v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
        v1.setDrawingCacheEnabled(true);

        final Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        File imageFile = new File(mPath);
        if (!imageFile.exists()) {
            imageFile.mkdirs();
        }
        imageFile = new File(imageFile+"/"+iterator+"_screenshot.png");
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] bitmapData = bos.toByteArray();


            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            emailPhoto(imageFile.getAbsolutePath());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void emailPhoto(String path) {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "buddyshare Photo");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Created with buddyshare app.");

        File file = new File(path);

        if (!file.exists() || !file.canRead()) {
            return;
        }

        Uri uri = Uri.fromFile(file);

        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
    }

    private void loadPhotoGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
    }

    public void red() {

        if (mPhoto != null) {
            operation = Bitmap.createBitmap(mPhoto.getWidth(), mPhoto.getHeight(), mPhoto.getConfig());
            double red = 0.33;
            double green = 0.59;
            double blue = 0.11;

            for (int i = 0; i < mPhoto.getWidth(); i++) {
                for (int j = 0; j < mPhoto.getHeight(); j++) {
                    int p = mPhoto.getPixel(i, j);
                    int r = Color.red(p);
                    int g = Color.green(p);
                    int b = Color.blue(p);
                    int alpha = Color.alpha(p);

                    r = r + 150;
                    g = 0;
                    b = 0;
                    alpha = 0;
                    operation.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));
                }
            }
            mPhotoView.setImageBitmap(operation);

        }
    }
    public void green() {

        if (mPhoto != null) {
            operation = Bitmap.createBitmap(mPhoto.getWidth(), mPhoto.getHeight(), mPhoto.getConfig());

            for (int i = 0; i < mPhoto.getWidth(); i++) {
                for (int j = 0; j < mPhoto.getHeight(); j++) {
                    int p = mPhoto.getPixel(i, j);
                    int r = Color.red(p);
                    int g = Color.green(p);
                    int b = Color.blue(p);
                    int alpha = Color.alpha(p);

                    r = 0;
                    g = g + 150;
                    b = 0;
                    alpha = 0;
                    operation.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));
                }
            }
            mPhotoView.setImageBitmap(operation);
        }


    }


    public void blue() {

        if (mPhoto != null) {
            operation = Bitmap.createBitmap(mPhoto.getWidth(), mPhoto.getHeight(), mPhoto.getConfig());

            for (int i = 0; i < mPhoto.getWidth(); i++) {
                for (int j = 0; j < mPhoto.getHeight(); j++) {
                    int p = mPhoto.getPixel(i, j);
                    int r = Color.red(p);
                    int g = Color.green(p);
                    int b = Color.blue(p);
                    int alpha = Color.alpha(p);

                    r = 0;
                    g = 0;
                    b = b + 150;
                    alpha = 0;
                    operation.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));
                }
            }
            mPhotoView.setImageBitmap(operation);
        }

    }

}
