package signalinfowidget.nop.net.signalinfowidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by nop-90 on 25/03/16.
 */
public class MainActivity extends AppWidgetProvider {
    RemoteViews remoteViews;
    AppWidgetManager appWidgetManager;
    ComponentName thisWidget;
    Context context;
    TelephonyManager telephonyManager;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetids) {
        this.appWidgetManager = appWidgetManager;
        this.context = context;
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_main);
        thisWidget = new ComponentName(context, MainActivity.class);
        init();
    }

    public void init() {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            public void onDataActivity(int direction) {
                remoteViews.setTextViewText(R.id.data_activity, getDataState(direction));
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            }
            public void onDataConnectionStateChanged(int state) {
                remoteViews.setTextViewText(R.id.data_activity, getDataState(state));
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            }
            public void onServiceStateChanged(ServiceState serviceState) {
                String networktype_str = getNetworkType();
                remoteViews.setTextViewText(R.id.network_type, networktype_str);
                remoteViews.setTextViewText(R.id.carrier_name, serviceState.getOperatorAlphaLong());
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            }
            public void onSignalStrengthsChanged(SignalStrength signal) {
                try {
                    Method method = signal.getClass().getDeclaredMethod("getDbm");
                    Integer strength = (Integer) method.invoke(signal, new Object[]{});
                    remoteViews.setTextViewText(R.id.network_power,  strength + " dBm");
                    appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                } catch (Exception ignored){
                    Log.d("Exp",ignored.getMessage());
                }
            }
        };
        telephonyManager.listen(phoneStateListener,
                        PhoneStateListener.LISTEN_CALL_STATE |
                        PhoneStateListener.LISTEN_DATA_ACTIVITY |
                        PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                        PhoneStateListener.LISTEN_SERVICE_STATE |
                        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        String networktype_str = getNetworkType();
        int cell_strength = getCellStrength();
        if (getPermissionCoarseLocationStatus(context) && getPermissionReadStateStatus(context)) {
            remoteViews.setTextViewText(R.id.carrier_name, getCarrierName());
            remoteViews.setTextViewText(R.id.network_power,  cell_strength + " dBm");
            remoteViews.setTextViewText(R.id.network_type, networktype_str);
            remoteViews.setTextViewText(R.id.data_activity, getDataState(telephonyManager.getDataState()));
            if (cell_strength < -90)
                remoteViews.setImageViewResource(R.id.signal_icon, R.drawable.red_ring);
            else if (cell_strength > -90 && cell_strength < -75)
                remoteViews.setImageViewResource(R.id.signal_icon, R.drawable.orange_ring);
            else if (cell_strength > -75 && cell_strength < -55)
                remoteViews.setImageViewResource(R.id.signal_icon, R.drawable.green_ring);
            else
                remoteViews.setImageViewResource(R.id.signal_icon, R.drawable.light_green_ring);

            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        } else {
            Toast.makeText(context, "Won't do anything. I haven't got the permissions", Toast.LENGTH_LONG);
        }
    }

    public String getNetworkType() {
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

    public String getDataState(int datastate) {
        String datastate_str = "";
        if (datastate == telephonyManager.DATA_ACTIVITY_DORMANT) {
            datastate_str = context.getString(R.string.inactive);
        } else if (datastate == telephonyManager.DATA_ACTIVITY_IN) {
            datastate_str = context.getString(R.string.in);
        } else if (datastate == telephonyManager.DATA_ACTIVITY_OUT) {
            datastate_str = context.getString(R.string.out);
        } else if (datastate == telephonyManager.DATA_ACTIVITY_INOUT) {
            datastate_str = context.getString(R.string.inout);
        } else if (datastate == telephonyManager.DATA_DISCONNECTED){
            datastate_str = context.getString(R.string.turnedoff);
        } else {
            datastate_str = "Unknown";
        }
        return datastate_str;
    }

    public int getCellStrength() {
        int signal_strength = -1;
        String networktype = getNetworkType();
        if (networktype.equals("LTE")) {
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
