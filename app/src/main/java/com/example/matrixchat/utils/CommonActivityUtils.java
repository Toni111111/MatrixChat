package com.example.matrixchat.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.matrixchat.Matrix;
import com.example.matrixchat.MyApplication;
import com.example.matrixchat.MyPresenceManager;
import com.example.matrixchat.PreferencesManager;
import com.example.matrixchat.ui.auth.AuthActivity;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.core.Log;
import org.matrix.androidsdk.core.callback.ApiCallback;
import org.matrix.androidsdk.core.callback.SimpleApiCallback;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.db.MXMediaCache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Contains useful functions which are called in multiple activities.
 */
public class CommonActivityUtils {
    private static final String LOG_TAG = CommonActivityUtils.class.getSimpleName();

    // global helper constants:

    public static final boolean UTILS_DISPLAY_PROGRESS_BAR = true;
    public static final boolean UTILS_HIDE_PROGRESS_BAR = false;

    // room details members:
    public static final String KEY_GROUPS_EXPANDED_STATE = "KEY_GROUPS_EXPANDED_STATE";
    public static final String KEY_SEARCH_PATTERN = "KEY_SEARCH_PATTERN";
    public static final boolean GROUP_IS_EXPANDED = true;
    public static final boolean GROUP_IS_COLLAPSED = false;

    // power levels
    public static final float UTILS_POWER_LEVEL_ADMIN = 100;
    public static final float UTILS_POWER_LEVEL_MODERATOR = 50;
    private static final int ROOM_SIZE_ONE_TO_ONE = 2;

    /**
     * Logout a sessions list
     *
     * @param context          the context
     * @param sessions         the sessions list
     * @param clearCredentials true to clear the credentials
     * @param callback         the asynchronous callback
     */
    public static void logout(Context context, List<MXSession> sessions, boolean clearCredentials, final ApiCallback<Void> callback) {
        logout(context, sessions.iterator(), clearCredentials, callback);
    }

    /**
     * Internal method to logout a sessions list
     *
     * @param context          the context
     * @param sessions         the sessions iterator
     * @param clearCredentials true to clear the credentials
     * @param callback         the asynchronous callback
     */
    private static void logout(final Context context,
                               final Iterator<MXSession> sessions,
                               final boolean clearCredentials,
                               final ApiCallback<Void> callback) {
        if (!sessions.hasNext()) {
            if (null != callback) {
                callback.onSuccess(null);
            }

            return;
        }

        MXSession session = sessions.next();

        if (session.isAlive()) {

            // clear credentials
            Matrix.getInstance(context).clearSession(context, session, clearCredentials, new SimpleApiCallback<Void>() {
                @Override
                public void onSuccess(Void info) {
                    logout(context, sessions, clearCredentials, callback);
                }
            });
        }
    }

    public static boolean shouldRestartApp(Context context) {
        // EventStreamService eventStreamService = EventStreamService.getInstance();

        if (!Matrix.hasValidSessions()) {
            Log.e(LOG_TAG, "shouldRestartApp : the client has no valid session");
        }

        /*
        if (null == eventStreamService) {
            Log.e(LOG_TAG, "eventStreamService is null : restart the event stream");
            CommonActivityUtils.startEventStreamService(context);
        }
        */

        return !Matrix.hasValidSessions();
    }


    private static final String RESTART_IN_PROGRESS_KEY = "RESTART_IN_PROGRESS_KEY";

    /**
     * The application has been started
     */
    public static void onApplicationStarted(Activity activity) {
        PreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean(RESTART_IN_PROGRESS_KEY, false)
                .apply();
    }


    /**
     * Logout the current user.
     * Jump to the login page when the logout is done.
     *
     * @param activity the caller activity
     */
    public static void logout(Activity activity) {
        logout(activity, true);
    }

    private static boolean isRecoveringFromInvalidatedToken = false;

    public static void recoverInvalidatedToken() {

        if (isRecoveringFromInvalidatedToken) {
            //ignore, we are doing it
            return;
        }
        isRecoveringFromInvalidatedToken = true;
        Context context = MyApplication.getCurrentActivity() != null ? MyApplication.getCurrentActivity() : MyApplication.getInstance();

        try {


            //todo      EventStreamServiceX.Companion.onLogout(context);
            // stopEventStream(context);

            //todo     BadgeProxy.INSTANCE.updateBadgeCount(context, 0);

            MXSession session = Matrix.getInstance(context).getDefaultSession();

            // Publish to the server that we're now offline
            MyPresenceManager.getInstance(context, session).advertiseOffline();
            MyPresenceManager.remove(session);

            // clear the preferences
            PreferencesManager.clearPreferences(context);


            // Clear the credentials
            Matrix.getInstance(context).getLoginStorage().clear();

            // clear the tmp store list
            Matrix.getInstance(context).clearTmpStoresList();


            MXMediaCache.clearThumbnailsCache(context);

            Matrix.getInstance(context).clearSessions(context, true, new SimpleApiCallback<Void>() {

                @Override
                public void onSuccess(Void info) {

                }
            });
            session.clear(context);
        } catch (Exception e) {
            Log.e(LOG_TAG, "## recoverInvalidatedToken: Error while cleaning: ", e);
        } finally {
            // go to login page
            isRecoveringFromInvalidatedToken = false;
        }
    }

    /**
     * Logout the current user.
     *
     * @param activity      the caller activity
     * @param goToLoginPage true to jump to the login page
     */
    public static void logout(final Activity activity, final boolean goToLoginPage) {
        Log.d(LOG_TAG, "## logout() : from " + activity + " goToLoginPage " + goToLoginPage);

        // if no activity is provided, use the application context instead.
        final Context context = (null == activity) ? MyApplication.getInstance().getApplicationContext() : activity;

        // todo EventStreamServiceX.Companion.onLogout(activity);
        // stopEventStream(context);

        // todo BadgeProxy.INSTANCE.updateBadgeCount(context, 0);

        // warn that the user logs out
        Collection<MXSession> sessions = Matrix.getMXSessions(context);
        for (MXSession session : sessions) {
            // Publish to the server that we're now offline
            MyPresenceManager.getInstance(context, session).advertiseOffline();
            MyPresenceManager.remove(session);
        }

        // clear the preferences
        PreferencesManager.clearPreferences(context);

        // clear the preferences when the application goes to the login screen.
        if (goToLoginPage) {


            Intent intent = new Intent(context, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }

        // clear credentials
        Matrix.getInstance(context).clearSessions(context, true, new SimpleApiCallback<Void>() {
            @Override
            public void onSuccess(Void info) {
                // ensure that corrupted values are cleared
                Matrix.getInstance(context).getLoginStorage().clear();

                // clear the tmp store list
                Matrix.getInstance(context).clearTmpStoresList();

                MXMediaCache.clearThumbnailsCache(context);

                if (goToLoginPage) {
                    Activity activeActivity = MyApplication.getCurrentActivity();

                    // go to login page
                    Intent intent = new Intent(activeActivity, AuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    if (null != activeActivity) {
                        activeActivity.startActivity(intent);
                    } else {
                        context.startActivity(intent);
                    }
                }
            }
        });
    }

    /**
     * Clear all local data after a user account deactivation
     *
     * @param context       the application context
     * @param mxSession     the session to deactivate
     * @param userPassword  the user password
     * @param eraseUserData true to also erase all the user data
     * @param callback      the callback success and failure callback
     */

    /*
    public static void deactivateAccount(final Context context,
                                         final MXSession mxSession,
                                         final String userPassword,
                                         final boolean eraseUserData,
                                         final @NonNull ApiCallback<Void> callback) {
        Matrix.getInstance(context).deactivateSession(context, mxSession, userPassword, eraseUserData, new SimpleApiCallback<Void>(callback) {

            @Override
            public void onSuccess(Void info) {
                MyApplication.getInstance().getNotificationDrawerManager().clearAllEvents();
                EventStreamServiceX.Companion.onLogout(context);
                // stopEventStream(context);

                BadgeProxy.INSTANCE.updateBadgeCount(context, 0);

                // Publish to the server that we're now offline
                MyPresenceManager.getInstance(context, mxSession).advertiseOffline();
                MyPresenceManager.remove(mxSession);

                // clear the preferences
                PreferencesManager.clearPreferences(context);



                // Clear the credentials
                Matrix.getInstance(context).getLoginStorage().clear();

                // clear the tmp store list
                Matrix.getInstance(context).clearTmpStoresList();



                MXMediaCache.clearThumbnailsCache(context);

                callback.onSuccess(info);
            }
        });
    }
    */

    /**
     * Start LoginActivity in a new task, and clear any other existing task
     *
     * @param activity the current Activity
     */
    public static void startLoginActivityNewTask(Activity activity) {
        Intent intent = new Intent(activity, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    //==============================================================================================================
    // Room preview methods.
    //==============================================================================================================

    /**
     * Start a room activity in preview mode.
     *
     * @param fromActivity    the caller activity.
     * @param roomPreviewData the room preview information
     */
    /*
    public static void previewRoom(final Activity fromActivity, RoomPreviewData roomPreviewData) {
        if ((null != fromActivity) && (null != roomPreviewData)) {
            RoomActivity.sRoomPreviewData = roomPreviewData;
            Intent intent = new Intent(fromActivity, RoomActivity.class);
            intent.putExtra(RoomActivity.EXTRA_ROOM_ID, roomPreviewData.getRoomId());
            intent.putExtra(RoomActivity.EXTRA_ROOM_PREVIEW_ID, roomPreviewData.getRoomId());
            intent.putExtra(RoomActivity.EXTRA_EXPAND_ROOM_HEADER, true);
            fromActivity.startActivity(intent);
        }
    }
    */

    /**
     * Helper method used to build an intent to trigger a room preview.
     *
     * @param aMatrixId       matrix ID of the user
     * @param aRoomId         room ID
     * @param aContext        application context
     * @param aTargetActivity the activity set in the returned intent
     * @return a valid intent if operation succeed, null otherwise
     */

    /*

    public static Intent buildIntentPreviewRoom(String aMatrixId, String aRoomId, Context aContext, Class<?> aTargetActivity) {
        Intent intentRetCode;

        // sanity check
        if ((null == aContext) || (null == aRoomId) || (null == aMatrixId)) {
            intentRetCode = null;
        } else {
            MXSession session;

            // get the session
            if (null == (session = Matrix.getInstance(aContext).getSession(aMatrixId))) {
                session = Matrix.getInstance(aContext).getDefaultSession();
            }

            // check session validity
            if ((null == session) || !session.isAlive()) {
                intentRetCode = null;
            } else {
                String roomAlias = null;
                Room room = session.getDataHandler().getRoom(aRoomId);

                // get the room alias (if any) for the preview data
                if ((null != room) && (null != room.getState())) {
                    roomAlias = room.getState().getCanonicalAlias();
                }

                intentRetCode = new Intent(aContext, aTargetActivity);
                // extra required by RoomActivity
                intentRetCode.putExtra(RoomActivity.EXTRA_ROOM_ID, aRoomId);
                intentRetCode.putExtra(RoomActivity.EXTRA_ROOM_PREVIEW_ID, aRoomId);
                intentRetCode.putExtra(RoomActivity.EXTRA_MATRIX_ID, aMatrixId);
                intentRetCode.putExtra(RoomActivity.EXTRA_EXPAND_ROOM_HEADER, true);
                // extra only required by VectorFakeRoomPreviewActivity
                intentRetCode.putExtra(RoomActivity.EXTRA_ROOM_PREVIEW_ROOM_ALIAS, roomAlias);
            }
        }
        return intentRetCode;
    }
*/
    /**
     * Start a room activity in preview mode.
     * If the room is already joined, open it in edition mode.
     *
     * @param fromActivity the caller activity.
     * @param session      the session
     * @param roomId       the roomId
     * @param roomAlias    the room alias
     * @param callback     the operation callback
     */


    /*
    public static void previewRoom(final Activity fromActivity,
                                   final MXSession session,
                                   final String roomId,
                                   final String roomAlias,
                                   final ApiCallback<Void> callback) {
        previewRoom(fromActivity, session, roomId, new RoomPreviewData(session, roomId, null, roomAlias, null), callback);
    }

    */

    /**
     * Start a room activity in preview mode.
     * If the room is already joined, open it in edition mode.
     *
     * @param fromActivity    the caller activity.
     * @param session         the session
     * @param roomId          the roomId
     * @param roomPreviewData the room preview data
     * @param callback        the operation callback
     */

    /*
    public static void previewRoom(final Activity fromActivity,
                                   final MXSession session,
                                   final String roomId,
                                   final RoomPreviewData roomPreviewData,
                                   final ApiCallback<Void> callback) {
        // Check whether the room exists to handled the cases where the user is invited or he has joined.
        // CAUTION: the room may exist whereas the user membership is neither invited nor joined.
        final Room room = session.getDataHandler().getRoom(roomId, false);
        if (null != room && room.isInvited()) {
            Log.d(LOG_TAG, "previewRoom : the user is invited -> display the preview " + MyApplication.getCurrentActivity());
            previewRoom(fromActivity, roomPreviewData);

            if (null != callback) {
                callback.onSuccess(null);
            }
        } else if (null != room && room.isJoined()) {
            Log.d(LOG_TAG, "previewRoom : the user joined the room -> open the room");
            final Map<String, Object> params = new HashMap<>();
            params.put(RoomActivity.EXTRA_MATRIX_ID, session.getMyUserId());
            params.put(RoomActivity.EXTRA_ROOM_ID, roomId);
            goToRoomPage(fromActivity, session, params);

            if (null != callback) {
                callback.onSuccess(null);
            }
        } else {
            // Display a preview by default.
            Log.d(LOG_TAG, "previewRoom : display the preview");
            roomPreviewData.fetchPreviewData(new ApiCallback<Void>() {
                private void onDone() {
                    if (null != callback) {
                        callback.onSuccess(null);
                    }
                    previewRoom(fromActivity, roomPreviewData);
                }

                @Override
                public void onSuccess(Void info) {
                    onDone();
                }

                @Override
                public void onNetworkError(Exception e) {
                    onDone();
                }

                @Override
                public void onMatrixError(MatrixError e) {
                    onDone();
                }

                @Override
                public void onUnexpectedError(Exception e) {
                    onDone();
                }
            });
        }


    }
    */
    //==============================================================================================================
    // Room jump methods.
    //==============================================================================================================

    /**
     * Start a room activity with the dedicated parameters.
     * Pop the activity to the homeActivity before pushing the new activity.
     *
     * @param fromActivity the caller activity.
     * @param session      the session.
     * @param params       the room activity parameters.
     *
     *
     */

    /*
    public static void goToRoomPage(@NonNull final Activity fromActivity,
                                    final MXSession session,
                                    @NonNull final Map<String, Object> params) {
        final MXSession finalSession = (session == null) ? Matrix.getMXSession(fromActivity, (String) params.get(RoomActivity.EXTRA_MATRIX_ID)) : session;

        // sanity check
        if (finalSession == null || !finalSession.isAlive()) {
            return;
        }

        String roomId = (String) params.get(RoomActivity.EXTRA_ROOM_ID);

        Room room = finalSession.getDataHandler().getRoom(roomId);

        // do not open a leaving room.
        // it does not make.
        if ((null != room) && (room.isLeaving())) {
            return;
        }

        fromActivity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        // if the activity is not the home activity
                        if (!(fromActivity instanceof VectorHomeActivity)) {
                            // pop to the home activity
                            Log.d(LOG_TAG, "## goToRoomPage(): start VectorHomeActivity..");
                            Intent intent = new Intent(fromActivity, VectorHomeActivity.class);
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            intent.putExtra(VectorHomeActivity.EXTRA_JUMP_TO_ROOM_PARAMS, (Serializable) params);
                            fromActivity.startActivity(intent);
                        } else {
                            // already to the home activity
                            // so just need to open the room activity
                            Log.d(LOG_TAG, "## goToRoomPage(): already in VectorHomeActivity..");
                            Intent intent = new Intent(fromActivity, RoomActivity.class);

                            for (String key : params.keySet()) {
                                Object value = params.get(key);

                                if (value instanceof String) {
                                    intent.putExtra(key, (String) value);
                                } else if (value instanceof Boolean) {
                                    intent.putExtra(key, (Boolean) value);
                                } else if (value instanceof Parcelable) {
                                    intent.putExtra(key, (Parcelable) value);
                                }
                            }

                            // try to find a displayed room name
                            if (null == params.get(RoomActivity.EXTRA_DEFAULT_NAME)) {

                                Room room = finalSession.getDataHandler().getRoom((String) params.get(RoomActivity.EXTRA_ROOM_ID));

                                if ((null != room) && room.isInvited()) {
                                    String displayName = room.getRoomDisplayName(fromActivity);

                                    if (null != displayName) {
                                        intent.putExtra(RoomActivity.EXTRA_DEFAULT_NAME, displayName);
                                    }
                                }
                            }

                            fromActivity.startActivity(intent);
                        }
                    }
                }
        );
    }
*/

    /**
     * Set a room as a direct chat room.<br>
     * In case of success the corresponding room is displayed.
     *
     * @param aSession           session
     * @param aRoomId            room ID
     * @param aParticipantUserId the direct chat invitee user ID
     * @param fromActivity       calling activity
     * @param callback           async response handler
     */
    public static void setToggleDirectMessageRoom(final MXSession aSession,
                                                  final String aRoomId,
                                                  String aParticipantUserId,
                                                  final Activity fromActivity,
                                                  @NonNull final ApiCallback<Void> callback) {

        if ((null == aSession) || (null == fromActivity) || TextUtils.isEmpty(aRoomId)) {
            Log.e(LOG_TAG, "## setToggleDirectMessageRoom(): failure - invalid input parameters");
            callback.onUnexpectedError(new Exception("## setToggleDirectMessageRoom(): failure - invalid input parameters"));
        } else {
            aSession.toggleDirectChatRoom(aRoomId, aParticipantUserId, new SimpleApiCallback<Void>(callback) {
                @Override
                public void onSuccess(Void info) {
                    callback.onSuccess(null);
                }
            });
        }
    }


    //==============================================================================================================
    // Parameters checkers.
    //==============================================================================================================


    //==============================================================================================================
    // Media utils
    //==============================================================================================================

    /**
     * Copy a file into a dstPath directory.
     * The output filename can be provided.
     * The output file is not overridden if it is already exist.
     *
     * @param sourceFile     the file source path
     * @param dstDirPath     the dst path
     * @param outputFilename optional the output filename
     * @param callback       the asynchronous callback
     */
    private static void saveFileInto(final File sourceFile, final String dstDirPath, final String outputFilename, final ApiCallback<String> callback) {
        // sanity check
        if ((null == sourceFile) || (null == dstDirPath)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (null != callback) {
                        callback.onNetworkError(new Exception("Null parameters"));
                    }
                }
            });
            return;
        }

        AsyncTask<Void, Void, Pair<String, Exception>> task = new AsyncTask<Void, Void, Pair<String, Exception>>() {
            @Override
            protected Pair<String, Exception> doInBackground(Void... params) {
                Pair<String, Exception> result;

                // defines another name for the external media
                String dstFileName;

                // build a filename is not provided
                if (null == outputFilename) {
                    // extract the file extension from the uri
                    int dotPos = sourceFile.getName().lastIndexOf(".");

                    String fileExt = "";
                    if (dotPos > 0) {
                        fileExt = sourceFile.getName().substring(dotPos);
                    }

                    dstFileName = "vector_" + System.currentTimeMillis() + fileExt;
                } else {
                    dstFileName = outputFilename;
                }

                File dstDir = Environment.getExternalStoragePublicDirectory(dstDirPath);
                if (dstDir != null) {
                    dstDir.mkdirs();
                }

                File dstFile = new File(dstDir, dstFileName);

                // if the file already exists, append a marker
                if (dstFile.exists()) {
                    String baseFileName = dstFileName;
                    String fileExt = "";

                    int lastDotPos = dstFileName.lastIndexOf(".");

                    if (lastDotPos > 0) {
                        baseFileName = dstFileName.substring(0, lastDotPos);
                        fileExt = dstFileName.substring(lastDotPos);
                    }

                    int counter = 1;

                    while (dstFile.exists()) {
                        dstFile = new File(dstDir, baseFileName + "(" + counter + ")" + fileExt);
                        counter++;
                    }
                }

                // Copy source file to destination
                FileInputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    dstFile.createNewFile();

                    inputStream = new FileInputStream(sourceFile);
                    outputStream = new FileOutputStream(dstFile);

                    byte[] buffer = new byte[1024 * 10];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    result = new Pair<>(dstFile.getAbsolutePath(), null);
                } catch (Exception e) {
                    result = new Pair<>(null, e);
                } finally {
                    // Close resources
                    try {
                        if (inputStream != null) inputStream.close();
                        if (outputStream != null) outputStream.close();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "## saveFileInto(): Exception Msg=" + e.getMessage(), e);
                        result = new Pair<>(null, e);
                    }
                }

                return result;
            }

            @Override
            protected void onPostExecute(Pair<String, Exception> result) {
                if (null != callback) {
                    if (null == result) {
                        callback.onUnexpectedError(new Exception("Null parameters"));
                    } else if (null != result.first) {
                        callback.onSuccess(result.first);
                    } else {
                        callback.onUnexpectedError(result.second);
                    }
                }
            }
        };

        try {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (final Exception e) {
            Log.e(LOG_TAG, "## saveFileInto() failed " + e.getMessage(), e);
            task.cancel(true);

            (new android.os.Handler(Looper.getMainLooper())).post(new Runnable() {
                @Override
                public void run() {
                    if (null != callback) {
                        callback.onUnexpectedError(e);
                    }
                }
            });
        }
    }

    /**
     * Save a media URI into the download directory
     *
     * @param context  the context
     * @param srcFile  the source file.
     * @param filename the filename (optional)
     * @param callback the asynchronous callback
     */
    @SuppressLint("NewApi")
    public static void saveMediaIntoDownloads(final Context context,
                                              final File srcFile,
                                              final String filename,
                                              final String mimeType,
                                              final ApiCallback<String> callback) {
        saveFileInto(srcFile, Environment.DIRECTORY_DOWNLOADS, filename, new ApiCallback<String>() {
            @Override
            public void onSuccess(String fullFilePath) {
                if (null != fullFilePath) {
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                    try {
                        File file = new File(fullFilePath);
                        downloadManager.addCompletedDownload(file.getName(), file.getName(), true, mimeType, file.getAbsolutePath(), file.length(), true);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "## saveMediaIntoDownloads(): Exception Msg=" + e.getMessage(), e);
                    }
                }

                if (null != callback) {
                    callback.onSuccess(fullFilePath);
                }
            }

            @Override
            public void onNetworkError(Exception e) {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                if (null != callback) {
                    callback.onNetworkError(e);
                }
            }

            @Override
            public void onMatrixError(MatrixError e) {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                if (null != callback) {
                    callback.onMatrixError(e);
                }
            }

            @Override
            public void onUnexpectedError(Exception e) {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                if (null != callback) {
                    callback.onUnexpectedError(e);
                }
            }
        });
    }

    //==============================================================================================================
    // Low memory management
    //==============================================================================================================

    private static final String LOW_MEMORY_LOG_TAG = "Memory usage";

    /**
     * Log the memory statuses.
     *
     * @param activity the calling activity
     * @return if the device is running on low memory.
     */
    public static boolean displayMemoryInformation(Activity activity, String title) {
        long freeSize = 0L;
        long totalSize = 0L;
        long usedSize = -1L;
        try {
            Runtime info = Runtime.getRuntime();
            freeSize = info.freeMemory();
            totalSize = info.totalMemory();
            usedSize = totalSize - freeSize;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(LOW_MEMORY_LOG_TAG, "---------------------------------------------------");
        Log.e(LOW_MEMORY_LOG_TAG, "----------- " + title + " -----------------");
        Log.e(LOW_MEMORY_LOG_TAG, "---------------------------------------------------");
        Log.e(LOW_MEMORY_LOG_TAG, "usedSize   " + (usedSize / 1048576L) + " MB");
        Log.e(LOW_MEMORY_LOG_TAG, "freeSize   " + (freeSize / 1048576L) + " MB");
        Log.e(LOW_MEMORY_LOG_TAG, "totalSize  " + (totalSize / 1048576L) + " MB");
        Log.e(LOW_MEMORY_LOG_TAG, "---------------------------------------------------");


        if (null != activity) {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);

            Log.e(LOW_MEMORY_LOG_TAG, "availMem   " + (mi.availMem / 1048576L) + " MB");
            Log.e(LOW_MEMORY_LOG_TAG, "totalMem   " + (mi.totalMem / 1048576L) + " MB");
            Log.e(LOW_MEMORY_LOG_TAG, "threshold  " + (mi.threshold / 1048576L) + " MB");
            Log.e(LOW_MEMORY_LOG_TAG, "lowMemory  " + (mi.lowMemory));
            Log.e(LOW_MEMORY_LOG_TAG, "---------------------------------------------------");
            return mi.lowMemory;
        } else {
            return false;
        }
    }


    public static void onTrimMemory(Activity activity, int level) {
        String activityName = (null != activity) ? activity.getClass().getSimpleName() : "NotAvailable";
        Log.e(LOW_MEMORY_LOG_TAG, "Active application : onTrimMemory from " + activityName + " level=" + level);
        // TODO implement things to reduce memory usage

        displayMemoryInformation(activity, "onTrimMemory");
    }


    /**
     * @param session  the session
     * @param password the password
     * @param callback the asynchronous callback.
     */
    public static void exportKeys(final MXSession session, final String password, final ApiCallback<String> callback) {
        final Context appContext = MyApplication.getInstance();

        if (null == session.getCrypto()) {
            if (null != callback) {
                callback.onMatrixError(new MatrixError("EMPTY", "No crypto"));
            }

            return;
        }

        session.getCrypto().exportRoomKeys(password, new SimpleApiCallback<byte[]>(callback) {
            @Override
            public void onSuccess(byte[] bytesArray) {
                try {
                    ByteArrayInputStream stream = new ByteArrayInputStream(bytesArray);
                    String url = session.getMediaCache().saveMedia(stream, "riot-" + System.currentTimeMillis() + ".txt", "text/plain");
                    stream.close();

                    saveMediaIntoDownloads(appContext,
                            new File(Uri.parse(url).getPath()), "riot-keys.txt", "text/plain", new SimpleApiCallback<String>(callback) {
                                @Override
                                public void onSuccess(String path) {
                                    if (null != callback) {
                                        callback.onSuccess(path);
                                    }
                                }
                            });
                } catch (Exception e) {
                    if (null != callback) {
                        callback.onUnexpectedError(e);
                    }
                }
            }
        });
    }

    private static final String TAG_FRAGMENT_UNKNOWN_DEVICES_DIALOG_DIALOG = "ActionBarActivity.TAG_FRAGMENT_UNKNOWN_DEVICES_DIALOG_DIALOG";


}