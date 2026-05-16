package ensa.ma.sensors.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {

    private final List<Float> values = new ArrayList<>();
    private final int maxPoints = 80;

    private final Paint axisPaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint();

    public LineChartView(Context context) {
        super(context);
        initPaints();
    }

    private void initPaints() {
        axisPaint.setColor(Color.LTGRAY);
        axisPaint.setStrokeWidth(3);

        linePaint.setColor(Color.rgb(33, 150, 243));
        linePaint.setStrokeWidth(5);
        linePaint.setStyle(Paint.Style.STROKE);

        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(30);
    }

    public void addValue(float value) {
        if (values.size() >= maxPoints) {
            values.remove(0);
        }
        values.add(value);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Axes
        canvas.drawLine(40, height - 40, width - 20, height - 40, axisPaint);
        canvas.drawLine(40, 20, 40, height - 40, axisPaint);

        if (values.size() < 2) {
            canvas.drawText("📊 En attente des données...", 60, height / 2, textPaint);
            return;
        }

        // Min/Max
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (float value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        if (max == min) max = min + 1;

        // Courbe
        Path path = new Path();
        for (int i = 0; i < values.size(); i++) {
            float x = 40 + i * ((width - 80f) / (maxPoints - 1));
            float normalized = (values.get(i) - min) / (max - min);
            float y = height - 40 - normalized * (height - 80);

            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        canvas.drawPath(path, linePaint);

        // Infos
        canvas.drawText("Min : " + min + " | Max : " + max, 60, 40, textPaint);
    }
}