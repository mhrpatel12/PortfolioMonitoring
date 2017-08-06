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
import com.mihir.portfoliomonitoring.models.User;
import com.msg91.sendotp.library.SendOtpVerification;
import com.msg91.sendotp.library.Verification;
import com.msg91.sendotp.library.VerificationListener;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_SMS;

/**
 * A login screen that offers login via Email/Mobile Number with OTP/regular password
 */
public class LoginActivity extends AppCompatActivity implements VerificationListener {

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
    private EditText edtMobileRegistration;
    private EditText edtPasswordRegistration;
    private EditText edtConfirmPasswordRegistration;

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

    private Verification mVerification;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

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
        edtMobileRegistration = (EditText) findViewById(R.id.edtMobileRegistration);
        edtPasswordRegistration = (EditText) findViewById(R.id.edtPasswordRegistration);
        edtConfirmPasswordRegistration = (EditText) findViewById(R.id.edtConfirmPasswordRegistration);

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
                    mDatabase.child(getString(R.string.users)).orderByChild(getString(R.string.mobile_number)).equalTo(edtMobileLogin.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                attemptLoginByOTP(edtMobileLogin.getText().toString().trim());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(LoginActivity.this, "Yikes !!", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    if (!progressDialog.isShowing()) {
                        progressDialog.setMessage(getString(R.string.title_signing_in_user));
                        progressDialog.show();
                    }
                    attemptLoginByEmailPassword(edtEmailLogin.getText().toString().trim(), edtPasswordLogin.getText().toString().trim());
                }
            }
        });

        Button mEmailRegistrationInButton = (Button) findViewById(R.id.btnRegisterUser);
        mEmailRegistrationInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!progressDialog.isShowing()) {
                    progressDialog.setMessage(getString(R.string.title_registering_user));
                    progressDialog.show();
                }
                attemptRegistrationByEmailPassword(edtEmailRegistration.getText().toString().trim(), edtPasswordRegistration.getText().toString().trim());
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

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLoginByOTP(String mobileNumber) {
        // Reset errors.
        edtEmailLogin.setError(null);
        edtPasswordLogin.setError(null);

        // Store values at the time of the login attempt.
        String email = edtEmailLogin.getText().toString().trim();
        String password = edtPasswordLogin.getText().toString().trim();

        //showProgress(true);
/*        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobileNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                LoginActivity.this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks*/

        mVerification = SendOtpVerification.createSmsVerification
                (SendOtpVerification
                        .config("+91" + mobileNumber)
                        .context(this)
                        .autoVerification(true)
                        .build(), this);

        mVerification.initiate();

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
                            FirebaseUser user = mAuth.getCurrentUser();
                            writeNewUser(user.getUid(), user.getDisplayName() + "",
                                    user.getEmail(), edtMobileRegistration.getText().toString().trim(),
                                    "", "", 50); //DUMMY COMPANY DATA.
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

    private void writeNewUser(String userID, String user_name, String user_email, String user_mobile, String company_name, String company_sector, long company_score) {
        User user = new User(userID, user_name, user_email, user_mobile, company_name, company_sector, company_score);
        mDatabase.child(getString(R.string.users)).child(userID).setValue(user);
    }

    private void startEmployerSelectionActivity() {
        Intent intent = new Intent(LoginActivity.this, EmployerSelectionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onInitiated(String response) {
        Log.d(TAG, "Initialized!" + response);
        //OTP successfully resent/sent.
    }

    @Override
    public void onInitiationFailed(Exception exception) {
        Log.e(TAG, "Verification initialization failed: " + exception.getMessage());
        //sending otp failed.
    }

    @Override
    public void onVerified(String response) {
        Log.d(TAG, "Verified!\n" + response);
        //OTP verified successfully.
    }

    @Override
    public void onVerificationFailed(Exception exception) {
        Log.e(TAG, "Verification failed: " + exception.getMessage());
        //OTP  verification failed.
    }
}

