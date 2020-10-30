package de.bittim.bitwarn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Menu extends AppCompatActivity {
    ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initBtn();
    }

    public void initBtn()
    {
        backBtn = (ImageView) findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                onBackBtn();
            }
        });
    }

    private void onBackBtn()
    {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}