package com.mihir.portfoliomonitoring.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mihir.portfoliomonitoring.R;
import com.mihir.portfoliomonitoring.models.CompanyMaster;

import java.util.ArrayList;

import fr.ganfra.materialspinner.MaterialSpinner;

public class EmployerSelectionFragment extends Fragment {

    private DatabaseReference mPortfolioManagerReference;
    private View view;
    private Context mContext;
    private ArrayList<String> companyNameArrayList = new ArrayList<>();
    private ArrayList<CompanyMaster> companyMasterArrayList = new ArrayList<>();
    private ArrayAdapter<String> adapterCompanyNames;
    private MaterialSpinner spinnerCompanyName;
    private TextView txtNext;
    private long totalUsers = 0;
    private boolean isThresholdCrossed = false;
    private boolean isSpinnerClicked = false;

    public EmployerSelectionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_employer_selection, container, false);
        mContext = view.getContext();

        mPortfolioManagerReference = FirebaseDatabase.getInstance().getReference();

        txtNext = (TextView) view.findViewById(R.id.txtNext);
        txtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
        spinnerCompanyName = (MaterialSpinner) view.findViewById(R.id.spinnerCompanyName);
        adapterCompanyNames = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, companyNameArrayList);
        spinnerCompanyName.setAdapter(adapterCompanyNames);

        mPortfolioManagerReference.child(getString(R.string.company_master)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                companyNameArrayList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    CompanyMaster companyMaster = ds.getValue(CompanyMaster.class);
                    companyMasterArrayList.add(companyMaster);
                    companyNameArrayList.add(companyMaster.getCompany_name());
                }
                adapterCompanyNames.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPortfolioManagerReference.child(getString(R.string.users)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    totalUsers = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        spinnerCompanyName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerClicked = true;
                return false;
            }
        });

        spinnerCompanyName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerClicked) {
                    mPortfolioManagerReference.child(getString(R.string.users)).orderByChild(getString(R.string.company_sector)).equalTo(companyMasterArrayList.get(position).getCompany_sector())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (((dataSnapshot.getChildrenCount() * 100) / totalUsers) > 10) {
                                            isThresholdCrossed = true;
                                        }
                                    } else {
                                        isThresholdCrossed = false;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }
}
