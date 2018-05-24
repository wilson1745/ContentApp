package e.wilso.contentapp;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.widget.TextView;
import android.content.ContentProviderOperation;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import java.util.ArrayList;

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

      //insertContact();
      //updateContact();
      //deleteContact();
   }

   private void deleteContact() {
      String where = Data.DISPLAY_NAME + " = ?";
      String[] params = new String[] {"Jane"};
      ArrayList ops = new ArrayList();

      ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
              .withSelection(where, params)
              .build());
      try {
         getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
      }catch (RemoteException e) {
         e.printStackTrace();
      }catch (OperationApplicationException e) {
         e.printStackTrace();
      }
      Log.d("DELETE", "deleteContact has finished!");
   }

   private void updateContact() {
      String where = Phone.DISPLAY_NAME + " = ? AND " + Data.MIMETYPE + " = ? ";
      String[] params = new String[] {"Jane", Phone.CONTENT_ITEM_TYPE};
      //Log.d("DATA", "Fuck you: " + Phone.DISPLAY_NAME + " || " + Data.MIMETYPE + " || " + Phone.CONTENT_ITEM_TYPE);
      ArrayList ops = new ArrayList();

      ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
              .withSelection(where, params)
              .withValue(Phone.NUMBER, "(090) 033-3312")
              .build());
      try {
         getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
      }catch (RemoteException e) {
         e.printStackTrace();
      }catch (OperationApplicationException e) {
         e.printStackTrace();
      }
   }

   private void insertContact() {
      ArrayList ops = new ArrayList();
      int index = ops.size();

      ops.add(ContentProviderOperation
              .newInsert(RawContacts.CONTENT_URI)
              .withValue(RawContacts.ACCOUNT_TYPE, null)
              .withValue(RawContacts.ACCOUNT_NAME, null)
              .build());

      ops.add(ContentProviderOperation
              .newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(Data.RAW_CONTACT_ID, index)
              .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
              .withValue(StructuredName.DISPLAY_NAME, "Jane")
              .build());

      ops.add(ContentProviderOperation
              .newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(Data.RAW_CONTACT_ID, index)
              .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
              .withValue(Phone.NUMBER, "(090)011-2233")
              .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
              .build());
      try {
         getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
      }catch (RemoteException e) {
         e.printStackTrace();
      }catch (OperationApplicationException e) {
         e.printStackTrace();
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
      // query中欲使用的欄位名稱
      //String[] projection = {Contacts._ID, Contacts.DISPLAY_NAME, Phone.NUMBER};
      Cursor cursor = resolver.query(Contacts.CONTENT_URI, null,null,null,null);

      /*while (cursor.moveToNext()) {
         //處理每一筆資料
         int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
         String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
         Log.d("RECORD", id + "/" + name);
      }*/

      SimpleCursorAdapter adapter = new SimpleCursorAdapter(
              this,
              android.R.layout.simple_list_item_2,
              cursor,
              //new String[] {ContactsContract.Contacts.DISPLAY_NAME},
              new String[] {Contacts.DISPLAY_NAME, Contacts.HAS_PHONE_NUMBER},
              new int[] {android.R.id.text1, android.R.id.text2},
              1) {
         //客製化複寫bindView讓沒電話顯示空字串
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            TextView phone = view.findViewById(android.R.id.text2);

            if (cursor.getInt(cursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) == 0) {
               phone.setText(" ");
            }
            else {
               int id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));
               Cursor pCursor = getContentResolver().query(
                       Phone.CONTENT_URI,
                       null,
                       Phone.CONTACT_ID + "=?",
                       new String[]{String.valueOf(id)},
                       null);

               if (pCursor.moveToFirst()) {
                  String number = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                  phone.setText(number);
                  Log.d("RECORD", id + "/" + number);
               }
            }
         }
      };

      ListView list = findViewById(R.id.list);
      list.setAdapter(adapter);
   }
}
