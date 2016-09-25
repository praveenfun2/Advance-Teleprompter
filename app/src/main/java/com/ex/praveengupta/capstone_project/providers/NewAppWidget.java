package com.ex.praveengupta.capstone_project.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.ex.praveengupta.capstone_project.MainActivity;
import com.ex.praveengupta.capstone_project.R;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    static String LAUNCH_ACTION="com.ex.praveengupta.capstone_project.LAUNCH";
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent=new Intent(context, MyService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        RemoteViews rm=new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        rm.setRemoteAdapter(R.id.widget_list, intent);
        rm.setEmptyView(R.id.widget_list,R.id.empty_view3);

        Intent intent1=new Intent(context, NewAppWidget.class);
        intent1.setAction(LAUNCH_ACTION);
        intent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent1.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rm.setPendingIntentTemplate(R.id.widget_list, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, rm);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {

        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {

        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(LAUNCH_ACTION)){
            Intent intent1=new Intent(context, MainActivity.class);
            intent1.putExtra("content_add",intent.getStringExtra("content_add"));
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
        super.onReceive(context, intent);
    }
}
