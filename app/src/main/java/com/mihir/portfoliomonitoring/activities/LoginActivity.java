package com.mihir.portfoliomonitoring.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mihir.portfoliomonitoring.R;
import com.mihir.portfoliomonitoring.models.ReferralCodeMaster;
import com.mihir.portfoliomonitoring.models.User;

import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_SMS;

/**
 * A login screen that offers login via Email/Mobile Number with OTP/regular password
 */
public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_READ_SMS = 0;

    // UI references.
    private LinearLayout layoutLogin;
    private LinearLayout layoutRegistration;

    private TextView txtLogin;
    private TextView txtRegistration;

    private EditText edtEmailLogin;
    private EditText edtMobileLogin;
    private EditText edtPasswordLogin;
    private AppCompatCheckBox chkSignInWithOTP;

    private EditText edtEmailRegistration;
    private EditText edtPasswordRegistration;
    private EditText edtConfirmPasswordRegistration;
    private EditText edtReferralCOde;

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final String TAG = "LoginActivity";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private DatabaseReference mDatabase;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ProgressDialog progressDialog;

    private String referralCodeKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

        mayRequestPermission();

        progressDialog = new ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set up the login form.
        layoutLogin = (LinearLayout) findViewById(R.id.email_login_form);
        layoutRegistration = (LinearLayout) findViewById(R.id.email_registration_form);

        txtLogin = (TextView) findViewById(R.id.txtLogin);
        txtRegistration = (TextView) findViewById(R.id.txtRegistration);

        edtEmailLogin = (EditText) findViewById(R.id.edtEmailLogin);
        edtMobileLogin = (EditText) findViewById(R.id.edtMobileLogin);
        edtPasswordLogin = (EditText) findViewById(R.id.edtPasswordLogin);

        edtEmailRegistration = (EditText) findViewById(R.id.edtEmailRegistration);
        edtPasswordRegistration = (EditText) findViewById(R.id.edtPasswordRegistration);
        edtConfirmPasswordRegistration = (EditText) findViewById(R.id.edtConfirmPasswordRegistration);
        edtReferralCOde = (EditText) findViewById(R.id.edtReferalCodeRegistration);

        chkSignInWithOTP = (AppCompatCheckBox) findViewById(R.id.chkSignInWithOTP);
        chkSignInWithOTP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtMobileLogin.setVisibility(View.VISIBLE);
                    edtEmailLogin.setVisibility(View.GONE);
                    edtPasswordLogin.setVisibility(View.GONE);
                } else {
                    edtMobileLogin.setVisibility(View.GONE);
                    edtEmailLogin.setVisibility(View.VISIBLE);
                    edtPasswordLogin.setVisibility(View.VISIBLE);
                }
            }
        });

        txtLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutLogin.setVisibility(View.VISIBLE);
                layoutRegistration.setVisibility(View.GONE);
            }
        });

        txtRegistration.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutLogin.setVisibility(View.GONE);
                layoutRegistration.setVisibility(View.VISIBLE);
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.btnSignIn);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chkSignInWithOTP.isChecked()) {
                    if (isMobileNumberValid()) {
                        if (!progressDialog.isShowing()) {
                            progressDialog.setMessage(getString(R.string.title_signing_in_user));
                            progressDialog.show();
                        }
                        attemptLoginByOTP(edtMobileLogin.getText().toString().trim());
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.error_invalid_mobile), Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (isLoginFormComplete()) {
                        if (!progressDialog.isShowing()) {
                            progressDialog.setMessage(getString(R.string.title_signing_in_user));
                            progressDialog.show();
                        }
                        attemptLoginByEmailPassword(edtEmailLogin.getText().toString().trim(), edtPasswordLogin.getText().toString().trim());
                    }
                }
            }
        });

        Button mEmailRegistrationInButton = (Button) findViewById(R.id.btnRegisterUser);
        mEmailRegistrationInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRegistrationFormComplete()) {
                    if (verifyPassword()) {
                        if (!progressDialog.isShowing()) {
                            progressDialog.setMessage(getString(R.string.title_registering_user));
                            progressDialog.show();
                        }
                        if ((edtReferralCOde.getText().toString().trim()).equals("")) {
                            attemptRegistrationByEmailPassword(edtEmailRegistration.getText().toString().trim(), edtPasswordRegistration.getText().toString().trim());
                        } else {
                            verifyReferralCode(edtReferralCOde.getText().toString().trim());
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.error_passwords_not_matching), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_registration), Toast.LENGTH_LONG).show();
                }
            }
        });

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        // [END phone_auth_callbacks]
    }

    private boolean isRegistrationFormComplete() {
        return (!(edtEmailRegistration.getText().toString().trim()).equals(""))
                && (!((edtPasswordRegistration.getText().toString().trim()).equals("")))
                && (!(edtConfirmPasswordRegistration.getText().toString().trim()).equals(""));
    }

    private boolean isLoginFormComplete() {
        return (!(edtEmailLogin.getText().toString().trim()).equals(""))
                && (!((edtPasswordLogin.getText().toString().trim()).equals("")));
    }

    private boolean isMobileNumberValid() {
        return (edtMobileLogin.getText().toString().trim()).length() == 10;
    }

    private boolean verifyPassword() {
        return ((edtPasswordRegistration.getText().toString().trim()).equals((edtConfirmPasswordRegistration.getText().toString().trim())));
    }

    private void verifyReferralCode(final String referralCode) {
        mDatabase.child(getString(R.string.referral_code_master)).orderByChild(getString(R.string.referral_code)).equalTo(referralCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ReferralCodeMaster referralCodeMaster = ds.getValue(ReferralCodeMaster.class);
                        if (referralCodeMaster.getIsActive() == 1) {
                            referralCodeKey = ds.getKey();
                            attemptRegistrationByEmailPassword(edtEmailRegistration.getText().toString().trim(), edtPasswordRegistration.getText().toString().trim());
                        } else {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(LoginActivity.this, getString(R.string.error_referral_code_expired),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(LoginActivity.this, getString(R.string.error_referral_code_invalid),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success,
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            startEmployerSelectionActivity();
                            // [START_EXCLUDE]
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                edtPasswordLogin.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone]

    private boolean mayRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_SMS)) {
            Snackbar.make(edtEmailLogin, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_SMS}, REQUEST_READ_SMS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_SMS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_SMS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    private void attemptLoginByOTP(String mobileNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobileNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                LoginActivity.this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private void attemptLoginByEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Authentication success.",
                                    Toast.LENGTH_LONG).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                            startEmployerSelectionActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void attemptRegistrationByEmailPassword(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Log.d(TAG, "createUserWithEmail:success");
                            int isReferral = 0;
                            if (!referralCodeKey.equals("")) {
                                isReferral = 1;
                                mDatabase.child(getString(R.string.referral_code_master)).child(referralCodeKey).child(getString(R.string.isActive)).setValue(0);
                            }
                            FirebaseUser user = mAuth.getCurrentUser();
                            writeNewUser(user.getUid(), user.getDisplayName() + "",
                                    user.getEmail(),
                                    "", "", 50, isReferral); //DUMMY COMPANY DATA.
                            Toast.makeText(LoginActivity.this, "Registration success.",
                                    Toast.LENGTH_LONG).show();
                            startEmployerSelectionActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException() + "",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void writeNewUser(String userID, String user_name, String user_email, String company_name, String company_sector, long company_score, int isReferral) {
        User user = new User(userID, user_name, user_email, company_name, company_sector, company_score, isReferral);
        mDatabase.child(getString(R.string.users)).child(userID).setValue(user);
    }

    private void startEmployerSelectionActivity() {
        Intent intent = new Intent(LoginActivity.this, EmployerSelectionActivity.class);
        startActivity(intent);
    }
}

