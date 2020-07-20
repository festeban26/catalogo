package com.dulacs.festeban26;


import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

/**
 * This fragment is based on the github library by dm77: https://github.com/dm77/barcodescanner
 * The choice to use this library was backed by the fact that Corporación La Favorita
 * could be using it on their app as on Jul 5, 2018.
 */


public class CameraPreviewFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private Context mContext;

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;

    public CameraPreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    /**
     * This class serves the purpose of resizing the scanner area by overriding the onDraw method
     */
    private class CustomViewFinderView extends ViewFinderView {
        Context context;

        public CustomViewFinderView(Context context) {
            super(context);
            this.context = context;
            setLaserEnabled(true);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (getFramingRect() != null)
                updateFrameRect();
        }

        /**
         * The scanner are is set up to be proportional to the fragment view size. 10% of its height
         * and 40% of its width.
         */
        private void updateFrameRect() {
            getFramingRect().left = (int) (this.getWidth() / 2 - (0.20 * this.getWidth()));
            getFramingRect().right = (int) (this.getWidth() / 2 + (0.20 * this.getWidth()));
            getFramingRect().top = (int) (this.getHeight() / 2 - (0.06 * this.getHeight()));
            getFramingRect().bottom = (int) (this.getHeight() / 2 + (0.06 * this.getHeight()));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        scannerView = new ZXingScannerView(getActivity()) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new CustomViewFinderView(context);
            }
        };

        scannerView.setAutoFocus(true);

        List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.EAN_13);
        barcodeFormats.add(BarcodeFormat.CODE_128);

        scannerView.setFormats(barcodeFormats);
        scannerView.setBorderCornerRadius(15);

        int currentApiVersion = Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.M)
            if (!checkPermission())
                requestPermission();

        return scannerView;                // Set the scanner view as the content view
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getActivity(), CAMERA)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                //if(scannerView == null) {
                //    scannerView = new ZXingScannerView(getActivity());
                //    setContentView(scannerView);
                //}
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else
                requestPermission();
        }

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {

        //ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        //toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            else
                v.vibrate(200); //deprecated in API 26
        }

        // In the future check if result can be converted to long. The result may be a QR code
        long barcode = Long.parseLong(result.getText());
        // If barcode is registered on the Application Database
        if (Database.getInstance().containsBarcode(barcode)) {
            if (mContext instanceof HomeActivity) {
                scannerView.stopCameraPreview();
                scannerView.stopCamera();
                ((HomeActivity) mContext).attachProductInformationFragmentForBarcode(barcode);
            }

        } else
            scannerView.resumeCameraPreview(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera(); // Stop camera on pause
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
