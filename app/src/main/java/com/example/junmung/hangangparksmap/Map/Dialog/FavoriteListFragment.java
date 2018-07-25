package com.example.junmung.hangangparksmap.Map.Dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.junmung.hangangparksmap.R;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListFragment extends Fragment {
    private ListView listView;
    private List<String> items;

    public FavoriteListFragment() {
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_filter_dialog_favorite_list, container, false);
        listView = view.findViewById(R.id.dialog_favorite_list_recyclerView);

        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        items = new ArrayList<>();

        for(int i = 0; i < 10; i++){
            items.add("자전거 대여소");
        }
        ArrayAdapter adapter = new ArrayAdapter(getContext().getApplicationContext(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }
}
