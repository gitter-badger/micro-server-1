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
package org.mx.oauth.client;

import org.mx.server.GlobalVariableService;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PWSec {
    
  private String encryptionAlgorithm = "DES";
  private String encryptionMode = "ECB";
  private String encryptionPadding = "PKCS5Padding";
  private String ENCRYPTED_KEY = "41254157g5o9s7h8m3e612awpmon!5jk";

  public PWSec(){
      String encryptKey = GlobalVariableService.getMicroServer().getEncryptKey();
      if( encryptKey != null || !encryptKey.isEmpty()){
          ENCRYPTED_KEY=encryptKey;
      }
  }

  public String getMessageDigest(String password){
      String string ="a";
      MessageDigest m;     
      try { 
          m = MessageDigest.getInstance("MD5"); 
          m.update(password.getBytes(),0,password.length()); 
          BigInteger i = new BigInteger(1, m.digest()); 
      
          //Formatando o resuldado em uma cadeia de 32 caracteres, completando com 0 caso falte 
          string = String.format("%1$032X", i); 
          
          return string;
      } catch (NoSuchAlgorithmException e) { 
          e.printStackTrace(); 
      } 
      return null;
  }
    
  public String decrypt(String passwordEncrypted){
      return decryptText(passwordEncrypted, ENCRYPTED_KEY);
  }
    
  public String encrypt(String passwordText) {
	  return encryptText(passwordText, ENCRYPTED_KEY);
  }
  
  public String decryptText(String encryptedText,String encryptionkey) {
    String cipherParameters = encryptionAlgorithm + "/" + encryptionMode + "/" +  encryptionPadding;
    
    try {
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptionAlgorithm);
      byte[] desKeyData = encryptionkey.getBytes();
      DESKeySpec desKeySpec = new DESKeySpec(desKeyData);
      SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

      byte decodedEncryptedText[] = Base64.getDecoder().decode(encryptedText.getBytes());
      Cipher c1 = Cipher.getInstance(cipherParameters);
      c1.init(c1.DECRYPT_MODE, secretKey);
      byte[] decryptedText = c1.doFinal(decodedEncryptedText);
      String decryptedTextString = new String(decryptedText);
      return decryptedTextString;
    }
    catch (Exception e) {
      System.out.println("Error: " + e);
      System.out.println("encryptedText:" + encryptedText);
      e.printStackTrace();
      return null;
    }
  }
  

  
  public String encryptText(String text, String encryptionkey) {
    String cipherParameters = encryptionAlgorithm + "/" + encryptionMode + "/" +  encryptionPadding;
    try {
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptionAlgorithm);
      byte[] desKeyData = encryptionkey.getBytes();
      DESKeySpec desKeySpec = new DESKeySpec(desKeyData);
      SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

      Cipher c1 = Cipher.getInstance(cipherParameters);
      if (secretKey != null) {
        c1.init(c1.ENCRYPT_MODE, secretKey);
        byte clearTextBytes[];
        clearTextBytes = text.getBytes();
        byte[] encryptedText = c1.doFinal(clearTextBytes);
        String encryptedEncodedText = Base64.getEncoder().encodeToString(encryptedText);
        return encryptedEncodedText;

      } else {
         System.out.println("ERROR! >> SecretKey not generated ....");
         System.out.println("ERROR! >> you are REQUIRED to specify the encryptionKey in the config xml");
         return null;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }

  }

}
