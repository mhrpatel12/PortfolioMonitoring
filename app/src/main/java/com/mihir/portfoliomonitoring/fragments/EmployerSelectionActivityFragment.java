package com.mihir.portfoliomonitoring.activities.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mihir.portfoliomonitoring.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class EmployerSelectionActivityFragment extends Fragment {

    public EmployerSelectionActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employer_selection, container, false);
    }
}
