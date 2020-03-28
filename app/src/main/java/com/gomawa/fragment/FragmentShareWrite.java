package com.gomawa.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gomawa.R;
import com.gomawa.common.Global;
import com.gomawa.dto.DailyThanks;
import com.gomawa.dto.Member;
import com.gomawa.dto.ShareItem;
import com.gomawa.network.RetrofitHelper;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentShareWrite extends Fragment {
    // TODO: 2020-02-21 확정 버튼 클릭시 서버로 데이터 전달 후 데이터베이스에 저장
    // TODO: 2020-02-21 배경이미지도 함께 저장 (저장소 필요)

    // 앨범 인텐트로 전달하는 requestCode
    private static final int PICK_FROM_CAMERA = 0; //카메라 촬영으로 사진 가져오기
    private static final int PICK_FROM_ALBUM = 1;

    // 앨범에서 가져온 이미지의 uri
    private Uri capturedImageUri = null;
    // 앨범에서 가져온 이미지가 최종 저장될 위치
    private String imagePath = null;
    // 선택한 배경화면 이미지 파일 (서버에 전송할 파일)
    private File uploadImageFile = null;

    private ViewGroup rootView = null;

    private EditText editText = null;

    // 배경화면 버튼을 감싸는 레이아웃
    private LinearLayout uploadBtn = null;
    // 완료 버튼을 감싸는 레이아웃
    private LinearLayout completeBtn = null;
    private ImageView writeBackgroundImageView = null;
    private FrameLayout frameLayout = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup)inflater.inflate(R.layout.fragment_share_write, container, false);

        initView();

        return rootView;
    }

    private void initView() {
        editText = rootView.findViewById(R.id.share_write_edit);
        writeBackgroundImageView = rootView.findViewById(R.id.share_write_set);
        frameLayout = rootView.findViewById(R.id.share_write_frame);

        /**
         * 배경 클릭하면 EditText에 포커스 주기
         */
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 키보드 보이게
                Global.showKeyboard(getActivity(), editText);
            }
        });

        /**
         * 화면이 렌더링 됨과 동시에 키보드 보이게
         */
        Global.showKeyboard(getActivity(), editText);

        /**
         * 업로드, 완료 버튼 이벤트 등록
         */
        uploadBtn = rootView.findViewById(R.id.share_write_upload);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 앨범 인텐트
                 */
                // File System.
                final Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*"); // 이미지 파일 호출
                galleryIntent.setAction(Intent.ACTION_PICK);

                // Chooser of file system options.
                final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Image");
                galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(chooserIntent, PICK_FROM_ALBUM);

                /**
                 * 키보드 내리기
                 */
                Global.hideKeyboard(getActivity(), editText);
            }
        });

        completeBtn = rootView.findViewById(R.id.share_write_complete);
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * ShareItem 생성
                 */
                ShareItem shareItem = new ShareItem();
                shareItem.setContent(editText.getText().toString());
                // 작성자 설정
                Member member = Global.getMember();
                shareItem.setKey(member.getKey());

                /**
                 * 서버에 데이터 전송
                 */
                MultipartBody.Part body = null;
                if (uploadImageFile != null) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), uploadImageFile);
                    body = MultipartBody.Part.createFormData("files[0]", uploadImageFile.getName(), requestFile);
                }

                Call<String> call = RetrofitHelper.getInstance().getRetrofitService().addShareItem(shareItem, body);
                Callback<String> callback = new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "setShareItem success" + response.body(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "setShareItem failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(getContext(), "failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("api", t.getMessage());
                    }
                };
                call.enqueue(callback);

                /**
                 * 키보드 내리기
                 */
                Global.hideKeyboard(getActivity(), editText);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("imageCapture", "resultCode: " + resultCode);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                /**
                 * 앨범에서 사진 선택
                 */
                capturedImageUri = data.getData();
                Log.d("imageCapture", capturedImageUri.getPath());

                // 실제 파일이 존재하는 경로
                imagePath = getRealPathFromUri(capturedImageUri);
                Log.d("imageCapture", imagePath);

                // 이미지뷰에 보여주기
                uploadImageFile = new File(imagePath);
                Picasso.get().load(uploadImageFile).fit().into(writeBackgroundImageView);

                /**
                 * 서버 파일 저장소에 이미지파일 업로드
                 */
            }
        }
    }

    /**
     * uri를 통해 실제 파일이 위치하는 경로를 리턴하는 함수
     * @param capturedImageUri
     * @return 실제 파일이 위치하는 경로
     */
    private String getRealPathFromUri(Uri capturedImageUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(capturedImageUri, proj, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }
}
