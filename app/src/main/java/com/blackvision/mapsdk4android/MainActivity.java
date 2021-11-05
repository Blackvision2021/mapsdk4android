package com.blackvision.mapsdk4android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.blackvision.bvmapmodule.area.AreaGreenLayout;
import com.blackvision.bvmapmodule.area.AreaRedLayout;
import com.blackvision.bvmapmodule.area.OnScrollCallback;
import com.blackvision.bvmapmodule.bean.MapIconBean;
import com.blackvision.bvmapmodule.bean.MqMapModel;
import com.blackvision.bvmapmodule.bean.RoomLiveBean;
import com.blackvision.bvmapmodule.livedata.RoomLiveData;
import com.blackvision.bvmapmodule.map.MapIconsLayout;
import com.blackvision.bvmapmodule.room.OnRoomCallback;
import com.blackvision.bvmapmodule.room.RoomView;
import com.blackvision.bvmapmodule.utils.MapUtils;
import com.blackvision.bvmapmodule.zoom.ZoomLayout;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    Button btn1;
    Button btn_2;
    Button btn_3;
    Button btn_4;
    ImageView ivMapView;
    AreaGreenLayout areaGreenLayout;
    AreaRedLayout areaLayout;
    MapIconsLayout iconsLayout;
    ZoomLayout zoomlayout;
    RoomView roomview;
    ImageView iv_route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btn_1);
        btn_2 = findViewById(R.id.btn_2);
        btn_3 = findViewById(R.id.btn_3);
        btn_4 = findViewById(R.id.btn_4);
        ivMapView = findViewById(R.id.iv_mapview);
        areaGreenLayout = findViewById(R.id.area_green_layout);
        areaLayout = findViewById(R.id.area_layout);
        iconsLayout = findViewById(R.id.icons_layout);
        zoomlayout = findViewById(R.id.zoomlayout);
        roomview = findViewById(R.id.roomview);
        iv_route = findViewById(R.id.iv_route);



        roomview.setChooseMode(RoomView.CHOOSE_DOUBLE);


        showMap();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMap();

                areaLayout.setEdit(false);
                areaGreenLayout.setEdit(false);
                roomview.setHuafen(false);
                roomview.setChooseMode(RoomView.CHOOSE_DOUBLE);

            }
        });

        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                areaGreenLayout.setEdit(true);
                areaGreenLayout.addArea(null);
                areaLayout.setEdit(false);
                roomview.setChooseMode(RoomView.CHOOSE_NO);
            }
        });

        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                areaLayout.setEdit(true);
                areaLayout.addWall(null);
                areaLayout.addArea(null);
//                areaLayout.getAreas()

                areaGreenLayout.setEdit(false);
                roomview.setChooseMode(RoomView.CHOOSE_NO);

            }
        });
        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                areaLayout.setEdit(false);
                areaGreenLayout.setEdit(false);
                roomview.setHuafen(true);
            }
        });

        RoomLiveData.getInstance().observe(this, new Observer<RoomLiveBean>() {
            @Override
            public void onChanged(RoomLiveBean roomLiveBean) {
                roomview.setRoom(roomLiveBean);
            }
        });


        //房间绘制成功回调， 在onRoom()设置显示房间号
        roomview.setOnRoomCallback(new OnRoomCallback() {
            @Override
            public void onRoom(List<MapIconBean> rooms) {
                //显示房间
                iconsLayout.setRoomList(rooms);
//                for ()
            }

            @Override
            public void onMove(boolean isMove) {
                //滑动冲突解决
                zoomlayout.setEnabled(!isMove);
            }

            @Override
            public void onRoomLine(int x1, int y1, int x2, int y2) {

            }
        });


        zoomlayout.setZoomLayoutGestureListener(new ZoomLayout.ZoomLayoutGestureListener() {
            @Override
            public void onScroll(int x, int y, float scale) {
                //传递缩放倍率
                iconsLayout.setScale(scale, x, y);
                areaLayout.setScale(x, y, scale);
                areaGreenLayout.setScale(x, y, scale);
            }
        });

        //滑动冲突处理
        areaGreenLayout.setOnScrollCallback(new OnScrollCallback() {
            @Override
            public void onScroll(boolean isScroll) {
                zoomlayout.setEnabled(!isScroll);
            }
        });
        areaLayout.setOnScrollCallback(new OnScrollCallback() {
            @Override
            public void onScroll(boolean isScroll) {
                zoomlayout.setEnabled(!isScroll);
            }
        });
    }

    private void showMap() {
        try {
            //读取地图文件
//            byte[] s = InputStreamTOByte(getResources().getAssets().open("210901172112006s523.bv"));
//            //解压缩成json字符串
//            String mMessage = GZipUtil.uncompressToString(s, GZipUtil.GZIP_ENCODE_UTF_8);


            String mMessage = getFromAssets1("json(1).log");


            //转bean
            MqMapModel.ParamBean model  =new Gson().fromJson(mMessage,MqMapModel.ParamBean.class);

            //先生成bitpmap 再用imageview显示
            Bitmap bitmap = MapUtils.createMapBitmap(MainActivity.this, model ,null);
            ivMapView.setImageBitmap(bitmap);
            //init划区
            areaGreenLayout.setMapInfo(model);
            //init禁区
            areaLayout.setMapInfo(model);
            //显示机器人图标
            iconsLayout.resetView(model);

            if (model.getTraceInfo().getData()!=null){
                Bitmap bitmap1 =  MapUtils.createPathBitmap(this,model);
                iv_route.setImageBitmap(bitmap1);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static byte[] InputStreamTOByte(InputStream in) throws IOException{

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[20000];
        int count = -1;
        while((count = in.read(data,0,20000)) != -1)
            outStream.write(data, 0, count);
        data = null;
        return outStream.toByteArray();
    }

    public String getFromAssets(String fileName) {
        String content = null; //结果字符串
        try {
            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName), "ISO_8859_1" );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            StringBuilder builder = new StringBuilder();
            while((line = bufReader.readLine()) != null){
                builder.append(line);
            }
            inputReader.close();
            bufReader.close();
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public String getFromAssets1(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";

        }}
}