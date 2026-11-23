package com.example.parcial3_vitapaw.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GraficaPesoView extends View {

    private List<Float> pesos = new ArrayList<>();
    private List<String> fechas = new ArrayList<>();

    private Paint linePaint;
    private Paint pointPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Paint fillPaint;

    public GraficaPesoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraficaPesoView(Context context) {
        super(context, null);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#0A3323")); // Deep Pine
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.parseColor("#105666")); // Deep Teal
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#0A3323"));
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#CCCCCC"));
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{10f, 10f}, 0));

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.parseColor("#33839958")); // Sage Green soft fill
        fillPaint.setStyle(Paint.Style.FILL);
    }

    public void setDatos(List<Float> pesos, List<String> fechas) {
        this.pesos = (pesos != null) ? new ArrayList<>(pesos) : new ArrayList<>();
        this.fechas = (fechas != null) ? new ArrayList<>(fechas) : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pesos == null || pesos.isEmpty()) {
            canvas.drawText("Sin suficientes datos para graficar", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        float paddingLeft = 60f;
        float paddingRight = 60f;
        float paddingTop = 50f;
        float paddingBottom = 60f;

        float width = getWidth() - paddingLeft - paddingRight;
        float height = getHeight() - paddingTop - paddingBottom;

        float minPeso = Collections.min(pesos);
        float maxPeso = Collections.max(pesos);
        if (minPeso == maxPeso) {
            minPeso = Math.max(0, minPeso - 2f);
            maxPeso += 2f;
        }

        int count = pesos.size();
        float stepX = (count > 1) ? width / (count - 1) : width / 2f;

        Path linePath = new Path();
        Path fillPath = new Path();

        float[] pointsX = new float[count];
        float[] pointsY = new float[count];

        for (int i = 0; i < count; i++) {
            float x = (count > 1) ? paddingLeft + i * stepX : paddingLeft + width / 2f;
            float p = pesos.get(i);
            float y = paddingTop + height - ((p - minPeso) / (maxPeso - minPeso) * height);

            pointsX[i] = x;
            pointsY[i] = y;

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, paddingTop + height);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }

        if (count > 0) {
            fillPath.lineTo(pointsX[count - 1], paddingTop + height);
            fillPath.close();
            canvas.drawPath(fillPath, fillPaint);
        }

        // Línea base del eje X
        canvas.drawLine(paddingLeft, paddingTop + height, getWidth() - paddingRight, paddingTop + height, gridPaint);

        canvas.drawPath(linePath, linePaint);

        for (int i = 0; i < count; i++) {
            float x = pointsX[i];
            float y = pointsY[i];

            canvas.drawCircle(x, y, 9f, pointPaint);

            String valStr = String.format(Locale.getDefault(), "%.1fkg", pesos.get(i));
            canvas.drawText(valStr, x, y - 16f, textPaint);

            if (i < fechas.size()) {
                canvas.drawText(fechas.get(i), x, paddingTop + height + 38f, textPaint);
            }
        }
    }
}