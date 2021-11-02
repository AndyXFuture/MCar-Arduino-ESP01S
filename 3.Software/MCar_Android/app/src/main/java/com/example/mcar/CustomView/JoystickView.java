package com.example.mcar.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.mcar.R;

/**
 * Created by Administrator on 2015/12/9.
 */
public class JoystickView extends LinearLayout {
    private View inflate;
    private int backgroundDiameter;
    private int joystickDiameter;
    private FrameLayout.LayoutParams layoutParams;
    private ImageView ivJoystick;
    private ImageView ivBackground;
    private JoyStickListener listener = null; // 事件回调接口
    public JoystickView(Context context) {
        super(context);
        inflate = LayoutInflater.from(context).inflate(R.layout.joystick_layout, this, true);
        ivJoystick = (ImageView) inflate.findViewById(R.id.ivJoystick);
        ivBackground = (ImageView) inflate.findViewById(R.id.ivBackground);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate = LayoutInflater.from(context).inflate(R.layout.joystick_layout, this, true);
        ivJoystick = (ImageView) inflate.findViewById(R.id.ivJoystick);
        ivBackground = (ImageView) inflate.findViewById(R.id.ivBackground);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.JoystickView);
        backgroundDiameter = (int) array.getDimension(R.styleable.JoystickView_backgroundDiameter, 40);//第一个是传递参数，第二个是默认值
        joystickDiameter = (int) array.getDimension(R.styleable.JoystickView_joystickDiameter, 40);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initBackground(0, 0);
        layoutParams = (FrameLayout.LayoutParams) ivJoystick
                .getLayoutParams();
        layoutParams.height = joystickDiameter;
        layoutParams.width = joystickDiameter;
        ivJoystick.setLayoutParams(layoutParams);
        //设置摇杆图标隐藏
        ivJoystick.setVisibility(INVISIBLE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int xOrigin = getWidth() / 2 - joystickDiameter / 2;
        int yOrigin = getHeight() / 2 - joystickDiameter / 2;
        int x_touch = (int) event.getX() - joystickDiameter / 2;
        int y_touch = (int) event.getY() - joystickDiameter / 2;
        int limit = backgroundDiameter / 2 - joystickDiameter / 2;

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            ivJoystick.setVisibility(VISIBLE);
            if (Math.sqrt(Math.pow((xOrigin - x_touch), 2) + Math.pow((yOrigin - y_touch), 2)) >= limit) {
                //得到摇杆与触屏点所形成的角度
                double tempRad = getRad(xOrigin, yOrigin, x_touch, y_touch);
                //保证内部小圆运动的长度限制
                getXY(xOrigin, yOrigin, limit, tempRad);
            } else {//如果小球中心点小于活动区域则随着用户触屏点移动即可
                Stickmove(x_touch, y_touch);
                DataTo16(x_touch - xOrigin , y_touch - yOrigin , xOrigin);
                //if (listener!=null) {
                //    listener.onSteeringWheelChanged(x_touch - xOrigin , y_touch - yOrigin);
                //}
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //当释放按键时摇杆要恢复摇杆的位置为初始位置
            Stickmove(xOrigin, yOrigin);
                if (listener!=null) {
                    listener.onSteeringWheelChanged(0,0);
                }
            //ivJoystick.setVisibility(INVISIBLE);
        }

        return true;
    }

    private void DataTo16(int x,int y, int max){
        float temp = max/16;

        int x1 = (int)(x/temp);
        int x2 = (int)(y/temp);

        if(x1 > 16){x1 = 16;}
        if(x2 > 16){x2 = 16;}
        if(x1 < -16){x1 = -16;}
        if(x2 < -16){x2 = -16;}
        if (listener!=null) {
            listener.onSteeringWheelChanged(x1, x2);
        }
    }


    private void initBackground(int x, int y) {//将背景圆移动到中心
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) ivBackground
                .getLayoutParams();
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;
        layoutParams.width = backgroundDiameter;
        layoutParams.height = backgroundDiameter;
        ivBackground.setLayoutParams(layoutParams);
    }

    private void Stickmove(int x, int y) {
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;
        ivJoystick.setLayoutParams(layoutParams);
    }

    /***
     * 得到两点之间的弧度
     */
    public double getRad(float px1, float py1, float px2, float py2) {
        //得到两点X的距离
        float x = px2 - px1;
        //得到两点Y的距离
        float y = py1 - py2;
        //算出斜边长
        float xie = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        //得到这个角度的余弦值（通过三角函数中的定理 ：邻边/斜边=角度余弦值）
        float cosAngle = x / xie;
        //通过反余弦定理获取到其角度的弧度
        float rad = (float) Math.acos(cosAngle);
        //注意：当触屏的位置Y坐标<摇杆的Y坐标我们要取反值-0~-180
        if (py2 < py1) {
            rad = -rad;
        }
        return rad;
    }

    //函数：getXY
    //功能：限制joystick移动范围不得大于R
    //参数：centerX，centerY圆心的x,y
    //     rad触点与圆心形成的直线与水平线之间的夹角
    public void getXY(float xOrigin, float yOrigin, float R, double rad) {
        int x, y;
        //获取圆周运动的X坐标
        x = (int) ((float) (R * Math.cos(rad)) + xOrigin);
        //获取圆周运动的Y坐标
        y = (int) ((float) (R * Math.sin(rad)) + yOrigin);
        Stickmove(x, y);
        DataTo16(x - (int)xOrigin , y - (int)yOrigin , (int)xOrigin);
        //if (listener!=null) {
        //    listener.onSteeringWheelChanged( x - (int)xOrigin, y - (int)yOrigin);
        //}
    }

    // 设置回调接口
    public void setJoystickListener(JoyStickListener rockerListener)
    {
        listener = rockerListener;
    }
    // 回调接口
    public interface JoyStickListener
    {
        void onSteeringWheelChanged(int x, int y);
    }


}
