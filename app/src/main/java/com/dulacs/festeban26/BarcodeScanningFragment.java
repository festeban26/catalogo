package com.dulacs.festeban26;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class BarcodeScanningFragment extends Fragment {

    //private OnFragmentInteractionListener mListener;

    public BarcodeScanningFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((HomeActivity) getActivity()).setActionBarTitle(getString(R.string.navActionBar_BarcodeScanning));
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_barcodescanning, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Fragment cameraPreviewFragment = new CameraPreviewFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout_barcodeScanning_cameraPreview, cameraPreviewFragment).commit();
    }
}
