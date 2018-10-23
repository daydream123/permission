package com.feizhang.permission.sample;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.feizhang.permission.OnEachGrantResult;
import com.feizhang.permission.OnGrantResult;
import com.feizhang.permission.Permission;
import com.feizhang.permission.Permissions;

public class MainActivity extends AppCompatActivity {
    private Permissions mPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init permissions
        mPermissions = new Permissions(this);

        Button requestBtn = findViewById(R.id.requestBtn);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPermissions.request(new OnGrantResult() {
                    @Override
                    public void onGrant() {
                        // do something when grant
                    }

                     @Override
                     public void onDenied() {
                         // do something when denied
                     }
                }, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE);
            }
        });

        Button requestEachBtn = findViewById(R.id.requestEachBtn);
        requestEachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPermissions.requestEach(new OnEachGrantResult() {
                    @Override
                    public void onNext(Permission permission) {
                        // check someone permission and
                        // its grant state and do something you want
                    }
                }, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE);
            }
        });
    }
}
