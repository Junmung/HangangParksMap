package com.example.junmung.hangangparksmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.junmung.hangangparksmap.ARGuide.ARGuideActivity;

public class MainActivity extends AppCompatActivity {
    Button arguide, map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arguide = findViewById(R.id.btn);
        arguide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btn){
                    Intent intent = new Intent(MainActivity.this, ARGuideActivity.class);
                    intent.putExtra("Destination", "목적지");
                    intent.putExtra("Latitude", 37.579540d);
                    intent.putExtra("Longitude", 127.086526d);
                    startActivity(intent);
                }
            }
        });

        map = findViewById(R.id.btn_map);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btn_map){
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
