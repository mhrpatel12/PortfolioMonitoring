package com.mihir.portfoliomonitoring.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mihir.portfoliomonitoring.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompanyDetailsFragment extends Fragment {

    private View view;
    private Context mContext;

    private TextView txtCompanyName;
    private TextView txtCompanySector;
    private TextView txtCompanyScore;

    public CompanyDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_company_details, container, false);
        mContext = view.getContext();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((TextView) toolbar.findViewById(R.id.txtTitle)).setText(getString(R.string.title_fragment_cpmpany_details));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtCompanyName = (TextView) view.findViewById(R.id.txtCompanyName);
        txtCompanySector = (TextView) view.findViewById(R.id.txtCompanySector);
        txtCompanyScore = (TextView) view.findViewById(R.id.txtCompanyScore);

        String companyName = getArguments().getString(getString(R.string.company_name));
        String companySector = getArguments().getString(getString(R.string.company_sector));
        String companyScore = getArguments().getString(getString(R.string.company_score));

        if (companyName != null) {
            txtCompanyName.setText(companyName);
        }
        if (companySector != null) {
            txtCompanySector.setText(companySector);
        }
        if (companyScore != null) {
            txtCompanyScore.setText(companyScore);
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

}
