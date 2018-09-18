package com.example.junmung.hangangparksmap.Culture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.junmung.hangangparksmap.R;

import java.util.ArrayList;
import java.util.List;

public class CultureActivity extends AppCompatActivity {
    // 행사목록 아이템
    // 아이템내에 즐겨찾기버튼, 기간 지난건 보여주지않기,
    // 최근일자 순서로 보여주고
    // 필터에 즐겨찾기를 넣는게 나을듯 - 액션바에서 컨트롤 하기 위함
    // 지역, 일자, 즐겨찾기

    // 클릭했을때 아래로 펼치는 형식? 새로운 액티비티?


    //  툴바 커스텀 작업 하기

    // 디비에서 리스트 받아오기
    // item 구성
    // 플로팅버튼 추가 ?

    private RecyclerView recyclerView;
    private CultureItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_culture);
        Toolbar toolbar = findViewById(R.id.activity_culture_toolbar);
        setSupportActionBar(toolbar);
        getID_SetListener();

        List<CultureItem> items = new ArrayList();

        items.add(new CultureItem("AAAA1"));
        items.add(new CultureItem("AAAA2"));
        items.add(new CultureItem("AAAA3"));
        items.add(new CultureItem("AAAA4"));
        items.add(new CultureItem("AAAA5"));
        items.add(new CultureItem("AAAA6"));
        items.add(new CultureItem("AAAA7"));
        items.add(new CultureItem("AAAA8"));
        items.add(new CultureItem("AAAA9"));
        items.add(new CultureItem("AAAA10"));


        itemAdapter = new CultureItemAdapter(items);
        recyclerView.setAdapter(itemAdapter);

    }

    private void getID_SetListener() {
        recyclerView = findViewById(R.id.activity_culture_recyclerView);

    }
}
