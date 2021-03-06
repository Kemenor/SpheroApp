package ch.fhnw.edu.emoba.spheroapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.fhnw.edu.emoba.spherolib.SpheroRobotFactory;
import ch.fhnw.edu.emoba.spherolib.SpheroRobotProxy;

public class TouchView extends View {

    private int deadZoneRadius = 50;

    private Paint linePaint;
    private Paint circlePaint;

    private float currentX;
    private float currentY;

    private int maxRadius;
    private double minVelocity;
    private double maxVelocity;
    private ScheduledFuture viewTask;

    SpheroRobotProxy spheroRobotProxy = SpheroRobotFactory.getActualRobotProxy();

    private AtomicBoolean isCancelled = new AtomicBoolean();

    public TouchView(Context context) {
        super(context);

        linePaint = new Paint();
        linePaint.setStrokeWidth(10);
        linePaint.setColor(Color.YELLOW);

        circlePaint = new Paint();
        circlePaint.setStrokeWidth(5);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.WHITE);

        // wait until height and width are set to calculate middle
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                reset();
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        ScheduledExecutorService scheduler;
        // Created scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Start View-Task after a delay of 50ms with an interval of 50ms
        viewTask = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                postInvalidate();
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        viewTask.cancel(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(getResources().getColor(R.color.colorPrimary));
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, deadZoneRadius, circlePaint);
        //canvas.drawCircle(getWidth() / 2, getHeight() / 2, maxRadius, circlePaint);
        canvas.drawLine(getWidth() / 2, getHeight() / 2, currentX, currentY, linePaint);
        canvas.drawCircle(currentX, currentY, 10, linePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        currentX = event.getX();
        currentY = event.getY();

        double deltaX = currentX - getWidth() / 2;
        double deltaY = currentY - getHeight() / 2;
        double rad = Math.atan2(-deltaX, deltaY); // start 0° at the top
        double heading = rad * (180 / Math.PI) + 180;


        double velocity = Math.pow(deltaX, 2) + Math.pow(deltaY, 2) - minVelocity;
        if(velocity < minVelocity){
            velocity = 0;
        }

        double speed = velocity / (maxVelocity - minVelocity);
        if(speed > 1){
            speed = 1;
        }

        spheroRobotProxy.drive((float)heading, (float)speed);

        return true;
    }

    public void reset(){
        spheroRobotProxy.drive(0, 0);

        currentX = getWidth() / 2;
        currentY = getHeight() / 2;

        int shorterEdgeLength = getWidth() < getHeight() ? getWidth() : getHeight();
        maxRadius = shorterEdgeLength / 2;
        minVelocity = Math.pow(deadZoneRadius, 2);
        maxVelocity = Math.pow(maxRadius, 2);
    }
}
