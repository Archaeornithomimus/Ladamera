package com.valentin.menage.radar;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

public class MyView extends View {
    Paint paint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
    Path starPath;
    Path curvePath;
    Paint textPaint = new Paint(Paint.LINEAR_TEXT_FLAG);

    public MyView(Context context)
    {
        super(context);

        // create star path
        starPath = createStarPath(300,  500);
        curvePath = createCurvePath();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        // draw basic shapes
        canvas.drawLine(5, 5, 200, 5, paint);
        canvas.drawLine(5, 15, 200, 15, paint);
        canvas.drawLine(5, 25, 200, 25, paint);

        paint.setColor(Color.YELLOW);
        canvas.drawCircle(50, 70, 35, paint);

        paint.setColor(Color.GREEN);
        canvas.drawRect(new Rect(100, 60, 150, 80), paint);

        paint.setColor(Color.DKGRAY);
        canvas.drawOval(new RectF(160, 60, 250, 80), paint);

        // draw text
        textPaint.setColor(Color.MAGENTA);
        textPaint.setTextSize(40);
        canvas.drawText("Hello World!", 20, 190, textPaint);
        //  transparency
        textPaint.setColor(0xFF465574);
        textPaint.setTextSize(60);
        canvas.drawText("Android Rocks", 20, 340, textPaint);

        // opaque circle
        canvas.drawCircle(80, 300, 20, paint);

        // semi-transparent circle
        paint.setAlpha(110);
        canvas.drawCircle(160, 300, 39, paint);
        paint.setColor(Color.YELLOW);
        paint.setAlpha(140);
        canvas.drawCircle(240, 330, 30, paint);
        paint.setColor(Color.MAGENTA);
        paint.setAlpha(30);
        canvas.drawCircle(288, 350, 30, paint);
        paint.setColor(Color.CYAN);
        paint.setAlpha(100);
        canvas.drawCircle(380, 330, 50, paint);

        // draw text on path
        textPaint.setColor(Color.rgb(155, 20, 10));
        canvas.drawTextOnPath("BTS SN LASALLE 84", curvePath, 10, 10, textPaint);

        // create a star-shaped clip
        canvas.drawPath(starPath, textPaint);
        textPaint.setColor(Color.CYAN);
        canvas.clipPath(starPath);
        //textPaint.setColor(Color.parseColor("yellow"));
        //canvas.drawText("Android", 350, 550, textPaint);
        textPaint.setColor(Color.parseColor("#abde97"));
        canvas.drawText("Android", 400, 600, textPaint);
        canvas.drawText("Android Rocks", 300, 650, textPaint);
        canvas.drawText("Android Rocks", 320, 700, textPaint);
        canvas.drawText("Android Rocks", 360, 750, textPaint);
        canvas.drawText("Android Rocks", 320, 800, textPaint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        Rect r = canvas.getClipBounds();
        canvas.drawRect(r, paint);
    }

    private Path createStarPath(int x,  int y)
    {
        Path path = new Path();
        path.moveTo(0 + x, 150 + y);
        path.lineTo(120 + x, 140 + y);
        path.lineTo(150 + x, 0 + y);
        path.lineTo(180 + x, 140 + y);
        path.lineTo(300 + x, 150 + y);
        path.lineTo(200 + x, 190 + y);
        path.lineTo(250 + x, 300 + y);
        path.lineTo(150 + x, 220 + y);
        path.lineTo(50 + x, 300 + y);
        path.lineTo(100 + x, 190 + y);
        path.lineTo(0 + x, 150 + y);
        return  path;
    }

    private Path createCurvePath()
    {
        Path path = new Path();
        path.addArc(new RectF(400, 40, 780, 300), -210, 230);
        return  path;
    }

}