package com.example.team_project.Environment.Store.Payment;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.team_project.R;

public class PaymentCompleteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_complete);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PaymentCompleteFragment())
                .commit();
        }
    }
}