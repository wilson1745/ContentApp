package e.wilso.contentapp;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

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
      ContentResolver resolver = getContentResolver();
      Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,"DISPLAY_NAME DESC");

      /*while (cursor.moveToNext()) {
         //處理每一筆資料
         int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
         String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
         Log.d("RECORD", id + "/" + name);
      }*/

      ListView list = findViewById(R.id.list);

      SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
              android.R.layout.simple_list_item_1,
              cursor,
              new String[] {ContactsContract.Contacts.DISPLAY_NAME},
              new int[] {android.R.id.text1},
              1);
      list.setAdapter(adapter);
   }
}
