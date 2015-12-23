package com.integreight.onesheeld.utils.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Author: Mostafa Mahmoud
 * Email: mostafa_mahmoud@protonmail.com
 * Created on: 12/13/15
 */

public class AnalogStickView extends View{
    private AnalogStickTouchListener analogStickTouchListener;
    private Handler handler = new Handler();
    private Runnable returnToCenter;
    private Runnable backToCenter;
    private int outerAnalogStickRadius;
    private int innerAnalogStickRadius;
    private int centerX;
    private int centerY;
    private int xPosition;
    private int yPosition;
    private int angle;
    private int power;
    private Paint outerCircle;
    private Paint line;
    private Paint stick;
    private static final double RAD = 57.2957795;


    public AnalogStickView(Context context) {
        super(context);
        initAnalogStickView();
    }

    public AnalogStickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAnalogStickView();
    }

    public AnalogStickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAnalogStickView();
    }

    public interface AnalogStickTouchListener {
        void onValueChange(int x, int y, int angle, int power, int direction);
    }

    public void setTouchListener(AnalogStickTouchListener listener) {
        this.analogStickTouchListener = listener;
    }

    private void initAnalogStickView() {
        outerCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerCircle.setColor(Color.GRAY);
        outerCircle.setStyle(Paint.Style.FILL_AND_STROKE);

        line = new Paint();
        line.setStrokeWidth(5);
        line.setColor(Color.LTGRAY);

        stick = new Paint(Paint.ANTI_ALIAS_FLAG);
        stick.setColor(Color.DKGRAY);
        stick.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        xPosition = centerX;
        yPosition = centerY;
        int diameter = Math.min(w, h);
        innerAnalogStickRadius = (int) (diameter / 2 * 0.25);
        outerAnalogStickRadius = (int) (diameter / 2 * 0.75);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawCircle(centerX, centerY, outerAnalogStickRadius, outerCircle);

        canvas.drawLine((float) (centerX - outerAnalogStickRadius), (float) centerY,
                (float) (centerX - innerAnalogStickRadius), (float) centerY, line);
        canvas.drawLine((float) (centerX + innerAnalogStickRadius), (float) centerY,
                (float) (centerX + outerAnalogStickRadius), (float) centerY, line);
        canvas.drawLine((float) (centerX), (float) (centerY - innerAnalogStickRadius),
                (float) (centerX), (float) (centerY - outerAnalogStickRadius), line);
        canvas.drawLine((float) centerX, (float) (centerY + innerAnalogStickRadius),
                (float) centerX, (float) (centerY + outerAnalogStickRadius), line);

        canvas.drawCircle(xPosition, yPosition, innerAnalogStickRadius, stick);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                moveAnalog((int)event.getX(),(int)event.getY());
                break;

            case MotionEvent.ACTION_UP:
                final int numberOfFrames = 5;
                final double xinterval = (centerX - xPosition) / numberOfFrames;
                final double yinterval = (centerY - yPosition) / numberOfFrames;
                returnToCenter = new Runnable() {
                    @Override
                    public void run() {
                        xPosition += xinterval;
                        yPosition += yinterval;
                        if (analogStickTouchListener != null)
                            analogStickTouchListener.onValueChange(getPositionX(), getPositionY(),
                                    getAngle(), getPower(), getDirection());
                        invalidate();
                    }
                };
                backToCenter  = new Runnable() {
                    @Override
                    public void run() {
                        xPosition = centerX;
                        yPosition = centerY;
                        if (analogStickTouchListener != null)
                            analogStickTouchListener.onValueChange(getPositionX(), getPositionY(),
                                    getAngle(), getPower(), getDirection());
                        invalidate();
                    }
                };


                for (int i = 0; i < numberOfFrames; i++) {
                    handler.postDelayed(returnToCenter, i * 30);
                }
                // Extra post to ensure that x position and y position return to the center
                handler.postDelayed(backToCenter,numberOfFrames*30);
                break;

            case MotionEvent.ACTION_DOWN:
                abortReturn();
                moveAnalog((int) event.getX(), (int) event.getY());
                break;
        }
        return true;
    }

    private int getAngle() {
        angle = (int) Math.toDegrees(Math.atan2(yPosition - centerY,xPosition - centerX));

        if(angle < 0){
            angle += 360;
        }

        if (angle != 0)
            angle = 360 - angle;

        return angle;
    }

    private int getPower() {
        power = (int) (100 * Math.sqrt((xPosition - centerX)
                * (xPosition - centerX) + (yPosition - centerY)
                * (yPosition - centerY)) / outerAnalogStickRadius);
        return power;
    }

    private int getDirection() {
        int direction = 0;
        if (power == 0)
            direction = 0;
        else if (angle > 338 || angle <= 22)
            direction = 1;
        else if (angle > 22 && angle <= 68)
            direction = 2;
        else if (angle > 68 && angle <= 112)
            direction = 3;
        else if (angle > 112 && angle <= 158)
            direction = 4;
        else if (angle > 158 && angle <= 202)
            direction = 5;
        else if (angle > 202 && angle <= 248)
            direction = 6;
        else if (angle > 248 && angle <= 292)
            direction = 7;
        else if (angle > 292 && angle <= 338)
            direction = 8;
        return direction;
    }

    private int getPositionX(){
        return (int)((float)(outerAnalogStickRadius +xPosition-centerX)/(2* outerAnalogStickRadius)*255);
    }
    private int getPositionY(){
        return (int)((float)(outerAnalogStickRadius +yPosition-centerY)/(2* outerAnalogStickRadius)*255);
    }
    private void abortReturn(){
        handler.removeCallbacks(returnToCenter);
        handler.removeCallbacks(backToCenter);
    }
    private void moveAnalog(int x , int y) {
        xPosition = x;
        yPosition = y;
        double distance = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (distance > outerAnalogStickRadius) {
            xPosition = (int) ((xPosition - centerX) * outerAnalogStickRadius / distance + centerX);
            yPosition = (int) ((yPosition - centerY) * outerAnalogStickRadius / distance + centerY);
        }
        if (analogStickTouchListener != null)
            analogStickTouchListener.onValueChange(getPositionX(), getPositionY(),
                    getAngle(), getPower(), getDirection());
        invalidate();
    }

}