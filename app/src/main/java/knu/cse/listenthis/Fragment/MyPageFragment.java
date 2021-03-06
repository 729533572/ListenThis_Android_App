package knu.cse.listenthis.Fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import knu.cse.listenthis.CustomAdapter.CustomAdapter;
import knu.cse.listenthis.CustomAdapter.CustomAdapter2;
import knu.cse.listenthis.PhysicalArchitecture.ClientController;
import knu.cse.listenthis.PhysicalArchitecture.ImageController;
import knu.cse.listenthis.ProblemDomain.Constants;
import knu.cse.listenthis.ProblemDomain.Posts;
import knu.cse.listenthis.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static knu.cse.listenthis.ProblemDomain.Constants.GET_PICTURE_URI;


public class MyPageFragment extends Fragment {

    private static Handler handler;
    private ClientController client = null;
    private ArrayList<Posts> myPostsList;

    private ImageButton change_arr;
    private Button editbutton;

    private ImageView fillbar;
    private ImageView unfillbar;
    private ImageView profileImage;

    private TextView postcnt;
    private TextView nametext;
    private TextView likecnt;

    private ListView mylist;
    private boolean lastitemVisibleFlag = false;
    private int arr_type=1;

    private  View view;

    public MyPageFragment() {
        // Required empty public constructor
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(client == null)
            client = ClientController.getClientControl();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                client.setMyPageHandler(null);

                if(msg.what== Constants.RECEIVE_REFRESH){
                    myPostsList = client.getMyPostsList();
                    int totalCnt = 0;
                    for(Posts posts : client.getMyPostsList()){
                        totalCnt += posts.getLike();
                    }
                    client.setMyLikeCount(totalCnt);
                    client.getMe().setTotalLike(totalCnt);
                    client.setMyPageHandler(this);
                    client.totalLike();
                    mylist.setAdapter(new CustomAdapter(myPostsList,client.getMe()));
                }else if(msg.what==Constants.RECEIVE_SUCCESSS){

                    // TODO when receive fin message
                }else if(msg.what==Constants.RECEIVE_FAILED){
                    // TODO when receive err message
                }
            }
        };

        client.setMyPageHandler(handler);
        client.myPosts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_my_page, container, false);

        profileImage = (ImageView) view.findViewById(R.id.user_image);

        if(client.getMe() != null){
            if(client.getMe().getIImage() != null){
                profileImage.setImageDrawable(ImageController.ByteToDrawable(client.getMe().getIImage()));
            }
        }

        nametext=(TextView)view.findViewById(R.id.user_name);
        String name=client.getMe().getId();
        String names[]=name.split("@");
        nametext.setText(names[0]);

        postcnt=(TextView)view.findViewById(R.id.mypostcntTextview);
        likecnt=(TextView)view.findViewById(R.id.mylikecount);

        mylist=(ListView)view.findViewById(R.id.mylist);
        mylist.setAdapter(new CustomAdapter(client.getMyPostsList(),client.getMe()));
        mylist.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //현재 화면에 보이는 첫번째 리스트 아이템의 번호(firstVisibleItem) + 현재 화면에 보이는 리스트 아이템의 갯수(visibleItemCount)가 리스트 전체의 갯수(totalItemCount) -1 보다 크거나 같을때
                lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //OnScrollListener.SCROLL_STATE_IDLE은 스크롤이 이동하다가 멈추었을때 발생되는 스크롤 상태입니다.
                //즉 스크롤이 바닦에 닿아 멈춘 상태에 처리를 하겠다는 뜻
                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastitemVisibleFlag) {
                    //TODO 화면이 바닦에 닿을때 처리
                    client.setMyPageHandler(handler);
                    client.moreMyPosts();
                }
            }
        });
/////////////////////stretch fill bar according to like count
        fillbar=(ImageView)view.findViewById(R.id.fill_like_bar);
        unfillbar=(ImageView)view.findViewById(R.id.unfillbar);

        RelativeLayout f=(RelativeLayout) view.findViewById(R.id.r);
        f.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                double h=(double) unfillbar.getHeight();
                double w=(double)unfillbar.getWidth()/3;
                double count=0.0;

                count=client.getMyLikeCount();
                if(count<250) w=(w*(count/250.0));
                fillbar.setLayoutParams(new RelativeLayout.LayoutParams((int)w,(int)h));

                postcnt.setText(""+client.getMe().getMyList().size());
                int cnt=0;
                try {
                    cnt = client.getMyLikeCount();
                }catch (Exception e) {}
                likecnt.setText("like " + cnt);
            }
        });

//////////////////////////////////////////////////////////////////////////////////////////

        editbutton = (Button)view.findViewById(R.id.profilebutton);
        editbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, GET_PICTURE_URI);
            }
        });

        change_arr=(ImageButton)view.findViewById(R.id.mypagechangearr);
        change_arr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("test","Change arr bnt onclick listner start ");
                if(arr_type==1) {
                    arr_type=2;
                    CustomAdapter2 adapter2=new CustomAdapter2(client.getMyPostsList());
                    mylist.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged();
                    change_arr.setImageResource(R.drawable.profilearr1);
                } else {
                    arr_type=1;
                    CustomAdapter adapter=new CustomAdapter(client.getMyPostsList(),client.getMe());
                    mylist.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    change_arr.setImageResource(R.drawable.profilearr2);
                }
            }
        });
        client.setMyPageHandler(handler);
        client.myPosts();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("test", "select picture");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_PICTURE_URI) {
            Log.d("test", "select picture2");
            try {
                ContentResolver resolver = getActivity().getContentResolver();
                Uri selPhotoUri = data.getData();
                Bitmap selPhoto = MediaStore.Images.Media.getBitmap(resolver, selPhotoUri);

                byte[] image = ImageController.bitmapToByteArray(selPhoto, 50);

                client.getMe().setIImage(image);
                profileImage.setImageBitmap(selPhoto);

                client.setMyPageHandler(handler);
                client.updateUser(client.getMe());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
