package de.bittim.bitwarn;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

public class GradientTextView extends AppCompatTextView {
    int priCol = 0;
    int secCol = 0;

    public GradientTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public GradientTextView(Context context, AttributeSet attrs, int style)
    {
        super(context, attrs, style);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GradientTextView, 0, 0);

        try
        {
            priCol = a.getColor(R.styleable.GradientTextView_primaryColor, 0xFFFFFFFF);
            secCol = a.getColor(R.styleable.GradientTextView_secondaryColor, 0xFF000000);
        }
        finally { a.recycle(); }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        if(changed){
            setTextColor(priCol);

            TextPaint paint = getPaint();
            Shader shader = new LinearGradient(0f, 0f, paint.measureText((String) getText()), getTextSize(),
                    new int[]{
                            priCol,
                            secCol
                    }, null, Shader.TileMode.CLAMP);
            paint.setShader(shader);
        }
    }
}
