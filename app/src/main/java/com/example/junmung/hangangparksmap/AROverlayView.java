package com.example.junmung.hangangparksmap;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;
import android.view.View;


public class AROverlayView extends View {
    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private MapPoint currentPoint;
    private MapPoint destPoint;



    public AROverlayView(Context context, MapPoint firstPoint) {
        super(context);
        this.context = context;
        destPoint = firstPoint;
    }




    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentPoint(MapPoint updatedPoint){
        currentPoint = updatedPoint;
        invalidate();
    }

    public void updateDestPoint(MapPoint destPoint){
        this.destPoint = destPoint;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentPoint == null) {
            return;
        }
        destPoint.getLocation().setAltitude(0);
        currentPoint.getLocation().setAltitude(0);

        // 지점 만들기
        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentPoint.getLocation());
        float[] pointInECEF = LocationHelper.WSG84toECEF(destPoint.getLocation());
        float[] pointInENU = LocationHelper.ECEFtoENU(currentPoint.getLocation(), currentLocationInECEF, pointInECEF);

        float[] cameraCoordinateVector = new float[4];
        Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

        // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
        // if z > 0, the point will display on the opposite
        if (cameraCoordinateVector[2] < 0) {
            float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
            float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();
            int distance = currentPoint.distanceTo(destPoint);


            canvas.drawCircle(x, y, radius, paint);
            canvas.drawText(destPoint.getName() + "\n( "+distance+"m )", x - (30 * destPoint.getName().length() / 2), y - 80, paint);
        }
    }


}
