package com.dulacs.festeban26;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutUsFragment extends Fragment {

    private WebView aboutUsWebView;
    private ProgressBar progressBar;
    private TextView textView_LoadingContent;


    public AboutUsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_aboutus, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((HomeActivity) getActivity()).setActionBarTitle(getString(R.string.navActionBar_AboutUs));


        aboutUsWebView = getActivity().findViewById(R.id.webView_aboutUsFragment);
        progressBar = getActivity().findViewById(R.id.progressBar_AboutUsFragment);
        textView_LoadingContent = getActivity().findViewById(R.id.textView_AboutUsFragment_LoadingContent);

        aboutUsWebView.getSettings().setJavaScriptEnabled(false);
        aboutUsWebView.getSettings().setSupportZoom(false);

        aboutUsWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    textView_LoadingContent.setVisibility(View.VISIBLE);
                }

                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                    textView_LoadingContent.setVisibility(View.GONE);
                }
            }
        });

        aboutUsWebView.loadUrl("http://inprolac.com.ec/somos-inprolac/");
    }
}
