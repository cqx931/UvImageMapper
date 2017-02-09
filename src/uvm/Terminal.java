package uvm;

import java.io.*;
import java.text.*;

import org.apache.commons.exec.*;

public class Terminal {
	
	/** True if a non-zero number, 'true', or 't', otherwise false; */
	public static boolean execToBool(String command) {
		
		Number number = null;
		String s = execToString(command);
		
		try {
			s = execToString(command).replaceAll("\\n", "");
			number = NumberFormat.getInstance().parse(s);
		}
		catch (ParseException e) {
			number = null;
		}
		
		if (number != null) {
			return number.doubleValue() != 0;
		}
		
		boolean b = false;
		if (s.equalsIgnoreCase("T"))
			b = true;
		else {
			b = Boolean.parseBoolean(s);
		}
		
		return b;
	}
	
	public static int execToInt(String command) {
		
		return Integer.parseInt(execToString(command));
	}
	
	public static float execToFloat(String command) {
		
		return Float.parseFloat(execToString(command));
	}
	
	public static String execToString(String command) {
		
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CommandLine commandline = CommandLine.parse(command);
    DefaultExecutor exec = new DefaultExecutor();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    exec.setStreamHandler(streamHandler);
    try {
			exec.execute(commandline);
		}
		catch (IOException e) {
			System.err.println(e);
			return null;
		}
    
    return(outputStream.toString());
	}

	public static int exec(String command) {

		DefaultExecutor executor = new DefaultExecutor();
		executor.setWatchdog(new ExecuteWatchdog(10000));
		try {
			return executor.execute(CommandLine.parse(command));
		}
		catch (Exception e) {
			System.err.println(e);
			return 1;
		}
	}
	
	public static void main(String[] args) {

		String result = execToString("pwd");
		System.out.println(result);
		
		float num = execToFloat("echo 1.0");
		System.out.println(num);

		boolean b = execToBool("echo 0.0");
		System.out.println(b);
		
		b = execToBool("echo 0");
		System.out.println(b);
		
		b = execToBool("echo f");
		System.out.println(b);
		
		b = execToBool("echo F");
		System.out.println(b);
		
		b = execToBool("echo t");
		System.out.println(b);
		
		b = execToBool("echo T");
		System.out.println(b);
		
		b = execToBool("echo 1");
		System.out.println(b);
		
		b = execToBool("echo -1");
		System.out.println(b);
		
		b = execToBool("echo 1.0");
		System.out.println(b);
	}
}
