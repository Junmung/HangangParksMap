package com.example.junmung.hangangparksmap.Map.Dialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.example.junmung.hangangparksmap.R;

import java.util.ArrayList;
import java.util.List;

public class FilterListFragment extends Fragment {
    static final int LIST_SIZE = 9;
    private GridView gridView;

    private List<FilterItem> filterItems;

    public FilterListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_filter_dialog_filter_list, container, false);
        gridView = view.findViewById(R.id.dialog_filter_list_gridView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        filterItems= new ArrayList<>(LIST_SIZE);

        for(int i = 0; i < LIST_SIZE; i++){
            FilterItem item = new FilterItem("테스트", BitmapFactory.decodeResource(getResources(), R.drawable.star_black_48));
            filterItems.add(item);
        }

        FilterItemAdapter adapter = new FilterItemAdapter(filterItems);
        gridView.setAdapter(adapter);
    }
}
