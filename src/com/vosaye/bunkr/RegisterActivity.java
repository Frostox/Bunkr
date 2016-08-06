package com.vosaye.bunkr;

import org.apache.commons.lang3.StringUtils;

import com.vosaye.bunkr.app.HelpActivity;
import com.vosaye.bunkr.app.ScheduleListActivity;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.services.ValidatorService;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	EditText pass1,pass2;
	Button submit;
	AuthDatabase authenticator;
	Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar action = this.getActionBar();
        //action.hide();
        action.show();
        BunKar.count++;

        System.out.println("Vosayen : "+BunKar.count);
        //action.setTitle("  Set up password for your data");
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;Set up password for your data!" + "</font>"));
        pass1 = (EditText) this.findViewById(R.id.reg_editText1);
        pass2 = (EditText) this.findViewById(R.id.reg_editText2);
        submit = (Button) this.findViewById(R.id.reg_button1);
        
        authenticator = ((BunKar) this.getApplication()).settings;
        
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        
        
    }
    
    
    
    public void onReg(View v){
    	String password,password2;
		password = pass1.getText().toString();
		password2 = pass2.getText().toString();
		InputMethodManager mngr = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		if(!authenticator.recordExists("*", "auth"))
			if(password.equals("")){
				toast.setText("Please enter the passwords");
				toast.show();
			}
			else if(password.length()<5){
				toast.setText("Atleast 5 characters are needed"); 
				toast.show();
				
			}
			else if(!StringUtils.isAlphanumeric(password)){
				toast.setText("Password should be alphanumeric");
				toast.show();
				
			}
			else if(!password.equals(password2)){
				toast.setText("Please reconfirm the passwords");
				toast.show();
				
			}
			else{
				authenticator.authentication.register("labsdecore", password);
				mngr.hideSoftInputFromWindow(pass2.getWindowToken(), 0);
				this.finish();
				
				Intent i = new Intent(this.getBaseContext(),HelpActivity.class);
				i.putExtra("base", "yes");
				i.putExtra("explicit", "false");
				this.startActivityForResult(i,0); 
				
				//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
				 
				
			}
			else{
				//toast.setText("Already Registered");
				mngr.hideSoftInputFromWindow(pass2.getWindowToken(), 0);
				//toast.show();
				this.finish();
				this.startActivityForResult(new Intent(this.getBaseContext(),LoginActivity.class),0);
				
				//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
				
				
			}
		}
    	public void onBackPressed(){
    	
    	}
    	public void onResume(){
        	super.onResume();
        	ValidatorService.FOCUSED = false;
        }
    	public void onDestroyed(){
    		super.onDestroy();
    		BunKar.count--;
    		if(BunKar.count==0){
        		((BunKar) this.getApplication()).deleteAllCache();
        	}
    	}
    	
}

    
    
    

