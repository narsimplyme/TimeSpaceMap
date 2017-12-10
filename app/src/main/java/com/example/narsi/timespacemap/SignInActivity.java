package com.example.narsi.timespacemap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.narsi.timespacemap.models.User;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";

    private DatabaseReference mDatabase;//데이터베이스 레퍼런스 변수
    private FirebaseAuth mAuth;//파이어베이스 권한 변수

    private EditText mEmailField;//아이디 변수
    private EditText mPasswordField;//비밀번호변수
    private Button mSignInButton;//signin버튼
    private Button mSignUpButton;//signup 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mDatabase = FirebaseDatabase.getInstance().getReference(); //파이어베이스에서 레퍼런스 변수가져오기
        mAuth = FirebaseAuth.getInstance();//권한 획득 변수에다 저장

        // Views
        mEmailField = findViewById(R.id.field_email);//이메일 필드에 작성된 것 가져오기
        mPasswordField = findViewById(R.id.field_password);//페스워드 필드에 작성된거 가져오기
        mSignInButton = findViewById(R.id.button_sign_in);//signinbutton 가져오기
        mSignUpButton = findViewById(R.id.button_sign_up);//signupbutton 가져오기

        // Click listeners
        mSignInButton.setOnClickListener(this);
        mSignUpButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    private void signIn() {
        Log.d(TAG, "signIn");
        if (!validateForm()) {//이메일 형식에 맞지 않는다면
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();//변수에 이메일 넣기
        String password = mPasswordField.getText().toString();//변수에 페스워드 넣기

        mAuth.signInWithEmailAndPassword(email, password)//권한 확인
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {//signin 성공시
                            onAuthSuccess(task.getResult().getUser());//권한 획득
                        } else {
                            Toast.makeText(SignInActivity.this, "Sign In Failed",//실패한것 뿌리기
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUp() {
        Log.d(TAG, "signUp");
        if (!validateForm()) {//형식에 맞지않으면(이메일형식)
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();//위랑 마찬가지
        String password = mPasswordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());//위랑 마찬가지
                        } else {
                            Toast.makeText(SignInActivity.this, "Sign Up Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());//유저 이메일 형식 string변수에

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to MainActivity
        startActivity(new Intent(SignInActivity.this, MapsActivity.class));
        finish();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {//이메일 필드가 비어있을때
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {//비밀번호 필드가 비어있을때
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        return result;
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);//신규 유저일때

        mDatabase.child("users").child(userId).setValue(user);//데이터베이스의 필드에 값 저장
    }
    // [END basic_write]

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_sign_in) {
            signIn();
        } else if (i == R.id.button_sign_up) {
            signUp();
        }
    }
}
