package e.wilso.contentapp;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity {

   private static final int REQUEST_CONTACTS = 1;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

      if(permission != PackageManager.PERMISSION_GRANTED) {
         //未取得權限，向使用者要求允許權限 選取完後進到onRequestPermissionsResult
         ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS, WRITE_CONTACTS}, REQUEST_CONTACTS);
      }
      else {
         //已有權限，可進行檔案存取
         readContacts();
      }
   }

   @Override
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      switch (requestCode) {
         case REQUEST_CONTACTS:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // 取得聯絡人權限 進行存取
               readContacts();
            }
            else {
               // 使用者拒絕權限 顯示對話窗口
               new AlertDialog.Builder(this).setMessage("必須允許聯絡人權限才能取得資料").setPositiveButton("OK", null).show();
            }
            return;
      }
   }

   private void readContacts() {
   }
}