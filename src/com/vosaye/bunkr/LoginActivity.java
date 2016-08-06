package com.vosaye.bunkr;

import java.text.ParseException;

import net.sqlcipher.Cursor;

import org.apache.commons.lang3.StringUtils;

import com.vosaye.bunkr.app.HelpActivity;
import com.vosaye.bunkr.app.Here;
import com.vosaye.bunkr.app.ScheduleListActivity;
import com.vosaye.bunkr.app.TOS;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.services.ValidatorService;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	EditText pass;
	Button submit;
	AuthDatabase authenticator; 
	BunKar bunker;
	Toast toast;
	CheckBox auto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar action = this.getActionBar(); 
        action.show();
        //action.setTitle("  Login into Bunk Kar");
        action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;Login into Bunkr" + "</font>"));
        BunKar.count++;

        System.out.println("Vosayen : "+BunKar.count);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        pass = (EditText) this.findViewById(R.id.login_editText1);
        submit = (Button) this.findViewById(R.id.login_button1);
        bunker = (BunKar) this.getApplication();
        auto = (CheckBox) this.findViewById(R.id.login_checkBox1);
        authenticator = bunker.settings;
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
    	
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_help) {
			Intent i = new Intent(this,HelpActivity.class);
			i.putExtra("explicit", "true");
			this.startActivity(i);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    
    public void onLog(View v){
    	String password;
		password = pass.getText().toString();
		InputMethodManager mngr = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		
		if(!password.equals(""))
			if(!StringUtils.isAlphanumeric(password)){
				toast.setText("Invalid Password");
				toast.show();
			}
			else{
				if(authenticator.authentication.login("labsdecore", password)){
					//toast.setText("Authentication was Successful");
					mngr.hideSoftInputFromWindow(pass.getWindowToken(), 0);
					boolean autologin = auto.isChecked();
					authenticator.execQuery("update settings set value = '"+autologin+"' where name = 'autologin'");
					//toast.show();
					//Intent i = new Intent(this,SchedulesList.class);
					//startActivityForResult(i);
					
					Cursor cx = authenticator.rawQuery("select value from settings where name = 'default' and value not like 'NONE'");
					if(cx.moveToFirst()){
						((BunKar) this.getApplication()).name = cx.getString(0);
						ScheduleDatabase db = (bunker.getDatabase(bunker.name));
						try {
							if(!db.exists("select * from session")||db.standards.getEndOfTerm()==null){
								this.finish();
								Intent inte = new Intent(this.getBaseContext(),TOS.class);
								inte.putExtra("base", "yes");
								startActivityForResult(inte,0);
								
								//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
								
								//toast.setText("go to tos");
								//toast.show();
								
							}
							else{
								this.finish();
								Intent inte = new Intent(this.getBaseContext(), Here.class);
								inte.putExtra("base", "yes");
								startActivityForResult(inte,0);
								
								//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
								
								//Toast.makeText(this, "Entry Point", Toast.LENGTH_LONG).show();
								//toast.setText("base activity");
								//toast.show();
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					else{
						cx.close();
						this.finish();
						Intent inte = new Intent(this.getBaseContext(), ScheduleListActivity.class);
						inte.putExtra("base", "yes");
						startActivityForResult(inte,0);
						
						//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
						
						//toast.setText("list of schedules");
						//toast.show();
					}
					
				}
				else{
					toast.setText("Invalid password");
					toast.show();
				}
			}
		else{
			toast.setText("Please enter the Password");
			toast.show();
			
			
		}
    }
    public void onResume(){
    	super.onResume();
    	ValidatorService.FOCUSED = false;
    }
    public void onDestroy(){
    	super.onDestroy();
    	BunKar.count--;
    	if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
    }
}
