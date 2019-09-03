package com.example.matrixchat.ui.room.fragment;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.example.matrixchat.Credentialses;
import com.example.matrixchat.Matrix;
import com.example.matrixchat.R;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.fragments.MatrixMessagesFragment;

public class MatrixChatMessagesFragment extends MatrixMessagesFragment {
    private static final String LOG_TAG = MatrixChatMessagesFragment.class.getSimpleName();

    public static MatrixChatMessagesFragment newInstance(String roomId) {
        MatrixChatMessagesFragment fragment = new MatrixChatMessagesFragment();
        fragment.setArguments(getArgument(roomId));
        return fragment;
    }

    @Override
    protected void displayInitializeTimelineError(Object error) {
        String errorMessage = "";

        if (error instanceof MatrixError) {
            MatrixError matrixError = (MatrixError) error;

            if (TextUtils.equals(matrixError.errcode, MatrixError.NOT_FOUND)) {
                errorMessage = getContext().getString(R.string.failed_to_load_timeline_position, Matrix.getApplicationName());
            } else {
                errorMessage = matrixError.getLocalizedMessage();
            }
        } else if (error instanceof Exception) {
            errorMessage = ((Exception) error).getLocalizedMessage();
        }

        if (!TextUtils.isEmpty(errorMessage)) {
            Log.d(LOG_TAG, "displayInitializeTimelineError : " + errorMessage);
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}