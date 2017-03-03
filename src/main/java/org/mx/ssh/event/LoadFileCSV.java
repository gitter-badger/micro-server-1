/*

 Copyright 2004-2015, MXDeploy Software, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package org.mx.ssh.event;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class LoadFileCSV {
	
	public List execute(String filePath){
	    List listServer = new ArrayList();
	    
		FileReader input = null;
		try {
			input = new FileReader( filePath );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		BufferedReader bufRead = new BufferedReader(input);
		String line; // String that holds current file
		try {
			while ( ( line = bufRead.readLine())!=null ) {
				StringTokenizer token = new StringTokenizer(line,";");
				while(token.hasMoreTokens()){
					
				}
				line = line.trim();
				line = line.replace("\r", "");
				line = line.replace("\n", "");
				
			    listServer.add(line); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try { 
			bufRead.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return listServer;
		
	}	


}
