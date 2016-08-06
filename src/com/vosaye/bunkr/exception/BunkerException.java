package com.vosaye.bunkr.exception;

import android.content.Context;
import android.widget.Toast;

public class BunkerException extends Exception{
	
	public static String outOfTerms = "Out Of Terms";
	public static String alreadyBlank = "Already Blank";
	public static String possibleOverWrite = "Data Might Be Lost After The Operation";
	public static String onlySixDaysAllowed = "Only Six Days Allowed";
	public static String notUnique = "not unique";
	public static String overwriteNotAllowed = "Overwrite Not Allowed";
	public BunkerException(String str, Context context){
		super(str);
		//Toast.makeText(context, str, Toast.LENGTH_LONG).show();
		
	}
} 
