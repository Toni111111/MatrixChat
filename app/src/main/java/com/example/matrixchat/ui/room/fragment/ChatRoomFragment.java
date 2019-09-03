package com.example.matrixchat.ui.room.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.matrixchat.Credentialses;
import com.example.matrixchat.Matrix;
import com.example.matrixchat.MyApplication;
import com.example.matrixchat.R;
import com.example.matrixchat.di.LocalStorage;
import com.example.matrixchat.model.Message;
import com.example.matrixchat.model.User;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.adapters.AbstractMessagesAdapter;
import org.matrix.androidsdk.core.JsonUtils;
import org.matrix.androidsdk.core.callback.ApiCallback;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.data.MyUser;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomMediaMessage;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.db.MXMediaCache;
import org.matrix.androidsdk.fragments.MatrixMessageListFragment;
import org.matrix.androidsdk.fragments.MatrixMessagesFragment;
import org.matrix.androidsdk.listeners.IMXEventListener;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.TokensChunkEvents;
import org.matrix.androidsdk.rest.model.bingrules.BingRule;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomFragment extends MatrixMessageListFragment
        implements MessageInput.InputListener,
        MessagesListAdapter.SelectionListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener,
        MessagesListAdapter.OnLoadMoreListener {

    private final String senderId = "0";
    private MessagesListAdapter<Message> messagesAdapter;
    private Room room;
    private static final int TOTAL_MESSAGES_COUNT = 15;
    private ArrayList<Message> messages = new ArrayList<>();
    private ImageLoader imageLoader;
    private MessagesList messagesList;
    private MessageInput messageInput;
    private LocalStorage pref;


    @Override
    public MXMediaCache getMXMediaCache() {
        return Matrix.getInstance(getActivity()).getMediaCache();
    }

    @Override
    public MXSession getSession(String matrixId) {
        return Matrix.getMXSession(getActivity(), matrixId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);
        messagesList = view.findViewById(R.id.messagesList);
        messageInput = view.findViewById(R.id.input);

        pref = MyApplication.getComponent().localStorage();

        room = Credentialses.INSTANCE.getRoom();

        mSession = Matrix.getInstance(getActivity()).getSession(Credentialses.INSTANCE.getUserId());

        mSession.getDataHandler().addListener(new IMXEventListener() {
            @Override
            public void onStoreReady() {

            }

            @Override
            public void onPresenceUpdate(Event event, org.matrix.androidsdk.rest.model.User user) {

            }

            @Override
            public void onAccountInfoUpdate(MyUser myUser) {

            }

            @Override
            public void onIgnoredUsersListUpdate() {

            }

            @Override
            public void onDirectMessageChatRoomsListUpdate() {

            }

            @Override
            public void onLiveEvent(Event event, RoomState roomState) {
                eventToMessage(event);
            }

            @Override
            public void onLiveEventsChunkProcessed(String fromToken, String toToken) {

            }

            @Override
            public void onBingEvent(Event event, RoomState roomState, BingRule bingRule) {

            }

            @Override
            public void onEventSentStateUpdated(Event event) {

            }

            @Override
            public void onEventSent(Event event, String prevEventId) {

            }

            @Override
            public void onEventDecrypted(String roomId, String eventId) {

            }

            @Override
            public void onBingRulesUpdate() {

            }

            @Override
            public void onInitialSyncComplete(String toToken) {

            }

            @Override
            public void onSyncError(MatrixError matrixError) {

            }

            @Override
            public void onCryptoSyncComplete() {

            }

            @Override
            public void onNewRoom(String roomId) {

            }

            @Override
            public void onJoinRoom(String roomId) {

            }

            @Override
            public void onRoomFlush(String roomId) {

            }

            @Override
            public void onRoomInternalUpdate(String roomId) {

            }

            @Override
            public void onNotificationCountUpdate(String roomId) {

            }

            @Override
            public void onLeaveRoom(String roomId) {

            }

            @Override
            public void onRoomKick(String roomId) {

            }

            @Override
            public void onReceiptEvent(String roomId, List<String> senderIds) {

            }

            @Override
            public void onRoomTagEvent(String roomId) {

            }

            @Override
            public void onReadMarkerEvent(String roomId) {

            }

            @Override
            public void onToDeviceEvent(Event event) {

            }

            @Override
            public void onNewGroupInvitation(String groupId) {

            }

            @Override
            public void onJoinGroup(String groupId) {

            }

            @Override
            public void onLeaveGroup(String groupId) {

            }

            @Override
            public void onGroupProfileUpdate(String groupId) {

            }

            @Override
            public void onGroupRoomsListUpdate(String groupId) {

            }

            @Override
            public void onGroupUsersListUpdate(String groupId) {

            }

            @Override
            public void onGroupInvitedUsersListUpdate(String groupId) {

            }

            @Override
            public void onAccountDataUpdated() {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        room.requestServerRoomHistory(room.getState().getToken(), 15, new ApiCallback<TokensChunkEvents>() {
            @Override
            public void onSuccess(TokensChunkEvents info) {
                addMessages(info.chunk);
            }

            @Override
            public void onUnexpectedError(Exception e) {
                Log.d("error:", "onUnexpectedError");
            }

            @Override
            public void onNetworkError(Exception e) {
                Log.d("error:", "onNetworkError");
            }

            @Override
            public void onMatrixError(MatrixError e) {
                Log.d("error:", "onNetworkError");
            }
        });


        imageLoader = (imageView, url, payload) -> {
            imageView.setVisibility(View.GONE);
        };

        initAdapter();
        messageInput.setInputListener(this);
        messageInput.setTypingListener(this);
        messageInput.setAttachmentsListener(this);
    }

    @Override
    public AbstractMessagesAdapter createMessagesAdapter() {
        return null;
    }

    @Override
    public MatrixMessagesFragment createMessagesFragmentInstance(String roomId) {
        return MatrixChatMessagesFragment.newInstance(roomId);
    }

    public static ChatRoomFragment newInstance(String matrixId, String roomId, String eventId, String previewMode, int layoutResId) {
        ChatRoomFragment f = new ChatRoomFragment();
        Bundle args = getArguments(matrixId, roomId, layoutResId);
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_PREVIEW_MODE_ID, previewMode);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        room.sendTextMessage(input.toString(), input.toString(), input.toString(), new RoomMediaMessage.EventCreationListener() {
            @Override
            public void onEventCreated(RoomMediaMessage roomMediaMessage) {
                Toast.makeText(getContext(), getContext().getString(R.string.success_msg_txt), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEventCreationFailed(RoomMediaMessage roomMediaMessage, String errorMessage) {
                Toast.makeText(getContext(), getContext().getString(R.string.error_msg_txt), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEncryptionFailed(RoomMediaMessage roomMediaMessage) {
                Toast.makeText(getContext(), getContext().getString(R.string.error_msg_txt), Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }

    @Override
    public void onAddAttachments() {

    }

    private void initAdapter() {
        messagesAdapter = new MessagesListAdapter<>(senderId, imageLoader);
        messagesAdapter.enableSelectionMode(this);
        messagesAdapter.setLoadMoreListener(this);
        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                (view, message) -> {

                });
        this.messagesList.setAdapter(messagesAdapter);
    }

    @Override
    public void onStartTyping() {
        Log.v("Typing listener", getString(R.string.start_typing_status));
    }

    @Override
    public void onStopTyping() {
        Log.v("Typing listener", getString(R.string.stop_typing_status));
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.i("TAG", "onLoadMore: " + page + " " + totalItemsCount);
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            loadMessages();
        }
    }

    private void loadMessages() {
        new Handler().postDelayed(() -> {
            ArrayList<Message> messages = Credentialses.INSTANCE.getMessages();
            messagesAdapter.addToEnd(messages, false);
        }, 1000);
    }

    @Override
    public void onSelectionChanged(int count) {

    }

    void addMessages(List<Event> events) {
        for (int i = 0; i < events.size(); i++) {
            org.matrix.androidsdk.rest.model.message.Message message = JsonUtils.toMessage(events.get(i).contentJson);
            if (i == 0) {
                if (events.get(i).userId.equals(pref.getCredentials().userId)) {
                    User user = new User("0", events.get(i).sender,
                            "", true);
                    Message msg = new Message(String.valueOf(events.get(i).originServerTs), user, message.body);
                    messagesAdapter.addToStart(msg, true);
                } else {
                    User user = new User(events.get(i).userId, events.get(i).sender,
                            "", true);
                    Message msg = new Message(String.valueOf(events.get(i).originServerTs), user, message.body);
                    messagesAdapter.addToStart(msg, true);
                }
            } else {
                if (events.get(i).userId.equals(pref.getCredentials().userId)) {
                    User user = new User("0", events.get(i).sender,
                            "", true);
                    Message msg = new Message(String.valueOf(events.get(i).originServerTs), user, message.body);
                    messages.add(msg);
                } else {
                    User user = new User(events.get(i).userId, events.get(i).sender,
                            "", true);
                    Message msg = new Message(String.valueOf(events.get(i).originServerTs), user, message.body);
                    messages.add(msg);
                }
            }
        }
        Credentialses.INSTANCE.setMessages(messages);
    }

    void eventToMessage(Event event) {
        //user id почему-то null, присвоил sender ,чтобы отображались сообщения,но это неправильно. надо разбираться //
        org.matrix.androidsdk.rest.model.message.Message message = JsonUtils.toMessage(event.contentJson);
        if (message.body != null) {
            if (event.sender.equals(pref.getCredentials().userId)) {
                User user = new User("0", event.sender,
                        "", true);
                Message msg = new Message(String.valueOf(event.originServerTs), user, message.body);
                messages.add(msg);
                messagesAdapter.addToStart(msg, true);
            } else {
                User user = new User(event.sender, event.sender,
                        "", true);
                Message msg = new Message(String.valueOf(event.originServerTs), user, message.body);
                messages.add(msg);
                messagesAdapter.addToStart(msg, true);
            }
        }
    }
}


