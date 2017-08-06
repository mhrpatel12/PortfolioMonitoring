package com.mihir.portfoliomonitoring.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_company_details, container, false);
        mContext = view.getContext();

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

}
