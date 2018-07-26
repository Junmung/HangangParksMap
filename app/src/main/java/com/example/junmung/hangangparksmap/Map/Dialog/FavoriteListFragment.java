package com.example.junmung.hangangparksmap.Map.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.junmung.hangangparksmap.ARGuide.Point;
import com.example.junmung.hangangparksmap.R;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListFragment extends Fragment {
    private ListView listView;
    private List<String> items;
    private FavoriteItemOnClickListener favoriteClickListener;

    public FavoriteListFragment() {
    }

    public interface FavoriteItemOnClickListener {
        void onFavoriteItemClicked(Point point);
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
        listView.setOnItemClickListener(itemClickListener);
        listView.setAdapter(adapter);
    }

    private ListView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // favorite item 은 realm 을 기반으로 데이터를 가져오기 때문에
            // 이미 만들어져있는 realm 데이터를 클릭했을때 보내줘야한다.
            // 즉 여기서 넘겨줘야 하는 위치인 position 값은 realm 에서의 index 로 구성을 해야한다.
            Point point = new Point("즐겨찾기", 0, 0, 0);
            favoriteClickListener.onFavoriteItemClicked(point);

//            Point point = (Point)parent.getItemAtPosition(position);
//            favoriteClickListener.onFavoriteItemClicked(Realm(position));

        }
    };






    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToParentFragment(getParentFragment());
    }

    private void onAttachToParentFragment(Fragment parentFragment) {
        try {
            favoriteClickListener = (FavoriteItemOnClickListener)parentFragment;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(
                    parentFragment.toString() + " must implement FavoriteItemOnClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        favoriteClickListener = null;
    }
}
