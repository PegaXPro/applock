package com.aviapp.app.security.applocker.util.view;

import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;

public class OnViewGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private int maxHeight;
    private View view;

    public OnViewGlobalLayoutListener(View view) {
        this.view = view;
        maxHeight = pxFromDp(view.getContext(), 320f);
    }

    @Override
    public void onGlobalLayout() {
        if (view.getHeight() > maxHeight)
            view.getLayoutParams().height = maxHeight;
    }


    public static int pxFromDp(final Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}