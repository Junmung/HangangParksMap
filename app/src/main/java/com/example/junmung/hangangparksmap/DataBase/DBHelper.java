package com.example.junmung.hangangparksmap.DataBase;

import android.util.Log;

import com.example.junmung.hangangparksmap.CulturePoint;
import com.example.junmung.hangangparksmap.CulturePointPOJO.Row;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class DBHelper {
    private static DBHelper ourInstance = new DBHelper();

    public static DBHelper getInstance() {
        return ourInstance;
    }

    Realm realm;

    public DBHelper() {
        realm = Realm.getDefaultInstance();
    }

    public Realm getRealmInstance() {
        return realm;
    }

    // 새로 추가하는 부분
    public void insertCultureInfos(ArrayList<Row> rows) {
        RealmList<CultureInfo> infos = new RealmList<>();

        int i =0;
        for(Row row: rows) {
            infos.add(new CultureInfo(i, row.getContentsName(), row.getAddress(), row.getLatitude(), row.getLongitude(),
                    row.getEventPark(), row.getEventDate(), row.getEventTime(), row.getCoordinateType()));

            Log.d("Realm _ " + i, row.getContentsName());

            i++;
        }
        Log.d("Realm Size", ""+rows.size());

        realm.beginTransaction();
        realm.insert(infos);
        realm.commitTransaction();

    }

    // 현제 테이블에 있는 모든 Member를 리스트로 받는 부분
    public ArrayList<CulturePoint> getCultureItems() {
        RealmResults<CultureInfo> infos = realm.where(CultureInfo.class).findAll().sort("index", Sort.DESCENDING);
        ArrayList<CulturePoint> culturePoints = new ArrayList<>();

        // 질의한 결과를 RecyclerView에서 이용할 수 있도록 arrayList에 넣어주는 부분
        int i = 0;
        for( CultureInfo info : infos){
            culturePoints.add(new CulturePoint(info.getContentsName(), info.getLatitude(), info.getLongitude(), 0, info.getAddress(),
                    info.getParkName(), info.getEventDate(), info.getEventTime(), info.getPointType()));
            i++;
        }

        return culturePoints;
    }

    public void deleteAll(){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<CultureInfo> memos = realm.where(CultureInfo.class).findAll();

                memos.deleteAllFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d("Realm _ ", "삭제되었습니다.");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });

    }

}
