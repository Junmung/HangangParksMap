package com.example.junmung.hangangparksmap;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;


public class AROverlayView extends View {
    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    ARPoint arPoint;



    public AROverlayView(Context context) {
        super(context);

        this.context = context;

        //Demo points
        arPoint = new ARPoint("사가정역", 37.580547,127.088488, 0);

//        arPoint = new ARPoint("한강씨름장", 37.5197932, 126.9419335, 0);

    }




    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location location){
        currentLocation = location;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        // 지점 만들기
        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
        float[] pointInECEF = LocationHelper.WSG84toECEF(arPoint.getLocation());
        float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

        float[] cameraCoordinateVector = new float[4];
        Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

        // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
        // if z > 0, the point will display on the opposite
        if (cameraCoordinateVector[2] < 0) {
            float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
            float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

            canvas.drawCircle(x, y, radius, paint);
            canvas.drawText(arPoint.getName(), x - (30 * arPoint.getName().length() / 2), y - 80, paint);
        }
    }


}
