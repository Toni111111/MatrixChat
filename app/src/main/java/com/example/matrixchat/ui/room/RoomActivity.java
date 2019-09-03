package com.example.matrixchat.ui.room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.matrixchat.Credentialses;
import com.example.matrixchat.Matrix;
import com.example.matrixchat.MyApplication;
import com.example.matrixchat.R;
import com.example.matrixchat.di.LocalStorage;
import com.example.matrixchat.utils.VectorUtils;
import com.example.matrixchat.ui.auth.AuthActivity;
import com.example.matrixchat.ui.room.fragment.ChatRoomFragment;
import de.hdodenhof.circleimageview.CircleImageView;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.core.callback.ApiCallback;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomPreviewData;
import org.matrix.androidsdk.rest.model.sync.RoomResponse;

public class RoomActivity extends AppCompatActivity {

    MXSession mSession;
    String mMyUserId;

    TextView memberValueTv;
    TextView nameRoomTv;
    CircleImageView roomAvatar;
    ImageView popupBtn;
    LocalStorage pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        pref = MyApplication.getComponent().localStorage();

        popupBtn = findViewById(R.id.popupBtn);
        nameRoomTv = findViewById(R.id.room_name_tv);
        memberValueTv = findViewById(R.id.number_info);
        roomAvatar = findViewById(R.id.icMenu);

        mSession = Matrix.getInstance(getApplicationContext()).getSession(Credentialses.INSTANCE.getUserId());
        mSession.startEventStream(null);

        mMyUserId = mSession.getCredentials().userId;

        popupBtn.setOnClickListener(v -> {


            PopupMenu popup = new PopupMenu(RoomActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.popupmenu, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.logout:
                            logout();
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popup.show();//showing popup menu
        });

        String idMyRoom = "#matrixchat:matrix.org";

        Handler handler = new Handler();
        handler.postDelayed(() -> mSession.getRoomsApiClient().joinRoom(idMyRoom, null, null, new ApiCallback<RoomResponse>() {
            @Override
            public void onSuccess(RoomResponse info) {
                Room room = mSession.getDataHandler().getRoom(info.roomId);
                String roomAlias = room.getState().getCanonicalAlias();
                RoomPreviewData roomPreviewData = new RoomPreviewData(mSession, idMyRoom, null, roomAlias, null);
                String eventId = roomPreviewData.getEventId();
                final String previewMode = null;

                Credentialses.INSTANCE.setRoom(room);
                Credentialses.INSTANCE.setMemberCount(room.getNumberOfMembers());
                Credentialses.INSTANCE.setNameRoom(room.getRoomDisplayName(getApplicationContext()));
                Credentialses.INSTANCE.setCurrentTopic(room.getTopic());
                VectorUtils.initAvatarColors(getApplicationContext());

                nameRoomTv.setText(Credentialses.INSTANCE.getNameRoom());
                memberValueTv.setText(Credentialses.INSTANCE.getMemberCount().toString() + "/" + Credentialses.INSTANCE.getCurrentTopic());
                VectorUtils.loadRoomAvatar(getApplicationContext(), mSession, roomAvatar, Credentialses.INSTANCE.getRoom());

                ChatRoomFragment mVectorMessageListFragment = ChatRoomFragment.newInstance(mMyUserId, idMyRoom, eventId, previewMode,
                        org.matrix.androidsdk.R.layout.fragment_matrix_message_list_fragment);
                getSupportFragmentManager().beginTransaction().add(R.id.container, mVectorMessageListFragment).commit();
            }

            @Override
            public void onUnexpectedError(Exception e) {
                Log.d("test", "test");
            }

            @Override
            public void onNetworkError(Exception e) {
                Log.d("test", "test");
            }

            @Override
            public void onMatrixError(MatrixError e) {
                Log.d("test", "test");
            }
        }), 2000);
    }

    //Видел есть в sdk похожий метод
    private void logout(){
        Intent intent = new Intent(RoomActivity.this,AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        pref.saveCredentials(null);
    }
}
