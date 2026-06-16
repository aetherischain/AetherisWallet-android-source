package com.breadwallet.tools.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.breadwallet.tools.util.BRConstants;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

import static com.breadwallet.tools.util.BRConstants.GEO_PERMISSIONS_REQUESTED;

public class BRSharedPrefs {

    private static final List<OnIsoChangedListener> isoChangedListeners = new ArrayList<>();
    public static final String SEND_TRANSACTION_COUNT = "send_transaction_count";
    public static final String IN_APP_REVIEW_DONE = "in_app_review_done";
    public static final String PREFERRED_FPRATE = "preferredFalsePositiveRate";

    public interface OnIsoChangedListener {
        void onIsoChanged(String iso);
    }

    public static void addIsoChangedListener(OnIsoChangedListener listener) {
        if (isoChangedListeners.contains(listener)) return;
        isoChangedListeners.add(listener);
    }

    public static void removeListener(OnIsoChangedListener listener) {
        isoChangedListeners.remove(listener);
    }

    public static String getIsoSymbol(Context context) {
        if (context == null) return "USD";
        SharedPreferences settingsToGet = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        String defIso;
        String defaultLanguage = Locale.getDefault().getLanguage();

        try {
            if (defaultLanguage == "ru") {
                defIso = Currency.getInstance(new Locale("ru", "RU")).getCurrencyCode();
            }
            else if (defaultLanguage == "en") {
                defIso = Currency.getInstance(Locale.US).getCurrencyCode();
            }
            else {
                defIso = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
             defIso = Currency.getInstance(Locale.US).getCurrencyCode();
        }
        return settingsToGet.getString(BRConstants.CURRENT_CURRENCY, defIso);
    }

    public static void putIso(Context context, String code) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(BRConstants.CURRENT_CURRENCY, code.equalsIgnoreCase(Locale.getDefault().getISO3Language()) ? null : code);
        editor.apply();

        notifyIsoChanged(code);
    }

    public static void notifyIsoChanged(String iso) {
        for (OnIsoChangedListener listener : isoChangedListeners) {
            if (listener != null) listener.onIsoChanged(iso);
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////// Active Shared Preferences ///////////////////////////////
    public static void putLastSyncTimestamp(Context activity, long time) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("lastSyncTime", time);
        editor.apply();
    }
    public static long getLastSyncTimestamp(Context activity) {
        if (activity == null) return 0;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("lastSyncTime", 0L);
    }
    public static void putStartSyncTimestamp(Context activity, long time) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("startSyncTime", time);
        editor.apply();
    }
    public static long getStartSyncTimestamp(Context activity) {
        if (activity == null) return 0;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("startSyncTime", 0L);
    }

    public static void putSyncTimeElapsed(Context activity, long time) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("syncTimeElapsed", time);
        editor.apply();
    }
    public static long getSyncTimeElapsed(Context activity) {
        if (activity == null) return 0;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("syncTimeElapsed", 0L);
    }

    public static boolean getPhraseWroteDown(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(BRConstants.PHRASE_WRITTEN, false);
    }

    public static void putPhraseWroteDown(Context context, boolean check) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(BRConstants.PHRASE_WRITTEN, check);
        editor.apply();
    }

    public static boolean getGreetingsShown(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("greetingsShown", false);
    }

    public static void putGreetingsShown(Context context, boolean shown) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("greetingsShown", shown);
        editor.apply();
    }

    public static int getCurrencyListPosition(Context context) {
        if (context == null) return 0;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        return settings.getInt(BRConstants.POSITION, 0);
    }

    public static void putCurrencyListPosition(Context context, int lastItemsPosition) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(BRConstants.POSITION, lastItemsPosition);
        editor.apply();
    }

    public static String getReceiveAddress(Context context) {
        if (context == null) return "";
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(BRConstants.RECEIVE_ADDRESS, "");
    }

    public static void putReceiveAddress(Context ctx, String tmpAddr) {
        if (ctx == null) return;
        SharedPreferences.Editor editor = ctx.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(BRConstants.RECEIVE_ADDRESS, tmpAddr);
        editor.apply();
    }

    public static String getFirstAddress(Context context) {
        if (context == null) return "";
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(BRConstants.FIRST_ADDRESS, "");
    }

    public static void putFirstAddress(Context context, String firstAddress) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(BRConstants.FIRST_ADDRESS, firstAddress);
        editor.apply();
    }

    public static long getCatchedBalance(Context context) {
        if (context == null) return 0;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("balance", 0);
    }

    public static void putCatchedBalance(Context context, long fee) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("balance", fee);
        editor.apply();
    }

    public static long getSecureTime(Context activity) {
        if (activity == null) return System.currentTimeMillis() / 1000;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(BRConstants.SECURE_TIME_PREFS, System.currentTimeMillis() / 1000);
    }

    //secure time from the server
    public static void putSecureTime(Context activity, long date) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(BRConstants.SECURE_TIME_PREFS, date);
        editor.apply();
    }

    public static long getFeeTime(Context activity) {
        if (activity == null) return 0;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("feeTime", 0);
    }

    public static void putFeeTime(Context activity, long feeTime) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("feeTime", feeTime);
        editor.apply();
    }

    public static boolean getAllowSpend(Context activity) {
        if (activity == null) return true;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(BRConstants.ALLOW_SPEND, true);
    }

    public static void putAllowSpend(Context activity, boolean allow) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(BRConstants.ALLOW_SPEND, allow);
        editor.apply();
    }

    //if the user prefers all in litecoin units, not other currencies
    public static boolean getPreferredLTC(Context activity) {
        if (activity == null) return true;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("priceSetToLitecoin", true);
    }

    //if the user prefers all in litecoin units, not other currencies
    public static void putPreferredLTC(Context activity, boolean b) {
        if (activity == null) return;
        Timber.d("timber: putPreferredLTC: %s", b);
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("priceSetToLitecoin", b);
        editor.apply();
    }

    //if the user prefers all in litecoin units, not other currencies
    public static boolean getUseFingerprint(Context activity) {
        if (activity == null) return false;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("useFingerprint", false);
    }

    //if the user prefers all in litecoin units, not other currencies
    public static void putUseFingerprint(Context activity, boolean use) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("useFingerprint", use);
        editor.apply();
    }
    public static int getStartHeight(Context context) {
        if (context == null) return 0;
        SharedPreferences settingsToGet = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        return  settingsToGet.getInt(BRConstants.START_HEIGHT, 0);
    }

    public static void putStartHeight(Context context, int startHeight) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(BRConstants.START_HEIGHT, startHeight);
        editor.apply();
    }

    public static int getLastBlockHeight(Context context) {
        if (context == null) return 0;
        SharedPreferences settingsToGet = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        return settingsToGet.getInt(BRConstants.LAST_BLOCK_HEIGHT, 0);
    }

    public static void putLastBlockHeight(Context context, int lastHeight) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(BRConstants.LAST_BLOCK_HEIGHT, lastHeight);
        editor.apply();
    }

    public static boolean getScanRecommended(Context context) {
        if (context == null) return false;
        SharedPreferences settingsToGet = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        return settingsToGet.getBoolean("scanRecommended", false);
    }

    public static void putScanRecommended(Context context, boolean recommended) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("scanRecommended", recommended);
        editor.apply();
    }

    public static int getCurrencyUnit(Context context) {
        if (context == null) return BRConstants.CURRENT_UNIT_LITECOINS;
        SharedPreferences settingsToGet = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        return settingsToGet.getInt(BRConstants.CURRENT_UNIT, BRConstants.CURRENT_UNIT_LITECOINS);
    }

    public static void putCurrencyUnit(Context context, int unit) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(BRConstants.CURRENT_UNIT, unit);
        editor.apply();
    }

    private static void setDeviceId(Context context, String uuid) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(BRConstants.USER_ID, uuid);
        editor.apply();
    }

    public static void clearAllPrefs(Context activity) {
        if (activity == null) return;
        SharedPreferences.Editor editor = activity.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    public static boolean getShowNotification(Context context) {
        if (context == null) return false;
        SharedPreferences settingsToGet = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return settingsToGet.getBoolean("showNotification", false);
    }

    public static void putShowNotification(Context context, boolean show) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("showNotification", show);
        editor.apply();
    }

    public static boolean getShareData(Context context) {
        if (context == null) return false;
        SharedPreferences settingsToGet = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return settingsToGet.getBoolean("shareData", false);
    }

    public static void putShareData(Context context, boolean show) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shareData", show);
        editor.apply();
    }

    public static boolean getShareDataDismissed(Context context) {
        if (context == null) return false;
        SharedPreferences settingsToGet = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return settingsToGet.getBoolean("shareDataDismissed", false);
    }

    public static void putShareDataDismissed(Context context, boolean dismissed) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shareDataDismissed", dismissed);
        editor.apply();
    }

    public static String getTrustNode(Context context) {
        if (context == null) return "";
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("trustNode", "");
    }

    public static void putTrustNode(Context context, String trustNode) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("trustNode", trustNode);
        editor.apply();
    }

    public static void incrementSendTransactionCount(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        int currentTransactions = prefs.getInt(SEND_TRANSACTION_COUNT, 0);
        prefs.edit().putInt(SEND_TRANSACTION_COUNT, currentTransactions + 1).apply();
    }

    public static int getSendTransactionCount(Context context) {
        if (context == null) return 0;
        return context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(SEND_TRANSACTION_COUNT, 0);
    }

    public static boolean isInAppReviewDone(Context context) {
        if (context == null) return false;
        return context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(IN_APP_REVIEW_DONE, false);
    }

    public static void inAppReviewDone(Context context) {
        if (context == null) return;
        context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(IN_APP_REVIEW_DONE, true).apply();
    }

    public static float getFalsePositivesRate(Context context) {
        if (context == null) return BRConstants.FALSE_POS_RATE_LOW_PRIVACY;
        return context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE).getFloat(PREFERRED_FPRATE, BRConstants.FALSE_POS_RATE_LOW_PRIVACY);
    }

    public static void putFalsePositivesRate(Context context, float preferredRate) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(BRConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(PREFERRED_FPRATE, preferredRate);
        editor.apply();
    }
}
