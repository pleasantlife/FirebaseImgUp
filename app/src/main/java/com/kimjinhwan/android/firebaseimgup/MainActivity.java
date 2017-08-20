package com.kimjinhwan.android.firebaseimgup;

import android.*;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView textUserEmail;
    Button imgUpload, btnLogout;
    ImageView imgPreview;
    Uri previewUri;

    static final int REQ_PERMISSION = 99;

    final int SELECT_IMG = 111;

    /**
     *  파이어베이스 스토리지를 사용하기 위한 선언부.
     */
    private StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUserName();
        initView();

        /**
         * 스마트폰의 내부 저장소를 사용하기 위해 퍼미션 허가를 받습니다.
         */
        if(Build.VERSION.SDK_INT >= M){
            checkPermission();
        }

        setOnClick();
        // 아래의 구문으로 파이어베이스 스토리지 객체화 및 현재 상태를 불러옵니다.
        storageRef = FirebaseStorage.getInstance().getReference();

    }


    private void setUserName(){
        Intent intent = getIntent();
        textUserEmail = (TextView) findViewById(R.id.textUserEmail);
        textUserEmail.setText(intent.getStringExtra("userEmail"));
    }

    private void initView(){
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        imgUpload = (Button) findViewById(R.id.imgUpload);
        btnLogout = (Button) findViewById(R.id.btnLogOut);
    }

    private void setOnClick(){
        imgPreview.setOnClickListener(this);
        imgUpload.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
    };

    /**
     *  갤러리에서 그림을 불러왔을 때 onActivityResult에서 Glide 라이브러리를 이용해 그림을 띄웁니다.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       switch(requestCode){
           case SELECT_IMG :
               previewUri = data.getData();
               Glide.with(this).load(previewUri).into(imgPreview);
               break;
       }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.imgPreview:
                Intent intentPic = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intentPic, "하이"), SELECT_IMG);
                break;
            case R.id.imgUpload:

                /**
                 *  그냥 previewUri를 쓰면 절대경로가 아니기 때문에, firebase에 업로드 할 수 없음.
                 *  checkRealPath 메소드에서는 Uri에서의 절대경로를 알아냄.
                 */

                Uri file = Uri.fromFile(new File(checkRealPath(previewUri)));

                /**
                 *  .child 메소드로 picture 폴더 아래에 파일을 업로드한다고 지정함.
                 *  file.getLastPathSegment로 업로드하는 파일명을 로컬파일에 있던 파일명과 일치시킴.
                 */
                StorageReference childRef = storageRef.child("pictures/"+file.getLastPathSegment());

                /**
                 *  업로드가 성공했을 때 해줄 일들을 기재. @SuppressWarning로 annotation을 지정해줘야 Uri 부분에서 오류가 나지 않음.
                 */
                childRef.putFile(previewUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") Uri uploadedUri = taskSnapshot.getUploadSessionUri();
                        Toast.makeText(MainActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();

                    }
                });
                break;
        }
    }

    /**
     *  갤러리를 통해 내부 메모리를 읽어오기 때문에, 권한 체크를 함.(android Manifest에도 반영함.)
     */

    @TargetApi(M)
    public void checkPermission() {   //권한이 승인이 되어있는지 확인
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            String permissions[] = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA};
            requestPermissions(permissions, REQ_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "권한 승인해야 앱을 사용할수 있습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Uri와 contentResolver를 통해 업로드 할 파일의 절대경로를 알아내는 메소드.
     */

    private String checkRealPath(Uri imageUri) {
        String path = "";
        if (imageUri != null) {
           String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
            cursor.moveToNext();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        } else {
            Toast.makeText(MainActivity.this, "이미지를 업로드 하세요", Toast.LENGTH_SHORT).show();
        }
        return path;
    }
}