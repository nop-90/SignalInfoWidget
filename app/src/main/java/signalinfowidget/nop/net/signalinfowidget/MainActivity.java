package signalinfowidget.nop.net.signalinfowidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Manifest;

/**
 * Created by nop-90 on 25/03/16.
 */
public class MainActivity extends AppWidgetProvider {
    private TelephonyManager telephonyManager;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetids) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new WidgetTimer(context, appWidgetManager), 1, 1000);
    }

    private class WidgetTimer extends TimerTask {
        RemoteViews remoteViews;
        AppWidgetManager appWidgetManager;
        ComponentName thisWidget;
        Context context;

        public WidgetTimer(Context context, AppWidgetManager appWidgetManager) {
            this.appWidgetManager = appWidgetManager;
            this.context = context;
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_main);
            thisWidget = new ComponentName(context, MainActivity.class);
        }
        @Override
        public void run() {
            String networktype_str = getNetworkType(context);
            if (getPermissionCoarseLocationStatus(context) && getPermissionReadStateStatus(context)) {
                remoteViews.setTextViewText(R.id.carrier_name, getCarrierName());
                remoteViews.setTextViewText(R.id.network_power, getCellStrength(networktype_str) + " dBm");
                remoteViews.setTextViewText(R.id.network_type, networktype_str);
                remoteViews.setTextViewText(R.id.data_activity, getDataState(context));
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            } else {
                Toast.makeText(context, "Won't do anything. I haven't got the permissions", Toast.LENGTH_LONG);
            }
        }
    }

    public String getNetworkType(Context context) {
        int networktype = telephonyManager.getNetworkType();
        String network_str = "";
        if (networktype == telephonyManager.NETWORK_TYPE_LTE) {
            network_str = "LTE";
        } else if (networktype == telephonyManager.NETWORK_TYPE_HSPA) {
            network_str = "HSPA";
        } else if (networktype == telephonyManager.NETWORK_TYPE_EDGE) {
            network_str = "EDGE";
        } else if (networktype == telephonyManager.NETWORK_TYPE_GPRS) {
            network_str = "GPRS";
        } else if (networktype == telephonyManager.NETWORK_TYPE_UMTS) {
            network_str = "UMTS";
        } else if (networktype == telephonyManager.NETWORK_TYPE_CDMA) {
            network_str = "CDMA";
        } else if (networktype == telephonyManager.NETWORK_TYPE_HSPAP) {
            network_str = "HSPA+";
        } else {
            network_str = context.getString(R.string.unk);
        }
        return network_str;
    }

    public String getDataState(Context context) {
        int datastate = telephonyManager.getDataState();
        String datastate_str = "";
        if (datastate == telephonyManager.DATA_ACTIVITY_DORMANT) {
            datastate_str = context.getString(R.string.inactive);
        } else if (datastate == telephonyManager.DATA_ACTIVITY_IN) {
            datastate_str = context.getString(R.string.in);
        } else if (datastate == telephonyManager.DATA_ACTIVITY_OUT) {
            datastate_str = context.getString(R.string.out);
        } else if (datastate == telephonyManager.DATA_ACTIVITY_INOUT) {
            datastate_str = context.getString(R.string.inout);
        } else {
            datastate_str = context.getString(R.string.turnedoff);
        }
        return datastate_str;
    }

    public int getCellStrength(String networktype) {
        int signal_strength = -1;
        if (networktype == "LTE") {
            CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
            signal_strength = cellSignalStrengthLte.getAsuLevel() - 140;
        } else if (networktype.equals("EDGE") || networktype.equals("GPRS")) {
            CellInfoGsm cellInfoGsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();
            signal_strength = cellSignalStrengthGsm.getAsuLevel()*2 - 113;
        } else if (networktype.equals("UMTS") || networktype.equals("HSPA+")
                || networktype.equals("HSPA")) {
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
            CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
            signal_strength = cellSignalStrengthWcdma.getAsuLevel() - 116;
        } else if (networktype.equals("CDMA")) {
            CellInfoCdma cellInfoCdma = (CellInfoCdma) telephonyManager.getAllCellInfo().get(0);
            CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
            signal_strength = cellSignalStrengthCdma.getCdmaDbm();
        }
        return signal_strength;
    }

    public String getCarrierName() {
        return telephonyManager.getNetworkOperatorName();
    }

    /**
     * Method to support permission asking in API 23
     * Doesn't ask if permission not given
     */
    public boolean getPermissionReadStateStatus(Context context) {
        boolean result = false;
        if (context == null) {
            result = true;
        }
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            result = true;
        }
        return result;
    }

    public boolean getPermissionCoarseLocationStatus(Context context) {
        boolean result = false;
        if (context == null) {
            result = true;
        }
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            result = true;
        }
        return result;
    }

}
