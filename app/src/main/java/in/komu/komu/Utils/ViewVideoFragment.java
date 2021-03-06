package in.komu.komu.Utils;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
//import com.hoanganhtuan95ptit.autoplayvideorecyclerview.AutoPlayVideoRecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import in.komu.komu.Models.Comment;
import in.komu.komu.Models.Photo;
import in.komu.komu.Models.Video;
import in.komu.komu.Models.users_profile;
import in.komu.komu.Profile.activity_accountSetting;
import in.komu.komu.R;


public class ViewVideoFragment extends Fragment {

    private static final String TAG = "ViewVideoFragment";
    //vars
    private Video mVideo;
    private int mActivityNum;

    public interface OnCommentThreadSelectedListener{
        void onCommentThreadSelectedListener(Video video);
    }
    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;


    public ViewVideoFragment(){
        super();
        setArguments(new Bundle());
    }



    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private users_profile mUserProfile;
    private Heart mHeart;

    private GestureDetector mGestureDetector;
    private GestureDetector mPostGestureDetector;



    //widgets
//    private AutoPlayVideoRecyclerView mVideoPost;
    private VideoView mVideoPost;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp, mLikes, mComments, mTVUsername;
    private ImageView mBackArrow, mEllipses, mHeartRed, mHeartWhite, mProfileImage, mComment;

    //vars
    private int mActivityNumber = 0;
    private String photoUsername = "";
    private String profilePhotoUrl = "";
    private activity_accountSetting mUserAccountSettings;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";
    private users_profile mCurrentUser;

    private MediaController videoMediaController;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_video_post, container, false);

        mVideoPost = (VideoView) view.findViewById(R.id.videoPost);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimestamp = (TextView) view.findViewById(R.id.image_time_posted);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mComment = (ImageView) view.findViewById(R.id.ic_comment);
        mEllipses = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo);
        mLikes = (TextView) view.findViewById(R.id.tvLikes);
        mCaption = (TextView) view.findViewById(R.id.imgCaption);
        mTVUsername = view.findViewById(R.id.tvUsername);
        mComment = (ImageView) view.findViewById(R.id.ic_comment);
        mComments = (TextView) view.findViewById(R.id.image_comments_link);




        mVideo = getVideoFromBundle();
//        Toast.makeText(getActivity(), "Caption is " + mPhoto.getCaption(), Toast.LENGTH_SHORT).show();


        mHeart = new Heart(mHeartRed, mHeartWhite);
        mGestureDetector = new GestureDetector(getActivity(), new gestureDetector());
        mPostGestureDetector = new GestureDetector(getActivity(), new PostImageGestureDetector());


//        CALLING METHOD
        setupFirebaseAuth();
//        setupWidgets();

        try{
//            UniversalImageLoader.setImage(mPhoto.getImage_path(), mPostImage, null, "");
//            Glide.with(getActivity())
//                    .load(mPhoto.getImage_path())
//                    .apply(new RequestOptions()
//                            .placeholder(R.drawable.loading_image)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .centerCrop()
//                    )
//                    .into(mPostImage);

            String videoPath = mVideo.toString();
//            Uri uri = Uri.parse(videoPath); //Declare your url here.

//            mVideoPost.setMediaController(new MediaController(this));
//            mVideoPost.setVideoURI(uri);
//            mVideoPost.requestFocus();
//            mVideoPost.start();
//            mVideoPost.

            videoMediaController = new MediaController(getContext());
            mVideoPost.setVideoPath(videoPath);
            videoMediaController.setMediaPlayer(mVideoPost);
            mVideoPost.setMediaController(videoMediaController);
            mVideoPost.requestFocus();
            mVideoPost.start();

            // get Activity Number
            mActivityNum = getActivityNumFromBundle();

        }catch (NullPointerException e ){
            Log.d(TAG, "onCreateView: NullPointerException "+ e.getMessage());
        }

        return view;
    }


    private void init(){
        try{
            //mPhoto = getPhotoFromBundle();
//            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), mPostImage, null, "");
//            Glide.with(getActivity())
//                    .load(getVideoFromBundle().getImage_path())
//                    .apply(new RequestOptions()
//                            .placeholder(R.drawable.loading_image)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .centerCrop()
//                    )
//                    .into(mPostImage);
            mActivityNumber = getActivityNumFromBundle();
            String video_id = getVideoFromBundle().getVideo_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.db_videos))
                    .orderByChild(getString(R.string.field_video_id))
                    .equalTo(video_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        Video newVideo = new Video();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        newVideo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        newVideo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        newVideo.setVideo_id(objectMap.get(getString(R.string.field_video_id)).toString());
                        newVideo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        newVideo.setTimestamp(objectMap.get(getString(R.string.field_time_stamp)).toString());
                        newVideo.setVideo_url(objectMap.get(getString(R.string.field_video_url)).toString());

                        List<Comment> commentsList = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            commentsList.add(comment);
                        }
                        newVideo.setComments(commentsList);

                        mVideo = newVideo;

                        getCurrentUser();
                        getVideoDetails();
                        //getLikesString();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: query cancelled.");
                }
            });

        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage() );
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(isAdded()){
            init();
        }
    }

    private void getLikesString(){
        Log.d(TAG, "getLikesString: getting likes string");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.db_videos))
                .child(mVideo.getVideo_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.db_users_profile))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found like: " +
                                        singleSnapshot.getValue(users_profile.class).getUsername());

                                mUsers.append(singleSnapshot.getValue(users_profile.class).getUsername());
                                mUsers.append(",");
                            }

                            String[] splitUsers = mUsers.toString().split(",");

                            if(mUsers.toString().contains(mCurrentUser.getUsername() + ",")){//mitch, mitchell.tabian
                                mLikedByCurrentUser = true;
                            }else{
                                mLikedByCurrentUser = false;
                            }

                            int length = splitUsers.length;
                            if(length == 1){
                                mLikesString = "Liked by " + splitUsers[0];
                            }
                            else if(length == 2){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + " and " + splitUsers[1];
                            }
                            else if(length == 3){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + " and " + splitUsers[2];

                            }
                            else if(length == 4){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + splitUsers[3];
                            }
                            else if(length > 4){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + (splitUsers.length - 3) + " others";
                            }
                            Log.d(TAG, "onDataChange: likes string: " + mLikesString);
                            setupWidgets();


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if(!dataSnapshot.exists()){
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
//                    mCaption.setText(mPhoto.getCaption());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.db_users_profile))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mCurrentUser = singleSnapshot.getValue(users_profile.class);
                }
                getLikesString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    public class gestureDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            GestureDetectorLike();
            return true;
        }
    }

    public class PostImageGestureDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            GestureDetectorLike();
            return true;
        }
    }

    public void GestureDetectorLike(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.db_videos))
                .child(mVideo.getVideo_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    String keyID = singleSnapshot.getKey();

                    //case1: Then user already liked the photo
                    if (mLikedByCurrentUser &&
                            singleSnapshot.getValue(Like.class).getUser_id()
                                    .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        myRef.child(getString(R.string.db_videos))
                                .child(mVideo.getVideo_id())
                                .child(getString(R.string.field_likes))
                                .child(keyID)
                                .removeValue();

                        mHeart.toggleLike();
                        getLikesString();
                    }
                    //case2: The user has not liked the photo
                    else if (!mLikedByCurrentUser) {
                        //add new like
                        addNewLike();
                        break;
                    }
                }
                if (!dataSnapshot.exists()) {
                    //add new like
                    addNewLike();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //    Get Photo Details
    private void getVideoDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.db_users_profile))
                .orderByChild(getString(R.string.user_id))
                .equalTo(mVideo.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){

                    mUserProfile = singleSnapshot.getValue(users_profile.class);

                    Glide.with(getContext().getApplicationContext())
                            .load(mUserProfile.getProfile_photo())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.loading_image)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                            )
                            .into(mProfileImage);
                    mUsername.setText(mUserProfile.getUsername());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Add New Like
    private void addNewLike(){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.db_videos))
                .child(mVideo.getVideo_url())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
    }


    private void setupWidgets()  {
        String timestampDiff = getTimestampDifference();

        if(!timestampDiff.equals("0")){
            mTimestamp.setText(timestampDiff + " DAYS AGO");
        }else{
            mTimestamp.setText("TODAY");
        }

        if(mVideo.getComments().size()> 0){
            mComments.setText("View all " + mVideo.getComments().size() + " comments");
        }else{
            mComments.setText("");
        }

        Glide.with(getContext().getApplicationContext())
                .load(mUserProfile.getProfile_photo())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.loading_image)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                )
                .into(mProfileImage);

        mUsername.setText(mUserProfile.getUsername());
        mLikes.setText(mLikesString);
        mCaption.setText(mVideo.getCaption());
        mTVUsername.setText(mUserProfile.getDisplay_name());

        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");

                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mVideo);

            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
//              Toast.makeText(getActivity(), "mPhoto" + mPhoto.getImage_path(), Toast.LENGTH_SHORT).show();
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mVideo);

            }
        });


        if(mLikedByCurrentUser){
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: red heart touch detected.");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
            mVideoPost.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mPostGestureDetector.onTouchEvent(event);
                }
            });
        }
        else{
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: white heart touch detected.");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
            mVideoPost.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mPostGestureDetector.onTouchEvent(event);
                }
            });
        }


        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });



    }

    // Get Time and Date For our post
    private String getTimestampDifference()  {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = mVideo.getTimestamp();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }

    private int getActivityNumFromBundle(){
        Log.d(TAG, "getActivityNumFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            return bundle.getInt("activity_number");
        }else{
            return 0;
        }
    }

    private Video getVideoFromBundle(){
        Log.d(TAG, "getVideoFromBundle: Get Video");

        Bundle bundle = this.getArguments();
        if (bundle!= null){
            return bundle.getParcelable(getString(R.string.videos));
        }else{

            return null;
        }
    }


       /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}




































