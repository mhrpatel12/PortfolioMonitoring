package com.mihir.portfoliomonitoring.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mihir.portfoliomonitoring.R;

public class EmployerSelectionFragment extends Fragment {

    public EmployerSelectionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employer_selection, container, false);
    }
}
