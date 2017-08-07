package com.mihir.portfoliomonitoring.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mihir.portfoliomonitoring.R;
import com.mihir.portfoliomonitoring.models.CompanyMaster;
import com.mihir.portfoliomonitoring.models.User;

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
    private TextView txtAddNewEmployer;
    private long totalUsers = 0;
    private long totalCompanies = 0;
    private boolean isThresholdCrossed = false;
    private boolean isSpinnerClicked = false;
    private boolean isEmployerPreSelected = false;
    private Bundle args;

    public EmployerSelectionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_employer_selection, container, false);
        mContext = view.getContext();

        args = new Bundle();
        mPortfolioManagerReference = FirebaseDatabase.getInstance().getReference();

        txtNext = (TextView) view.findViewById(R.id.txtNext);
        txtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmployerPreSelected) {
                    startCompanyDetailsFragment();
                    return;
                }
                if ((!isThresholdCrossed)) {
                    mPortfolioManagerReference.child(getString(R.string.users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(R.string.company_name)).setValue(companyMasterArrayList.get(spinnerCompanyName.getSelectedItemPosition() - 1).getCompany_name());
                    mPortfolioManagerReference.child(getString(R.string.users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(R.string.company_sector)).setValue(companyMasterArrayList.get(spinnerCompanyName.getSelectedItemPosition() - 1).getCompany_sector());
                    mPortfolioManagerReference.child(getString(R.string.users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(R.string.company_score)).setValue(companyMasterArrayList.get(spinnerCompanyName.getSelectedItemPosition() - 1).getCompany_score());

                    startCompanyDetailsFragment();
                } else {
                    Toast.makeText(mContext, getString(R.string.error_threshold), Toast.LENGTH_LONG).show();
                }
            }
        });
        txtAddNewEmployer = (TextView) view.findViewById(R.id.btnAddNewEmployer);
        txtAddNewEmployer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.dialog_new_employer);
                dialog.setTitle(getString(R.string.add_new_employer));
                dialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;

                TextView btnAddEmployer = (TextView) dialog.findViewById(R.id.btnAddEmployer);
                final EditText edtEmployerName = (EditText) dialog.findViewById(R.id.edtEmployerName);

                // if button is clicked, close the custom dialog
                btnAddEmployer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!edtEmployerName.getText().toString().trim().equals("")) {
                            CompanyMaster companyMaster = new CompanyMaster();
                            companyMaster.setCompany_name(edtEmployerName.getText().toString().trim());
                            companyMaster.setCompany_sector("");
                            companyMaster.setCompany_score(0);
                            companyMaster.setIsActive(0);
                            mPortfolioManagerReference.child(getString(R.string.company_master)).child(totalCompanies + "").setValue(companyMaster);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(mContext, getString(R.string.error_employer_name), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.show();
            }
        });
        spinnerCompanyName = (MaterialSpinner) view.findViewById(R.id.spinnerCompanyName);
        adapterCompanyNames = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, companyNameArrayList);
        spinnerCompanyName.setAdapter(adapterCompanyNames);

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

        mPortfolioManagerReference.child(getString(R.string.company_master)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    totalCompanies = dataSnapshot.getChildrenCount();
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
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (isSpinnerClicked) {
                    mPortfolioManagerReference.child(getString(R.string.users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(R.string.company_sector)).setValue("");

                    mPortfolioManagerReference.child(getString(R.string.users)).orderByChild(getString(R.string.company_sector)).equalTo(companyMasterArrayList.get(position).getCompany_sector())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (((dataSnapshot.getChildrenCount() * 100) / totalUsers) > 10) {
                                            isThresholdCrossed = true;
                                            isEmployerPreSelected = false;
                                        }
                                    } else {
                                        isThresholdCrossed = false;
                                        isEmployerPreSelected = false;
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

    public void startCompanyDetailsFragment() {
        CompanyDetailsFragment companyDetailsFragment = new CompanyDetailsFragment();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_Content, companyDetailsFragment);
        fragmentTransaction.addToBackStack(null);
        args.putString(getString(R.string.company_name), companyMasterArrayList.get(spinnerCompanyName.getSelectedItemPosition() - 1).getCompany_name());
        args.putString(getString(R.string.company_sector), companyMasterArrayList.get(spinnerCompanyName.getSelectedItemPosition() - 1).getCompany_sector());
        args.putString(getString(R.string.company_score), companyMasterArrayList.get(spinnerCompanyName.getSelectedItemPosition() - 1).getCompany_score() + "");
        companyDetailsFragment.setArguments(args);
        fragmentTransaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        isSpinnerClicked = false;

        mPortfolioManagerReference.child(getString(R.string.company_master)).orderByChild(getString(R.string.isActive)).equalTo(1).addValueEventListener(new ValueEventListener() {
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

        mPortfolioManagerReference.child(getString(R.string.users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getCompany_name() != null) {
                    spinnerCompanyName.setSelection(adapterCompanyNames.getPosition(user.getCompany_name()) + 1);
                    isEmployerPreSelected = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
