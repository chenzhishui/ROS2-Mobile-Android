package com.schneewittchen.rosandroid.widgets.xinference;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.ui.views.widgets.SubscriberWidgetView;

import org.ros2.rcljava.interfaces.MessageDefinition;

import javax.annotation.Nullable;


/**
 * View for displaying Xinference AI model inference results.
 *
 * @author Copilot
 * @version 1.0.0
 * @created on 18.12.2024
 */

public class XinferenceView extends SubscriberWidgetView {

    public static final String TAG = XinferenceView.class.getSimpleName();

    TextPaint textPaint;
    Paint backgroundPaint;
    StaticLayout staticLayout;

    public XinferenceView(Context context) {
        super(context);
        init();
    }

    public XinferenceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#1E1E1E"));
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(16 * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onNewMessage(MessageDefinition message) {
        super.onNewMessage(message);

        std_msgs.msg.String stringMessage = (std_msgs.msg.String) message;
        XinferenceEntity entity = (XinferenceEntity) widgetEntity;
        entity.displayText = stringMessage.getData();
        
        this.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float textLayoutWidth = width;

        XinferenceEntity entity = (XinferenceEntity) widgetEntity;

        if (entity.rotation == 90 || entity.rotation == 270) {
            textLayoutWidth = height;
        }

        canvas.drawRect(new Rect(0, 0, (int) width, (int) height), backgroundPaint);

        staticLayout = new StaticLayout(entity.displayText,
                textPaint,
                (int) textLayoutWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0,
                false);
        canvas.save();
        canvas.rotate(entity.rotation, width / 2, height / 2);
        canvas.translate(((width / 2) - staticLayout.getWidth() / 2), height / 2 - staticLayout.getHeight() / 2);
        staticLayout.draw(canvas);
        canvas.restore();
    }
}
