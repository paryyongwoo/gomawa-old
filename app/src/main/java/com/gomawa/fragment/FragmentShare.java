package com.gomawa.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.gomawa.R;
import com.gomawa.activity.WriteActivity;
import com.gomawa.common.CommonUtils;
import com.gomawa.common.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FragmentShare extends Fragment {

    /**
     * 뷰
     */
    private ViewGroup rootView = null;
    private ListView shareListView = null;
    private ImageButton writeBtn = null;
    private ImageButton listBtn = null;
    private ImageButton myListBtn = null;
    private TextView writeTextView = null;
    private TextView listTextView = null;
    private TextView myListTextView = null;
    private TextView pageTextView = null;

    /**
     * 글쓰기, 목록, 내글 프래그먼트
     */
    private Fragment writeFragment = null;
    private Fragment allListFragment = null;
    private Fragment myListFragment = null;

    /**
     * 프래그먼트 매니저
     */
    private FragmentManager fm = null;

    /**
     * 현재 선택한 메뉴 값
     */
    private final String WRITE = "WRITE";
    private final String LIST = "LIST";
    private final String MY_LIST = "MY_LIST";

    public boolean isWrite = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /**
         * rootView 생성
         */
        rootView = (ViewGroup)inflater.inflate(R.layout.fragment_share, container, false);

        /**
         * 초기화 작업
         */
        initView();

        /**
         * FragmentManager 얻기
         */
        fm = getChildFragmentManager();

        if (allListFragment == null) {
            /**
             * fm에 프래그먼트를 add 할때, tag를 지정해준다. (글작성 후에 listFragment의 데이터 로딩 함수를 호출하기 위해)
             */
            allListFragment = new FragmentShareList(Constants.ALL_LIST);
            fm.beginTransaction().add(R.id.share_frame_layout, allListFragment, "allListFragment").commit();
        }

        return rootView;
    }

    /**
     * @description
     * 프래그먼트별 액션바 설정하는 방법
     * 1. 프래그먼트의 onCreate메소드를 오버라이딩해서 setHasOptionsMenu(true)를 통해 프래그먼트의 액션바 메뉴를 사용하겠다고 설정한다.
     * 2. onCreateOptionsMenu메소드를 오버라이딩해서 메뉴 리소스를 설정한다.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d("fragment", "onCreateShare");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // 메뉴 리소스 설정
        inflater.inflate(R.menu.menu_toolbar, menu);
    }

    private void initView() {
        /**
         * 헤더 텍스트 설정
         */
        String headerTitle = getResources().getString(R.string.header_title_share);
        String headerSubTitle = getResources().getString(R.string.sub_title_share);
        TextView headerText = rootView.findViewById(R.id.header_title);
        headerText.setText(headerTitle);
        TextView headerSubTitleText = rootView.findViewById(R.id.header_subtitle);
        headerSubTitleText.setText(headerSubTitle);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) headerSubTitleText.getLayoutParams();
        marginLayoutParams.setMargins(0, marginLayoutParams.topMargin - 20, 0, 0);

        /**
         * 헤더 버튼 (목록, 글쓰기, 내글보기)
         */
        listBtn = rootView.findViewById(R.id.list_btn);
        writeBtn = rootView.findViewById(R.id.write_btn);
        myListBtn = rootView.findViewById(R.id.my_list_btn);
        listTextView = rootView.findViewById(R.id.list_textview);
        writeTextView = rootView.findViewById(R.id.write_textview);
        myListTextView = rootView.findViewById(R.id.my_list_textview);

        // TODO: 2020-04-20 버튼 눌렀을 때 adapter notify 해줘야함 ( 좋아요 반영이 안됨 ) 스크롤은 안 해야함
        
        /**
         * 목록 (목록과 내글보기는 프래그먼트 전환시 구분자를 함께 전달해줘야함)
         */
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (writeFragment != null) fm.beginTransaction().hide(writeFragment).commit();
                if (allListFragment != null) {
                    if(!(allListFragment.isHidden())) {
                        // 이미 보여지고 있었다면 새로고침
                        Toast.makeText(getContext(), "새로고침", Toast.LENGTH_SHORT).show();

                        FragmentShareList allListFragment = (FragmentShareList) fm.findFragmentByTag("allListFragment");
                        allListFragment.getShareItems(0);
                    }

                    fm.beginTransaction().show(allListFragment).commit();
                }
                if (myListFragment != null) fm.beginTransaction().hide(myListFragment).commit();

                // 메뉴 활성화
                setMenuActive(LIST);
            }
        });

        /**
         * 글쓰기
         */
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (writeFragment == null) {
                    writeFragment = new FragmentShareWrite();
                    fm.beginTransaction().add(R.id.share_frame_layout, writeFragment, "writeFragment").commit();
                }
                if (writeFragment != null) fm.beginTransaction().show(writeFragment).commit();
                if (allListFragment != null) fm.beginTransaction().hide(allListFragment).commit();
                if (myListFragment != null) fm.beginTransaction().hide(myListFragment).commit();

                // 메뉴 활성화
                setMenuActive(WRITE);
            }
        });

        /**
         * 내글보기 (목록과 내글보기는 프래그먼트 전환시 구분자를 함께 전달해줘야함)
         */
        myListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myListFragment == null) {
                    myListFragment = new FragmentShareList(Constants.MY_LIST);
                    fm.beginTransaction().add(R.id.share_frame_layout, myListFragment, "myListFragment").commit();
                    fm.beginTransaction().show(myListFragment).commit();
                } else {
                    if(!(myListFragment.isHidden())) {
                        // 이미 보여지고 있었다면 새로고침
                        FragmentShareList myListFragment = (FragmentShareList) fm.findFragmentByTag("myListFragment");
                        myListFragment.getShareItems(0);
                    }
                    fm.beginTransaction().show(myListFragment).commit();
                }

                if (writeFragment != null) fm.beginTransaction().hide(writeFragment).commit();
                if (allListFragment != null) fm.beginTransaction().hide(allListFragment).commit();

                // 메뉴 활성화
                setMenuActive(MY_LIST);
            }
        });

        /**
         * Floating Button
         */
        FloatingActionButton floatingActionButton = rootView.findViewById(R.id.share_floating_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WriteActivity.class);
                startActivityForResult(intent, Constants.REQUEST_WRITE);
            }
        });
    }

    /**
     * 글작성 완료 후에, 최신 상태의 shareList를 가져오기 위한 함수
     */
    public void moveShareList() {
        isWrite = true;
        if (writeFragment != null) fm.beginTransaction().hide(writeFragment).commit();
        if (this.allListFragment != null) fm.beginTransaction().show(this.allListFragment).commit();
    }

    /**
     * 상단 메뉴 클릭시 리소스 변경해주는 메소드
     * @param menuType
     */
    private void setMenuActive(String menuType) {
        switch (menuType) {
            case WRITE:
                writeBtn.setImageResource(R.drawable.write_btn);
                listBtn.setImageResource(R.drawable.list_btn_disable);
                myListBtn.setImageResource(R.drawable.user_btn_disable);
                writeTextView.setTextColor(getResources().getColor(R.color.mainColor));
                listTextView.setTextColor(getResources().getColor(R.color.blackColor));
                myListTextView.setTextColor(getResources().getColor(R.color.blackColor));
                break;
            case LIST:
                writeBtn.setImageResource(R.drawable.write_btn_disable);
                listBtn.setImageResource(R.drawable.list_btn);
                myListBtn.setImageResource(R.drawable.user_btn_disable);
                writeTextView.setTextColor(getResources().getColor(R.color.blackColor));
                listTextView.setTextColor(getResources().getColor(R.color.mainColor));
                myListTextView.setTextColor(getResources().getColor(R.color.blackColor));
                break;
            case MY_LIST:
                writeBtn.setImageResource(R.drawable.write_btn_disable);
                listBtn.setImageResource(R.drawable.list_btn_disable);
                myListBtn.setImageResource(R.drawable.user_btn);
                writeTextView.setTextColor(getResources().getColor(R.color.blackColor));
                listTextView.setTextColor(getResources().getColor(R.color.blackColor));
                myListTextView.setTextColor(getResources().getColor(R.color.mainColor));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.REQUEST_WRITE) {
            if(resultCode == Constants.RESULT_OK) {
                FragmentShareList allListFragment = (FragmentShareList) fm.findFragmentByTag("allListFragment");
                allListFragment.getShareItems(0);

                if(myListFragment != null) {
                    FragmentShareList myListFragment = (FragmentShareList) fm.findFragmentByTag("myListFragment");
                    myListFragment.getShareItems(0);
                }

            }
        }
    }
}
