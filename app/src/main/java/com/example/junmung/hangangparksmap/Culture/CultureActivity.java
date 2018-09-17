package com.example.junmung.hangangparksmap.Culture;

import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.junmung.hangangparksmap.R;

public class CultureActivity extends AppCompatActivity {
    // 행사목록 아이템
    // 아이템내에 즐겨찾기버튼, 기간 지난건 보여주지않기,
    // 최근일자 순서로 보여주고
    // 필터가 굉장히 중요!
    // 필터에 즐겨찾기를 넣는게 나을듯 - 액션바에서 컨트롤 하기 위함
    // 지역, 일자, 즐겨찾기

    // 클릭했을때 아래로 펼치는 형식? 새로운 액티비티?


    //  툴바 커스텀 작업 하기



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_culture);

        getID_SetListener();

    }

    private void getID_SetListener() {




    }
}
