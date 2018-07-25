package com.example.junmung.hangangparksmap.Map.Dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.junmung.hangangparksmap.R;


public class FilterDialogFragment extends DialogFragment{
    private ViewPager viewPager;
    private BottomNavigationView tabLayout;
    private MenuItem tabMenuItem;


    public FilterDialogFragment() { }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_filter_dialog, container, false);

        viewPager = view.findViewById(R.id.fragment_map_filter_dialog_viewPager);
        tabLayout = view.findViewById(R.id.fragment_map_filter_dialog_bottomNavigationView);


        // 뷰페이저 세팅
        viewPager.setAdapter(new PagerAdapter(getChildFragmentManager()));
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(0);

        tabLayout.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.item_tab_filter:    viewPager.setCurrentItem(0);    return true;
                    case R.id.item_tab_favorite:   viewPager.setCurrentItem(1);    return true;
                }
                return false;
            }
        });
        return view;
    }


    // 페이지 변경 리스너
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if(tabMenuItem != null)
                tabMenuItem.setChecked(false);
            Log.d("position", ""+position);

            tabMenuItem = tabLayout.getMenu().getItem(position);
            tabMenuItem.setChecked(true);

            // 인덱스 오버플로우가 발생하니까 position에 따라서 처리해주는 로직이 필요할듯
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    // 뷰페이저 Adapter
    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:     return new FilterListFragment();
                case 1:     return new FavoriteListFragment();
                default:    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
